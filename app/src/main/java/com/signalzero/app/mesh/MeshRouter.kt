package com.signalzero.app.mesh

import android.util.Log
import com.signalzero.app.data.local.dao.MeshPacketDao
import com.signalzero.app.data.local.entity.MeshPacketEntity
import com.signalzero.app.data.model.ChatMessage
import com.signalzero.app.data.model.DeliveryStatus
import com.signalzero.app.data.model.Incident
import com.signalzero.app.data.model.MeshPacket
import com.signalzero.app.data.repository.ChatRepository
import com.signalzero.app.data.repository.IncidentRepository
import com.signalzero.app.data.repository.PeerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed class MeshEvent {
    data class PacketTransferred(val peerAddress: String, val packetId: String) : MeshEvent()
    data class PacketTransferFailed(val peerAddress: String, val packetId: String, val reason: String) : MeshEvent()
    data class IncidentRelayed(val incidentId: String, val hopCount: Int) : MeshEvent()
    data class ChatMessageReceived(val message: ChatMessage) : MeshEvent()
}

class MeshRouter(
    private val packetDao: MeshPacketDao,
    private val incidentRepository: IncidentRepository,
    private val chatRepository: ChatRepository,
    private val peerRepository: PeerRepository,
    private val peerRegistry: PeerRegistry,
    private val gattClientManager: GattClientManager,
    private val scope: CoroutineScope
) : PacketTransferCallback, PacketReceivedListener {

    private val tag = "SignalZero_MeshRouter"
    private val _events = MutableSharedFlow<MeshEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<MeshEvent> = _events.asSharedFlow()

    private var queueDrainJob: Job? = null

    init {
        startQueueDrainWorker()
    }

    suspend fun enqueueIncident(incident: Incident, myNodeId: String) {
        val payload = JSONObject().apply {
            put("incidentId", incident.id)
            put("category", incident.category.name)
            put("message", incident.message)
            put("createdAt", incident.createdAt)
            put("latitude", incident.latitude)
            put("longitude", incident.longitude)
            put("accuracy", incident.accuracyMeters)
            put("severity", incident.severityLevel)
        }

        val packet = MeshPacket(
            packetId = UUID.randomUUID().toString(),
            incidentId = incident.id,
            type = MeshPacket.PacketType.SOS_BROADCAST,
            originPeerId = myNodeId,
            senderPseudonym = "Me",
            payloadJson = payload.toString(),
            hopCount = 0,
            maxHops = 5,
            visitedNodes = listOf(myNodeId)
        )

        packetDao.insertPacket(MeshPacketEntity.fromDomain(packet))
        incidentRepository.updateDeliveryStatus(incident.id, DeliveryStatus.QUEUED_FOR_RELAY)
        attemptForwardPacket(packet)
    }

    suspend fun sendChatMessage(peerAddress: String, text: String, myNodeId: String = "Rescuer", senderName: String = "Rescuer") {
        val msgId = UUID.randomUUID().toString()
        val chatMessage = ChatMessage(
            messageId = msgId,
            peerAddress = peerAddress,
            senderName = senderName,
            text = text,
            timestamp = System.currentTimeMillis(),
            isOutgoing = true,
            status = ChatMessage.MessageStatus.PENDING_MESH
        )
        chatRepository.saveMessage(chatMessage)

        val payload = JSONObject().apply {
            put("messageId", msgId)
            put("text", text)
            put("senderName", senderName)
            put("timestamp", chatMessage.timestamp)
        }

        val packet = MeshPacket(
            packetId = UUID.randomUUID().toString(),
            type = MeshPacket.PacketType.CHAT_MESSAGE,
            originPeerId = myNodeId,
            targetPeerId = peerAddress,
            senderPseudonym = senderName,
            payloadJson = payload.toString(),
            visitedNodes = listOf(myNodeId)
        )

        packetDao.insertPacket(MeshPacketEntity.fromDomain(packet))
        val json = serializePacket(packet)
        gattClientManager.sendPacket(peerAddress, packet.packetId, json)
    }

    override fun onTransferSuccess(deviceAddress: String, messageId: String) {
        Log.i(tag, "Packet $messageId transferred to $deviceAddress successfully.")
        scope.launch(Dispatchers.IO) {
            packetDao.markDelivered(messageId)
            peerRepository.incrementSent(deviceAddress)

            val packet = packetDao.getPacketById(messageId)?.toDomain()
            if (packet?.incidentId != null) {
                incidentRepository.updateDeliveryStatus(packet.incidentId, DeliveryStatus.ACCEPTED_BY_PEER)
            } else if (packet?.type == MeshPacket.PacketType.CHAT_MESSAGE) {
                val payload = try { JSONObject(packet.payloadJson) } catch (e: Exception) { null }
                val chatMsgId = payload?.optString("messageId")
                if (!chatMsgId.isNullOrBlank()) {
                    chatRepository.updateStatus(chatMsgId, ChatMessage.MessageStatus.SENT_VIA_BLE)
                }
            }
            _events.emit(MeshEvent.PacketTransferred(deviceAddress, messageId))
        }
    }

    override fun onTransferFailure(deviceAddress: String, messageId: String, reason: String) {
        Log.w(tag, "Packet $messageId failed to transfer to $deviceAddress: $reason")
        scope.launch(Dispatchers.IO) {
            packetDao.incrementRetry(messageId)
            peerRegistry.markPeerFailed(deviceAddress)
            _events.emit(MeshEvent.PacketTransferFailed(deviceAddress, messageId, reason))
        }
    }

    override fun onRawPacketReceived(packetJson: String, originDeviceAddress: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val packet = deserializePacket(packetJson) ?: return@launch

                // Deduplication check: drop if already stored or expired
                if (packetDao.packetExists(packet.packetId) || packet.isExpired) {
                    Log.d(tag, "Duplicate or expired packet ${packet.packetId} discarded.")
                    return@launch
                }

                packetDao.insertPacket(MeshPacketEntity.fromDomain(packet))
                peerRepository.incrementReceived(originDeviceAddress)

                when (packet.type) {
                    MeshPacket.PacketType.SOS_BROADCAST, MeshPacket.PacketType.SOS_RELAY -> {
                        handleIncomingSos(packet, originDeviceAddress)
                    }
                    MeshPacket.PacketType.CHAT_MESSAGE -> {
                        handleIncomingChat(packet, originDeviceAddress)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(tag, "Error parsing incoming packet: ${e.message}")
            }
        }
    }

    private suspend fun handleIncomingSos(packet: MeshPacket, originAddress: String) {
        val json = JSONObject(packet.payloadJson)
        val incidentId = json.optString("incidentId")
        if (incidentId.isBlank()) return

        val existing = incidentRepository.getIncident(incidentId)
        if (existing == null) {
            val incident = Incident(
                id = incidentId,
                category = com.signalzero.app.data.model.EmergencyCategory.fromString(json.optString("category")),
                message = json.optString("message"),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                latitude = if (json.has("latitude") && !json.isNull("latitude")) json.getDouble("latitude") else null,
                longitude = if (json.has("longitude") && !json.isNull("longitude")) json.getDouble("longitude") else null,
                accuracyMeters = if (json.has("accuracy") && !json.isNull("accuracy")) json.getDouble("accuracy").toFloat() else null,
                deliveryStatus = DeliveryStatus.RELAYED_BY_PEER,
                hopCount = packet.hopCount + 1,
                meshPeerId = originAddress
            )
            incidentRepository.saveIncident(incident)
            _events.emit(MeshEvent.IncidentRelayed(incidentId, packet.hopCount + 1))
        }

        // Store-and-Forward: Relaying to next hop if hops remain
        if (packet.hopCount + 1 < packet.maxHops) {
            val relayedPacket = packet.copy(
                hopCount = packet.hopCount + 1,
                type = MeshPacket.PacketType.SOS_RELAY,
                visitedNodes = packet.visitedNodes + originAddress
            )
            attemptForwardPacket(relayedPacket)
        }
    }

    private suspend fun handleIncomingChat(packet: MeshPacket, originAddress: String) {
        val json = JSONObject(packet.payloadJson)
        val msgId = json.optString("messageId", packet.packetId)
        val text = json.optString("text")
        val sender = json.optString("senderName", "Nearby Peer")
        val timestamp = json.optLong("timestamp", System.currentTimeMillis())

        val chat = ChatMessage(
            messageId = msgId,
            peerAddress = originAddress,
            senderName = sender,
            text = text,
            timestamp = timestamp,
            isOutgoing = false,
            status = ChatMessage.MessageStatus.DELIVERED_ACK
        )
        chatRepository.saveMessage(chat)
        _events.emit(MeshEvent.ChatMessageReceived(chat))
    }

    private suspend fun attemptForwardPacket(packet: MeshPacket) {
        val routablePeers = peerRegistry.getRoutablePeers()
            .filterNot { packet.visitedNodes.contains(it.address) }

        if (routablePeers.isNotEmpty()) {
            val target = routablePeers.first()
            val json = serializePacket(packet)
            gattClientManager.sendPacket(target.address, packet.packetId, json)
        } else {
            Log.i(tag, "No eligible unvisited peer for packet ${packet.packetId}. Retaining in persistent queue.")
        }
    }

    private fun startQueueDrainWorker() {
        queueDrainJob?.cancel()
        queueDrainJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(10000L) // Periodically retry every 10s
                val pending = packetDao.getPendingPacketsList()
                val routable = peerRegistry.getRoutablePeers()

                if (pending.isNotEmpty() && routable.isNotEmpty()) {
                    for (entity in pending.take(3)) {
                        attemptForwardPacket(entity.toDomain())
                    }
                }
            }
        }
    }

    private fun serializePacket(packet: MeshPacket): String {
        return JSONObject().apply {
            put("packetId", packet.packetId)
            put("incidentId", packet.incidentId)
            put("type", packet.type.name)
            put("originPeerId", packet.originPeerId)
            put("targetPeerId", packet.targetPeerId)
            put("senderPseudonym", packet.senderPseudonym)
            put("payloadJson", packet.payloadJson)
            put("hopCount", packet.hopCount)
            put("maxHops", packet.maxHops)
            put("createdAt", packet.createdAt)
            put("expiresAt", packet.expiresAt)
            put("visitedNodes", JSONArray(packet.visitedNodes))
        }.toString()
    }

    private fun deserializePacket(json: String): MeshPacket? {
        return try {
            val obj = JSONObject(json)
            val visited = mutableListOf<String>()
            val arr = obj.optJSONArray("visitedNodes")
            if (arr != null) {
                for (i in 0 until arr.length()) visited.add(arr.getString(i))
            }
            MeshPacket(
                packetId = obj.getString("packetId"),
                incidentId = obj.optString("incidentId").ifBlank { null },
                type = MeshPacket.PacketType.valueOf(obj.getString("type")),
                originPeerId = obj.getString("originPeerId"),
                targetPeerId = obj.optString("targetPeerId").ifBlank { null },
                senderPseudonym = obj.optString("senderPseudonym", "Peer"),
                payloadJson = obj.getString("payloadJson"),
                hopCount = obj.getInt("hopCount"),
                maxHops = obj.getInt("maxHops"),
                createdAt = obj.getLong("createdAt"),
                expiresAt = obj.getLong("expiresAt"),
                visitedNodes = visited
            )
        } catch (e: Exception) {
            null
        }
    }
}

package com.signalzero.app.mesh

import com.signalzero.app.data.local.entity.PeerEntity
import com.signalzero.app.data.model.PeerState
import com.signalzero.app.data.repository.PeerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class PeerRegistry(
    private val peerRepository: PeerRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : PeerDiscoveryListener {
    private val activePeers = ConcurrentHashMap<String, PeerEntity>()
    private val _peersState = MutableStateFlow<List<PeerEntity>>(emptyList())
    val peersState: StateFlow<List<PeerEntity>> = _peersState.asStateFlow()

    override fun onPeerFound(deviceAddress: String, deviceName: String?, rssi: Int, serviceData: ByteArray?) {
        val pseudonym = if (serviceData != null && serviceData.isNotEmpty()) {
            try { String(serviceData, Charsets.UTF_8).trim() } catch (e: Exception) { deviceName ?: "Rescuer" }
        } else {
            deviceName ?: "Rescuer"
        }
        onPeerDiscovered(deviceAddress, pseudonym, rssi)
    }

    private var watchdogJob: Job? = null

    init {
        startHeartbeatWatchdog()
    }

    fun onPeerDiscovered(address: String, pseudonym: String, rssi: Int) {
        val now = System.currentTimeMillis()
        val existing = activePeers[address]
        val previousState = existing?.peerState ?: PeerState.DISCOVERING

        val newState = if (previousState == PeerState.UNAVAILABLE || previousState == PeerState.SUSPECT) {
            PeerState.RECOVERING
        } else {
            PeerState.AVAILABLE
        }

        val updated = PeerEntity(
            address = address,
            pseudonym = pseudonym,
            state = newState.name,
            rssi = rssi,
            lastSeenTimestamp = now,
            failureCount = 0,
            packetsSent = existing?.packetsSent ?: 0,
            packetsReceived = existing?.packetsReceived ?: 0
        )

        activePeers[address] = updated
        publishState()

        scope.launch(Dispatchers.IO) {
            peerRepository.recordPeerDiscovered(address, pseudonym, rssi)
        }
    }

    fun markPeerConnected(address: String) {
        val peer = activePeers[address] ?: return
        val updated = peer.copy(state = PeerState.CONNECTED.name)
        activePeers[address] = updated
        publishState()
        scope.launch(Dispatchers.IO) { peerRepository.updatePeerState(address, PeerState.CONNECTED) }
    }

    fun markPeerFailed(address: String) {
        val peer = activePeers[address] ?: return
        val failures = peer.failureCount + 1
        val newState = if (failures >= 2) PeerState.UNAVAILABLE else PeerState.SUSPECT
        val updated = peer.copy(state = newState.name, failureCount = failures)
        activePeers[address] = updated
        publishState()
        scope.launch(Dispatchers.IO) {
            if (newState == PeerState.UNAVAILABLE) {
                peerRepository.updatePeerState(address, PeerState.UNAVAILABLE)
            } else {
                peerRepository.markFailure(address)
            }
        }
    }

    fun getRoutablePeers(): List<PeerEntity> {
        return activePeers.values
            .filter { it.peerState.isRoutable }
            .sortedByDescending { it.rssi }
    }

    private fun startHeartbeatWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(5000L)
                val now = System.currentTimeMillis()
                var changed = false

                activePeers.forEach { (address, peer) ->
                    val age = now - peer.lastSeenTimestamp
                    if (peer.peerState == PeerState.AVAILABLE || peer.peerState == PeerState.CONNECTED) {
                        if (age > 30000L) {
                            activePeers[address] = peer.copy(state = PeerState.UNAVAILABLE.name)
                            changed = true
                        } else if (age > 12000L) {
                            activePeers[address] = peer.copy(state = PeerState.SUSPECT.name)
                            changed = true
                        }
                    } else if (peer.peerState == PeerState.RECOVERING && age < 6000L) {
                        activePeers[address] = peer.copy(state = PeerState.AVAILABLE.name)
                        changed = true
                    }
                }

                if (changed) {
                    publishState()
                }
            }
        }
    }

    private fun publishState() {
        _peersState.value = activePeers.values.toList().sortedByDescending { it.lastSeenTimestamp }
    }
}

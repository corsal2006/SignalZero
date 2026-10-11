package com.signalzero.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signalzero.app.data.model.MeshPacket

@Entity(tableName = "mesh_packets")
data class MeshPacketEntity(
    @PrimaryKey val packetId: String,
    val incidentId: String?,
    val type: String,
    val originPeerId: String,
    val targetPeerId: String?,
    val senderPseudonym: String,
    val payloadJson: String,
    val hopCount: Int,
    val maxHops: Int,
    val createdAt: Long,
    val expiresAt: Long,
    val checksum: Int,
    val visitedNodesCsv: String,
    val isDelivered: Boolean,
    val retryCount: Int
) {
    fun toDomain(): MeshPacket {
        val visited = if (visitedNodesCsv.isBlank()) emptyList() else visitedNodesCsv.split(",")
        return MeshPacket(
            packetId = packetId,
            incidentId = incidentId,
            type = MeshPacket.PacketType.valueOf(type),
            originPeerId = originPeerId,
            targetPeerId = targetPeerId,
            senderPseudonym = senderPseudonym,
            payloadJson = payloadJson,
            hopCount = hopCount,
            maxHops = maxHops,
            createdAt = createdAt,
            expiresAt = expiresAt,
            checksum = checksum,
            visitedNodes = visited,
            isDelivered = isDelivered,
            retryCount = retryCount
        )
    }

    companion object {
        fun fromDomain(domain: MeshPacket): MeshPacketEntity {
            return MeshPacketEntity(
                packetId = domain.packetId,
                incidentId = domain.incidentId,
                type = domain.type.name,
                originPeerId = domain.originPeerId,
                targetPeerId = domain.targetPeerId,
                senderPseudonym = domain.senderPseudonym,
                payloadJson = domain.payloadJson,
                hopCount = domain.hopCount,
                maxHops = domain.maxHops,
                createdAt = domain.createdAt,
                expiresAt = domain.expiresAt,
                checksum = domain.checksum,
                visitedNodesCsv = domain.visitedNodes.joinToString(","),
                isDelivered = domain.isDelivered,
                retryCount = domain.retryCount
            )
        }
    }
}

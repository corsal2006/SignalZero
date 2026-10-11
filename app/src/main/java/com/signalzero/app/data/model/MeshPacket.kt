package com.signalzero.app.data.model

data class MeshPacket(
    val packetId: String,
    val incidentId: String? = null,
    val type: PacketType = PacketType.SOS_BROADCAST,
    val originPeerId: String,
    val targetPeerId: String? = null,
    val senderPseudonym: String = "Rescuer",
    val payloadJson: String,
    val hopCount: Int = 0,
    val maxHops: Int = 5,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 3600000L,
    val checksum: Int = 0,
    val visitedNodes: List<String> = emptyList(),
    val isDelivered: Boolean = false,
    val retryCount: Int = 0
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt
    val isHopExhausted: Boolean get() = hopCount >= maxHops

    enum class PacketType {
        SOS_BROADCAST,
        SOS_RELAY,
        CHAT_MESSAGE,
        ACK,
        HEARTBEAT
    }
}

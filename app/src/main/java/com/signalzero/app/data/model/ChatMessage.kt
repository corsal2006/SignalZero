package com.signalzero.app.data.model

data class ChatMessage(
    val messageId: String,
    val peerAddress: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean,
    val status: MessageStatus = MessageStatus.PENDING_MESH
) {
    enum class MessageStatus {
        PENDING_MESH,
        SENT_VIA_BLE,
        DELIVERED_ACK,
        FAILED
    }
}

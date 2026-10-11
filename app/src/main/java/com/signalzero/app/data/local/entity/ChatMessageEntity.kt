package com.signalzero.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signalzero.app.data.model.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val messageId: String,
    val peerAddress: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val status: String
) {
    fun toDomain(): ChatMessage {
        return ChatMessage(
            messageId = messageId,
            peerAddress = peerAddress,
            senderName = senderName,
            text = text,
            timestamp = timestamp,
            isOutgoing = isOutgoing,
            status = ChatMessage.MessageStatus.valueOf(status)
        )
    }

    companion object {
        fun fromDomain(domain: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                messageId = domain.messageId,
                peerAddress = domain.peerAddress,
                senderName = domain.senderName,
                text = domain.text,
                timestamp = domain.timestamp,
                isOutgoing = domain.isOutgoing,
                status = domain.status.name
            )
        }
    }
}

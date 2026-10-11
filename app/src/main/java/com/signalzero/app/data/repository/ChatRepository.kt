package com.signalzero.app.data.repository

import com.signalzero.app.data.local.dao.ChatMessageDao
import com.signalzero.app.data.local.entity.ChatMessageEntity
import com.signalzero.app.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(private val chatMessageDao: ChatMessageDao) {

    fun getMessagesForPeer(peerAddress: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForPeer(peerAddress).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getConversation(peerAddress: String): Flow<List<ChatMessage>> = getMessagesForPeer(peerAddress)

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun saveMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(ChatMessageEntity.fromDomain(message))
    }

    suspend fun updateStatus(messageId: String, status: ChatMessage.MessageStatus) {
        chatMessageDao.updateMessageStatus(messageId, status.name)
    }
}

package com.dinachi.passit.storage.repository


import com.dinachi.passit.datamodel.ChatMessage
import com.dinachi.passit.storage.local.ChatMessageEntity
import com.dinachi.passit.storage.local.dao.ChatMessageDao
import com.dinachi.passit.storage.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ChatRepo - Manages chat messages
 */
class ChatRepo(
    private val chatMessageDao: ChatMessageDao,
    private val firestoreDataSource: FirestoreDataSource = FirestoreDataSource()
) {

    fun observeMessages(chatRoomId: String): Flow<List<ChatMessage>> {
        return firestoreDataSource.observeMessages(chatRoomId)
    }

    suspend fun sendMessage(message: ChatMessage) {
        firestoreDataSource.sendMessage(
            chatRoomId = message.chatRoomId,
            receiverId = "", // Not needed in new signature
            text = message.messageText
        )
        chatMessageDao.insertMessage(message.toEntity())
    }

    suspend fun getMessages(chatRoomId: String): List<ChatMessage> {
        return chatMessageDao.getMessagesByChatRoom(chatRoomId)
            .map { it.toDomainModel() }
    }

    suspend fun markAsRead(messageId: String) {
        chatMessageDao.markAsRead(messageId)
    }

    // Extension functions
    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = this.id,
            chatRoomId = this.chatRoomId,
            senderId = this.senderId,
            messageText = this.messageText,
            timestamp = this.timestamp,
            isRead = this.isRead,
            imageUrl = this.imageUrl
        )
    }

    private fun ChatMessageEntity.toDomainModel(): ChatMessage {
        return ChatMessage(
            id = this.id,
            chatRoomId = this.chatRoomId,
            senderId = this.senderId,
            messageText = this.messageText,
            timestamp = this.timestamp,
            isRead = this.isRead,
            imageUrl = this.imageUrl
        )
    }
}
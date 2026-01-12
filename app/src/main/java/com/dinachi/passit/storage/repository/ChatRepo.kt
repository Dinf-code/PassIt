package com.dinachi.passit.storage.repository


import com.dinachi.passit.datamodel.ChatMessage
import com.dinachi.passit.storage.local.ChatMessageEntity
import com.dinachi.passit.storage.local.dao.ChatMessageDao
import com.dinachi.passit.storage.remote.FirestoreDataSource
import com.dinachi.passit.viewmodel.ChatThread
import kotlinx.coroutines.flow.Flow

class ChatRepo(
    private val chatMessageDao: ChatMessageDao,
    private val firestoreDataSource: FirestoreDataSource = FirestoreDataSource()
) {

    fun observeMessages(chatRoomId: String): Flow<List<ChatMessage>> {
        return firestoreDataSource.observeMessages(chatRoomId)
    }

    fun observeChatThreads(currentUserId: String): Flow<List<ChatThread>> {
        return firestoreDataSource.observeChatThreads(currentUserId)
    }

    suspend fun sendMessage(message: ChatMessage) {
        // Extract receiver ID from chatRoomId format: "chat_listingId_userId1_userId2"
        val parts = message.chatRoomId.split("_")
        val listingId = if (parts.size > 1) parts[1] else ""
        val userId1 = if (parts.size > 2) parts[2] else ""
        val userId2 = if (parts.size > 3) parts[3] else ""
        val receiverId = if (userId1 == message.senderId) userId2 else userId1

        // Get listing info for chat metadata
        val listing = try {
            if (listingId.isNotEmpty()) {
                firestoreDataSource.getListing(listingId)
            } else null
        } catch (e: Exception) {
            null
        }

        // Get receiver user info for chat metadata
        val receiverUser = try {
            if (receiverId.isNotEmpty()) {
                firestoreDataSource.getUser(receiverId)
            } else null
        } catch (e: Exception) {
            null
        }

        firestoreDataSource.sendMessage(
            chatRoomId = message.chatRoomId,
            receiverId = receiverId,
            text = message.messageText,
            listingTitle = listing?.title,
            listingPhotoUrl = listing?.imageUrls?.firstOrNull(),
            otherUserName = receiverUser?.name,
            otherUserPhotoUrl = receiverUser?.photoUrl
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
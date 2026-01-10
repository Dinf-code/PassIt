package com.dinachi.passit.storage.local.dao


import androidx.room.*
import com.dinachi.passit.storage.local.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE chatRoomId = :chatRoomId ORDER BY timestamp ASC")
    fun observeMessages(chatRoomId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chatRoomId = :chatRoomId ORDER BY timestamp ASC")
    suspend fun getMessagesByChatRoom(chatRoomId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: String)

    @Query("DELETE FROM chat_messages WHERE chatRoomId = :chatRoomId")
    suspend fun deleteMessagesByChatRoom(chatRoomId: String)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
}
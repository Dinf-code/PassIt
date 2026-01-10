package com.dinachi.passit.storage.local.dao

import androidx.room.*
import com.dinachi.passit.storage.local.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    /**
     * Observe a user by ID (real-time)
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: String): Flow<UserEntity?>

    /**
     * Get a user by ID (one-time)
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    /**
     * Search users by name
     */
    @Query("SELECT * FROM users WHERE name LIKE :query")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    /**
     * Insert a user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Insert multiple users
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /**
     * Update a user
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Delete a user by ID
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    /**
     * Delete all users
     */
    @Query("DELETE FROM users")
    suspend fun deleteAll()

    /**
     * Update online status
     */
    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean, lastSeen: Long)
}
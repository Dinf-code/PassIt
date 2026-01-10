package com.dinachi.passit.storage.repository

import com.dinachi.passit.datamodel.User
import com.dinachi.passit.storage.local.UserEntity
import com.dinachi.passit.storage.local.dao.UserDao
import com.dinachi.passit.storage.remote.FirestoreDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * UserRepo - Single source of truth for user data
 */
class UserRepo(
    private val userDao: UserDao,
    private val firestoreDataSource: FirestoreDataSource = FirestoreDataSource()
) {

    fun observeUser(userId: String): Flow<User> {
        syncUserFromFirestore(userId)
        return userDao.observeUser(userId)
            .map { entity ->
                entity?.toDomainModel() ?: throw Exception("User not found")
            }
    }

    suspend fun getUser(userId: String): User {
        val cached = userDao.getUserById(userId)
        if (cached != null) {
            return cached.toDomainModel()
        }
        val user = firestoreDataSource.getUser(userId)
        userDao.insertUser(user.toEntity())
        return user
    }

    suspend fun createUser(user: User) {
        firestoreDataSource.createUser(user)
        userDao.insertUser(user.toEntity())
    }

    suspend fun updateUser(user: User) {
        firestoreDataSource.updateUser(user)
        userDao.updateUser(user.toEntity())
    }

    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        val user = getUser(userId).copy(isOnline = isOnline)
        updateUser(user)
    }

    suspend fun followUser(userId: String) {
        firestoreDataSource.followUser(userId)
    }

    suspend fun unfollowUser(userId: String) {
        firestoreDataSource.unfollowUser(userId)
    }

    fun searchUsers(query: String): Flow<List<User>> {
        return userDao.searchUsers("%$query%")
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    private fun syncUserFromFirestore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = firestoreDataSource.getUser(userId)
                userDao.insertUser(user.toEntity())
            } catch (e: Exception) {
                println("Failed to sync user: ${e.message}")
            }
        }
    }

    // Extension functions
    private fun User.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            name = this.name,
            email = this.email,
            photoUrl = this.photoUrl,
            bio = this.bio,
            location = this.location,
            phoneNumber = this.phoneNumber,
            rating = this.rating,
            reviewsCount = this.reviewsCount,
            followersCount = this.followersCount,
            followingCount = this.followingCount,
            isVerified = this.isVerified,
            isOnline = this.isOnline,
            createdAt = this.createdAt,
            lastSeen = this.lastSeen
        )
    }

    private fun UserEntity.toDomainModel(): User {
        return User(
            id = this.id,
            name = this.name,
            email = this.email,
            photoUrl = this.photoUrl,
            bio = this.bio,
            location = this.location,
            phoneNumber = this.phoneNumber,
            rating = this.rating,
            reviewsCount = this.reviewsCount,
            followersCount = this.followersCount,
            followingCount = this.followingCount,
            isVerified = this.isVerified,
            isOnline = this.isOnline,
            createdAt = this.createdAt,
            lastSeen = this.lastSeen
        )
    }
}
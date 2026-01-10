package com.dinachi.passit.storage.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey
    val id: String,
    val sellerId: String,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String,
    val condition: String,  // Stored as enum name
    val category: String,   // Stored as enum name
    val brand: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrls: String,  // Stored as comma-separated string
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val viewCount: Int,
    val favoriteCount: Int,
    val isSold: Boolean
)


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String,
    val bio: String,
    val location: String,
    val phoneNumber: String,
    val rating: Float,
    val reviewsCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val createdAt: Long,
    val lastSeen: Long
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val chatRoomId: String,
    val senderId: String,
    val messageText: String,
    val timestamp: Long,
    val isRead: Boolean,
    val imageUrl: String?  // Nullable for image messages
)

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey
    val id: String,  // Format: "CAD_USD"
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double,
    val timestamp: Long  // When this rate was cached
)


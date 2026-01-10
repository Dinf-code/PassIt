package com.dinachi.passit.datamodel


// ============ USER ============

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val location: String = "",
    val phoneNumber: String = "",
    val rating: Float = 0f,
    val reviewsCount: Int = 0,  // ← Changed from reviewCount
    val followersCount: Int = 0,  // ← Added
    val followingCount: Int = 0,  // ← Added
    val isVerified: Boolean = false,  // ← Added
    val isOnline: Boolean = false,
    val createdAt: Long = 0L,  // ← Changed from joinedTimestamp
    val lastSeen: Long = 0L  // ← Changed from lastSeenTimestamp
)

// ============ LISTINGS ============

data class Listing(
    val id: String = "",
    val sellerId: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val currency: String = "CAD",
    val condition: Condition = Condition.Good,
    val category: Category = Category.Other,
    val brand: String = "",
    val location: String = "",
    val latitude: Double = 0.0,  // ← Kept for future map features
    val longitude: Double = 0.0,  // ← Kept for future map features
    val imageUrls: List<String> = emptyList(),
    val createdTimestamp: Long = 0L,
    val updatedTimestamp: Long = 0L,
    val viewCount: Int = 0,  // ← Kept for analytics
    val favoriteCount: Int = 0,  // ← Kept for analytics
    val isSold: Boolean = false  // ← Changed from status enum (simpler)
)

enum class Condition {
    New,
    LikeNew,
    Good,
    Fair
}

enum class Category {
    Electronics,
    Clothing,
    Furniture,
    Home,  // ← Changed from HomeGarden
    Sports,
    Books,
    Toys,  // ← Changed from ToysGames
    Other
}

// ============ CHAT ============

data class ChatRoom(
    val id: String = "",
    val listingId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val isActive: Boolean = true
)

data class ChatMessage(
    val id: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val messageText: String = "",  // ← Changed from message
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val imageUrl: String? = null  // ← Changed from attachmentUrl, now nullable
)

// ============ FAVORITES ============

data class Favorite(
    val id: String = "",
    val userId: String = "",
    val listingId: String = "",
    val timestamp: Long = 0L
)

// ============ REVIEWS ============

data class Review(
    val id: String = "",
    val reviewerId: String = "",
    val reviewedUserId: String = "",
    val listingId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = 0L
)

// ============ NOTIFICATIONS ============

data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: NotificationType = NotificationType.Message,
    val title: String = "",
    val message: String = "",
    val relatedId: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

enum class NotificationType {
    Message,
    ListingSold,
    NewReview,
    PriceReduced,
    System
}

// ============ EXCHANGE RATES ============

data class ExchangeRate(
    val baseCurrency: String = "CAD",
    val rates: Map<String, Double> = emptyMap(),
    val timestamp: Long = 0L
)

// ============ LOCATION ============

data class Location(
    val city: String = "",
    val province: String = "",
    val country: String = "Canada",
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
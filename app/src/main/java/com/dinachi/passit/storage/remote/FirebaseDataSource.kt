package com.dinachi.passit.storage.remote


import com.dinachi.passit.datamodel.Category
import com.dinachi.passit.datamodel.ChatMessage
import com.dinachi.passit.datamodel.Condition
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.datamodel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Collections
    private val listingsCol get() = db.collection("listings")
    private val usersCol get() = db.collection("users")
    private fun messagesCol(chatRoomId: String) = db.collection("chats").document(chatRoomId).collection("messages")

    fun currentUserId(): String? = auth.currentUser?.uid

    // ==================== LISTINGS ====================

    /**
     * Observe listings with optional category filter (real-time)
     */
    fun observeListings(category: Category?): Flow<List<Listing>> = callbackFlow {
        var q: Query = listingsCol
            .whereEqualTo("isSold", false)  // ← Changed from status
            .orderBy("createdTimestamp", Query.Direction.DESCENDING)

        if (category != null) {
            q = q.whereEqualTo("category", category.name)
        }

        val reg = q.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { doc ->
                doc.toListingOrNull()
            }.orEmpty()
            trySend(list)
        }

        awaitClose { reg.remove() }
    }

    /**
     * Get all listings (one-time fetch)
     */
    suspend fun getAllListings(): List<Listing> {
        val snap = listingsCol
            .whereEqualTo("isSold", false)
            .orderBy("createdTimestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return snap.documents.mapNotNull { it.toListingOrNull() }
    }

    /**
     * Get listings by category (one-time fetch)
     */
    suspend fun getListingsByCategory(category: Category): List<Listing> {
        val snap = listingsCol
            .whereEqualTo("category", category.name)
            .whereEqualTo("isSold", false)
            .get()
            .await()

        return snap.documents.mapNotNull { it.toListingOrNull() }
    }

    /**
     * Get listings by seller (one-time fetch)
     */
    suspend fun getListingsBySeller(sellerId: String): List<Listing> {
        val snap = listingsCol
            .whereEqualTo("sellerId", sellerId)
            .orderBy("createdTimestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return snap.documents.mapNotNull { it.toListingOrNull() }
    }

    /**
     * Get a single listing
     */
    suspend fun getListing(listingId: String): Listing {
        val doc = listingsCol.document(listingId).get().await()
        return doc.toListingOrNull() ?: throw Exception("Listing not found")
    }

    /**
     * Create a new listing
     */
    suspend fun createListing(listing: Listing): String {
        val uid = currentUserId() ?: error("Not logged in")
        val now = System.currentTimeMillis()
        val docRef = listingsCol.document()

        val payload = listing.copy(
            id = docRef.id,
            sellerId = uid,
            createdTimestamp = now,
            updatedTimestamp = now,
            isSold = false
        ).toFirestoreMap()

        docRef.set(payload).await()
        return docRef.id
    }

    /**
     * Update an existing listing
     */
    suspend fun updateListing(listing: Listing) {
        val payload = listing.copy(
            updatedTimestamp = System.currentTimeMillis()
        ).toFirestoreMap()

        listingsCol.document(listing.id).set(payload).await()
    }

    /**
     * Delete a listing
     */
    suspend fun deleteListing(listingId: String) {
        listingsCol.document(listingId).delete().await()
    }

    // ==================== USERS ====================

    /**
     * Observe user changes in real-time
     */
    fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val reg = usersCol.document(userId).addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            val user = snap?.toUserOrNull()
            trySend(user)
        }
        awaitClose { reg.remove() }
    }

    /**
     * Get a single user (one-time fetch)
     */
    suspend fun getUser(userId: String): User {
        val doc = usersCol.document(userId).get().await()
        return doc.toUserOrNull() ?: throw Exception("User not found")
    }

    /**
     * Create a new user
     */
    suspend fun createUser(user: User) {
        val payload = user.toFirestoreMap()
        usersCol.document(user.id).set(payload).await()
    }

    /**
     * Update an existing user
     */
    suspend fun updateUser(user: User) {
        val payload = user.toFirestoreMap()
        usersCol.document(user.id).set(payload).await()
    }

    /**
     * Follow a user
     */
    suspend fun followUser(userId: String) {
        val currentUser = currentUserId() ?: error("Not logged in")
        // TODO: Implement following logic (update followersCount, etc.)
        // This is a placeholder
    }

    /**
     * Unfollow a user
     */
    suspend fun unfollowUser(userId: String) {
        val currentUser = currentUserId() ?: error("Not logged in")
        // TODO: Implement unfollowing logic
    }

    // ==================== CHAT ====================

    /**
     * Observe messages in real-time
     */
    fun observeMessages(chatRoomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val reg = messagesCol(chatRoomId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }

                val msgs = snap?.documents?.mapNotNull { d ->
                    d.toChatMessageOrNull()
                }.orEmpty()

                trySend(msgs)
            }

        awaitClose { reg.remove() }
    }

    /**
     * Send a text message
     */
    suspend fun sendMessage(chatRoomId: String, receiverId: String, text: String) {
        val senderId = currentUserId() ?: error("Not logged in")
        val now = System.currentTimeMillis()
        val msgRef = messagesCol(chatRoomId).document()

        val payload = mapOf(
            "id" to msgRef.id,
            "chatRoomId" to chatRoomId,
            "senderId" to senderId,
            "messageText" to text,  // ← Changed from "message"
            "timestamp" to now,
            "isRead" to false,
            "imageUrl" to null
        )

        msgRef.set(payload).await()
    }

    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(chatRoomId: String, userId: String) {
        // TODO: Update all messages where receiverId == userId to isRead = true
        // This requires a batch write
    }

    /**
     * Delete a message
     */
    suspend fun deleteMessage(messageId: String) {
        // TODO: Need to know chatRoomId to delete
        // For now, this is a placeholder
    }
}

// ==================== MAPPING HELPERS ====================

/**
 * Convert Firestore document to User
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toUserOrNull(): User? {
    if (!exists()) return null
    return User(
        id = id,
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        photoUrl = getString("photoUrl") ?: "",
        bio = getString("bio") ?: "",
        location = getString("location") ?: "",
        phoneNumber = getString("phoneNumber") ?: "",
        rating = (getDouble("rating") ?: 0.0).toFloat(),
        reviewsCount = (getLong("reviewsCount") ?: 0L).toInt(),  // ← Changed
        followersCount = (getLong("followersCount") ?: 0L).toInt(),  // ← Added
        followingCount = (getLong("followingCount") ?: 0L).toInt(),  // ← Added
        isVerified = getBoolean("isVerified") ?: false,  // ← Added
        isOnline = getBoolean("isOnline") ?: false,
        createdAt = getLong("createdAt") ?: 0L,  // ← Changed
        lastSeen = getLong("lastSeen") ?: 0L  // ← Changed
    )
}

/**
 * Convert Firestore document to Listing
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toListingOrNull(): Listing? {
    if (!exists()) return null

    val condition = runCatching {
        Condition.valueOf(getString("condition") ?: Condition.Good.name)
    }.getOrDefault(Condition.Good)

    val category = runCatching {
        Category.valueOf(getString("category") ?: Category.Other.name)
    }.getOrDefault(Category.Other)

    @Suppress("UNCHECKED_CAST")
    val imageUrls = (get("imageUrls") as? List<String>).orEmpty()

    return Listing(
        id = id,
        sellerId = getString("sellerId") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        price = getDouble("price") ?: 0.0,
        currency = getString("currency") ?: "CAD",
        condition = condition,
        category = category,
        brand = getString("brand") ?: "",
        location = getString("location") ?: "",
        latitude = getDouble("latitude") ?: 0.0,
        longitude = getDouble("longitude") ?: 0.0,
        imageUrls = imageUrls,
        createdTimestamp = getLong("createdTimestamp") ?: 0L,
        updatedTimestamp = getLong("updatedTimestamp") ?: 0L,
        viewCount = (getLong("viewCount") ?: 0L).toInt(),
        favoriteCount = (getLong("favoriteCount") ?: 0L).toInt(),
        isSold = getBoolean("isSold") ?: false  // ← Changed from status
    )
}

/**
 * Convert Firestore document to ChatMessage
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toChatMessageOrNull(): ChatMessage? {
    if (!exists()) return null
    return ChatMessage(
        id = getString("id") ?: id,
        chatRoomId = getString("chatRoomId") ?: "",
        senderId = getString("senderId") ?: "",
        messageText = getString("messageText") ?: getString("message") ?: "",  // ← Support both
        timestamp = getLong("timestamp") ?: 0L,
        isRead = getBoolean("isRead") ?: false,
        imageUrl = getString("imageUrl")  // ← Changed
    )
}

/**
 * Convert User to Firestore map
 */
private fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "email" to email,
    "photoUrl" to photoUrl,
    "bio" to bio,
    "location" to location,
    "phoneNumber" to phoneNumber,
    "rating" to rating,
    "reviewsCount" to reviewsCount,
    "followersCount" to followersCount,
    "followingCount" to followingCount,
    "isVerified" to isVerified,
    "isOnline" to isOnline,
    "createdAt" to createdAt,
    "lastSeen" to lastSeen
)

/**
 * Convert Listing to Firestore map
 */
private fun Listing.toFirestoreMap(): Map<String, Any?> = mapOf(
    "sellerId" to sellerId,
    "title" to title,
    "description" to description,
    "price" to price,
    "currency" to currency,
    "condition" to condition.name,
    "category" to category.name,
    "brand" to brand,
    "location" to location,
    "latitude" to latitude,
    "longitude" to longitude,
    "imageUrls" to imageUrls,
    "createdTimestamp" to createdTimestamp,
    "updatedTimestamp" to updatedTimestamp,
    "viewCount" to viewCount,
    "favoriteCount" to favoriteCount,
    "isSold" to isSold
)
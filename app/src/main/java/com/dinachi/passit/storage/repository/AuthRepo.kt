package com.dinachi.passit.storage.repository


import com.dinachi.passit.datamodel.User
import com.dinachi.passit.storage.remote.FirebaseAuthDataSource
import com.google.firebase.auth.FirebaseAuth

/**
 * AuthRepo - Handles authentication operations
 * Works with Firebase Auth
 */
class AuthRepo(
    private val authDataSource: FirebaseAuthDataSource,
    private val userRepo: UserRepo
) {

    /**
     * Get current user ID (null if not logged in)
     */
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): User {
        // Authenticate with Firebase Auth
        val userId = authDataSource.signInWithEmail(email, password)

        // Get user profile
        val user = userRepo.getUser(userId)

        // Update online status
        userRepo.updateOnlineStatus(userId, true)

        return user
    }

    /**
     * Sign up with email and password
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String
    ): User {
        // Create Firebase Auth account
        val userId = authDataSource.signUpWithEmail(email, password)

        // Create user profile
        val user = User(
            id = userId,
            name = name,
            email = email,
            photoUrl = "",
            bio = "",
            location = "",
            phoneNumber = "",
            rating = 0f,
            reviewsCount = 0,
            followersCount = 0,
            followingCount = 0,
            isVerified = false,
            isOnline = true,
            createdAt = System.currentTimeMillis(),
            lastSeen = System.currentTimeMillis()
        )

        // Save user profile in Firestore
        userRepo.createUser(user)

        return user
    }

    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(idToken: String): User {
        // Authenticate with Firebase Auth
        val userId = authDataSource.signInWithGoogle(idToken)

        // Check if user profile exists
        val existingUser = try {
            userRepo.getUser(userId)
        } catch (e: Exception) {
            null
        }

        // If new user, create profile
        if (existingUser == null) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser!!
            val newUser = User(
                id = userId,
                name = firebaseUser.displayName ?: "User",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                bio = "",
                location = "",
                phoneNumber = firebaseUser.phoneNumber ?: "",
                rating = 0f,
                reviewsCount = 0,
                followersCount = 0,
                followingCount = 0,
                isVerified = false,
                isOnline = true,
                createdAt = System.currentTimeMillis(),
                lastSeen = System.currentTimeMillis()
            )
            userRepo.createUser(newUser)
            return newUser
        }

        // Update online status
        userRepo.updateOnlineStatus(userId, true)

        return existingUser
    }

    /**
     * Sign in with Apple
     */
    suspend fun signInWithApple(idToken: String, nonce: String): User {
        // Authenticate with Firebase Auth
        val userId = authDataSource.signInWithApple(idToken, nonce)

        // Check if user profile exists
        val existingUser = try {
            userRepo.getUser(userId)
        } catch (e: Exception) {
            null
        }

        // If new user, create profile
        if (existingUser == null) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser!!
            val newUser = User(
                id = userId,
                name = firebaseUser.displayName ?: "User",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                bio = "",
                location = "",
                phoneNumber = "",
                rating = 0f,
                reviewsCount = 0,
                followersCount = 0,
                followingCount = 0,
                isVerified = false,
                isOnline = true,
                createdAt = System.currentTimeMillis(),
                lastSeen = System.currentTimeMillis()
            )
            userRepo.createUser(newUser)
            return newUser
        }

        // Update online status
        userRepo.updateOnlineStatus(userId, true)

        return existingUser
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String) {
        authDataSource.sendPasswordResetEmail(email)
    }

    /**
     * Sign out
     */
    suspend fun signOut() {
        val userId = getCurrentUserId()

        // Update online status to false
        if (userId != null) {
            userRepo.updateOnlineStatus(userId, false)
        }

        // Sign out from Firebase Auth
        authDataSource.signOut()
    }

    /**
     * Delete account
     */
    suspend fun deleteAccount() {
        val userId = getCurrentUserId() ?: return

        // Delete user data from Firestore
        // TODO: Implement cleanup of user's listings, chats, etc.

        // Delete Firebase Auth account
        authDataSource.deleteAccount()
    }

    /**
     * Update email
     */
    suspend fun updateEmail(newEmail: String) {
        authDataSource.updateEmail(newEmail)

        // Update in user profile
        val userId = getCurrentUserId() ?: return
        val user = userRepo.getUser(userId).copy(email = newEmail)
        userRepo.updateUser(user)
    }

    /**
     * Update password
     */
    suspend fun updatePassword(newPassword: String) {
        authDataSource.updatePassword(newPassword)
    }

    /**
     * Re-authenticate user (required before sensitive operations)
     */
    suspend fun reauthenticate(email: String, password: String) {
        authDataSource.reauthenticate(email, password)
    }
}
package com.dinachi.passit.storage.remote


import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * FirebaseAuthDataSource - Handles Firebase Authentication operations
 */
class FirebaseAuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Get current user ID (null if not logged in)
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Sign in with email and password
     * @return User ID
     */
    suspend fun signInWithEmail(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Sign in failed")
    }

    /**
     * Sign up with email and password
     * @return User ID
     */
    suspend fun signUpWithEmail(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Sign up failed")
    }

    /**
     * Sign in with Google
     * @param idToken Google ID token from Google Sign In
     * @return User ID
     */
    suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user?.uid ?: throw Exception("Google sign in failed")
    }

    /**
     * Sign in with Apple
     * @param idToken Apple ID token
     * @param nonce Nonce used for the sign in request
     * @return User ID
     */
    suspend fun signInWithApple(idToken: String, nonce: String): String {
        val credential = OAuthProvider.newCredentialBuilder("apple.com")
            .setIdToken(idToken)
            .build()
        val result = auth.signInWithCredential(credential).await()
        return result.user?.uid ?: throw Exception("Apple sign in failed")
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Delete current user account
     */
    suspend fun deleteAccount() {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.delete().await()
    }

    /**
     * Update email address
     */
    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.updateEmail(newEmail).await()
    }

    /**
     * Update password
     */
    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.updatePassword(newPassword).await()
    }

    /**
     * Re-authenticate user (required before sensitive operations like email/password change)
     */
    suspend fun reauthenticate(email: String, password: String) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
    }

    /**
     * Send email verification
     */
    suspend fun sendEmailVerification() {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.sendEmailVerification().await()
    }

    /**
     * Check if email is verified
     */
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    /**
     * Reload current user data from Firebase
     */
    suspend fun reloadUser() {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.reload().await()
    }
}
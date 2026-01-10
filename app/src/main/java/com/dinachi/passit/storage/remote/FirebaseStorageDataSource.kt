package com.dinachi.passit.storage.remote


import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * FirebaseStorageDataSource - Handles Firebase Storage operations for images
 */
class FirebaseStorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private val storageRef: StorageReference = storage.reference

    /**
     * Upload a listing image
     * @param uri Local image URI
     * @param listingId Listing ID
     * @param imageIndex Index of the image (0 for first image, 1 for second, etc.)
     * @return Download URL
     */
    suspend fun uploadListingImage(
        uri: Uri,
        listingId: String,
        imageIndex: Int
    ): String {
        // Create unique filename
        val timestamp = System.currentTimeMillis()
        val filename = "listing_${listingId}_${imageIndex}_${timestamp}.jpg"
        val imageRef = storageRef.child("listings/$listingId/$filename")

        // Upload file
        val uploadTask = imageRef.putFile(uri).await()

        // Get download URL
        return imageRef.downloadUrl.await().toString()
    }

    /**
     * Upload multiple listing images
     * @return List of download URLs
     */
    suspend fun uploadListingImages(
        uris: List<Uri>,
        listingId: String
    ): List<String> {
        return uris.mapIndexed { index, uri ->
            uploadListingImage(uri, listingId, index)
        }
    }

    /**
     * Upload profile photo
     * @param uri Local image URI
     * @param userId User ID
     * @return Download URL
     */
    suspend fun uploadProfilePhoto(uri: Uri, userId: String): String {
        val timestamp = System.currentTimeMillis()
        val filename = "profile_${userId}_${timestamp}.jpg"
        val imageRef = storageRef.child("profiles/$userId/$filename")

        // Upload file
        imageRef.putFile(uri).await()

        // Get download URL
        return imageRef.downloadUrl.await().toString()
    }

    /**
     * Upload chat image
     * @param uri Local image URI
     * @param chatRoomId Chat room ID
     * @return Download URL
     */
    suspend fun uploadChatImage(uri: Uri, chatRoomId: String): String {
        val timestamp = System.currentTimeMillis()
        val uniqueId = UUID.randomUUID().toString()
        val filename = "chat_${chatRoomId}_${timestamp}_${uniqueId}.jpg"
        val imageRef = storageRef.child("chats/$chatRoomId/$filename")

        // Upload file
        imageRef.putFile(uri).await()

        // Get download URL
        return imageRef.downloadUrl.await().toString()
    }

    /**
     * Delete an image from storage
     * @param imageUrl Full download URL of the image
     */
    suspend fun deleteImage(imageUrl: String) {
        try {
            // Extract storage path from URL
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            // Image might not exist or URL is invalid
            println("Failed to delete image: $imageUrl - ${e.message}")
        }
    }

    /**
     * Delete all images for a listing
     * @param listingId Listing ID
     */
    suspend fun deleteListingImages(listingId: String) {
        try {
            val listingFolder = storageRef.child("listings/$listingId")
            val listResult = listingFolder.listAll().await()

            // Delete all files in the listing folder
            listResult.items.forEach { item ->
                item.delete().await()
            }
        } catch (e: Exception) {
            println("Failed to delete listing images: $listingId - ${e.message}")
        }
    }

    /**
     * Delete old profile photos for a user (keep only the latest)
     * @param userId User ID
     * @param currentPhotoUrl URL of the current photo to keep
     */
    suspend fun deleteOldProfilePhotos(userId: String, currentPhotoUrl: String) {
        try {
            val profileFolder = storageRef.child("profiles/$userId")
            val listResult = profileFolder.listAll().await()

            // Get current photo reference
            val currentPhotoRef = try {
                storage.getReferenceFromUrl(currentPhotoUrl)
            } catch (e: Exception) {
                null
            }

            // Delete all files except the current one
            listResult.items.forEach { item ->
                if (item.path != currentPhotoRef?.path) {
                    item.delete().await()
                }
            }
        } catch (e: Exception) {
            println("Failed to delete old profile photos: $userId - ${e.message}")
        }
    }

    /**
     * Get download URL for a storage reference
     * (Used for existing images)
     */
    suspend fun getDownloadUrl(storagePath: String): String {
        val imageRef = storageRef.child(storagePath)
        return imageRef.downloadUrl.await().toString()
    }
}
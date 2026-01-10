package com.dinachi.passit.storage.repository

import android.net.Uri
import com.dinachi.passit.storage.remote.FirebaseStorageDataSource

/**
 * ImageRepo - Handles image upload operations
 * Works with Firebase Storage
 */
class ImageRepo(
    private val storageDataSource: FirebaseStorageDataSource = FirebaseStorageDataSource()
) {

    /**
     * Upload listing images
     * @return List of download URLs
     */
    suspend fun uploadListingImages(uris: List<Uri>, listingId: String): List<String> {
        return uris.mapIndexed { index, uri ->
            storageDataSource.uploadListingImage(
                uri = uri,
                listingId = listingId,
                imageIndex = index
            )
        }
    }

    /**
     * Upload a single listing image
     * @return Download URL
     */
    suspend fun uploadListingImage(uri: Uri, listingId: String, imageIndex: Int): String {
        return storageDataSource.uploadListingImage(uri, listingId, imageIndex)
    }

    /**
     * Upload profile photo
     * @return Download URL
     */
    suspend fun uploadProfilePhoto(uri: Uri, userId: String): String {
        return storageDataSource.uploadProfilePhoto(uri, userId)
    }

    /**
     * Upload chat image
     * @return Download URL
     */
    suspend fun uploadChatImage(uri: Uri, chatRoomId: String): String {
        return storageDataSource.uploadChatImage(uri, chatRoomId)
    }

    /**
     * Delete an image from storage
     */
    suspend fun deleteImage(imageUrl: String) {
        storageDataSource.deleteImage(imageUrl)
    }

    /**
     * Delete all listing images
     */
    suspend fun deleteListingImages(imageUrls: List<String>) {
        imageUrls.forEach { url ->
            try {
                storageDataSource.deleteImage(url)
            } catch (e: Exception) {
                println("Failed to delete image: $url")
            }
        }
    }
}
package com.dinachi.passit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.datamodel.User
import com.dinachi.passit.storage.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ProfileViewModel - Handles user profile functionality
 */
class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userRepo = RepositoryProvider.provideUserRepo()
    private val listingRepo = RepositoryProvider.provideListingRepo()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Load user profile and their listings
     */
    fun loadProfile(userId: String, currentUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load user data
                val user = userRepo.getUser(userId)

                // Check if this is the current user's own profile
                val isOwnProfile = userId == currentUserId

                _uiState.update {
                    it.copy(
                        user = user,
                        isOwnProfile = isOwnProfile,
                        isLoading = false
                    )
                }

                // Load user's listings
                observeUserListings(userId)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    /**
     * Observe user profile changes in real-time
     */
    fun observeUser(userId: String) {
        viewModelScope.launch {
            userRepo.observeUser(userId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collectLatest { user ->
                    _uiState.update { it.copy(user = user) }
                }
        }
    }

    /**
     * Switch between Active/Sold/Reviews tabs
     */
    fun onTabSelected(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * Update user profile
     */
    fun updateProfile(
        name: String,
        bio: String,
        photoUrl: String,
        onSuccess: () -> Unit
    ) {
        val currentUser = _uiState.value.user ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            try {
                val updatedUser = currentUser.copy(
                    name = name,
                    bio = bio,
                    photoUrl = photoUrl
                )

                userRepo.updateUser(updatedUser)

                _uiState.update {
                    it.copy(
                        user = updatedUser,
                        isUpdating = false
                    )
                }

                onSuccess()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        error = e.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    /**
     * Follow/Unfollow a user
     */
    fun toggleFollow() {
        val user = _uiState.value.user ?: return
        val isFollowing = _uiState.value.isFollowing

        viewModelScope.launch {
            try {
                if (isFollowing) {
                    userRepo.unfollowUser(user.id)
                } else {
                    userRepo.followUser(user.id)
                }

                _uiState.update { it.copy(isFollowing = !isFollowing) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to ${if (isFollowing) "unfollow" else "follow"} user")
                }
            }
        }
    }

    /**
     * Upload profile photo
     */
    fun uploadProfilePhoto(imageUri: android.net.Uri, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }

            try {
                // TODO: Upload image via ImageRepo
                // val photoUrl = imageRepo.uploadProfilePhoto(imageUri)
                // For now, simulate
                val photoUrl = imageUri.toString()

                _uiState.update { it.copy(isUpdating = false) }
                onSuccess(photoUrl)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        error = "Failed to upload photo"
                    )
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Observe user's listings in real-time
     */
    private fun observeUserListings(userId: String) {
        viewModelScope.launch {
            listingRepo.observeUserListings(userId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collectLatest { listings ->
                    // Split into active and sold
                    val activeListings = listings.filter { !it.isSold }
                    val soldListings = listings.filter { it.isSold }

                    _uiState.update {
                        it.copy(
                            activeListings = activeListings,
                            soldListings = soldListings,
                            itemsSold = soldListings.size
                        )
                    }
                }
        }
    }
}

/**
 * UI State for ProfileScreen
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val isOwnProfile: Boolean = false,
    val isFollowing: Boolean = false,
    val selectedTab: ProfileTab = ProfileTab.Active,
    val activeListings: List<Listing> = emptyList(),
    val soldListings: List<Listing> = emptyList(),
    val itemsSold: Int = 0
)

/**
 * Profile tabs
 */
enum class ProfileTab {
    Active,
    Sold,
    Reviews
}
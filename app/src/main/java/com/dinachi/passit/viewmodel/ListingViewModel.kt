package com.dinachi.passit.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dinachi.passit.datamodel.Category
import com.dinachi.passit.datamodel.Condition
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.storage.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ListingViewModel - Handles both CreateListingScreen and ListingDetailScreen
 */
class ListingViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repo = RepositoryProvider.provideListingRepo()
    private val imageRepo = RepositoryProvider.provideImageRepo()

    // ==================== DETAIL SCREEN STATE ====================

    private val _detailUiState = MutableStateFlow(ListingDetailUiState())
    val detailUiState: StateFlow<ListingDetailUiState> = _detailUiState.asStateFlow()

    // For backward compatibility (used in ListingDetailScreen)
    val uiState: StateFlow<ListingDetailUiState> get() = detailUiState

    // ==================== CREATE SCREEN STATE ====================

    private val _createUiState = MutableStateFlow(CreateListingUiState())
    val createUiState: StateFlow<CreateListingUiState> = _createUiState.asStateFlow()

    // ==================== DETAIL SCREEN METHODS ====================

    /**
     * Load a single listing by ID
     */
    fun loadListing(listingId: String) {
        viewModelScope.launch {
            _detailUiState.update { it.copy(isLoading = true, error = null) }

            runCatching { repo.getListing(listingId) }
                .onSuccess { listing ->
                    _detailUiState.update { it.copy(isLoading = false, listing = listing) }
                }
                .onFailure { e ->
                    _detailUiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Failed to load listing")
                    }
                }
        }
    }

    /**
     * Toggle favorite for a listing
     */
    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement in repo
                // repo.toggleFavorite(listingId)
                println("Toggled favorite: $listingId")
            } catch (e: Exception) {
                _detailUiState.update { it.copy(error = "Failed to update favorite") }
            }
        }
    }

    // ==================== CREATE SCREEN METHODS ====================

    /**
     * Update selected photos
     */
    fun onPhotosSelected(uris: List<Uri>) {
        val current = _createUiState.value.selectedPhotos
        if (current.size + uris.size <= 10) {
            _createUiState.update { it.copy(selectedPhotos = current + uris) }
        }
    }

    /**
     * Remove a photo at index
     */
    fun onRemovePhoto(index: Int) {
        val updated = _createUiState.value.selectedPhotos.filterIndexed { i, _ -> i != index }
        _createUiState.update { it.copy(selectedPhotos = updated) }
    }

    /**
     * Update listing title
     */
    fun onTitleChange(title: String) {
        _createUiState.update { it.copy(title = title, titleError = null) }
    }

    /**
     * Update listing price
     */
    fun onPriceChange(price: String) {
        // Only allow numbers and decimal point
        val filtered = price.filter { it.isDigit() || it == '.' }
        _createUiState.update { it.copy(price = filtered, priceError = null) }
    }

    /**
     * Update currency
     */
    fun onCurrencyChange(currency: String) {
        _createUiState.update { it.copy(selectedCurrency = currency) }
    }

    /**
     * Update condition
     */
    fun onConditionChange(condition: String) {
        _createUiState.update { it.copy(selectedCondition = condition) }
    }

    /**
     * Update category
     */
    fun onCategoryChange(category: String) {
        _createUiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Update description
     */
    fun onDescriptionChange(description: String) {
        _createUiState.update { it.copy(description = description) }
    }

    /**
     * Update brand (optional field)
     */
    fun onBrandChange(brand: String) {
        _createUiState.update { it.copy(brand = brand) }
    }

    /**
     * Update location
     */
    fun onLocationChange(location: String) {
        _createUiState.update { it.copy(location = location) }
    }

    /**
     * Create and post the listing
     */
    fun createListing(onSuccess: () -> Unit) {
        // Validate inputs
        if (!validateCreateInputs()) return

        viewModelScope.launch {
            _createUiState.update { it.copy(isCreating = true, error = null) }

            try {
                val state = _createUiState.value

                // TODO: Upload images first
                // val imageUrls = repo.uploadImages(state.selectedPhotos)
                // For now, simulate with empty list
                val imageUrls = emptyList<String>()

                // Map UI data to domain model
                val listing = Listing(
                    id = "", // Will be generated by Firebase
                    title = state.title,
                    description = state.description,
                    price = state.price.toDoubleOrNull() ?: 0.0,
                    currency = state.selectedCurrency,
                    category = mapCategory(state.selectedCategory),
                    condition = mapCondition(state.selectedCondition),
                    location = state.location,
                    imageUrls = imageUrls,
                    sellerId = "", // TODO: Get from auth
                    createdTimestamp = System.currentTimeMillis(),
                    updatedTimestamp = System.currentTimeMillis(),
                    isSold = false,
                    brand = state.brand
                )

                // TODO: Call repo to create listing
                // repo.createListing(listing)

                // Simulate success
                kotlinx.coroutines.delay(1000)

                _createUiState.update { it.copy(isCreating = false) }

                // Reset form
                resetCreateForm()

                // Navigate back
                onSuccess()

            } catch (e: Exception) {
                _createUiState.update {
                    it.copy(
                        isCreating = false,
                        error = e.message ?: "Failed to create listing"
                    )
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _createUiState.update { it.copy(error = null) }
        _detailUiState.update { it.copy(error = null) }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate create listing inputs
     */
    private fun validateCreateInputs(): Boolean {
        val state = _createUiState.value
        var hasError = false

        // Validate photos
        if (state.selectedPhotos.isEmpty()) {
            _createUiState.update { it.copy(error = "Please add at least one photo") }
            hasError = true
        }

        // Validate title
        if (state.title.isBlank()) {
            _createUiState.update { it.copy(titleError = "Title is required") }
            hasError = true
        } else if (state.title.length < 3) {
            _createUiState.update { it.copy(titleError = "Title must be at least 3 characters") }
            hasError = true
        }

        // Validate price
        if (state.price.isBlank()) {
            _createUiState.update { it.copy(priceError = "Price is required") }
            hasError = true
        } else {
            val priceValue = state.price.toDoubleOrNull()
            if (priceValue == null || priceValue <= 0) {
                _createUiState.update { it.copy(priceError = "Please enter a valid price") }
                hasError = true
            }
        }

        // Validate description
        if (state.description.isBlank()) {
            _createUiState.update { it.copy(descriptionError = "Description is required") }
            hasError = true
        } else if (state.description.length < 10) {
            _createUiState.update { it.copy(descriptionError = "Description must be at least 10 characters") }
            hasError = true
        }

        return !hasError
    }

    /**
     * Reset create form to initial state
     */
    private fun resetCreateForm() {
        _createUiState.value = CreateListingUiState()
    }

    /**
     * Map category string to Category enum
     */
    private fun mapCategory(categoryString: String): Category {
        return when (categoryString) {
            "Electronics & Gadgets" -> Category.Electronics
            "Clothing & Fashion" -> Category.Clothing
            "Home & Garden", "Furniture" -> Category.Furniture
            "Sports & Outdoors" -> Category.Sports
            "Books & Media" -> Category.Books
            "Toys & Games" -> Category.Toys
            "Vehicles" -> Category.Other
            "Art & Collectibles" -> Category.Other
            else -> Category.Other
        }
    }

    /**
     * Map condition string to Condition enum
     */
    private fun mapCondition(conditionString: String): Condition {
        return when (conditionString) {
            "New" -> Condition.New
            "Like New" -> Condition.LikeNew
            "Good" -> Condition.Good
            "Fair" -> Condition.Fair
            else -> Condition.Good
        }
    }
}

// ==================== UI STATE CLASSES ====================

/**
 * UI State for ListingDetailScreen
 */
data class ListingDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val listing: Listing? = null
)

/**
 * UI State for CreateListingScreen
 */
data class CreateListingUiState(
    val selectedPhotos: List<Uri> = emptyList(),
    val title: String = "",
    val price: String = "",
    val selectedCurrency: String = "CAD",
    val selectedCondition: String = "Like New",
    val selectedCategory: String = "Electronics & Gadgets",
    val description: String = "",
    val brand: String = "",
    val location: String = "Toronto, ON",
    val isCreating: Boolean = false,
    val error: String? = null,
    val titleError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null
)
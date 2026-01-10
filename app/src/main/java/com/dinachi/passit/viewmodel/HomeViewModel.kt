package com.dinachi.passit.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinachi.passit.datamodel.Category
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.storage.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * HomeViewModel - Handles home feed, search, and filter logic
 * Used by: HomeFeedScreen & SearchFilterScreen
 */
class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val listingRepo = RepositoryProvider.provideListingRepo()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeListings()
    }

    // ==================== HOME FEED METHODS ====================

    /**
     * Handle category selection and filter listings
     */
    fun onCategorySelected(category: String) {
        val mapped = mapCategoryStringToEnum(category)

        _uiState.update { it.copy(selectedCategory = category) }
        observeListings(mapped)
    }

    /**
     * Refresh listings (pull-to-refresh)
     */
    fun refreshListings() {
        val category = mapCategoryStringToEnum(_uiState.value.selectedCategory)
        observeListings(category)
    }

    /**
     * Toggle favorite status for a listing
     */
    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            try {
                // TODO: Call repository to toggle favorite
                // listingRepo.toggleFavorite(listingId)
                println("Toggled favorite for listing: $listingId")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update favorite.")
                }
            }
        }
    }

    // ==================== SEARCH & FILTER METHODS ====================

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Auto-search when query changes
        if (query.isNotBlank()) {
            searchListings(query)
        } else {
            // Clear search results when query is empty
            _uiState.update { it.copy(searchResults = emptyList(), resultsCount = 0) }
        }
    }

    /**
     * Update sort option
     */
    fun updateSortOption(sort: String) {
        _uiState.update { it.copy(selectedSort = sort) }
        applyFilters()
    }

    /**
     * Update price range
     */
    fun updatePriceRange(range: ClosedFloatingPointRange<Float>) {
        _uiState.update { it.copy(priceRange = range) }
    }

    /**
     * Update condition filter
     */
    fun updateCondition(condition: String) {
        _uiState.update { it.copy(selectedCondition = condition) }
    }

    /**
     * Apply all filters and re-fetch results
     */
    fun applyFilters() {
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            searchListings(query)
        }
    }

    /**
     * Reset all filters to default
     */
    fun resetFilters() {
        _uiState.update {
            it.copy(
                selectedSort = "Recommended",
                priceRange = 0f..5000f,
                selectedCondition = "Any"
            )
        }
        applyFilters()
    }

    /**
     * Search listings by query with current filters applied
     */
    fun searchListings(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), resultsCount = 0) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            listingRepo.searchListings(query)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Search failed"
                        )
                    }
                }
                .collectLatest { listings ->
                    // Apply filters
                    var filtered = listings

                    // Filter by price range
                    val currentState = _uiState.value
                    filtered = filtered.filter { listing ->
                        listing.price >= currentState.priceRange.start &&
                                listing.price <= currentState.priceRange.endInclusive
                    }

                    // Filter by condition
                    // Filter by condition
                    if (currentState.selectedCondition != "Any") {
                        filtered = filtered.filter { listing ->
                            listing.condition.name == currentState.selectedCondition
                        }
                    }

                    // Sort results
                    // Sort results
                    filtered = when (currentState.selectedSort) {
                        "Lowest Price" -> filtered.sortedBy { it.price }
                        "Newest" -> filtered.sortedByDescending { it.createdTimestamp }
                        else -> filtered // "Recommended" - keep as is
                    }

                    // Map to UI models
                    val uiListings = filtered.map { it.toHomeListingUi() }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = uiListings,
                            resultsCount = uiListings.size,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Observe listings from repository
     */
    private fun observeListings(category: Category? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            listingRepo.observeListings(category = category)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load listings"
                        )
                    }
                }
                .collectLatest { listings ->
                    // Map domain models to UI models
                    val uiListings = listings.map { it.toHomeListingUi() }

                    // Split into sections for UI
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            freshFinds = uiListings.take(5),
                            exploreLocal = uiListings.drop(5),
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * Map category string to Category enum
     */
    private fun mapCategoryStringToEnum(category: String): Category? {
        return when (category) {
            "All" -> null
            "Furniture" -> Category.Furniture
            "Electronics" -> Category.Electronics
            "Clothing" -> Category.Clothing
            "Books" -> Category.Books
            "Sports" -> Category.Sports
            "Toys" -> Category.Toys
            "Home" -> Category.Home
            "Other" -> Category.Other
            else -> null
        }
    }

    /**
     * Map domain Listing to UI model
     */
    private fun Listing.toHomeListingUi(): HomeListingUi {
        return HomeListingUi(
            id = this.id,
            title = this.title,
            priceText = "$${this.price}",
            locationText = this.location,
            imageUrl = this.imageUrls.firstOrNull() ?: ""
        )
    }
}

// ==================== UI STATE & MODELS ====================

/**
 * UI State for HomeFeedScreen and SearchFilterScreen
 */
data class HomeUiState(
    // Loading & Error
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Home Feed
    val selectedCategory: String = "All",
    val freshFinds: List<HomeListingUi> = emptyList(),
    val exploreLocal: List<HomeListingUi> = emptyList(),

    // Search & Filters
    val searchQuery: String = "",
    val searchResults: List<HomeListingUi> = emptyList(),
    val selectedSort: String = "Recommended",
    val priceRange: ClosedFloatingPointRange<Float> = 50f..1200f,
    val selectedCondition: String = "Any",
    val resultsCount: Int = 0
)

/**
 * UI model for displaying listings in home feed
 */
data class HomeListingUi(
    val id: String,
    val title: String,
    val priceText: String,
    val locationText: String,
    val imageUrl: String
)
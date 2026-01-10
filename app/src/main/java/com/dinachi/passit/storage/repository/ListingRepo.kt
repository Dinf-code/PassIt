package com.dinachi.passit.storage.repository


import com.dinachi.passit.datamodel.Category
import com.dinachi.passit.datamodel.Condition
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.storage.local.ListingEntity
import com.dinachi.passit.storage.local.dao.ListingDao
import com.dinachi.passit.storage.remote.FirestoreDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ListingRepo - Single source of truth for listing data
 */
class ListingRepo(
    private val listingDao: ListingDao,
    private val firestoreDataSource: FirestoreDataSource = FirestoreDataSource()
) {

    fun observeListings(category: Category? = null): Flow<List<Listing>> {
        syncListingsFromFirestore(category)
        return if (category != null) {
            listingDao.observeListingsByCategory(category.name)
        } else {
            listingDao.observeAllListings()
        }.map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getListing(listingId: String): Listing {
        val cached = listingDao.getListingById(listingId)
        if (cached != null) {
            return cached.toDomainModel()
        }
        val listing = firestoreDataSource.getListing(listingId)
        listingDao.insertListing(listing.toEntity())
        return listing
    }

    fun observeUserListings(userId: String): Flow<List<Listing>> {
        syncUserListingsFromFirestore(userId)
        return listingDao.observeListingsBySeller(userId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    fun searchListings(query: String): Flow<List<Listing>> {
        return listingDao.searchListings("%$query%")
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    suspend fun createListing(listing: Listing): String {
        val listingId = firestoreDataSource.createListing(listing)
        val listingWithId = listing.copy(id = listingId)
        listingDao.insertListing(listingWithId.toEntity())
        return listingId
    }

    suspend fun updateListing(listing: Listing) {
        firestoreDataSource.updateListing(listing)
        listingDao.updateListing(listing.toEntity())
    }

    suspend fun deleteListing(listingId: String) {
        firestoreDataSource.deleteListing(listingId)
        listingDao.deleteListingById(listingId)
    }

    suspend fun markAsSold(listingId: String) {
        val listing = getListing(listingId).copy(isSold = true)
        updateListing(listing)
    }

    private fun syncListingsFromFirestore(category: Category?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listings = if (category != null) {
                    firestoreDataSource.getListingsByCategory(category)
                } else {
                    firestoreDataSource.getAllListings()
                }
                listingDao.insertListings(listings.map { it.toEntity() })
            } catch (e: Exception) {
                println("Failed to sync listings: ${e.message}")
            }
        }
    }

    private fun syncUserListingsFromFirestore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listings = firestoreDataSource.getListingsBySeller(userId)
                listingDao.insertListings(listings.map { it.toEntity() })
            } catch (e: Exception) {
                println("Failed to sync user listings: ${e.message}")
            }
        }
    }

    // Extension functions
    private fun Listing.toEntity(): ListingEntity {
        return ListingEntity(
            id = this.id,
            sellerId = this.sellerId,
            title = this.title,
            description = this.description,
            price = this.price,
            currency = this.currency,
            condition = this.condition.name,
            category = this.category.name,
            brand = this.brand,
            location = this.location,
            latitude = this.latitude,
            longitude = this.longitude,
            imageUrls = this.imageUrls.joinToString(","),
            createdTimestamp = this.createdTimestamp,
            updatedTimestamp = this.updatedTimestamp,
            viewCount = this.viewCount,
            favoriteCount = this.favoriteCount,
            isSold = this.isSold
        )
    }

    private fun ListingEntity.toDomainModel(): Listing {
        return Listing(
            id = this.id,
            sellerId = this.sellerId,
            title = this.title,
            description = this.description,
            price = this.price,
            currency = this.currency,
            condition = Condition.valueOf(this.condition),
            category = Category.valueOf(this.category),
            brand = this.brand,
            location = this.location,
            latitude = this.latitude,
            longitude = this.longitude,
            imageUrls = if (this.imageUrls.isBlank()) emptyList() else this.imageUrls.split(","),
            createdTimestamp = this.createdTimestamp,
            updatedTimestamp = this.updatedTimestamp,
            viewCount = this.viewCount,
            favoriteCount = this.favoriteCount,
            isSold = this.isSold
        )
    }
}
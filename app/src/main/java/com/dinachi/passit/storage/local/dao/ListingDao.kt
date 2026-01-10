package com.dinachi.passit.storage.local.dao


import androidx.room.*
import androidx.room.Dao
import com.dinachi.passit.storage.local.ListingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    /**
     * Observe all listings (not sold)
     */
    @Query("SELECT * FROM listings WHERE isSold = 0 ORDER BY createdTimestamp DESC")
    fun observeAllListings(): Flow<List<ListingEntity>>

    /**
     * Observe listings by category
     */
    @Query("SELECT * FROM listings WHERE category = :category AND isSold = 0 ORDER BY createdTimestamp DESC")
    fun observeListingsByCategory(category: String): Flow<List<ListingEntity>>

    /**
     * Observe listings by seller
     */
    @Query("SELECT * FROM listings WHERE sellerId = :sellerId ORDER BY createdTimestamp DESC")
    fun observeListingsBySeller(sellerId: String): Flow<List<ListingEntity>>

    /**
     * Get a single listing by ID
     */
    @Query("SELECT * FROM listings WHERE id = :listingId")
    suspend fun getListingById(listingId: String): ListingEntity?

    /**
     * Search listings by title
     */
    @Query("SELECT * FROM listings WHERE title LIKE :query AND isSold = 0 ORDER BY createdTimestamp DESC")
    fun searchListings(query: String): Flow<List<ListingEntity>>

    /**
     * Insert a single listing
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    /**
     * Insert multiple listings
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<ListingEntity>)

    /**
     * Update a listing
     */
    @Update
    suspend fun updateListing(listing: ListingEntity)

    /**
     * Delete a listing by ID
     */
    @Query("DELETE FROM listings WHERE id = :listingId")
    suspend fun deleteListingById(listingId: String)

    /**
     * Delete all listings
     */
    @Query("DELETE FROM listings")
    suspend fun deleteAll()

    /**
     * Get sold listings count for a seller
     */
    @Query("SELECT COUNT(*) FROM listings WHERE sellerId = :sellerId AND isSold = 1")
    suspend fun getSoldListingsCount(sellerId: String): Int
}
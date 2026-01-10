package com.dinachi.passit.storage.local.dao


import androidx.room.*
import com.dinachi.passit.storage.local.ExchangeRateEntity

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates WHERE fromCurrency = :from AND toCurrency = :to LIMIT 1")
    suspend fun getRate(from: String, to: String): ExchangeRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: ExchangeRateEntity)

    @Query("SELECT * FROM exchange_rates")
    suspend fun getAllRates(): List<ExchangeRateEntity>

    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAll()

    @Query("DELETE FROM exchange_rates WHERE timestamp < :timestamp")
    suspend fun deleteOldRates(timestamp: Long)
}
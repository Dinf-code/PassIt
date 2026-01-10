package com.dinachi.passit.storage.repository


import com.dinachi.passit.storage.local.ExchangeRateEntity
import com.dinachi.passit.storage.local.dao.ExchangeRateDao
import com.dinachi.passit.storage.remote.ExchangeRateAPIService

/**
 * ExchangeRateRepo - Manages currency exchange rates with caching
 */
class ExchangeRateRepo(
    private val exchangeRateDao: ExchangeRateDao,
    private val apiService: ExchangeRateAPIService = ExchangeRateAPIService()
) {

    suspend fun getExchangeRate(from: String, to: String): Double {
        val cached = exchangeRateDao.getRate(from, to)
        val cacheAge = System.currentTimeMillis() - (cached?.timestamp ?: 0L)
        val cacheExpired = cacheAge > CACHE_DURATION_MS

        if (cached != null && !cacheExpired) {
            return cached.rate
        }

        val rate = apiService.getExchangeRate(from, to)
        exchangeRateDao.insertRate(
            ExchangeRateEntity(
                id = "${from}_$to",
                fromCurrency = from,
                toCurrency = to,
                rate = rate,
                timestamp = System.currentTimeMillis()
            )
        )
        return rate
    }

    suspend fun getAllRates(baseCurrency: String): Map<String, Double> {
        return apiService.getAllRates(baseCurrency)
    }

    suspend fun clearCache() {
        exchangeRateDao.deleteAll()
    }

    companion object {
        private const val CACHE_DURATION_MS = 3600_000L // 1 hour
    }
}
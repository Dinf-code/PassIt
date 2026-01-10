package com.dinachi.passit.storage.remote


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * ExchangeRateAPIService - Fetches currency exchange rates from external API
 * Uses exchangerate-api.com (free tier)
 */
class ExchangeRateAPIService {

    // Free API endpoint
    private val baseUrl = "https://open.er-api.com/v6/latest"

    /**
     * Get exchange rate between two currencies
     * @param from Source currency code (e.g., "CAD")
     * @param to Target currency code (e.g., "USD")
     * @return Exchange rate
     */
    suspend fun getExchangeRate(from: String, to: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch rates from API
                val url = "$baseUrl/$from"
                val response = URL(url).readText()
                val json = JSONObject(response)

                // Check if request was successful
                val result = json.getString("result")
                if (result != "success") {
                    throw Exception("API request failed: $result")
                }

                // Get the exchange rate
                val rates = json.getJSONObject("rates")
                rates.getDouble(to)

            } catch (e: Exception) {
                // Fallback to hardcoded rates if API fails
                println("Exchange rate API failed: ${e.message}")
                getHardcodedRate(from, to)
            }
        }
    }

    /**
     * Get all exchange rates for a base currency
     * @param baseCurrency Base currency code (e.g., "CAD")
     * @return Map of currency codes to exchange rates
     */
    suspend fun getAllRates(baseCurrency: String): Map<String, Double> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$baseUrl/$baseCurrency"
                val response = URL(url).readText()
                val json = JSONObject(response)

                val result = json.getString("result")
                if (result != "success") {
                    throw Exception("API request failed: $result")
                }

                val rates = json.getJSONObject("rates")
                val ratesMap = mutableMapOf<String, Double>()

                rates.keys().forEach { key ->
                    ratesMap[key] = rates.getDouble(key)
                }

                ratesMap

            } catch (e: Exception) {
                println("Exchange rate API failed: ${e.message}")
                getHardcodedRates(baseCurrency)
            }
        }
    }

    /**
     * Hardcoded fallback rates (approximate, updated periodically)
     * Used when API fails
     */
    private fun getHardcodedRate(from: String, to: String): Double {
        if (from == to) return 1.0

        // Rates relative to CAD (as of January 2025, approximate)
        val cadRates = mapOf(
            "USD" to 0.74,
            "EUR" to 0.68,
            "GBP" to 0.58,
            "JPY" to 107.0,
            "AUD" to 1.08,
            "CHF" to 0.64,
            "CNY" to 5.20,
            "INR" to 61.0,
            "CAD" to 1.0
        )

        // Convert through CAD
        val fromToCAD = 1.0 / (cadRates[from] ?: 1.0)
        val cadToTarget = cadRates[to] ?: 1.0

        return fromToCAD * cadToTarget
    }

    /**
     * Get all hardcoded rates for a base currency
     */
    private fun getHardcodedRates(baseCurrency: String): Map<String, Double> {
        val cadRates = mapOf(
            "CAD" to 1.0,
            "USD" to 0.74,
            "EUR" to 0.68,
            "GBP" to 0.58,
            "JPY" to 107.0,
            "AUD" to 1.08,
            "CHF" to 0.64,
            "CNY" to 5.20,
            "INR" to 61.0
        )

        // If base is CAD, return as is
        if (baseCurrency == "CAD") {
            return cadRates
        }

        // Convert all rates to the requested base currency
        val baseToCAD = 1.0 / (cadRates[baseCurrency] ?: 1.0)
        return cadRates.mapValues { (_, rate) ->
            baseToCAD * rate
        }
    }
}
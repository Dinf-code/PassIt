package com.dinachi.passit.storage

import android.content.Context
import com.dinachi.passit.storage.local.PassItDatabase
import com.dinachi.passit.storage.remote.*
import com.dinachi.passit.storage.repository.*

/**
 * RepositoryProvider - Simple dependency injection
 * Provides pre-configured repository instances
 */
object RepositoryProvider {

    private lateinit var database: PassItDatabase
    private lateinit var firestoreDataSource: FirestoreDataSource
    private lateinit var authDataSource: FirebaseAuthDataSource
    private lateinit var storageDataSource: FirebaseStorageDataSource
    private lateinit var exchangeRateService: ExchangeRateAPIService

    /**
     * Initialize all dependencies
     * Call this once in Application.onCreate()
     */
    fun initialize(context: Context) {
        // Initialize Room Database
        database = PassItDatabase.getDatabase(context)

        // Initialize Firebase DataSources
        firestoreDataSource = FirestoreDataSource()
        authDataSource = FirebaseAuthDataSource()
        storageDataSource = FirebaseStorageDataSource()
        exchangeRateService = ExchangeRateAPIService()
    }

    // ==================== REPOSITORIES ====================

    fun provideAuthRepo(): AuthRepo {
        return AuthRepo(
            authDataSource = authDataSource,
            userRepo = provideUserRepo()
        )
    }

    fun provideListingRepo(): ListingRepo {
        return ListingRepo(
            listingDao = database.listingDao(),
            firestoreDataSource = firestoreDataSource
        )
    }

    fun provideUserRepo(): UserRepo {
        return UserRepo(
            userDao = database.userDao(),
            firestoreDataSource = firestoreDataSource
        )
    }

    fun provideChatRepo(): ChatRepo {
        return ChatRepo(
            chatMessageDao = database.chatMessageDao(),
            firestoreDataSource = firestoreDataSource
        )
    }

    fun provideImageRepo(): ImageRepo {
        return ImageRepo(
            storageDataSource = storageDataSource
        )
    }

    fun provideExchangeRateRepo(): ExchangeRateRepo {
        return ExchangeRateRepo(
            exchangeRateDao = database.exchangeRateDao(),
            apiService = exchangeRateService
        )
    }
}
package com.dinachi.passit.AppWide

import android.app.Application
import com.dinachi.passit.storage.RepositoryProvider
import com.google.firebase.FirebaseApp

/**
 * PassItApplication - Custom Application class
 * Initializes dependencies on app startup
 */
class PassItApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase FIRST
        FirebaseApp.initializeApp(this)

        // Initialize Repository Provider with DAOs and DataSources
        RepositoryProvider.initialize(this)
    }
}
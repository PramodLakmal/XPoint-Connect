package com.xpoint.connect

import android.app.Application
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.database.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class XPointConnectApplication : Application() {

    lateinit var userPreferencesManager: UserPreferencesManager
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize SQLite-based UserPreferencesManager
        userPreferencesManager = UserPreferencesManager(this)

        // Initialize database
        applicationScope.launch { userPreferencesManager.initialize() }

        // Initialize API Client with preferences
        ApiClient.init(userPreferencesManager)
    }
}

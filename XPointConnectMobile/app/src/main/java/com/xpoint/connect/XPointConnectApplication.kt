/**
 * XPointConnectApplication.kt
 *
 * Purpose: Main application class for XPoint Connect mobile application initialization Author:
 * XPoint Connect Development Team Date: September 27, 2025
 *
 * Description: This application class serves as the entry point for the XPoint Connect mobile
 * application. It initializes core application components including Room SQLite database
 * management, user preferences, API client configuration, and application-wide services. The class
 * manages the application lifecycle and provides global access to essential services throughout the
 * app.
 *
 * Key Features:
 * - Room SQLite database initialization and management (replaces SharedPreferences)
 * - User preferences manager with structured database storage
 * - Automatic data migration from SharedPreferences to SQLite
 * - API client initialization with authentication context
 * - Application-wide coroutine scope management
 * - Global service provider for dependency injection
 */
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

    /**
     * Initializes the application and its core components Sets up Room SQLite database, user
     * preferences manager, API client, and application services
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize Room SQLite-based UserPreferencesManager
        userPreferencesManager = UserPreferencesManager(this)

        // Initialize database and migrate data from SharedPreferences if needed
        applicationScope.launch {
            try {
                userPreferencesManager.initialize()
            } catch (e: Exception) {
                // Log initialization error but don't crash the app
                e.printStackTrace()
            }
        }

        // Initialize API Client with preferences
        ApiClient.init(userPreferencesManager)
    }
}

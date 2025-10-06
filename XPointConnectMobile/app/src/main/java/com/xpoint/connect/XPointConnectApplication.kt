/**
 * XPointConnectApplication.kt
 *
 * Purpose: Main application class for XPoint Connect mobile application initialization Author:
 * XPoint Connect Development Team Date: September 27, 2025
 *
 * Description: This application class serves as the entry point for the XPoint Connect mobile
 * application. It initializes core application components including database management, user
 * preferences, API client configuration, and application-wide services. The class manages the
 * application lifecycle and provides global access to essential services throughout the app.
 *
 * Key Features:
 * - SQLite database initialization and management
 * - User preferences manager setup and configuration
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
     * Initializes the application and its core components Sets up database, user preferences, API
     * client, and application services
     */
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

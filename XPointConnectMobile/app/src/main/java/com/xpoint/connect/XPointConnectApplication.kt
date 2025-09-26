package com.xpoint.connect

import android.app.Application
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.utils.SharedPreferencesManager

class XPointConnectApplication : Application() {

    lateinit var sharedPreferencesManager: SharedPreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize SharedPreferences
        sharedPreferencesManager = SharedPreferencesManager(this)

        // Initialize API Client with preferences
        ApiClient.init(sharedPreferencesManager)
    }
}

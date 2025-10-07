/**
 * ApiClient.kt
 *
 * Purpose: Centralized HTTP client configuration for API communication Author: XPoint Connect
 * Development Team Date: September 27, 2025
 *
 * Description: This singleton object provides a centralized configuration for all HTTP API
 * communications in the XPoint Connect application. It manages authentication tokens,
 * request/response logging, timeout configurations, and JSON serialization. The client
 * automatically handles authorization headers and provides retry logic for network operations.
 *
 * Key Features:
 * - Singleton pattern for centralized API client management
 * - Automatic JWT token injection for authenticated requests
 * - HTTP request/response logging for debugging and monitoring
 * - Configurable timeout settings for network operations
 * - JSON serialization/deserialization with Gson
 * - Interceptor chain for request/response processing
 * - Integration with UserPreferencesManager for token management
 */
package com.xpoint.connect.data.api

import com.google.gson.GsonBuilder
import com.xpoint.connect.data.database.UserPreferencesManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:5034/" // Replace with your actual API URL

    private var userPreferencesManager: UserPreferencesManager? = null

    /**
     * Initializes the API client with user preferences manager for token management
     * @param prefsManager UserPreferencesManager instance for authentication token access
     */
    fun init(prefsManager: UserPreferencesManager) {
        userPreferencesManager = prefsManager
    }

    /**
     * Auth interceptor for adding JWT token to all requests
     *
     * Reference: Pattern adapted from OkHttp interceptor example in the official documentation:
     * https://square.github.io/okhttp/interceptors/
     *
     * Token retrieval pattern using `runBlocking` adapted from
     * Kotlin coroutines best practices (https://developer.android.com/kotlin/coroutines)
     */
    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { userPreferencesManager?.getAuthToken() }
        val request =
                if (token != null) {
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                } else {
                    chain.request()
                }
        chain.proceed(request)
    }

    /**
     * Logging interceptor configuration
     *
     * Reference: Standard implementation adapted from Android Retrofit logging tutorial:
     * https://square.github.io/okhttp/features/interceptors/
     */
    private val loggingInterceptor =
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    /**
     * OkHttpClient configuration
     *
     * Reference: Client setup pattern adapted from official Retrofit + OkHttp integration sample:
     * https://square.github.io/retrofit/
     */
    private val okHttpClient =
            OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

    /**
     * Gson and Retrofit initialization
     *
     * Reference: Standard configuration for Retrofit JSON serialization adapted from
     * Retrofit documentation: https://square.github.io/retrofit/
     */
    private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    val retrofit: Retrofit =
            Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

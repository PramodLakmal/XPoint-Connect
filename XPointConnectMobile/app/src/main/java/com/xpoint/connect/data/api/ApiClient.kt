package com.xpoint.connect.data.api

import com.google.gson.GsonBuilder
import com.xpoint.connect.utils.SharedPreferencesManager
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL =
            "https://your-api-base-url.com/" // Replace with your actual API URL

    private var sharedPreferencesManager: SharedPreferencesManager? = null

    fun init(prefsManager: SharedPreferencesManager) {
        sharedPreferencesManager = prefsManager
    }

    private val authInterceptor = Interceptor { chain ->
        val token = sharedPreferencesManager?.getAuthToken()
        val request =
                if (token != null) {
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                } else {
                    chain.request()
                }
        chain.proceed(request)
    }

    private val loggingInterceptor =
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttpClient =
            OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

    private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    val retrofit: Retrofit =
            Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

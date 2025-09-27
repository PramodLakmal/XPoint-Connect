/**
 * AuthRepository.kt
 *
 * Purpose: Manages authentication-related API communications and data handling Author: XPoint
 * Connect Development Team Date: September 27, 2025
 *
 * Description: This repository class serves as the single source of truth for authentication
 * operations. It handles API communication for login and registration processes, manages network
 * exceptions, and provides clean data interfaces to ViewModels. All network operations are
 * performed on the IO dispatcher for optimal performance.
 *
 * Key Features:
 * - EV Owner login API integration with new simplified response format
 * - EV Owner registration API handling
 * - Centralized error handling and response processing
 * - Coroutine-based asynchronous operations
 * - Clean architecture separation between UI and network layers
 */
package com.xpoint.connect.data.repository

import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.model.*
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository {

    private val apiService = ApiClient.apiService

    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred during login")
            }
        }
    }

    /**
     * Authenticates EV owner using NIC and password through backend API Makes API call to login
     * endpoint and returns simplified authentication data
     * @param nic User's National Identity Card number
     * @param password User's password
     * @return Resource wrapper containing login response or error information
     */
    suspend fun evOwnerLogin(nic: String, password: String): Resource<EVOwnerLoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.evOwnerLogin(EVOwnerLoginRequest(nic, password))
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred during login")
            }
        }
    }

    suspend fun registerEVOwner(
            request: RegisterEVOwnerRequest
    ): Resource<RegisterEVOwnerResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.registerEVOwner(request)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred during registration")
            }
        }
    }

    private fun <T> handleApiResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            response.body()?.let { body -> Resource.Success(body) }
                    ?: Resource.Error("Empty response body")
        } else {
            Resource.Error(response.message() ?: "Unknown error occurred")
        }
    }
}

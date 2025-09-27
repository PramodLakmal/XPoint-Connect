/**
 * UserRepository.kt
 *
 * Purpose: Repository class for managing user profile data and related operations Author: XPoint
 * Connect Development Team Date: September 27, 2025
 *
 * Description: This repository class handles all user-related data operations including profile
 * retrieval, updates, and management. It serves as the single source of truth for user data,
 * abstracting API communication details from ViewModels and providing a clean interface for user
 * profile management throughout the application.
 *
 * Key Features:
 * - EV owner profile retrieval and management
 * - Profile update operations with validation
 * - Centralized error handling for user operations
 * - Coroutine-based asynchronous data operations
 * - Resource wrapper for consistent state management
 * - Clean architecture separation between UI and network layers
 */
package com.xpoint.connect.data.repository

import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.model.*
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class UserRepository {

    private val apiService = ApiClient.apiService

    /**
     * Retrieves EV owner profile information from the backend API
     * @param nic National Identity Card number of the EV owner
     * @return Resource wrapper containing EVOwner data or error information
     */
    suspend fun getEVOwnerProfile(nic: String): Resource<EVOwner> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEVOwnerProfile(nic)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch profile")
            }
        }
    }

    /**
     * Updates EV owner profile information through the backend API
     * @param nic National Identity Card number of the EV owner
     * @param request Updated profile information in RegisterEVOwnerRequest format
     * @return Resource wrapper containing updated EVOwner data or error information
     */
    suspend fun updateEVOwnerProfile(
            nic: String,
            request: RegisterEVOwnerRequest
    ): Resource<EVOwner> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateEVOwnerProfile(nic, request)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to update profile")
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

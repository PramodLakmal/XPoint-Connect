/**
 * StationRepository.kt
 *
 * Purpose: Repository class for managing charging station data and operations Author: XPoint
 * Connect Development Team Date: September 27, 2025
 *
 * Description: This repository class handles all charging station related data operations including
 * station discovery, detailed information retrieval, and location-based searches. It provides a
 * clean abstraction layer between the UI components and the backend API, managing network calls and
 * data transformation for charging station features.
 *
 * Key Features:
 * - Comprehensive charging station data retrieval
 * - Location-based station discovery with radius filtering
 * - Individual station detailed information access
 * - Active/inactive station filtering capabilities
 * - Centralized error handling for station operations
 * - Coroutine-based asynchronous operations
 * - Resource wrapper for consistent state management
 */
package com.xpoint.connect.data.repository

import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.model.*
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class StationRepository {

    private val apiService = ApiClient.apiService

    suspend fun getAllStations(activeOnly: Boolean = true): Resource<List<ChargingStation>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllStations(activeOnly)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch stations")
            }
        }
    }

    suspend fun getStationById(id: String): Resource<ChargingStation> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStationById(id)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch station details")
            }
        }
    }

    suspend fun getNearbyStations(
            latitude: Double,
            longitude: Double,
            radiusKm: Double = 10.0
    ): Resource<List<ChargingStation>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = NearbyStationsRequest(latitude, longitude, radiusKm)
                val response = apiService.getNearbyStations(request)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch nearby stations")
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

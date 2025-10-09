package com.xpoint.connect.data.repository

import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.model.AssignedStation
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.model.ChargingStation
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class OperatorRepository {

    private val apiService = ApiClient.apiService

    suspend fun getOperatorStations(operatorId: String): Resource<List<AssignedStation>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOperatorStations(operatorId)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch operator stations")
            }
        }
    }

    suspend fun getChargingStationsByOperator(operatorId: String): Resource<List<ChargingStation>> {
        return withContext(Dispatchers.IO) {
            try {
                // Get all charging stations
                val response = apiService.getAllStations(activeOnly = true)
                if (response.isSuccessful) {
                    val allStations = response.body() ?: emptyList()
                    
                    // Debug logging
                    android.util.Log.d("OperatorRepository", "Total stations: ${allStations.size}")
                    android.util.Log.d("OperatorRepository", "Looking for operatorId: $operatorId")
                    
                    // Filter stations by operator ID
                    val operatorStations = allStations.filter { it.operatorId == operatorId }
                    android.util.Log.d("OperatorRepository", "Filtered stations: ${operatorStations.size}")
                    
                    if (operatorStations.isNotEmpty()) {
                        operatorStations.forEach { station ->
                            android.util.Log.d("OperatorRepository", "Found matching station: ${station.name} (ID: ${station.id})")
                        }
                    }
                    
                    Resource.Success(operatorStations)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch charging stations")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch charging stations")
            }
        }
    }

    suspend fun getBookingsByStation(stationId: String): Resource<List<Booking>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingsByStation(stationId)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch station bookings")
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



package com.xpoint.connect.data.repository

import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.model.AssignedStation
import com.xpoint.connect.data.model.OperatorWithStations
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class OperatorRepository {

    private val apiService = ApiClient.apiService

    suspend fun getOperatorsWithStations(): Resource<List<OperatorWithStations>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOperatorsWithStations()
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch operators")
            }
        }
    }

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

    private fun <T> handleApiResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            response.body()?.let { body -> Resource.Success(body) }
                ?: Resource.Error("Empty response body")
        } else {
            Resource.Error(response.message() ?: "Unknown error occurred")
        }
    }
}



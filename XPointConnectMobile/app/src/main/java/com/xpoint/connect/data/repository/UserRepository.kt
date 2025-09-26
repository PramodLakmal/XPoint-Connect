package com.xpoint.connect.data.repository

import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.model.*
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class UserRepository {

    private val apiService = ApiClient.apiService

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

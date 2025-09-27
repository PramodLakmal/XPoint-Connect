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

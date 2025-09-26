package com.xpoint.connect.data.repository

import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.model.*
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class BookingRepository {

    private val apiService = ApiClient.apiService

    suspend fun createBooking(request: CreateBookingRequest): Resource<Booking> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createBooking(request)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to create booking")
            }
        }
    }

    suspend fun previewBooking(request: CreateBookingRequest): Resource<BookingPreview> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.previewBooking(request)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to preview booking")
            }
        }
    }

    suspend fun getBookingById(id: String): Resource<Booking> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingById(id)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch booking details")
            }
        }
    }

    suspend fun getBookingsByEVOwner(nic: String): Resource<List<Booking>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingsByEVOwner(nic)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch bookings")
            }
        }
    }

    suspend fun getUpcomingBookings(nic: String): Resource<List<Booking>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUpcomingBookings(nic)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch upcoming bookings")
            }
        }
    }

    suspend fun getBookingHistory(nic: String): Resource<List<Booking>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingHistory(nic)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch booking history")
            }
        }
    }

    suspend fun getDashboardStats(nic: String): Resource<DashboardStats> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDashboardStats(nic)
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch dashboard stats")
            }
        }
    }

    suspend fun cancelBooking(id: String, reason: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelBooking(id, mapOf("cancellationReason" to reason))
                handleApiResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to cancel booking")
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

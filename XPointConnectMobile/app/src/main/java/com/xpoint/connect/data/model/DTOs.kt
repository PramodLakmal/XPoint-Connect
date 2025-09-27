/**
 * DTOs.kt
 *
 * Purpose: Defines data transfer objects for API communication and data serialization Author:
 * XPoint Connect Development Team Date: September 27, 2025
 *
 * Description: This file contains all Data Transfer Objects (DTOs) used for API communication
 * between the mobile application and backend services. DTOs are structured to match the JSON
 * formats expected by REST API endpoints and include proper serialization annotations for automatic
 * parsing with Gson.
 *
 * Key Features:
 * - Authentication DTOs for login and registration processes
 * - Booking management DTOs for reservation operations
 * - Charging station DTOs for location and availability data
 * - Error handling DTOs for API response management
 * - Proper JSON serialization with @SerializedName annotations
 */
package com.xpoint.connect.data.model

import com.google.gson.annotations.SerializedName

// Authentication DTOs
data class LoginRequest(
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String
)

data class LoginResponse(
        @SerializedName("token") val token: String,
        @SerializedName("refreshToken") val refreshToken: String,
        @SerializedName("userType") val userType: String,
        @SerializedName("expirationTime") val expirationTime: String,
        @SerializedName("user") val user: User
)

data class EVOwnerLoginRequest(
        @SerializedName("nic") val nic: String,
        @SerializedName("password") val password: String
)

data class EVOwnerLoginResponse(
        @SerializedName("token") val token: String,
        @SerializedName("nic") val nic: String,
        @SerializedName("fullName") val fullName: String,
        @SerializedName("expiresAt") val expiresAt: String
)

data class RegisterEVOwnerRequest(
        @SerializedName("nic") val nic: String,
        @SerializedName("firstName") val firstName: String,
        @SerializedName("lastName") val lastName: String,
        @SerializedName("email") val email: String,
        @SerializedName("phoneNumber") val phoneNumber: String,
        @SerializedName("address") val address: String,
        @SerializedName("password") val password: String
)

data class RegisterEVOwnerResponse(
        @SerializedName("nic") val nic: String = "",
        @SerializedName("firstName") val firstName: String = "",
        @SerializedName("lastName") val lastName: String = "",
        @SerializedName("fullName") val fullName: String = "",
        @SerializedName("email") val email: String = "",
        @SerializedName("phoneNumber") val phoneNumber: String = "",
        @SerializedName("address") val address: String = "",
        @SerializedName("isActive") val isActive: Boolean = true,
        @SerializedName("createdAt") val createdAt: String = "",
        @SerializedName("updatedAt") val updatedAt: String = "",
        @SerializedName("requiresReactivation") val requiresReactivation: Boolean = false
)

// Booking DTOs
data class CreateBookingRequest(
        @SerializedName("evOwnerNIC") val evOwnerNIC: String,
        @SerializedName("chargingStationId") val chargingStationId: String,
        @SerializedName("reservationDateTime") val reservationDateTime: String,
        @SerializedName("durationMinutes") val durationMinutes: Int = 60
)

data class BookingPreview(
        @SerializedName("chargingStationName") val chargingStationName: String,
        @SerializedName("reservationDateTime") val reservationDateTime: String,
        @SerializedName("durationMinutes") val durationMinutes: Int,
        @SerializedName("totalAmount") val totalAmount: Double,
        @SerializedName("estimatedEndTime") val estimatedEndTime: String
)

// Station DTOs
data class NearbyStationsRequest(
        @SerializedName("latitude") val latitude: Double,
        @SerializedName("longitude") val longitude: Double,
        @SerializedName("radiusKm") val radiusKm: Double = 10.0
)

// Generic Response Wrapper
data class ApiResponse<T>(
        @SerializedName("success") val success: Boolean,
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: T?
)

// Error Response
data class ErrorResponse(
        @SerializedName("title") val title: String = "",
        @SerializedName("status") val status: Int = 0,
        @SerializedName("errors") val errors: Map<String, List<String>>? = null
)

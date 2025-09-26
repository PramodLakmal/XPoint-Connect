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
        @SerializedName("refreshToken") val refreshToken: String,
        @SerializedName("userType") val userType: String,
        @SerializedName("expirationTime") val expirationTime: String,
        @SerializedName("evOwner") val evOwner: EVOwner
)

data class RegisterEVOwnerRequest(
        @SerializedName("nic") val nic: String,
        @SerializedName("firstName") val firstName: String,
        @SerializedName("lastName") val lastName: String,
        @SerializedName("email") val email: String,
        @SerializedName("phoneNumber") val phoneNumber: String,
        @SerializedName("password") val password: String,
        @SerializedName("licenseNumber") val licenseNumber: String,
        @SerializedName("vehicleModel") val vehicleModel: String,
        @SerializedName("vehicleYear") val vehicleYear: Int,
        @SerializedName("batteryCapacity") val batteryCapacity: Double
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

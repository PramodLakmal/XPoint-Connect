/**
 * ApiService.kt
 *
 * Purpose: Retrofit interface defining all REST API endpoints for XPoint Connect backend Author:
 * XPoint Connect Development Team Date: September 27, 2025
 *
 * Description: This interface defines all REST API endpoints used by the XPoint Connect mobile
 * application to communicate with the backend services. It includes authentication, charging
 * station management, booking operations, and user profile management endpoints with proper HTTP
 * method annotations and parameter definitions.
 *
 * Key Features:
 * - Authentication endpoints for login and registration
 * - Charging station discovery and management APIs
 * - Booking creation, management, and status tracking
 * - EV owner profile management and updates
 * - Dashboard statistics and reporting endpoints
 * - Geolocation-based station discovery
 * - QR code and check-in/check-out functionality
 */
package com.xpoint.connect.data.api

import com.xpoint.connect.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface
 *
 * Reference: Structure and annotations based on Retrofit official documentation
 * https://square.github.io/retrofit/
 *
 * Also informed by Android Developers guide to REST API integration using Retrofit
 * https://developer.android.com/training/volley/retrofit
 */
interface ApiService {

        // Authentication
        @POST("api/auth/login")
        suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

        // Operator login (BackOffice & StationOperator)
        @POST("api/Auth/login")
        suspend fun operatorLogin(@Body request: OperatorLoginRequest): Response<OperatorLoginResponse>

        @POST("api/Auth/evowner/login")
        suspend fun evOwnerLogin(@Body request: EVOwnerLoginRequest): Response<EVOwnerLoginResponse>

        @POST("api/Auth/evowner/register")
        suspend fun registerEVOwner(
                @Body request: RegisterEVOwnerRequest
        ): Response<RegisterEVOwnerResponse>

        // Charging Stations
        @GET("api/ChargingStations")
        suspend fun getAllStations(
                @Query("activeOnly") activeOnly: Boolean = true
        ): Response<List<ChargingStation>>

        @GET("api/ChargingStations/{id}")
        suspend fun getStationById(@Path("id") id: String): Response<ChargingStation>

        @POST("api/ChargingStations/nearby")
        suspend fun getNearbyStations(
                @Body request: NearbyStationsRequest
        ): Response<List<ChargingStation>>

        // Bookings - Updated to match API specification
        @POST("api/Bookings")
        suspend fun createBooking(@Body request: CreateBookingRequest): Response<Booking>

        @PUT("api/Bookings/{id}")
        suspend fun updateBooking(
                @Path("id") id: String,
                @Body request: UpdateBookingRequest
        ): Response<Booking>

        @GET("api/Bookings/{id}")
        suspend fun getBookingById(@Path("id") id: String): Response<Booking>

        @POST("api/Bookings/{id}/cancel")
        suspend fun cancelBooking(
                @Path("id") id: String,
                @Body request: CancelBookingRequest
        ): Response<Unit>

        @GET("api/Bookings/evowner/{nic}")
        suspend fun getBookingsByEVOwner(@Path("nic") nic: String): Response<List<Booking>>

        @GET("api/Bookings/station/{stationId}")
        suspend fun getBookingsByStation(
                @Path("stationId") stationId: String
        ): Response<List<Booking>>

        // Operator Management
        @GET("api/operatorassignments/operators/{operatorId}")
        suspend fun getOperatorStations(
                @Path("operatorId") operatorId: String
        ): Response<List<AssignedStation>>

        @GET("api/Bookings/history/{nic}")
        suspend fun getBookingHistory(@Path("nic") nic: String): Response<List<Booking>>

        @POST("api/Bookings/preview")
        suspend fun previewBooking(@Body request: CreateBookingRequest): Response<BookingPreview>

        @GET("api/Bookings/upcoming/{nic}")
        suspend fun getUpcomingBookings(@Path("nic") nic: String): Response<List<Booking>>

        @GET("api/Bookings/dashboard/{nic}")
        suspend fun getDashboardStats(@Path("nic") nic: String): Response<DashboardStats>

        // EV Owner Profile Management
        @GET("api/EVOwners/{nic}")
        suspend fun getEVOwnerProfile(@Path("nic") nic: String): Response<EVOwner>

        @PUT("api/EVOwners/{nic}")
        suspend fun updateEVOwnerProfile(
                @Path("nic") nic: String,
                @Body request: UpdateEVOwnerProfileRequest
        ): Response<EVOwner>

        @PUT("api/EVOwners/{nic}/password")
        suspend fun changePassword(
                @Path("nic") nic: String,
                @Body request: ChangePasswordRequest
        ): Response<Unit>

        @POST("api/EVOwners/{nic}/deactivate")
        suspend fun deactivateAccount(
                @Path("nic") nic: String,
                @Body request: DeactivateAccountRequest
        ): Response<Unit>

        @POST("api/EVOwners/reactivate")
        suspend fun reactivateAccount(
                @Body request: ReactivateAccountRequest
        ): Response<EVOwnerLoginResponse>
}

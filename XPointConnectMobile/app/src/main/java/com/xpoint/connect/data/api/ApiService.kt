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

interface ApiService {

        // Authentication
        @POST("api/auth/login")
        suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

        @POST("api/Auth/evowner/login")
        suspend fun evOwnerLogin(@Body request: EVOwnerLoginRequest): Response<EVOwnerLoginResponse>

        @POST("api/Auth/evowner/register")
        suspend fun registerEVOwner(
                @Body request: RegisterEVOwnerRequest
        ): Response<RegisterEVOwnerResponse>

        // Charging Stations
        @GET("api/chargingstations")
        suspend fun getAllStations(
                @Query("activeOnly") activeOnly: Boolean = true
        ): Response<List<ChargingStation>>

        @GET("api/chargingstations/{id}")
        suspend fun getStationById(@Path("id") id: String): Response<ChargingStation>

        @POST("api/chargingstations/nearby")
        suspend fun getNearbyStations(
                @Body request: NearbyStationsRequest
        ): Response<List<ChargingStation>>

        // Bookings
        @POST("api/bookings")
        suspend fun createBooking(@Body request: CreateBookingRequest): Response<Booking>

        @POST("api/bookings/preview")
        suspend fun previewBooking(@Body request: CreateBookingRequest): Response<BookingPreview>

        @GET("api/bookings/{id}")
        suspend fun getBookingById(@Path("id") id: String): Response<Booking>

        @GET("api/bookings/evowner/{nic}")
        suspend fun getBookingsByEVOwner(@Path("nic") nic: String): Response<List<Booking>>

        @GET("api/bookings/upcoming/{nic}")
        suspend fun getUpcomingBookings(@Path("nic") nic: String): Response<List<Booking>>

        @GET("api/bookings/history/{nic}")
        suspend fun getBookingHistory(@Path("nic") nic: String): Response<List<Booking>>

        @GET("api/bookings/dashboard/{nic}")
        suspend fun getDashboardStats(@Path("nic") nic: String): Response<DashboardStats>

        @POST("api/bookings/{id}/cancel")
        suspend fun cancelBooking(
                @Path("id") id: String,
                @Body reason: Map<String, String>
        ): Response<Unit>

        // EV Owner Profile
        @GET("api/evowners/{nic}")
        suspend fun getEVOwnerProfile(@Path("nic") nic: String): Response<EVOwner>

        @PUT("api/evowners/{nic}")
        suspend fun updateEVOwnerProfile(
                @Path("nic") nic: String,
                @Body request: RegisterEVOwnerRequest
        ): Response<EVOwner>
}

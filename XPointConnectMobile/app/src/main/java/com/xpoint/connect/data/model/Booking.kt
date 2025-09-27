/**
 * Booking.kt
 *
 * Purpose: Data models for charging station booking and reservation management Author: XPoint
 * Connect Development Team Date: September 27, 2025
 *
 * Description: This file contains data models for managing charging station bookings and
 * reservations. It includes comprehensive booking lifecycle management from reservation creation
 * through completion, with support for status tracking, payment processing, QR code generation, and
 * check-in/check-out functionality.
 *
 * Key Features:
 * - Complete booking lifecycle management with status tracking
 * - EV owner and charging station relationship mapping
 * - Reservation date/time scheduling with duration management
 * - Payment integration with total amount calculation
 * - QR code generation for contactless check-in/check-out
 * - Cancellation support with reason tracking
 * - Operator notes and booking management functionality
 * - Dashboard statistics and reporting data models
 */
package com.xpoint.connect.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Booking(
        @SerializedName("id") val id: String = "",
        @SerializedName("evOwnerNIC") val evOwnerNIC: String = "",
        @SerializedName("evOwnerName") val evOwnerName: String = "",
        @SerializedName("chargingStationId") val chargingStationId: String = "",
        @SerializedName("chargingStationName") val chargingStationName: String = "",
        @SerializedName("stationName") val stationName: String? = null,
        @SerializedName("reservationDateTime") val reservationDateTime: String = "",
        @SerializedName("bookingDate") val bookingDate: String = "",
        @SerializedName("startTime") val startTime: Date = Date(),
        @SerializedName("endTime") val endTime: Date = Date(),
        @SerializedName("durationMinutes") val durationMinutes: Int = 60,
        @SerializedName("status") val status: BookingStatus = BookingStatus.Pending,
        @SerializedName("totalAmount") val totalAmount: Double = 0.0,
        @SerializedName("qrCode") val qrCode: String = "",
        @SerializedName("checkInTime") val checkInTime: String? = null,
        @SerializedName("checkOutTime") val checkOutTime: String? = null,
        @SerializedName("cancellationReason") val cancellationReason: String? = null,
        @SerializedName("operatorNotes") val operatorNotes: String? = null
)

enum class BookingStatus {
        @SerializedName("Pending") Pending,
        @SerializedName("Approved") Approved,
        @SerializedName("CheckedIn") CheckedIn,
        @SerializedName("Completed") Completed,
        @SerializedName("Cancelled") Cancelled,
        @SerializedName("NoShow") NoShow
}

data class DashboardStats(
        @SerializedName("pendingReservations") val pendingReservations: Int = 0,
        @SerializedName("approvedFutureReservations") val approvedFutureReservations: Int = 0,
        @SerializedName("completedBookingsThisMonth") val completedBookingsThisMonth: Int = 0,
        @SerializedName("totalSpentThisMonth") val totalSpentThisMonth: Double = 0.0,
        @SerializedName("nearbyStations") val nearbyStations: List<ChargingStation> = emptyList()
)

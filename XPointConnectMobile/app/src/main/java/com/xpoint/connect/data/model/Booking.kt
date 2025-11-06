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

/**
 * Reference:
 * Data class and JSON field mapping structure based on Gson serialization documentation:
 * https://github.com/google/gson
 *
 * Kotlin data class conventions based on JetBrains Kotlin language reference:
 * https://kotlinlang.org/docs/data-classes.html
 */
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
        @SerializedName("durationMinutes") val durationMinutes: Int = 480,
        @SerializedName("status") val status: String = "Pending", // Changed to String to match API
        @SerializedName("totalAmount") val totalAmount: Double = 0.0,
        @SerializedName("qrCode") val qrCode: String = "",
        @SerializedName("checkInTime") val checkInTime: String? = null,
        @SerializedName("checkOutTime") val checkOutTime: String? = null,
        @SerializedName("cancellationReason") val cancellationReason: String? = null,
        @SerializedName("cancelledAt") val cancelledAt: String? = null,
        @SerializedName("createdAt") val createdAt: String? = null,
        @SerializedName("updatedAt") val updatedAt: String? = null,
        @SerializedName("operatorNotes") val operatorNotes: String? = null
) {
        // Helper property to convert status string to enum
        val bookingStatus: BookingStatus
                get() {
                        // Handle both integer strings and text status values
                        return when {
                                status.toIntOrNull() != null -> BookingStatus.fromInt(status.toInt())
                                else -> BookingStatus.fromString(status)
                        }
                }
}

/**
 * Enum representation of booking lifecycle states.
 *
 * Reference:
 * Kotlin enum implementation style based on JetBrains official documentation:
 * https://kotlinlang.org/docs/enum-classes.html
 */
enum class BookingStatus(val value: String) {
        Pending("Pending"),
        Approved("Approved"),
        CheckedIn("CheckedIn"),
        Completed("Completed"),
        Cancelled("Cancelled"),
        NoShow("NoShow");

        companion object {
                fun fromString(value: String): BookingStatus {
                        return values().find { it.value.equals(value, ignoreCase = true) } ?: Pending
                }
                
                fun fromInt(value: Int): BookingStatus {
                        // Backward compatibility mapping
                        return when(value) {
                                0 -> Pending
                                1 -> Approved
                                2 -> CheckedIn
                                3 -> Completed
                                4 -> Cancelled
                                5 -> NoShow
                                else -> Pending
                        }
                }
        }
}

data class DashboardStats(
        @SerializedName("pendingReservations") val pendingReservations: Int = 0,
        @SerializedName("approvedFutureReservations") val approvedFutureReservations: Int = 0,
        @SerializedName("completedBookingsThisMonth") val completedBookingsThisMonth: Int = 0,
        @SerializedName("totalSpentThisMonth") val totalSpentThisMonth: Double = 0.0,
        @SerializedName("nearbyStations") val nearbyStations: List<ChargingStation> = emptyList()
)

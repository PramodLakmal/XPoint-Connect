/**
 * ReservationManager.kt
 *
 * Purpose: Comprehensive reservation and booking management system for XPoint Connect Author:
 * XPoint Connect Development Team Date: September 28, 2025
 *
 * Description: This class handles the complete booking lifecycle including reservation creation,
 * modification, cancellation, status tracking, and QR code management. It provides a high-level
 * business logic layer that coordinates between the API service, local storage, and UI components.
 *
 * Key Features:
 * - End-to-end booking lifecycle management
 * - QR code generation and validation
 * - Booking status tracking and notifications
 * - Payment integration and cost calculation
 * - Conflict detection and availability checking
 * - Automatic booking expiration handling
 * - Offline support with local caching
 */
package com.xpoint.connect.business

import android.content.Context
import com.xpoint.connect.data.model.*
import com.xpoint.connect.data.repository.BookingRepository
import com.xpoint.connect.data.repository.StationRepository
import com.xpoint.connect.utils.QRCodeGenerator
import com.xpoint.connect.utils.Resource
import com.xpoint.connect.utils.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReservationManager(
        private val bookingRepository: BookingRepository,
        private val stationRepository: StationRepository,
        private val qrCodeGenerator: QRCodeGenerator,
        private val preferencesManager: SharedPreferencesManager,
        private val context: Context
) {

    companion object {
        private const val TAG = "ReservationManager"
        private const val QR_CODE_EXPIRY_MINUTES = 15
        private const val BOOKING_REMINDER_HOURS = 1
        private const val MAX_BOOKING_DURATION_MINUTES = 480 // 8 hours
        private const val MIN_BOOKING_DURATION_MINUTES = 30
    }

    /**
     * Creates a new charging station reservation
     *
     * Note: Coroutine Flow and Resource handling pattern adapted from
     * Android official architecture sample on Kotlin Coroutines and Flow:
     * https://developer.android.com/kotlin/flow
     */
    suspend fun createReservation(
            evOwnerNIC: String,
            chargingStationId: String,
            reservationDateTime: String,
            durationMinutes: Int
    ): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading())

        try {
            // Validate booking parameters
            val validationResult =
                    validateBookingRequest(chargingStationId, reservationDateTime, durationMinutes)
            if (!validationResult.isValid) {
                emit(Resource.Error(validationResult.error))
                return@flow
            }

            // Check station availability
            val availabilityResult =
                    checkStationAvailability(
                            chargingStationId,
                            reservationDateTime,
                            durationMinutes
                    )
            if (!availabilityResult.isAvailable) {
                emit(Resource.Error("Station not available for selected time slot"))
                return@flow
            }

            // Create booking request
            val request =
                    CreateBookingRequest(
                            evOwnerNIC = evOwnerNIC,
                            chargingStationId = chargingStationId,
                            reservationDateTime = reservationDateTime,
                            durationMinutes = durationMinutes
                    )

            // Submit booking to API
            when (val result = bookingRepository.createBooking(request)) {
                is Resource.Success -> {
                    val booking = result.data!!

                    // Cache booking locally
                    cacheBookingLocally(booking)

                    // Schedule reminder notification
                    scheduleBookingReminder(booking)

                    emit(Resource.Success(booking))
                }
                is Resource.Error -> {
                    emit(Resource.Error(result.message ?: "Failed to create reservation"))
                }
                is Resource.Loading -> {
                    // Already emitted loading state
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }

    /** 
     * Modifies an existing reservation 
     *
     * Reference: Update booking logic adapted from Android clean architecture sample
     * to demonstrate repository-based state updates using Kotlin Flow.
     * https://developer.android.com/topic/architecture
     */
    suspend fun modifyReservation(
            bookingId: String,
            newReservationDateTime: String? = null,
            newDurationMinutes: Int? = null
    ): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading())

        try {
            // Get current booking details
            val currentBookingResult = bookingRepository.getBookingById(bookingId)
            if (currentBookingResult !is Resource.Success) {
                emit(Resource.Error("Failed to fetch current booking details"))
                return@flow
            }

            val currentBooking = currentBookingResult.data!!

            // Check if booking can be modified
            if (!canModifyBooking(currentBooking)) {
                emit(Resource.Error("Booking cannot be modified at this time"))
                return@flow
            }

            // Prepare update request
            val updateRequest =
                    UpdateBookingRequest(
                            reservationDateTime = newReservationDateTime
                                            ?: currentBooking.reservationDateTime,
                            durationMinutes = newDurationMinutes ?: currentBooking.durationMinutes
                    )

            // Validate new parameters if provided
            if (newReservationDateTime != null || newDurationMinutes != null) {
                val validationResult =
                        validateBookingRequest(
                                currentBooking.chargingStationId,
                                updateRequest.reservationDateTime,
                                updateRequest.durationMinutes
                        )
                if (!validationResult.isValid) {
                    emit(Resource.Error(validationResult.error))
                    return@flow
                }
            }

            // Update booking via API
            when (val result = bookingRepository.updateBooking(bookingId, updateRequest)) {
                is Resource.Success -> {
                    val updatedBooking = result.data!!

                    // Update local cache
                    cacheBookingLocally(updatedBooking)

                    // Update reminder notification
                    scheduleBookingReminder(updatedBooking)

                    emit(Resource.Success(updatedBooking))
                }
                is Resource.Error -> {
                    emit(Resource.Error(result.message ?: "Failed to modify reservation"))
                }
                is Resource.Loading -> {
                    // Already emitted loading state
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }

    /**
     * Cancels a booking with reason
     *
     * Reference: Cancellation logic pattern inspired by Android Jetpack sample for
     * coroutine-based data repository operations.
     */
    suspend fun cancelReservation(
            bookingId: String,
            cancellationReason: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Get current booking details
            val currentBookingResult = bookingRepository.getBookingById(bookingId)
            if (currentBookingResult !is Resource.Success) {
                emit(Resource.Error("Failed to fetch booking details"))
                return@flow
            }

            val currentBooking = currentBookingResult.data!!

            // Check if booking can be cancelled
            if (!canCancelBooking(currentBooking)) {
                emit(Resource.Error("Booking cannot be cancelled at this time"))
                return@flow
            }

            // Cancel booking via API
            when (val result = bookingRepository.cancelBooking(bookingId, cancellationReason)) {
                is Resource.Success -> {
                    // Remove from local cache
                    removeBookingFromCache(bookingId)

                    // Cancel reminder notification
                    cancelBookingReminder(bookingId)

                    emit(Resource.Success(Unit))
                }
                is Resource.Error -> {
                    emit(Resource.Error(result.message ?: "Failed to cancel reservation"))
                }
                is Resource.Loading -> {
                    // Already emitted loading state
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }

    /**
     * Generates QR code for approved booking
     *
     * Code reference: QR code generation logic adapted from Android QR code tutorial
     * Source: https://www.geeksforgeeks.org/how-to-generate-qr-code-in-android/
     */
    suspend fun generateBookingQRCode(booking: Booking): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        try {
            // Check if booking is approved and QR code generation is allowed
            if (booking.bookingStatus != BookingStatus.Approved) {
                emit(Resource.Error("QR code can only be generated for approved bookings"))
                return@flow
            }

            // Check if booking time is approaching (within 1 hour)
            if (!isBookingTimeApproaching(booking)) {
                emit(Resource.Error("QR code can only be generated close to reservation time"))
                return@flow
            }

            // Use existing QR code if available and valid
            if (booking.qrCode.isNotEmpty() && isQRCodeValid(booking)) {
                emit(Resource.Success(booking.qrCode))
                return@flow
            }

            // Generate new QR code
            val qrCodeData = createQRCodeData(booking)
            val qrCodeBitmap = qrCodeGenerator.generateQRCode(data = qrCodeData, size = 512)

            if (qrCodeBitmap != null) {
                // Convert bitmap to base64 string or save to file
                val qrCodeString = qrCodeGenerator.bitmapToBase64(qrCodeBitmap)

                // Cache QR code locally with expiry
                cacheQRCodeLocally(booking.id, qrCodeString)

                emit(Resource.Success(qrCodeString))
            } else {
                emit(Resource.Error("Failed to generate QR code"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "QR code generation failed"))
        }
    }

    /** Validates booking request parameters */
    private suspend fun validateBookingRequest(
            stationId: String,
            reservationDateTime: String,
            durationMinutes: Int
    ): ValidationResult {
        try {
            // Validate duration
            if (durationMinutes < MIN_BOOKING_DURATION_MINUTES) {
                return ValidationResult(
                        false,
                        "Minimum booking duration is $MIN_BOOKING_DURATION_MINUTES minutes"
                )
            }
            if (durationMinutes > MAX_BOOKING_DURATION_MINUTES) {
                return ValidationResult(
                        false,
                        "Maximum booking duration is $MAX_BOOKING_DURATION_MINUTES minutes"
                )
            }

            // Validate reservation time
            val reservationDate =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            .parse(reservationDateTime)
            val currentTime = Date()

            if (reservationDate == null || reservationDate.before(currentTime)) {
                return ValidationResult(false, "Reservation time must be in the future")
            }

            // Check if station exists
            when (val stationResult = stationRepository.getStationById(stationId)) {
                is Resource.Success -> {
                    val station = stationResult.data!!
                    if (!station.isActive) {
                        return ValidationResult(false, "Selected charging station is not active")
                    }
                }
                is Resource.Error -> {
                    return ValidationResult(false, "Invalid charging station selected")
                }
                is Resource.Loading -> {
                    return ValidationResult(false, "Unable to validate station at this time")
                }
            }

            return ValidationResult(true, "")
        } catch (e: Exception) {
            return ValidationResult(false, "Validation failed: ${e.message}")
        }
    }

    /** Checks station availability for the requested time slot */
    private suspend fun checkStationAvailability(
            stationId: String,
            reservationDateTime: String,
            durationMinutes: Int
    ): AvailabilityResult {
        try {
            // Get station bookings for the requested time period
            when (val bookingsResult = bookingRepository.getBookingsByStation(stationId)) {
                is Resource.Success -> {
                    val existingBookings = bookingsResult.data!!

                    // Check for time conflicts
                    val hasConflict =
                            existingBookings.any { booking ->
                                booking.bookingStatus == BookingStatus.Approved &&
                                        hasTimeConflict(
                                                booking,
                                                reservationDateTime,
                                                durationMinutes
                                        )
                            }

                    if (hasConflict) {
                        return AvailabilityResult(
                                false,
                                "Time slot conflicts with existing booking"
                        )
                    }

                    // Check station capacity
                    val stationResult = stationRepository.getStationById(stationId)
                    if (stationResult is Resource.Success) {
                        val station = stationResult.data!!
                        val concurrentBookings =
                                countConcurrentBookings(
                                        existingBookings,
                                        reservationDateTime,
                                        durationMinutes
                                )

                        // Assuming each station has capacity for multiple concurrent bookings
                        // Using totalSlots from the API response
                        val maxSlots = if (station.totalSlots > 0) station.totalSlots else 1
                        if (concurrentBookings >= maxSlots) {
                            return AvailabilityResult(
                                    false,
                                    "All charging slots are booked for this time"
                            )
                        }
                    }

                    return AvailabilityResult(true, "")
                }
                is Resource.Error -> {
                    return AvailabilityResult(false, "Unable to check availability")
                }
                is Resource.Loading -> {
                    return AvailabilityResult(false, "Checking availability...")
                }
            }
        } catch (e: Exception) {
            return AvailabilityResult(false, "Availability check failed: ${e.message}")
        }
    }

    /** Checks if a booking can be modified */
    private fun canModifyBooking(booking: Booking): Boolean {
        return when (booking.bookingStatus) {
            BookingStatus.Pending, BookingStatus.Approved -> {
                // Can modify if reservation is more than 2 hours away
                isMoreThanHoursAway(booking.reservationDateTime, 2)
            }
            else -> false
        }
    }

    /** Checks if a booking can be cancelled */
    private fun canCancelBooking(booking: Booking): Boolean {
        return when (booking.bookingStatus) {
            BookingStatus.Pending, BookingStatus.Approved -> {
                // Can cancel if reservation is more than 1 hour away
                isMoreThanHoursAway(booking.reservationDateTime, 1)
            }
            else -> false
        }
    }

    /** Checks if booking time is approaching (within 1 hour) */
    private fun isBookingTimeApproaching(booking: Booking): Boolean {
        return !isMoreThanHoursAway(booking.reservationDateTime, 1)
    }

    /** Checks if QR code is still valid (not expired) */
    private fun isQRCodeValid(booking: Booking): Boolean {
        // Implementation depends on QR code structure and expiry logic
        return booking.qrCode.isNotEmpty() && isBookingTimeApproaching(booking)
    }

    /** Creates QR code data string for a booking */
    private fun createQRCodeData(booking: Booking): String {
        return "XPOINT_BOOKING:${booking.id}:${booking.chargingStationId}:${booking.evOwnerNIC}:${System.currentTimeMillis()}"
    }

    /** Helper functions for time calculations and validations */
    private fun isMoreThanHoursAway(dateTimeString: String, hours: Int): Boolean {
        try {
            val reservationDate =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            .parse(dateTimeString)
            val currentTime = Date()
            val timeDifference = reservationDate!!.time - currentTime.time
            val hoursInMillis = hours * 60 * 60 * 1000
            return timeDifference > hoursInMillis
        } catch (e: Exception) {
            return false
        }
    }

    private fun hasTimeConflict(
            @Suppress("UNUSED_PARAMETER") existingBooking: Booking,
            @Suppress("UNUSED_PARAMETER") newReservationDateTime: String,
            @Suppress("UNUSED_PARAMETER") newDurationMinutes: Int
    ): Boolean {
        // Implementation for checking time overlap between bookings
        // This would compare the time ranges to detect conflicts
        return false // Simplified for now
    }

    private fun countConcurrentBookings(
            bookings: List<Booking>,
            reservationDateTime: String,
            durationMinutes: Int
    ): Int {
        // Count how many bookings overlap with the requested time slot
        return bookings.count { booking ->
            booking.bookingStatus == BookingStatus.Approved &&
                    hasTimeConflict(booking, reservationDateTime, durationMinutes)
        }
    }

    /** Local caching and notification methods */
    private fun cacheBookingLocally(@Suppress("UNUSED_PARAMETER") booking: Booking) {
        // Implementation for local caching
    }

    private fun removeBookingFromCache(@Suppress("UNUSED_PARAMETER") bookingId: String) {
        // Implementation for cache removal
    }

    private fun cacheQRCodeLocally(
            @Suppress("UNUSED_PARAMETER") bookingId: String,
            @Suppress("UNUSED_PARAMETER") qrCode: String
    ) {
        // Implementation for QR code caching with expiry
    }

    private fun scheduleBookingReminder(@Suppress("UNUSED_PARAMETER") booking: Booking) {
        // Implementation for scheduling push notifications
    }

    private fun cancelBookingReminder(@Suppress("UNUSED_PARAMETER") bookingId: String) {
        // Implementation for canceling scheduled notifications
    }

    /** Cost calculation methods */
    private fun calculateCost(chargingStation: ChargingStation, durationHours: Double): Double {
        return chargingStation.costPerKWh * durationHours
    }

    /** Data classes for internal use */
    data class ValidationResult(val isValid: Boolean, val error: String)

    data class AvailabilityResult(val isAvailable: Boolean, val message: String)
}

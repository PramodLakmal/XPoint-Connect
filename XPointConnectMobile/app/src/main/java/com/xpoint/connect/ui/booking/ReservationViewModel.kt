/**
 * ReservationViewModel.kt
 *
 * Purpose: ViewModel for managing reservation operations and UI state Author: XPoint Connect
 * Development Team Date: September 28, 2025
 *
 * Description: This ViewModel handles the business logic for reservation management, including
 * booking creation, modification, cancellation, and QR code generation. It provides LiveData
 * streams for the UI to observe reservation state changes.
 */
package com.xpoint.connect.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.business.ReservationManager
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.utils.Resource
import com.xpoint.connect.utils.SharedPreferencesManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReservationViewModel(
        private val reservationManager: ReservationManager,
        private val preferencesManager: SharedPreferencesManager
) : ViewModel() {

    // LiveData for reservation creation
    private val _createReservationState = MutableLiveData<Resource<Booking>>()
    val createReservationState: LiveData<Resource<Booking>> = _createReservationState

    // LiveData for reservation modification
    private val _modifyReservationState = MutableLiveData<Resource<Booking>>()
    val modifyReservationState: LiveData<Resource<Booking>> = _modifyReservationState

    // LiveData for reservation cancellation
    private val _cancelReservationState = MutableLiveData<Resource<Unit>>()
    val cancelReservationState: LiveData<Resource<Unit>> = _cancelReservationState

    // LiveData for QR code generation
    private val _qrCodeState = MutableLiveData<Resource<String>>()
    val qrCodeState: LiveData<Resource<String>> = _qrCodeState

    // LiveData for current booking details
    private val _currentBooking = MutableLiveData<Booking?>()
    val currentBooking: LiveData<Booking?> = _currentBooking

    // Loading state for UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /** Creates a new reservation */
    fun createReservation(
            chargingStationId: String,
            reservationDateTime: String,
            durationMinutes: Int
    ) {
        viewModelScope.launch {
            val evOwnerNIC = preferencesManager.getUserNIC()
            if (evOwnerNIC.isNullOrEmpty()) {
                _createReservationState.value = Resource.Error("User not logged in")
                return@launch
            }

            reservationManager.createReservation(
                            evOwnerNIC = evOwnerNIC,
                            chargingStationId = chargingStationId,
                            reservationDateTime = reservationDateTime,
                            durationMinutes = durationMinutes
                    )
                    .collectLatest { result ->
                        _createReservationState.value = result
                        _isLoading.value = result is Resource.Loading

                        when (result) {
                            is Resource.Success -> {
                                _currentBooking.value = result.data
                                _errorMessage.value = null
                            }
                            is Resource.Error -> {
                                _errorMessage.value = result.message
                            }
                            is Resource.Loading -> {
                                _errorMessage.value = null
                            }
                        }
                    }
        }
    }

    /** Modifies an existing reservation */
    fun modifyReservation(
            bookingId: String,
            newReservationDateTime: String? = null,
            newDurationMinutes: Int? = null
    ) {
        viewModelScope.launch {
            reservationManager.modifyReservation(
                            bookingId = bookingId,
                            newReservationDateTime = newReservationDateTime,
                            newDurationMinutes = newDurationMinutes
                    )
                    .collectLatest { result ->
                        _modifyReservationState.value = result
                        _isLoading.value = result is Resource.Loading

                        when (result) {
                            is Resource.Success -> {
                                _currentBooking.value = result.data
                                _errorMessage.value = null
                            }
                            is Resource.Error -> {
                                _errorMessage.value = result.message
                            }
                            is Resource.Loading -> {
                                _errorMessage.value = null
                            }
                        }
                    }
        }
    }

    /** Cancels a reservation with reason */
    fun cancelReservation(bookingId: String, cancellationReason: String) {
        viewModelScope.launch {
            reservationManager.cancelReservation(
                            bookingId = bookingId,
                            cancellationReason = cancellationReason
                    )
                    .collectLatest { result ->
                        _cancelReservationState.value = result
                        _isLoading.value = result is Resource.Loading

                        when (result) {
                            is Resource.Success -> {
                                _currentBooking.value = null
                                _errorMessage.value = null
                            }
                            is Resource.Error -> {
                                _errorMessage.value = result.message
                            }
                            is Resource.Loading -> {
                                _errorMessage.value = null
                            }
                        }
                    }
        }
    }

    /** Generates QR code for approved booking */
    fun generateQRCode(booking: Booking) {
        viewModelScope.launch {
            reservationManager.generateBookingQRCode(booking).collectLatest { result ->
                _qrCodeState.value = result
                _isLoading.value = result is Resource.Loading

                when (result) {
                    is Resource.Success -> {
                        _errorMessage.value = null
                    }
                    is Resource.Error -> {
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {
                        _errorMessage.value = null
                    }
                }
            }
        }
    }

    /** Sets the current booking for UI display */
    fun setCurrentBooking(booking: Booking) {
        _currentBooking.value = booking
    }

    /** Clears error messages */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /** Clears all state */
    fun clearState() {
        _createReservationState.value = null
        _modifyReservationState.value = null
        _cancelReservationState.value = null
        _qrCodeState.value = null
        _currentBooking.value = null
        _errorMessage.value = null
        _isLoading.value = false
    }

    /** Utility methods for UI */
    fun canModifyBooking(booking: Booking): Boolean {
        return booking.status in arrayOf("Pending", "Approved") && // Pending or Approved
        isMoreThanHoursAway(booking.reservationDateTime, 2)
    }

    fun canCancelBooking(booking: Booking): Boolean {
        return booking.status in arrayOf("Pending", "Approved") && // Pending or Approved
        isMoreThanHoursAway(booking.reservationDateTime, 1)
    }

    fun canGenerateQRCode(booking: Booking): Boolean {
        return booking.status == "Approved" && // Approved
        !isMoreThanHoursAway(booking.reservationDateTime, 1)
    }

    private fun isMoreThanHoursAway(dateTimeString: String, hours: Int): Boolean {
        try {
            val reservationDate =
                    java.text.SimpleDateFormat(
                                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                    java.util.Locale.getDefault()
                            )
                            .parse(dateTimeString)
            val currentTime = java.util.Date()
            val timeDifference = reservationDate!!.time - currentTime.time
            val hoursInMillis = hours * 60 * 60 * 1000
            return timeDifference > hoursInMillis
        } catch (e: Exception) {
            return false
        }
    }
}

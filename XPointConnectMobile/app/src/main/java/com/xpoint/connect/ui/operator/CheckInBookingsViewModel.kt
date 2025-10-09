package com.xpoint.connect.ui.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.repository.BookingRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckInBookingsUiState(
    val isLoading: Boolean = false,
    val bookings: List<Booking> = emptyList(),
    val error: String? = null,
    val userMessage: String? = null
) {
    val hasBookings: Boolean get() = bookings.isNotEmpty()
}

class CheckInBookingsViewModel : ViewModel() {
    
    private val bookingRepository = BookingRepository()
    
    private val _uiState = MutableStateFlow(CheckInBookingsUiState())
    val uiState: StateFlow<CheckInBookingsUiState> = _uiState.asStateFlow()

    fun loadCheckInBookings(stationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = bookingRepository.getBookingsByStation(stationId)) {
                is Resource.Loading -> {
                    // Loading state is already handled by setting isLoading = true above
                }
                is Resource.Success -> {
                    val allBookings = result.data ?: emptyList()
                    
                    // Filter for check-in eligible bookings (Pending=0, Approved=1)
                    val checkInBookings = allBookings.filter { booking ->
                        booking.status == "Pending" || booking.status == "Approved"  // Pending or Approved
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookings = checkInBookings,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load bookings",
                        bookings = emptyList()
                    )
                }
            }
        }
    }

    fun checkInBooking(bookingId: String) {
        viewModelScope.launch {
            when (val result = bookingRepository.checkInBooking(bookingId)) {
                is Resource.Loading -> {
                    // Can show loading indicator if needed
                }
                is Resource.Success -> {
                    // Remove the booking from current list since it's now checked in
                    val updatedBookings = _uiState.value.bookings.filter { it.id != bookingId }
                    _uiState.value = _uiState.value.copy(
                        bookings = updatedBookings,
                        userMessage = "Customer checked in successfully!"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        userMessage = "Check-in failed: ${result.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, userMessage = null)
    }
}
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

data class CheckOutBookingsUiState(
    val isLoading: Boolean = false,
    val bookings: List<Booking> = emptyList(),
    val error: String? = null,
    val userMessage: String? = null
) {
    val hasBookings: Boolean get() = bookings.isNotEmpty()
}

class CheckOutBookingsViewModel : ViewModel() {
    
    private val bookingRepository = BookingRepository()
    
    private val _uiState = MutableStateFlow(CheckOutBookingsUiState())
    val uiState: StateFlow<CheckOutBookingsUiState> = _uiState.asStateFlow()

    fun loadCheckOutBookings(stationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = bookingRepository.getBookingsByStation(stationId)) {
                is Resource.Loading -> {
                    // Loading state is already handled by setting isLoading = true above
                }
                is Resource.Success -> {
                    val allBookings = result.data ?: emptyList()
                    
                    // Filter for check-out eligible bookings (CheckedIn=2)
                    val checkOutBookings = allBookings.filter { booking ->
                        booking.status == "CheckedIn"  // CheckedIn
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookings = checkOutBookings,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load active sessions",
                        bookings = emptyList()
                    )
                }
            }
        }
    }

    fun checkOutBooking(bookingId: String) {
        viewModelScope.launch {
            when (val result = bookingRepository.checkOutBooking(bookingId)) {
                is Resource.Loading -> {
                    // Can show loading indicator if needed
                }
                is Resource.Success -> {
                    // Remove the booking from current list since it's now completed
                    val updatedBookings = _uiState.value.bookings.filter { it.id != bookingId }
                    _uiState.value = _uiState.value.copy(
                        bookings = updatedBookings,
                        userMessage = "Session completed successfully!"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        userMessage = "Check-out failed: ${result.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, userMessage = null)
    }
}
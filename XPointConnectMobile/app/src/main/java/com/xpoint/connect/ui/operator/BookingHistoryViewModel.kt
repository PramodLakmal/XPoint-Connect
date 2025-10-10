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

data class BookingHistoryUiState(
    val isLoading: Boolean = false,
    val bookings: List<Booking> = emptyList(),
    val error: String? = null,
    val userMessage: String? = null
) {
    val hasBookings: Boolean get() = bookings.isNotEmpty()
}

class BookingHistoryViewModel : ViewModel() {
    
    private val bookingRepository = BookingRepository()
    
    private val _uiState = MutableStateFlow(BookingHistoryUiState())
    val uiState: StateFlow<BookingHistoryUiState> = _uiState.asStateFlow()

    fun loadBookingHistory(stationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = bookingRepository.getBookingsByStation(stationId)) {
                is Resource.Loading -> {
                    // Loading state is already handled by setting isLoading = true above
                }
                is Resource.Success -> {
                    val allBookings = result.data ?: emptyList()
                    
                    // Filter for completed bookings (Completed=3, Cancelled=4, NoShow=5)
                    val historyBookings = allBookings.filter { booking ->
                        booking.status == "Completed" || booking.status == "Cancelled" || booking.status == "NoShow"  // Completed, Cancelled, NoShow
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookings = historyBookings,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load booking history",
                        bookings = emptyList()
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, userMessage = null)
    }
}
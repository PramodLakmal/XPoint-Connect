package com.xpoint.connect.ui.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.repository.OperatorRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingStatusOverviewUiState(
    val isLoading: Boolean = false,
    val activeBookings: List<Booking> = emptyList(),
    val completedBookings: List<Booking> = emptyList(),
    val cancelledBookings: List<Booking> = emptyList(),
    val error: String? = null
)

class BookingStatusOverviewViewModel : ViewModel() {
    
    private val operatorRepository = OperatorRepository()
    
    private val _uiState = MutableStateFlow(BookingStatusOverviewUiState())
    val uiState: StateFlow<BookingStatusOverviewUiState> = _uiState.asStateFlow()
    
    fun loadBookingsForStation(stationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            android.util.Log.d("BookingStatusVM", "Loading bookings for stationId: $stationId")
            
            when (val result = operatorRepository.getBookingsByStation(stationId)) {
                is Resource.Loading -> {
                    // Loading state already set above
                }
                is Resource.Success -> {
                    val allBookings = result.data ?: emptyList()
                    android.util.Log.d("BookingStatusVM", "Loaded ${allBookings.size} bookings")
                    
                    // Categorize bookings by status
                    val activeBookings = allBookings.filter { booking ->
                        booking.status in listOf("Pending", "Approved", "CheckedIn")
                    }
                    
                    val completedBookings = allBookings.filter { booking ->
                        booking.status == "Completed"
                    }
                    
                    val cancelledBookings = allBookings.filter { booking ->
                        booking.status in listOf("Cancelled", "NoShow")
                    }
                    
                    android.util.Log.d("BookingStatusVM", "Active: ${activeBookings.size}, Completed: ${completedBookings.size}, Cancelled: ${cancelledBookings.size}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activeBookings = activeBookings,
                        completedBookings = completedBookings,
                        cancelledBookings = cancelledBookings,
                        error = null
                    )
                }
                is Resource.Error -> {
                    android.util.Log.e("BookingStatusVM", "Error loading bookings: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load bookings"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
package com.xpoint.connect.ui.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.AssignedStation
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.repository.BookingRepository
import com.xpoint.connect.data.repository.OperatorRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing operator bookings and station assignments
 * Handles loading bookings for all stations assigned to an operator
 */
class OperatorBookingsViewModel : ViewModel() {

    private val operatorRepository = OperatorRepository()
    private val bookingRepository = BookingRepository()

    private val _uiState = MutableStateFlow(OperatorBookingsUiState())
    val uiState: StateFlow<OperatorBookingsUiState> = _uiState.asStateFlow()

    /**
     * Load all bookings for stations assigned to the given operator
     */
    fun loadOperatorBookings(operatorId: String) {
        if (operatorId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Invalid operator ID"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // First, get the operator's assigned stations
                when (val stationsResult = operatorRepository.getOperatorStations(operatorId)) {
                    is Resource.Success -> {
                        val stations = stationsResult.data ?: emptyList()
                        
                        if (stations.isEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                assignedStations = emptyList(),
                                bookings = emptyList(),
                                userMessage = "You don't have any stations assigned yet. Please contact your administrator."
                            )
                            return@launch
                        }

                        // Load bookings for all assigned stations
                        loadBookingsForStations(stations)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = stationsResult.message ?: "Failed to load assigned stations",
                            userMessage = "Unable to load your assigned stations. Please check your connection and try again."
                        )
                    }
                    is Resource.Loading -> {
                        // Keep showing loading state
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unexpected error occurred",
                    userMessage = "Something went wrong. Please try again later."
                )
            }
        }
    }

    /**
     * Load bookings for all assigned stations
     */
    private suspend fun loadBookingsForStations(stations: List<AssignedStation>) {
        val allBookings = mutableListOf<Booking>()
        var hasErrors = false
        val stationNames = mutableListOf<String>()

        // Collect bookings from all stations
        for (station in stations) {
            stationNames.add(station.name)
            
            when (val bookingsResult = bookingRepository.getBookingsByStation(station.id)) {
                is Resource.Success -> {
                    val stationBookings = bookingsResult.data ?: emptyList()
                    allBookings.addAll(stationBookings)
                }
                is Resource.Error -> {
                    hasErrors = true
                    // Continue loading other stations even if one fails
                }
                is Resource.Loading -> {
                    // Ignore; we're fetching sequentially
                }
            }
        }

        // Sort bookings by date (most recent first)
        allBookings.sortByDescending { it.reservationDateTime }

        // Update UI state
        val userMessage = when {
            allBookings.isEmpty() && !hasErrors -> {
                "No bookings found for your assigned stations: ${stationNames.joinToString(", ")}. " +
                "Bookings will appear here when customers make reservations."
            }
            allBookings.isEmpty() && hasErrors -> {
                "Unable to load some booking information. Please check your connection and try again."
            }
            hasErrors -> {
                "Showing ${allBookings.size} bookings. Some information may be incomplete due to connection issues."
            }
            else -> null
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            assignedStations = stations,
            bookings = allBookings,
            userMessage = userMessage,
            error = if (hasErrors && allBookings.isEmpty()) "Failed to load bookings" else null
        )
    }

    /**
     * Refresh the bookings data
     */
    fun refresh(operatorId: String) {
        loadOperatorBookings(operatorId)
    }

    /**
     * Clear any error or user message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, userMessage = null)
    }
}

/**
 * UI State for operator bookings screen
 */
data class OperatorBookingsUiState(
    val isLoading: Boolean = false,
    val assignedStations: List<AssignedStation> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val error: String? = null,
    val userMessage: String? = null
) {
    val hasStations: Boolean get() = assignedStations.isNotEmpty()
    val hasBookings: Boolean get() = bookings.isNotEmpty()
    val showEmptyState: Boolean get() = !isLoading && !hasBookings && error == null
}
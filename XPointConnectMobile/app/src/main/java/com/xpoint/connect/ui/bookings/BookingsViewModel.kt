package com.xpoint.connect.ui.bookings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.repository.BookingRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.launch

class BookingsViewModel : ViewModel() {

    private val bookingRepository = BookingRepository()

    private val _bookings = MutableLiveData<Resource<List<Booking>>>()
    val bookings: LiveData<Resource<List<Booking>>> = _bookings

    fun loadBookings(userNIC: String) {
        _bookings.value = Resource.Loading()

        viewModelScope.launch { _bookings.value = bookingRepository.getBookingsByEVOwner(userNIC) }
    }

    fun loadUpcomingBookings(userNIC: String) {
        _bookings.value = Resource.Loading()

        viewModelScope.launch { _bookings.value = bookingRepository.getUpcomingBookings(userNIC) }
    }

    fun loadBookingHistory(userNIC: String) {
        _bookings.value = Resource.Loading()

        viewModelScope.launch { _bookings.value = bookingRepository.getBookingHistory(userNIC) }
    }

    fun cancelBooking(bookingId: String, reason: String) {
        viewModelScope.launch { bookingRepository.cancelBooking(bookingId, reason) }
    }
}

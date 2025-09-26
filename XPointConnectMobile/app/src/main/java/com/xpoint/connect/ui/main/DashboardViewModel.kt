package com.xpoint.connect.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.DashboardStats
import com.xpoint.connect.data.repository.BookingRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val bookingRepository = BookingRepository()

    private val _dashboardStats = MutableLiveData<Resource<DashboardStats>>()
    val dashboardStats: LiveData<Resource<DashboardStats>> = _dashboardStats

    fun loadDashboardStats(userNIC: String) {
        _dashboardStats.value = Resource.Loading()

        viewModelScope.launch {
            _dashboardStats.value = bookingRepository.getDashboardStats(userNIC)
        }
    }
}

package com.xpoint.connect.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.ChargingStation
import com.xpoint.connect.data.repository.StationRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.launch

class StationsViewModel : ViewModel() {

    private val stationRepository = StationRepository()

    private val _stations = MutableLiveData<Resource<List<ChargingStation>>>()
    val stations: LiveData<Resource<List<ChargingStation>>> = _stations

    fun loadStations() {
        _stations.value = Resource.Loading()

        viewModelScope.launch { _stations.value = stationRepository.getAllStations() }
    }

    fun loadNearbyStations(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        _stations.value = Resource.Loading()

        viewModelScope.launch {
            _stations.value = stationRepository.getNearbyStations(latitude, longitude, radiusKm)
        }
    }
}

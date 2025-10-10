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

    private val _filteredStations = MutableLiveData<Resource<List<ChargingStation>>>()
    val filteredStations: LiveData<Resource<List<ChargingStation>>> = _filteredStations

    private var allStations: List<ChargingStation> = emptyList()
    private var currentSearchQuery: String = ""

    fun loadStations() {
        _stations.value = Resource.Loading()
        _filteredStations.value = Resource.Loading()

        viewModelScope.launch {
            val result = stationRepository.getAllStations()
            _stations.value = result

            if (result is Resource.Success) {
                allStations = result.data ?: emptyList()
                applyCurrentFilter()
            } else {
                _filteredStations.value = result
            }
        }
    }

    fun loadNearbyStations(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        _stations.value = Resource.Loading()
        _filteredStations.value = Resource.Loading()

        viewModelScope.launch {
            val result = stationRepository.getNearbyStations(latitude, longitude, radiusKm)
            _stations.value = result

            if (result is Resource.Success) {
                allStations = result.data ?: emptyList()
                applyCurrentFilter()
            } else {
                _filteredStations.value = result
            }
        }
    }

    fun searchStations(query: String) {
        currentSearchQuery = query.trim()
        applyCurrentFilter()
    }

    fun clearSearch() {
        currentSearchQuery = ""
        applyCurrentFilter()
    }

    private fun applyCurrentFilter() {
        val filtered =
                if (currentSearchQuery.isEmpty()) {
                    allStations
                } else {
                    allStations.filter { station ->
                        station.name.contains(currentSearchQuery, ignoreCase = true) ||
                                station.location.address.contains(
                                        currentSearchQuery,
                                        ignoreCase = true
                                ) ||
                                station.description.contains(currentSearchQuery, ignoreCase = true)
                    }
                }
        _filteredStations.value = Resource.Success(filtered)
    }
}

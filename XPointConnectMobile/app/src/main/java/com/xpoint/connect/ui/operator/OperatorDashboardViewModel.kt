package com.xpoint.connect.ui.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.AssignedStation
import com.xpoint.connect.data.model.ChargingStation
import com.xpoint.connect.data.repository.OperatorRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OperatorDashboardUiState(
    val isLoading: Boolean = false,
    val assignedStation: AssignedStation? = null,
    val chargingStation: ChargingStation? = null,
    val error: String? = null,
    val userMessage: String? = null
)

class OperatorDashboardViewModel : ViewModel() {
    
    private val operatorRepository = OperatorRepository()
    
    private val _uiState = MutableStateFlow(OperatorDashboardUiState())
    val uiState: StateFlow<OperatorDashboardUiState> = _uiState.asStateFlow()

    fun loadOperatorStations(operatorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            android.util.Log.d("OperatorDashboardVM", "Starting to load stations for operatorId: $operatorId")
            
            when (val result = operatorRepository.getChargingStationsByOperator(operatorId)) {
                is Resource.Loading -> {
                    android.util.Log.d("OperatorDashboardVM", "Loading state")
                    // Loading state is already handled by setting isLoading = true above
                }
                is Resource.Success -> {
                    val stations = result.data ?: emptyList()
                    android.util.Log.d("OperatorDashboardVM", "Success: received ${stations.size} stations")
                    
                    if (stations.isNotEmpty()) {
                        // For now, use the first assigned station
                        // In a more complex setup, you might let the operator choose
                        val primaryStation = stations.first()
                        android.util.Log.d("OperatorDashboardVM", "Using primary station: ${primaryStation.name} (ID: ${primaryStation.id})")
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            chargingStation = primaryStation,
                            userMessage = "Connected to ${primaryStation.name}",
                            error = null
                        )
                    } else {
                        android.util.Log.w("OperatorDashboardVM", "No stations found for operator")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No stations assigned to this operator",
                            chargingStation = null
                        )
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("OperatorDashboardVM", "Error loading stations: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load station assignments",
                        chargingStation = null
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, userMessage = null)
    }
}
package com.xpoint.connect.ui.operator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.xpoint.connect.R
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class OperatorDashboardActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPERATOR_ID = "extra_operator_id"
        const val EXTRA_OPERATOR_USERNAME = "extra_operator_username"
    }

    private lateinit var viewModel: OperatorDashboardViewModel
    private lateinit var tvTitle: TextView
    private var operatorId: String = ""
    private var stationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_dashboard)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[OperatorDashboardViewModel::class.java]

        // Initialize UI components
        initializeViews()
        
        // Get operator info from intent
        operatorId = intent.getStringExtra(EXTRA_OPERATOR_ID).orEmpty()
        val operatorUsername = intent.getStringExtra(EXTRA_OPERATOR_USERNAME).orEmpty()

        if (operatorId.isBlank()) {
            showToast("Missing operator information")
            finish()
            return
        }

        // Update title with operator name
        tvTitle.text = if (operatorUsername.isNotEmpty()) {
            "Welcome, $operatorUsername"
        } else {
            "Operator Dashboard"
        }

        // Setup card click listeners
        setupCardClickListeners()
        
        // Observe UI state
        observeUiState()

        // Load operator stations
        viewModel.loadOperatorStations(operatorId)
        
        // Debug logging
        android.util.Log.d("OperatorDashboard", "Loading stations for operatorId: $operatorId")
    }

    private fun initializeViews() {
        tvTitle = findViewById(R.id.tvTitle)
    }

    private fun setupCardClickListeners() {
        // Check In Bookings Card
        findViewById<View>(R.id.cardCheckInBookings).setOnClickListener {
            if (stationId.isNotBlank()) {
                val intent = Intent(this, CheckInBookingsActivity::class.java).apply {
                    putExtra(CheckInBookingsActivity.EXTRA_OPERATOR_ID, operatorId)
                    putExtra(CheckInBookingsActivity.EXTRA_STATION_ID, stationId)
                }
                startActivity(intent)
            } else {
                showToast("Station information not available")
            }
        }

        // Check Out Bookings Card
        findViewById<View>(R.id.cardCheckOutBookings).setOnClickListener {
            if (stationId.isNotBlank()) {
                val intent = Intent(this, CheckOutBookingsActivity::class.java).apply {
                    putExtra(CheckOutBookingsActivity.EXTRA_OPERATOR_ID, operatorId)
                    putExtra(CheckOutBookingsActivity.EXTRA_STATION_ID, stationId)
                }
                startActivity(intent)
            } else {
                showToast("Station information not available")
            }
        }

        // History Card
        findViewById<View>(R.id.cardHistory).setOnClickListener {
            if (stationId.isNotBlank()) {
                val intent = Intent(this, BookingHistoryActivity::class.java).apply {
                    putExtra(BookingHistoryActivity.EXTRA_OPERATOR_ID, operatorId)
                    putExtra(BookingHistoryActivity.EXTRA_STATION_ID, stationId)
                }
                startActivity(intent)
            } else {
                showToast("Station information not available")
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                android.util.Log.d("OperatorDashboard", "UI State updated - error: ${state.error}, assignedStation: ${state.assignedStation?.id}, chargingStation: ${state.chargingStation?.id}")
                when {
                    state.error != null -> {
                        showToast("Error: ${state.error}")
                        android.util.Log.e("OperatorDashboard", "Error: ${state.error}")
                        viewModel.clearMessage()
                    }
                    state.assignedStation != null -> {
                        stationId = state.assignedStation.id
                        showToast("Station loaded: ${state.assignedStation.name}")
                        android.util.Log.d("OperatorDashboard", "Assigned station loaded: ${state.assignedStation.id}")
                        viewModel.clearMessage()
                    }
                    state.chargingStation != null -> {
                        stationId = state.chargingStation.id
                        showToast("Station loaded: ${state.chargingStation.name}")
                        android.util.Log.d("OperatorDashboard", "Charging station loaded: ${state.chargingStation.id}")
                        updateStationIdDisplay(state.chargingStation.id)
                        viewModel.clearMessage()
                    }
                }
            }
        }
    }

    private fun updateStationIdDisplay(stationId: String) {
        android.util.Log.d("OperatorDashboard", "updateStationIdDisplay called with stationId: $stationId")
        val tvStationId = findViewById<TextView>(R.id.tvStationId)
        if (tvStationId != null) {
            tvStationId.text = "Station ID: $stationId"
            tvStationId.visibility = View.VISIBLE
            android.util.Log.d("OperatorDashboard", "Station ID display updated successfully")
        } else {
            android.util.Log.e("OperatorDashboard", "tvStationId TextView not found!")
        }
    }
}
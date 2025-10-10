package com.xpoint.connect.ui.operator

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.ui.auth.OperatorLoginActivity
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class OperatorDashboardActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPERATOR_ID = "extra_operator_id"
        const val EXTRA_OPERATOR_USERNAME = "extra_operator_username"
    }

    private lateinit var viewModel: OperatorDashboardViewModel
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var tvTitle: TextView
    private var operatorId: String = ""
    private var stationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_dashboard)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[OperatorDashboardViewModel::class.java]
        
        // Initialize UserPreferencesManager
        userPreferencesManager = (application as XPointConnectApplication).userPreferencesManager

        // Initialize UI components
        initializeViews()
        
        // Get operator info from intent
        operatorId = intent.getStringExtra(EXTRA_OPERATOR_ID).orEmpty()
        val operatorUsername = intent.getStringExtra(EXTRA_OPERATOR_USERNAME).orEmpty()

        // If operator details are missing from intent, try to get them from preferences
        if (operatorId.isBlank()) {
            android.util.Log.d("OperatorDashboard", "Operator ID missing from intent, checking saved preferences...")
            lifecycleScope.launch {
                val savedUserId = userPreferencesManager.getUserId()
                val savedUserName = userPreferencesManager.getUserName()
                android.util.Log.d("OperatorDashboard", "Saved user ID: $savedUserId")
                android.util.Log.d("OperatorDashboard", "Saved user name: $savedUserName")
                
                if (!savedUserId.isNullOrBlank()) {
                    operatorId = savedUserId
                    android.util.Log.d("OperatorDashboard", "Using saved operator ID: $operatorId")
                    
                    // Update title with saved user data
                    tvTitle.text = if (!savedUserName.isNullOrBlank()) {
                        "Welcome Back, $savedUserName!"
                    } else {
                        "Welcome Back!"
                    }
                    
                    // Setup card click listeners and continue with initialization
                    setupCardClickListeners()
                    observeUiState()
                } else {
                    // No operator information available, redirect to login
                    android.util.Log.w("OperatorDashboard", "No operator information found, redirecting to login")
                    showToast("Session expired. Please login again.")
                    redirectToLogin()
                }
            }
        } else {
            // Update title with operator name from intent
            tvTitle.text = if (operatorUsername.isNotEmpty()) {
                "Welcome Back, $operatorUsername!"
            } else {
                "Welcome Back!"
            }
            
            // Setup card click listeners and continue with normal flow
            setupCardClickListeners()
            observeUiState()
        }

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
                        viewModel.clearMessage()
                    }
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.operator_dashboard_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Performs logout by clearing session data and returning to login screen
     */
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // Clear session data
                userPreferencesManager.clearOperatorSession()
                userPreferencesManager.logout()
                
                showToast("👋 Logged out successfully")
                
                // Navigate back to login
                val intent = Intent(this@OperatorDashboardActivity, OperatorLoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                showToast("❌ Error during logout: ${e.message}")
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, OperatorLoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
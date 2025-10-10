package com.xpoint.connect.ui.operator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xpoint.connect.R
import com.xpoint.connect.ui.operator.ScannerActivity
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class CheckInBookingsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPERATOR_ID = "extra_operator_id"
        const val EXTRA_STATION_ID = "extra_station_id"
        const val QR_SCAN_REQUEST_CODE = 1001
    }

    private lateinit var viewModel: CheckInBookingsViewModel
    private lateinit var bookingsAdapter: CheckInBookingsAdapter

    // UI components
    private lateinit var progressContainer: View
    private lateinit var recyclerBookings: RecyclerView
    private lateinit var emptyStateContainer: View
    private lateinit var tvEmptyState: TextView

    private var operatorId: String = ""
    private var stationId: String = ""
    private var currentBookingForQR: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_bookings)

        // Get extras
        operatorId = intent.getStringExtra(EXTRA_OPERATOR_ID).orEmpty()
        stationId = intent.getStringExtra(EXTRA_STATION_ID).orEmpty()

        if (operatorId.isBlank() || stationId.isBlank()) {
            showToast("Missing required information")
            finish()
            return
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CheckInBookingsViewModel::class.java]

        // Initialize UI components
        initializeViews()
        setupRecyclerView()
        
        // Setup back button
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Observe UI state changes
        observeUiState()

        // Load bookings
        viewModel.loadCheckInBookings(stationId)
    }

    private fun initializeViews() {
        progressContainer = findViewById(R.id.progressContainer)
        recyclerBookings = findViewById(R.id.recyclerBookings)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }

    private fun setupRecyclerView() {
        bookingsAdapter = CheckInBookingsAdapter { booking ->
            // Handle QR scan click
            currentBookingForQR = booking.id
            val intent = Intent(this, ScannerActivity::class.java).apply {
                putExtra(ScannerActivity.EXTRA_EXPECTED_BOOKING_ID, booking.id)
            }
            startActivityForResult(intent, QR_SCAN_REQUEST_CODE)
        }
        
        recyclerBookings.apply {
            layoutManager = LinearLayoutManager(this@CheckInBookingsActivity)
            adapter = bookingsAdapter
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: CheckInBookingsUiState) {
        // Update loading state
        progressContainer.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Update bookings list
        if (state.hasBookings) {
            bookingsAdapter.submit(state.bookings)
            recyclerBookings.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
        } else {
            recyclerBookings.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
            
            when {
                state.error != null -> {
                    tvEmptyState.text = "❌ ${state.error}\n\nPull to refresh to try again."
                }
                state.userMessage != null -> {
                    tvEmptyState.text = state.userMessage
                }
                else -> {
                    tvEmptyState.text = "No check-in bookings available\n\nBookings ready for check-in will appear here"
                }
            }
        }

        // Show user messages
        state.userMessage?.let { message ->
            if (state.hasBookings) {
                showToast(message)
                viewModel.clearMessage()
            }
        }

        // Show error messages
        state.error?.let { error ->
            if (!state.hasBookings) {
                viewModel.clearMessage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            val scannedBookingId = data?.getStringExtra(ScannerActivity.RESULT_SCANNED_BOOKING_ID)
            val expectedBookingId = currentBookingForQR
            
            if (scannedBookingId != null && expectedBookingId != null) {
                if (scannedBookingId == expectedBookingId) {
                    // QR matches booking - perform check-in
                    viewModel.checkInBooking(scannedBookingId)
                    showToast("QR verified! Checking in...")
                } else {
                    showToast("QR code doesn't match this booking")
                }
            } else {
                showToast("Invalid QR code")
            }
            
            currentBookingForQR = null
        }
    }
}
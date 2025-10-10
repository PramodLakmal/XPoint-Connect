package com.xpoint.connect.ui.operator

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xpoint.connect.R
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class CheckOutBookingsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPERATOR_ID = "extra_operator_id"
        const val EXTRA_STATION_ID = "extra_station_id"
    }

    private lateinit var viewModel: CheckOutBookingsViewModel
    private lateinit var bookingsAdapter: CheckOutBookingsAdapter

    // UI components
    private lateinit var progressContainer: View
    private lateinit var recyclerBookings: RecyclerView
    private lateinit var emptyStateContainer: View
    private lateinit var tvEmptyState: TextView

    private var operatorId: String = ""
    private var stationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out_bookings)

        // Get extras
        operatorId = intent.getStringExtra(EXTRA_OPERATOR_ID).orEmpty()
        stationId = intent.getStringExtra(EXTRA_STATION_ID).orEmpty()

        if (operatorId.isBlank() || stationId.isBlank()) {
            showToast("Missing required information")
            finish()
            return
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CheckOutBookingsViewModel::class.java]

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
        viewModel.loadCheckOutBookings(stationId)
    }

    private fun initializeViews() {
        progressContainer = findViewById(R.id.progressContainer)
        recyclerBookings = findViewById(R.id.recyclerBookings)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }

    private fun setupRecyclerView() {
        bookingsAdapter = CheckOutBookingsAdapter { booking, operatorNotes ->
            // Handle Done button click with operator notes
            viewModel.checkOutBooking(booking.id, operatorNotes)
        }
        
        recyclerBookings.apply {
            layoutManager = LinearLayoutManager(this@CheckOutBookingsActivity)
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

    private fun updateUI(state: CheckOutBookingsUiState) {
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
                    tvEmptyState.text = "No active charging sessions\n\nActive sessions ready for checkout will appear here"
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
}
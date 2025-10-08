package com.xpoint.connect.ui.operator

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xpoint.connect.R
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class OperatorDashboardActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPERATOR_ID = "extra_operator_id"
        const val EXTRA_OPERATOR_USERNAME = "extra_operator_username"
    }

    private lateinit var viewModel: OperatorBookingsViewModel
    private lateinit var bookingsAdapter: OperatorBookingsAdapter

    // UI components
    private lateinit var tvTitle: TextView
    private lateinit var progressBar: View
    private lateinit var progressContainer: View
    private lateinit var recyclerBookings: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var emptyStateContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_dashboard)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[OperatorBookingsViewModel::class.java]

        // Initialize UI components
        initializeViews()
        setupRecyclerView()
        
        // Get operator info from intent
        val operatorId = intent.getStringExtra(EXTRA_OPERATOR_ID).orEmpty()
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

        // View bookings on demand
        findViewById<View>(R.id.cardViewBookings).setOnClickListener {
            viewModel.loadOperatorBookings(operatorId)
        }

        // Wire Scan QR card
        findViewById<View>(R.id.cardScanQR).setOnClickListener {
            startActivity(android.content.Intent(this, ScannerActivity::class.java))
        }

        // Observe UI state changes
        observeUiState()

        // Don't load initial data automatically - wait for user to tap "View Bookings"
    }

    private fun initializeViews() {
        tvTitle = findViewById(R.id.tvTitle)
        progressBar = findViewById(R.id.progressBar)
        progressContainer = findViewById(R.id.progressContainer)
        recyclerBookings = findViewById(R.id.recyclerBookings)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
    }

    private fun setupRecyclerView() {
        bookingsAdapter = OperatorBookingsAdapter(emptyList())
        recyclerBookings.apply {
            layoutManager = LinearLayoutManager(this@OperatorDashboardActivity)
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

    private fun updateUI(state: OperatorBookingsUiState) {
        // Update loading state
        progressContainer.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Update bookings list
        if (state.hasBookings) {
            bookingsAdapter.submit(state.bookings)
            recyclerBookings.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
        } else {
            recyclerBookings.visibility = View.GONE
            
            // Show appropriate empty state message
            when {
                state.isLoading -> {
                    emptyStateContainer.visibility = View.GONE
                }
                state.error != null -> {
                    emptyStateContainer.visibility = View.VISIBLE
                    tvEmptyState.text = "❌ ${state.error}\n\nTap View Bookings to try again."
                }
                state.userMessage != null -> {
                    emptyStateContainer.visibility = View.VISIBLE
                    tvEmptyState.text = state.userMessage
                }
                else -> {
                    emptyStateContainer.visibility = View.VISIBLE
                    tvEmptyState.text = "📋 No bookings available\n\nBookings will appear here when customers make reservations."
                }
            }
        }

        // Show user messages
        state.userMessage?.let { message ->
            if (state.hasBookings) {
                // Show as toast if we have bookings (partial error case)
                showToast(message)
                viewModel.clearMessage()
            }
        }

        // Show error messages
        state.error?.let { error ->
            if (!state.hasBookings) {
                // Error message is already shown in empty state
                // Clear the error to prevent repeated display
                viewModel.clearMessage()
            }
        }
    }
}



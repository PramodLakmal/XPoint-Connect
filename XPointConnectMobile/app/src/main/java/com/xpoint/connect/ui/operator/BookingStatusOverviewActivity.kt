package com.xpoint.connect.ui.operator

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.xpoint.connect.R
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class BookingStatusOverviewActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_STATION_ID = "extra_station_id"
        const val EXTRA_STATION_NAME = "extra_station_name"
    }
    
    private val viewModel: BookingStatusOverviewViewModel by viewModels()
    
    private lateinit var stationId: String
    private lateinit var stationName: String
    
    // Active/Upcoming bookings views
    private lateinit var cardActiveBookings: MaterialCardView
    private lateinit var tvActiveCount: TextView
    private lateinit var rvActiveBookings: RecyclerView
    private lateinit var activeBookingsAdapter: BookingStatusAdapter
    
    // Completed bookings views  
    private lateinit var cardCompletedBookings: MaterialCardView
    private lateinit var tvCompletedCount: TextView
    private lateinit var rvCompletedBookings: RecyclerView
    private lateinit var completedBookingsAdapter: BookingStatusAdapter
    
    // Cancelled/Issues bookings views
    private lateinit var cardCancelledBookings: MaterialCardView
    private lateinit var tvCancelledCount: TextView
    private lateinit var rvCancelledBookings: RecyclerView
    private lateinit var cancelledBookingsAdapter: BookingStatusAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_status_overview)
        
        // Get station info from intent
        stationId = intent.getStringExtra(EXTRA_STATION_ID) ?: ""
        stationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: "Station"
        
        if (stationId.isEmpty()) {
            showToast("Station ID not provided")
            finish()
            return
        }
        
        initializeViews()
        setupRecyclerViews()
        observeViewModel()
        
        // Load bookings for the station
        viewModel.loadBookingsForStation(stationId)
    }
    
    private fun initializeViews() {
        // Set title
        findViewById<TextView>(R.id.tvTitle).text = "Bookings - $stationName"
        
        // Active/Upcoming bookings
        cardActiveBookings = findViewById(R.id.cardActiveBookings)
        tvActiveCount = findViewById(R.id.tvActiveCount)
        rvActiveBookings = findViewById(R.id.rvActiveBookings)
        
        // Completed bookings
        cardCompletedBookings = findViewById(R.id.cardCompletedBookings)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        rvCompletedBookings = findViewById(R.id.rvCompletedBookings)
        
        // Cancelled/Issues bookings
        cardCancelledBookings = findViewById(R.id.cardCancelledBookings)
        tvCancelledCount = findViewById(R.id.tvCancelledCount)
        rvCancelledBookings = findViewById(R.id.rvCancelledBookings)
        
        // Back button
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerViews() {
        // Active bookings
        activeBookingsAdapter = BookingStatusAdapter { booking ->
            // Handle active booking click
            showToast("Active booking: ${booking.evOwnerName}")
        }
        rvActiveBookings.layoutManager = LinearLayoutManager(this)
        rvActiveBookings.adapter = activeBookingsAdapter
        
        // Completed bookings
        completedBookingsAdapter = BookingStatusAdapter { booking ->
            // Handle completed booking click
            showToast("Completed booking: ${booking.evOwnerName}")
        }
        rvCompletedBookings.layoutManager = LinearLayoutManager(this)
        rvCompletedBookings.adapter = completedBookingsAdapter
        
        // Cancelled bookings
        cancelledBookingsAdapter = BookingStatusAdapter { booking ->
            // Handle cancelled booking click
            showToast("Cancelled booking: ${booking.evOwnerName}")
        }
        rvCancelledBookings.layoutManager = LinearLayoutManager(this)
        rvCancelledBookings.adapter = cancelledBookingsAdapter
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when {
                    state.isLoading -> {
                        // Show loading state
                    }
                    state.error != null -> {
                        showToast("Error: ${state.error}")
                        viewModel.clearError()
                    }
                    else -> {
                        updateBookingCards(
                            state.activeBookings,
                            state.completedBookings,
                            state.cancelledBookings
                        )
                    }
                }
            }
        }
    }
    
    private fun updateBookingCards(
        activeBookings: List<Booking>,
        completedBookings: List<Booking>,
        cancelledBookings: List<Booking>
    ) {
        // Update active bookings
        tvActiveCount.text = activeBookings.size.toString()
        activeBookingsAdapter.updateBookings(activeBookings)
        rvActiveBookings.visibility = if (activeBookings.isEmpty()) View.GONE else View.VISIBLE
        
        // Update completed bookings
        tvCompletedCount.text = completedBookings.size.toString()
        completedBookingsAdapter.updateBookings(completedBookings)
        rvCompletedBookings.visibility = if (completedBookings.isEmpty()) View.GONE else View.VISIBLE
        
        // Update cancelled bookings
        tvCancelledCount.text = cancelledBookings.size.toString()
        cancelledBookingsAdapter.updateBookings(cancelledBookings)
        rvCancelledBookings.visibility = if (cancelledBookings.isEmpty()) View.GONE else View.VISIBLE
    }
}
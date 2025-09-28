package com.xpoint.connect.ui.bookings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.xpoint.connect.R
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.api.ApiService
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.ui.booking.BookingDetailsActivity
import com.xpoint.connect.ui.booking.CreateBookingActivity
import com.xpoint.connect.ui.main.BookingsAdapter
import com.xpoint.connect.utils.SharedPreferencesManager
import kotlinx.coroutines.launch

class BookingsFragment : Fragment() {

    // Direct API service instead of ViewModel
    private lateinit var apiService: ApiService
    private lateinit var preferencesManager: SharedPreferencesManager
    private lateinit var bookingsAdapter: BookingsAdapter
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize API service and preferences manager
        apiService = ApiClient.apiService
        preferencesManager = SharedPreferencesManager(requireContext())

        setupViews(view)
        loadBookings()
    }

    private fun setupViews(view: View) {
        // Setup RecyclerView with adapter
        bookingsAdapter = BookingsAdapter { booking ->
            // Navigate to booking details
            val intent = Intent(requireContext(), BookingDetailsActivity::class.java)
            intent.putExtra("booking_id", booking.id)
            startActivity(intent)
        }

        view.findViewById<RecyclerView>(R.id.recyclerViewBookings)?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookingsAdapter
        }

        // Initialize progress bar
        progressBar = view.findViewById(R.id.progressBar)

        // Setup tab layout or segments for upcoming/history
        view.findViewById<View>(R.id.btnUpcoming)?.setOnClickListener { loadUpcomingBookings() }

        view.findViewById<View>(R.id.btnHistory)?.setOnClickListener { loadBookingHistory() }

        // Setup FAB for creating new booking
        view.findViewById<View>(R.id.fabCreateBooking)?.setOnClickListener {
            val intent = Intent(requireContext(), CreateBookingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun updateBookingsList(bookings: List<Booking>) {
        bookingsAdapter.submitList(bookings)
        if (bookings.isEmpty()) {
            showError("No bookings found")
        }
    }

    private fun loadBookings() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val userNIC = preferencesManager.getUserNIC()
                if (userNIC != null) {
                    val response = apiService.getBookingsByEVOwner(userNIC)
                    if (response.isSuccessful) {
                        response.body()?.let { bookings: List<Booking> ->
                            updateBookingsList(bookings)
                        }
                    } else {
                        showError("Failed to load bookings: ${response.message()}")
                    }
                } else {
                    showError("User not logged in")
                }
            } catch (e: Exception) {
                showError("Error loading bookings: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun loadUpcomingBookings() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val userNIC = preferencesManager.getUserNIC()
                if (userNIC != null) {
                    val response = apiService.getUpcomingBookings(userNIC)
                    if (response.isSuccessful) {
                        response.body()?.let { bookings: List<Booking> ->
                            updateBookingsList(bookings)
                        }
                    } else {
                        showError("Failed to load upcoming bookings: ${response.message()}")
                    }
                } else {
                    showError("User not logged in")
                }
            } catch (e: Exception) {
                showError("Error loading upcoming bookings: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun loadBookingHistory() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val userNIC = preferencesManager.getUserNIC()
                if (userNIC != null) {
                    val response = apiService.getBookingHistory(userNIC)
                    if (response.isSuccessful) {
                        response.body()?.let { bookings: List<Booking> ->
                            updateBookingsList(bookings)
                        }
                    } else {
                        showError("Failed to load booking history: ${response.message()}")
                    }
                } else {
                    showError("User not logged in")
                }
            } catch (e: Exception) {
                showError("Error loading booking history: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
}

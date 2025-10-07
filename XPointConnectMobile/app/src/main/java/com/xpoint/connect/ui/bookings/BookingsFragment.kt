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
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.api.ApiService
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.ui.booking.BookingDetailsActivity
import com.xpoint.connect.ui.booking.CreateBookingActivity
import com.xpoint.connect.ui.main.BookingsAdapter
import kotlinx.coroutines.launch

class BookingsFragment : Fragment() {

    // Direct API service instead of ViewModel
    private lateinit var apiService: ApiService
    private lateinit var preferencesManager: UserPreferencesManager
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
        preferencesManager =
                (requireActivity().application as XPointConnectApplication).userPreferencesManager

        setupViews(view)
        loadBookings()
    }

    private fun setupViews(view: View) {
        // Setup RecyclerView with enhanced adapter that includes edit actions
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

        // Setup enhanced tab functionality with pending bookings focus
        val btnPending = view.findViewById<View>(R.id.btnPending)
        val btnUpcoming = view.findViewById<View>(R.id.btnUpcoming)
        val btnHistory = view.findViewById<View>(R.id.btnHistory)

        btnPending?.setOnClickListener {
            selectTab(btnPending, listOf(btnUpcoming, btnHistory))
            loadPendingBookings()
        }

        btnUpcoming?.setOnClickListener {
            selectTab(btnUpcoming, listOf(btnPending, btnHistory))
            loadUpcomingBookings() // Load upcoming bookings
        }

        btnHistory?.setOnClickListener {
            selectTab(btnHistory, listOf(btnPending, btnUpcoming))
            loadBookingHistory()
        }

        // Setup FAB for creating new booking
        view.findViewById<View>(R.id.fabCreateBooking)?.setOnClickListener {
            val intent = Intent(requireContext(), CreateBookingActivity::class.java)
            startActivity(intent)
        }

        // Load pending bookings by default and make sure RecyclerView is visible
        view.findViewById<RecyclerView>(R.id.recyclerViewBookings)?.visibility = View.VISIBLE

        // Select pending tab by default
        btnPending?.let { selectTab(it, listOf(btnUpcoming!!, btnHistory!!)) }

        loadPendingBookings()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun selectTab(selectedTab: View, otherTabs: List<View>) {
        // Update selected tab style
        selectedTab.setBackgroundResource(R.drawable.tab_selected_background)
        selectedTab.alpha = 1.0f

        // Update other tabs style
        otherTabs.forEach { tab ->
            tab.setBackgroundResource(R.drawable.tab_unselected_background)
            tab.alpha = 0.7f
        }
    }

    private fun updateBookingsList(bookings: List<Booking>, type: String = "Bookings") {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewBookings)
        val emptyStateLayout = view?.findViewById<View>(R.id.layoutEmptyState)

        bookingsAdapter.submitList(bookings)

        if (bookings.isEmpty()) {
            recyclerView?.visibility = View.GONE
            emptyStateLayout?.visibility = View.VISIBLE
            showError("No $type found")
        } else {
            recyclerView?.visibility = View.VISIBLE
            emptyStateLayout?.visibility = View.GONE
            println("BookingsFragment: Displaying ${bookings.size} $type")
            // Log booking details for debugging
            bookings.take(3).forEach { booking ->
                println(
                        "BookingsFragment: ${type} - ID: ${booking.id}, Station: ${booking.chargingStationName}, Status: ${booking.bookingStatus}"
                )
            }
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
                            updateBookingsList(bookings, "All Bookings")
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
                            updateBookingsList(bookings, "Upcoming")
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
                            updateBookingsList(bookings, "History")
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

    private fun loadPendingBookings() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val userNIC = preferencesManager.getUserNIC()
                if (userNIC != null) {
                    // Get all bookings and filter for pending ones
                    val response = apiService.getBookingsByEVOwner(userNIC)
                    if (response.isSuccessful) {
                        response.body()?.let { allBookings: List<Booking> ->
                            // Filter for pending bookings (status = 0)
                            val pendingBookings =
                                    allBookings.filter {
                                        it.bookingStatus ==
                                                com.xpoint.connect.data.model.BookingStatus.Pending
                                    }
                            println(
                                    "BookingsFragment: Found ${pendingBookings.size} pending bookings out of ${allBookings.size} total"
                            )
                            updateBookingsList(pendingBookings, "Pending")
                        }
                    } else {
                        showError("Failed to load pending bookings: ${response.message()}")
                    }
                } else {
                    showError("User not logged in")
                }
            } catch (e: Exception) {
                showError("Error loading pending bookings: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
}

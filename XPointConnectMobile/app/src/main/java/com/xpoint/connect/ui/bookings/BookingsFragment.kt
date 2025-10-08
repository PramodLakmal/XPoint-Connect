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
            animateTabClick(btnPending) {
                selectTab(btnPending, listOf(btnUpcoming, btnHistory))
                loadPendingBookings()
            }
        }

        btnUpcoming?.setOnClickListener {
            animateTabClick(btnUpcoming) {
                selectTab(btnUpcoming, listOf(btnPending, btnHistory))
                loadUpcomingBookings() // Load upcoming bookings
            }
        }

        btnHistory?.setOnClickListener {
            animateTabClick(btnHistory) {
                selectTab(btnHistory, listOf(btnPending, btnUpcoming))
                loadBookingHistory()
            }
        }

        // Setup FAB for creating new booking with animation
        view.findViewById<View>(R.id.fabCreateBooking)?.let { fab ->
            fab.setOnClickListener {
                // Add subtle click animation
                fab.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction {
                        fab.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .withEndAction {
                                val intent = Intent(requireContext(), CreateBookingActivity::class.java)
                                startActivity(intent)
                            }
                            .start()
                    }
                    .start()
            }
        }

        // Load pending bookings by default and make sure RecyclerView is visible
        view.findViewById<RecyclerView>(R.id.recyclerViewBookings)?.visibility = View.VISIBLE

        // Animate initial appearance
        animateInitialAppearance(view, btnPending, btnUpcoming, btnHistory)

        // Select pending tab by default with delay for smooth animation
        view.postDelayed({
            btnPending?.let { selectTab(it, listOf(btnUpcoming!!, btnHistory!!)) }
            loadPendingBookings()
        }, 100)
    }

    /**
     * Animates the initial appearance of the bookings interface
     */
    private fun animateInitialAppearance(view: View, vararg tabs: View?) {
        // Start tabs slightly off-screen and animate them in
        tabs.forEachIndexed { index, tab ->
            tab?.let { t ->
                t.alpha = 0f
                t.translationY = -50f
                t.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(index * 50L)
                    .start()
            }
        }

        // Animate RecyclerView entrance
        view.findViewById<RecyclerView>(R.id.recyclerViewBookings)?.let { rv ->
            rv.alpha = 0f
            rv.translationY = 100f
            rv.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .start()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar?.let { pb ->
            if (isLoading) {
                pb.visibility = View.VISIBLE
                pb.animate()
                    .alpha(1.0f)
                    .setDuration(200)
                    .start()
            } else {
                pb.animate()
                    .alpha(0.0f)
                    .setDuration(200)
                    .withEndAction {
                        pb.visibility = View.GONE
                    }
                    .start()
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun selectTab(selectedTab: View, otherTabs: List<View>) {
        // Enhanced selected tab animation with multiple effects
        selectedTab.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .alpha(1.0f)
            .translationZ(8f)
            .setDuration(250)
            .withStartAction {
                selectedTab.setBackgroundResource(R.drawable.tab_selected_background)
            }
            .withEndAction {
                // Gentle bounce back with subtle glow effect
                selectedTab.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .translationZ(4f)
                    .setDuration(200)
                    .start()
                
                // Add subtle pulse animation
                addPulseAnimation(selectedTab)
            }
            .start()

        // Enhanced animation for other tabs
        otherTabs.forEachIndexed { index, tab ->
            tab.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .alpha(0.6f)
                .translationZ(0f)
                .setDuration(250)
                .setStartDelay(index * 25L) // Stagger animation
                .withStartAction {
                    tab.setBackgroundResource(R.drawable.tab_unselected_background)
                }
                .withEndAction {
                    // Scale back to normal with smooth transition
                    tab.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }
    }

    /**
     * Adds a subtle pulse animation to the selected tab
     */
    private fun addPulseAnimation(tab: View) {
        tab.animate()
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(800)
            .withEndAction {
                tab.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(800)
                    .withEndAction {
                        // Repeat pulse if tab is still selected (check by scale)
                        if (tab.scaleX >= 0.99f) {
                            addPulseAnimation(tab)
                        }
                    }
                    .start()
            }
            .start()
    }

    /**
     * Provides immediate tactile feedback for tab clicks
     */
    private fun animateTabClick(tab: View, onComplete: () -> Unit) {
        tab.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                tab.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction {
                        onComplete()
                    }
                    .start()
            }
            .start()
    }

    private fun updateBookingsList(bookings: List<Booking>, type: String = "Bookings") {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewBookings)
        val emptyStateLayout = view?.findViewById<View>(R.id.layoutEmptyState)

        // Animate content transition
        recyclerView?.let { rv ->
            rv.animate()
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    bookingsAdapter.submitList(bookings)
                    
                    // Animate content back in
                    rv.animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .start()
                }
                .start()
        } ?: run {
            // Fallback if no animation
            bookingsAdapter.submitList(bookings)
        }

        // Handle empty state with smooth animations
        if (bookings.isEmpty()) {
            // Animate out RecyclerView
            recyclerView?.animate()
                ?.alpha(0f)
                ?.translationY(-50f)
                ?.setDuration(200)
                ?.withEndAction {
                    recyclerView.visibility = View.GONE
                    
                    // Animate in empty state
                    emptyStateLayout?.let { emptyLayout ->
                        emptyLayout.visibility = View.VISIBLE
                        emptyLayout.alpha = 0f
                        emptyLayout.translationY = 100f
                        emptyLayout.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .start()
                    }
                }
                ?.start()
                
            showError("No $type found")
        } else {
            // Animate out empty state
            emptyStateLayout?.animate()
                ?.alpha(0f)
                ?.translationY(100f)
                ?.setDuration(200)
                ?.withEndAction {
                    emptyStateLayout.visibility = View.GONE
                    
                    // Animate in RecyclerView
                    recyclerView?.let { rv ->
                        rv.visibility = View.VISIBLE
                        if (rv.alpha == 0f) {
                            rv.alpha = 0f
                            rv.translationY = 50f
                            rv.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(300)
                                .start()
                        }
                    }
                }
                ?.start()
                
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

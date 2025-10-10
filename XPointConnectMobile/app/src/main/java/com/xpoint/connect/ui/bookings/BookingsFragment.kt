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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.xpoint.connect.utils.EVOwnerToast
import com.xpoint.connect.utils.showEVOwnerToast
import kotlinx.coroutines.launch

class BookingsFragment : Fragment() {

    // Direct API service instead of ViewModel
    private lateinit var apiService: ApiService
    private lateinit var preferencesManager: UserPreferencesManager
    private lateinit var bookingsAdapter: BookingsAdapter
    private var progressBar: ProgressBar? = null
    private var swipeRefresh: SwipeRefreshLayout? = null

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

    override fun onResume() {
        super.onResume()
        println("BookingsFragment: onResume - refreshing booking data")
        // Refresh the current tab data
        refreshCurrentTab()
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

        // Initialize progress bar and swipe refresh
        progressBar = view.findViewById(R.id.progressBar)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        // Setup SwipeRefreshLayout
        swipeRefresh?.setOnRefreshListener { refreshCurrentTab() }

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

        // Refresh Button
        view.findViewById<View>(R.id.btnRefresh)?.setOnClickListener {
            it.animate().rotation(360f).setDuration(500).withEndAction { it.rotation = 0f }.start()

            refreshCurrentTab()
            showEVOwnerToast("Refreshing booking data...", EVOwnerToast.ToastType.INFO)
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
                                        val intent =
                                                Intent(
                                                        requireContext(),
                                                        CreateBookingActivity::class.java
                                                )
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
        view.postDelayed(
                {
                    btnPending?.let { selectTab(it, listOf(btnUpcoming!!, btnHistory!!)) }
                    loadPendingBookings()
                },
                100
        )
    }

    /** Animates the initial appearance of the bookings interface */
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
            rv.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(200).start()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar?.let { pb ->
            if (isLoading) {
                pb.visibility = View.VISIBLE
                pb.animate().alpha(1.0f).setDuration(200).start()
            } else {
                pb.animate()
                        .alpha(0.0f)
                        .setDuration(200)
                        .withEndAction { pb.visibility = View.GONE }
                        .start()
            }
        }

        // Also handle SwipeRefreshLayout
        swipeRefresh?.isRefreshing = isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun selectTab(selectedTab: View, otherTabs: List<View>) {
        // Enhanced selected tab animation with multiple effects
        selectedTab
                .animate()
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
                    selectedTab
                            .animate()
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
                        tab.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                    }
                    .start()
        }
    }

    /** Adds a subtle pulse animation to the selected tab */
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

    /** Provides immediate tactile feedback for tab clicks */
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
                            .withEndAction { onComplete() }
                            .start()
                }
                .start()
    }

    private fun updateBookingsList(bookings: List<Booking>, type: String = "Bookings") {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewBookings)
        val emptyStateLayout = view?.findViewById<View>(R.id.layoutEmptyState)

        // Always update the adapter first to ensure data is set
        bookingsAdapter.submitList(bookings) {
            // Callback after list is submitted
            println("BookingsFragment: List submitted with ${bookings.size} items")

            // Then handle animations if RecyclerView is available
            recyclerView?.let { rv ->
                if (rv.alpha < 1.0f) {
                    rv.animate().alpha(1.0f).setDuration(200).start()
                }
            }
        }

        // Handle visibility and empty states more reliably
        if (bookings.isEmpty()) {
            println("BookingsFragment: No $type found - showing empty state")

            // Ensure RecyclerView is hidden
            recyclerView?.let { rv ->
                if (rv.visibility == View.VISIBLE) {
                    rv.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction { rv.visibility = View.GONE }
                            .start()
                }
            }

            // Show empty state
            emptyStateLayout?.let { emptyLayout ->
                if (emptyLayout.visibility != View.VISIBLE) {
                    emptyLayout.visibility = View.VISIBLE
                    emptyLayout.alpha = 0f
                    emptyLayout.animate().alpha(1f).setDuration(300).start()
                }
            }

            showError("No $type found")
        } else {
            println("BookingsFragment: Displaying ${bookings.size} $type")

            // Ensure empty state is hidden
            emptyStateLayout?.let { emptyLayout ->
                if (emptyLayout.visibility == View.VISIBLE) {
                    emptyLayout
                            .animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction { emptyLayout.visibility = View.GONE }
                            .start()
                }
            }

            // Ensure RecyclerView is visible
            recyclerView?.let { rv ->
                if (rv.visibility != View.VISIBLE) {
                    rv.visibility = View.VISIBLE
                    rv.alpha = 0f
                    rv.animate().alpha(1f).setDuration(300).start()
                } else if (rv.alpha < 1f) {
                    rv.animate().alpha(1f).setDuration(200).start()
                }
            }

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
                println("BookingsFragment: Loading upcoming bookings for user: $userNIC")

                if (userNIC != null) {
                    val response = apiService.getUpcomingBookings(userNIC)
                    println("BookingsFragment: Upcoming bookings API response: ${response.code()}")

                    if (response.isSuccessful) {
                        response.body()?.let { bookings: List<Booking> ->
                            println("BookingsFragment: Received ${bookings.size} upcoming bookings")
                            bookings.forEachIndexed { index, booking ->
                                println(
                                        "  $index. ID: ${booking.id}, Status: ${booking.bookingStatus} (${booking.status}), Station: ${booking.chargingStationName}, Date: ${booking.reservationDateTime}"
                                )
                            }
                            updateBookingsList(bookings, "Upcoming")
                        }
                                ?: run {
                                    println(
                                            "BookingsFragment: Upcoming bookings response body is null"
                                    )
                                    updateBookingsList(emptyList(), "Upcoming")
                                }
                    } else {
                        println(
                                "BookingsFragment: Failed to load upcoming bookings: ${response.code()} - ${response.message()}"
                        )
                        showError("Failed to load upcoming bookings: ${response.message()}")
                    }
                } else {
                    println("BookingsFragment: User not logged in")
                    showError("User not logged in")
                }
            } catch (e: Exception) {
                println("BookingsFragment: Exception loading upcoming bookings: ${e.message}")
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

                            // Ensure data is visible after a short delay
                            view?.findViewById<RecyclerView>(R.id.recyclerViewBookings)
                                    ?.postDelayed(
                                            { ensureDataVisibility(pendingBookings, "Pending") },
                                            500
                                    )
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

    private fun refreshCurrentTab() {
        // Determine which tab is currently selected and refresh its data
        val pendingTab = view?.findViewById<View>(R.id.btnPending)
        val upcomingTab = view?.findViewById<View>(R.id.btnUpcoming)
        val historyTab = view?.findViewById<View>(R.id.btnHistory)

        when {
            pendingTab?.isSelected == true -> {
                println("BookingsFragment: Refreshing pending bookings tab")
                loadPendingBookings()
            }
            upcomingTab?.isSelected == true -> {
                println("BookingsFragment: Refreshing upcoming bookings tab")
                loadUpcomingBookings()
            }
            historyTab?.isSelected == true -> {
                println("BookingsFragment: Refreshing history tab")
                loadBookingHistory()
            }
            else -> {
                println("BookingsFragment: No tab selected, loading pending by default")
                loadPendingBookings()
            }
        }
    }

    private fun forceAdapterRefresh() {
        try {
            view?.findViewById<RecyclerView>(R.id.recyclerViewBookings)?.let { rv ->
                rv.post {
                    bookingsAdapter.notifyDataSetChanged()
                    println("BookingsFragment: Forced adapter refresh")
                }
            }
        } catch (e: Exception) {
            println("BookingsFragment: Error in forceAdapterRefresh: ${e.message}")
        }
    }

    private fun ensureDataVisibility(bookings: List<Booking>, type: String) {
        println(
                "BookingsFragment: ensureDataVisibility called with ${bookings.size} $type bookings"
        )

        view?.findViewById<RecyclerView>(R.id.recyclerViewBookings)?.post {
            // Double-check adapter state
            val currentList = bookingsAdapter.currentList?.size ?: 0
            println("BookingsFragment: Adapter currently has $currentList items")

            if (bookings.isNotEmpty() && currentList == 0) {
                println("BookingsFragment: Data mismatch detected, forcing refresh")
                bookingsAdapter.submitList(bookings) { forceAdapterRefresh() }
            }
        }
    }
}

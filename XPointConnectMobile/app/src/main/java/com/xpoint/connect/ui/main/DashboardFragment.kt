package com.xpoint.connect.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.ui.booking.CreateBookingActivity
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var userPreferencesManager: UserPreferencesManager
    private var swipeRefresh: SwipeRefreshLayout? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferencesManager =
                (requireActivity().application as XPointConnectApplication).userPreferencesManager

        setupViews(view)
        observeViewModel()
        loadDashboardData()
    }

    private fun setupViews(view: View) {
        // Setup SwipeRefreshLayout
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        swipeRefresh?.setOnRefreshListener { refreshDashboardData() }

        // Set welcome message
        lifecycleScope.launch {
            val userName = userPreferencesManager.getUserName() ?: "User"
            view.findViewById<android.widget.TextView>(R.id.tvWelcome)?.text =
                    getString(R.string.welcome_back) + ", $userName"
        }

        // Setup quick actions
        view.findViewById<View>(R.id.cardFindStations)?.setOnClickListener {
            // Navigate to stations
            requireActivity()
                    .findViewById<
                            com.google.android.material.bottomnavigation.BottomNavigationView>(
                            R.id.bottom_navigation
                    )
                    .selectedItemId = R.id.nav_stations
        }

        view.findViewById<View>(R.id.cardViewBookings)?.setOnClickListener {
            // Navigate to bookings
            requireActivity()
                    .findViewById<
                            com.google.android.material.bottomnavigation.BottomNavigationView>(
                            R.id.bottom_navigation
                    )
                    .selectedItemId = R.id.nav_bookings
        }

        view.findViewById<View>(R.id.cardCreateBooking)?.setOnClickListener {
            // Navigate to create booking activity
            val intent = Intent(requireContext(), CreateBookingActivity::class.java)
            startActivity(intent)
        }

        // Add refresh functionality - long press on stats to refresh
        view.findViewById<View>(R.id.tvPendingCount)?.setOnLongClickListener {
            refreshDashboardData()
            showToast("Refreshing dashboard data...")
            true
        }

        view.findViewById<View>(R.id.tvApprovedCount)?.setOnLongClickListener {
            refreshDashboardData()
            showToast("Refreshing dashboard data...")
            true
        }
    }

    private fun observeViewModel() {
        viewModel.dashboardStats.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is com.xpoint.connect.utils.Resource.Success -> {
                    swipeRefresh?.isRefreshing = false
                    resource.data?.let { stats -> updateStatsViews(stats) }
                }
                is com.xpoint.connect.utils.Resource.Error -> {
                    swipeRefresh?.isRefreshing = false
                    showToast(resource.message ?: "Failed to load dashboard data")
                }
                is com.xpoint.connect.utils.Resource.Loading -> {
                    // SwipeRefreshLayout handles loading state
                }
            }
        }
    }

    private fun updateStatsViews(stats: com.xpoint.connect.data.model.DashboardStats) {
        println("DashboardFragment: Updating dashboard stats:")
        println("  - Pending: ${stats.pendingReservations}")
        println("  - Approved Future: ${stats.approvedFutureReservations}")
        println("  - Completed This Month: ${stats.completedBookingsThisMonth}")
        println("  - Total Spent: ${stats.totalSpentThisMonth}")

        view?.let { v ->
            v.findViewById<android.widget.TextView>(R.id.tvPendingCount)?.text =
                    stats.pendingReservations.toString()
            v.findViewById<android.widget.TextView>(R.id.tvApprovedCount)?.text =
                    stats.approvedFutureReservations.toString()
            v.findViewById<android.widget.TextView>(R.id.tvCompletedCount)?.text =
                    stats.completedBookingsThisMonth.toString()
            v.findViewById<android.widget.TextView>(R.id.tvTotalSpent)?.text =
                    "Rs. ${String.format("%.2f", stats.totalSpentThisMonth)}"
        }
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            val userNIC = userPreferencesManager.getUserNIC()
            println("DashboardFragment: Loading dashboard data for user: $userNIC")
            if (userNIC != null) {
                viewModel.loadDashboardStats(userNIC)
            }
        }
    }

    private fun refreshDashboardData() {
        println("DashboardFragment: Manual refresh triggered")
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        println("DashboardFragment: onResume - refreshing dashboard data")
        loadDashboardData()
    }
}

package com.xpoint.connect.ui.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.xpoint.connect.R
import com.xpoint.connect.utils.SharedPreferencesManager
import com.xpoint.connect.utils.showToast

class BookingsFragment : Fragment() {

    private val viewModel: BookingsViewModel by viewModels()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        setupViews(view)
        observeViewModel()
        loadBookings()
    }

    private fun setupViews(view: View) {
        // Setup tab layout or segments for upcoming/history
        view.findViewById<View>(R.id.btnUpcoming)?.setOnClickListener { loadUpcomingBookings() }

        view.findViewById<View>(R.id.btnHistory)?.setOnClickListener { loadBookingHistory() }
    }

    private fun observeViewModel() {
        viewModel.bookings.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is com.xpoint.connect.utils.Resource.Success -> {
                    resource.data?.let { bookings ->
                        // Update UI with bookings
                        showToast("Loaded ${bookings.size} bookings")
                    }
                }
                is com.xpoint.connect.utils.Resource.Error -> {
                    showToast(resource.message ?: "Failed to load bookings")
                }
                is com.xpoint.connect.utils.Resource.Loading -> {
                    // Show loading state
                }
            }
        }
    }

    private fun loadBookings() {
        val userNIC = sharedPreferencesManager.getUserNIC()
        if (userNIC != null) {
            viewModel.loadBookings(userNIC)
        }
    }

    private fun loadUpcomingBookings() {
        val userNIC = sharedPreferencesManager.getUserNIC()
        if (userNIC != null) {
            viewModel.loadUpcomingBookings(userNIC)
        }
    }

    private fun loadBookingHistory() {
        val userNIC = sharedPreferencesManager.getUserNIC()
        if (userNIC != null) {
            viewModel.loadBookingHistory(userNIC)
        }
    }
}

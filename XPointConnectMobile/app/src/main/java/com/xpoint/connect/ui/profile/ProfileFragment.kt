package com.xpoint.connect.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.api.ApiService
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.ui.auth.LoginActivity
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var apiService: ApiService

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferencesManager =
                (requireActivity().application as XPointConnectApplication).userPreferencesManager
        apiService = ApiClient.apiService

        setupViews(view)
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when fragment becomes visible
        loadUserData()
    }

    private fun setupViews(view: View) {
        // Edit Profile button
        view.findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Account Settings button
        view.findViewById<View>(R.id.layoutAccountSettings)?.setOnClickListener {
            val intent = Intent(requireContext(), AccountSettingsActivity::class.java)
            startActivity(intent)
        }

        // Logout button
        view.findViewById<View>(R.id.btnLogout)?.setOnClickListener { logout() }

        // Quick Actions (will be added after UI is tested)
        // TODO: Add navigation to bookings and payment history

        // Settings items
        view.findViewById<View>(R.id.layoutNotifications)?.setOnClickListener {
            showToast("Notifications settings coming soon!")
        }

        view.findViewById<View>(R.id.layoutPrivacyPolicy)?.setOnClickListener {
            showToast("Privacy policy coming soon!")
        }

        view.findViewById<View>(R.id.layoutTermsOfService)?.setOnClickListener {
            showToast("Terms of service coming soon!")
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            view?.let { v ->
                try {
                    // Debug logging
                    val isLoggedIn = userPreferencesManager.isLoggedIn()
                    val userNIC = userPreferencesManager.getUserNIC()
                    android.util.Log.d(
                            "ProfileFragment",
                            "IsLoggedIn: $isLoggedIn, UserNIC: $userNIC"
                    )

                    if (userNIC != null && userNIC.isNotEmpty()) {
                        // Load data from API like EditProfileActivity does
                        val response = apiService.getEVOwnerProfile(userNIC)

                        if (response.isSuccessful) {
                            response.body()?.let { profile ->
                                // Update UI with fresh data from API
                                val fullName = "${profile.firstName} ${profile.lastName}".trim()
                                v.findViewById<android.widget.TextView>(R.id.tvUserName)?.text =
                                        if (fullName.isNotEmpty()) fullName else "EV Owner"

                                // Handle email display - now from API
                                val emailTextView =
                                        v.findViewById<android.widget.TextView>(R.id.tvUserEmail)
                                if (profile.email.isNullOrEmpty()) {
                                    emailTextView?.text = "No email available"
                                    emailTextView?.alpha = 0.7f
                                } else {
                                    emailTextView?.text = profile.email
                                    emailTextView?.alpha = 0.9f
                                }

                                v.findViewById<android.widget.TextView>(R.id.tvVehicleModel)?.text =
                                        if (profile.vehicleModel.isNullOrEmpty()) "Electric Vehicle"
                                        else profile.vehicleModel
                                v.findViewById<android.widget.TextView>(R.id.tvBatteryCapacity)
                                        ?.text =
                                        if (profile.batteryCapacity > 0)
                                                "${profile.batteryCapacity} kWh"
                                        else "Not specified"

                                // Debug logging
                                android.util.Log.d(
                                        "ProfileFragment",
                                        "API data loaded - Name: $fullName, Email: ${profile.email}, Vehicle: ${profile.vehicleModel}, Battery: ${profile.batteryCapacity}"
                                )
                            }
                        } else {
                            // Fallback to preferences data
                            loadFromPreferences(v)
                            android.util.Log.w(
                                    "ProfileFragment",
                                    "API failed, using preferences - Response: ${response.message()}"
                            )
                        }
                    } else {
                        // Fallback to preferences data
                        loadFromPreferences(v)
                        android.util.Log.w("ProfileFragment", "No user NIC, using preferences")
                    }
                } catch (e: Exception) {
                    // Fallback to preferences data on error
                    loadFromPreferences(v)
                    android.util.Log.e(
                            "ProfileFragment",
                            "Error loading from API, using preferences: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadFromPreferences(view: View) {
        // Fallback method using preferences like before
        val userName = userPreferencesManager.getUserName() ?: "EV Owner"
        val userEmail = userPreferencesManager.getUserEmail()
        val vehicleModel = userPreferencesManager.getVehicleModel() ?: "Electric Vehicle"
        val batteryCapacity = userPreferencesManager.getBatteryCapacity()

        view.findViewById<android.widget.TextView>(R.id.tvUserName)?.text = userName

        val emailTextView = view.findViewById<android.widget.TextView>(R.id.tvUserEmail)
        if (userEmail.isNullOrEmpty()) {
            emailTextView?.text = "No email available"
            emailTextView?.alpha = 0.7f
        } else {
            emailTextView?.text = userEmail
            emailTextView?.alpha = 0.9f
        }

        view.findViewById<android.widget.TextView>(R.id.tvVehicleModel)?.text = vehicleModel
        view.findViewById<android.widget.TextView>(R.id.tvBatteryCapacity)?.text =
                if (batteryCapacity > 0) "${batteryCapacity} kWh" else "Not specified"

        android.util.Log.d(
                "ProfileFragment",
                "Preferences data - Name: $userName, Email: $userEmail, Vehicle: $vehicleModel, Battery: $batteryCapacity"
        )
    }

    private fun logout() {
        lifecycleScope.launch {
            userPreferencesManager.logout()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}

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
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.ui.auth.LoginActivity
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var userPreferencesManager: UserPreferencesManager

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

        setupViews(view)
        loadUserData()
    }

    private fun setupViews(view: View) {
        // Edit Profile button
        view.findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            showToast("Edit profile feature coming soon!")
        }

        // Logout button
        view.findViewById<View>(R.id.btnLogout)?.setOnClickListener { logout() }

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
                val userName = userPreferencesManager.getUserName() ?: "User"
                val userEmail = userPreferencesManager.getUserEmail() ?: "email@example.com"
                val vehicleModel = userPreferencesManager.getVehicleModel() ?: "N/A"
                val batteryCapacity = userPreferencesManager.getBatteryCapacity()

                v.findViewById<android.widget.TextView>(R.id.tvUserName)?.text = userName
                v.findViewById<android.widget.TextView>(R.id.tvUserEmail)?.text = userEmail
                v.findViewById<android.widget.TextView>(R.id.tvVehicleModel)?.text = vehicleModel
                v.findViewById<android.widget.TextView>(R.id.tvBatteryCapacity)?.text =
                        "${batteryCapacity} kWh"
            }
        }
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

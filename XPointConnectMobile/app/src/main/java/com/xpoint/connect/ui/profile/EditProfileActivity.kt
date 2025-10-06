/**
 * EditProfileActivity.kt
 *
 * Purpose: Activity for editing EV Owner profile information Author: XPoint Connect Development
 * Team Date: September 28, 2025
 *
 * Description: This activity allows EV owners to update their profile information including
 * personal details, contact information, and vehicle specifications. It provides form validation,
 * error handling, and seamless API integration for profile updates.
 *
 * Key Features:
 * - Comprehensive profile editing form with validation
 * - Real-time input validation and error display
 * - Vehicle information management (model, year, battery capacity)
 * - Contact information updates (email, phone, address)
 * - Direct API integration with proper error handling
 * - Loading states and user feedback
 */
package com.xpoint.connect.ui.profile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.api.ApiService
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.data.model.UpdateEVOwnerProfileRequest
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var preferencesManager: UserPreferencesManager

    // Form fields
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etAddress: EditText
    private lateinit var etLicenseNumber: EditText
    private lateinit var etVehicleModel: EditText
    private lateinit var etVehicleYear: EditText
    private lateinit var etBatteryCapacity: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Profile"

        // Initialize API service and preferences
        apiService = ApiClient.apiService
        preferencesManager = (application as XPointConnectApplication).userPreferencesManager

        setupViews()
        loadCurrentProfile()
    }

    private fun setupViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etAddress = findViewById(R.id.etAddress)
        etLicenseNumber = findViewById(R.id.etLicenseNumber)
        etVehicleModel = findViewById(R.id.etVehicleModel)
        etVehicleYear = findViewById(R.id.etVehicleYear)
        etBatteryCapacity = findViewById(R.id.etBatteryCapacity)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        progressBar = findViewById(R.id.progressBar)

        btnSaveProfile.setOnClickListener { updateProfile() }
    }

    private fun loadCurrentProfile() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                // Debug logging
                val isLoggedIn = preferencesManager.isLoggedIn()
                val userNIC = preferencesManager.getUserNIC()
                android.util.Log.d("EditProfile", "IsLoggedIn: $isLoggedIn, UserNIC: $userNIC")

                if (userNIC != null && userNIC.isNotEmpty()) {
                    val response = apiService.getEVOwnerProfile(userNIC)

                    if (response.isSuccessful) {
                        response.body()?.let { profile -> populateFields(profile) }
                    } else {
                        showToast("Failed to load profile: ${response.message()}")
                    }
                } else {
                    val userName = preferencesManager.getUserName()
                    android.util.Log.d("EditProfile", "UserName from prefs: $userName")
                    showToast("User not logged in - please login again")
                    finish()
                }
            } catch (e: Exception) {
                showToast("Error loading profile: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun populateFields(profile: com.xpoint.connect.data.model.EVOwner) {
        etFirstName.setText(profile.firstName)
        etLastName.setText(profile.lastName)
        etEmail.setText(profile.email)
        etPhoneNumber.setText(profile.phoneNumber)
        etAddress.setText(profile.address)
        etLicenseNumber.setText(profile.licenseNumber)
        etVehicleModel.setText(profile.vehicleModel)
        if (profile.vehicleYear > 0) {
            etVehicleYear.setText(profile.vehicleYear.toString())
        }
        if (profile.batteryCapacity > 0) {
            etBatteryCapacity.setText(profile.batteryCapacity.toString())
        }
    }

    private fun updateProfile() {
        if (!validateForm()) {
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val userNIC = preferencesManager.getUserNIC()
                if (userNIC != null) {
                    val updateRequest =
                            UpdateEVOwnerProfileRequest(
                                    firstName = etFirstName.text.toString().trim(),
                                    lastName = etLastName.text.toString().trim(),
                                    email = etEmail.text.toString().trim(),
                                    phoneNumber = etPhoneNumber.text.toString().trim(),
                                    address = etAddress.text.toString().trim(),
                                    licenseNumber =
                                            etLicenseNumber.text.toString().trim().takeIf {
                                                it.isNotEmpty()
                                            },
                                    vehicleModel =
                                            etVehicleModel.text.toString().trim().takeIf {
                                                it.isNotEmpty()
                                            },
                                    vehicleYear =
                                            etVehicleYear.text.toString().trim().toIntOrNull(),
                                    batteryCapacity =
                                            etBatteryCapacity
                                                    .text
                                                    .toString()
                                                    .trim()
                                                    .toDoubleOrNull()
                            )

                    val response = apiService.updateEVOwnerProfile(userNIC, updateRequest)

                    if (response.isSuccessful) {
                        response.body()?.let { updatedProfile ->
                            // Update shared preferences with new data
                            preferencesManager.saveUserData(updatedProfile)
                        }

                        showToast("Profile updated successfully!")
                        finish()
                    } else {
                        showToast("Failed to update profile: ${response.message()}")
                    }
                } else {
                    showToast("User not logged in")
                    finish()
                }
            } catch (e: Exception) {
                showToast("Error updating profile: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (etFirstName.text.toString().trim().isEmpty()) {
            etFirstName.error = "First name is required"
            isValid = false
        }

        if (etLastName.text.toString().trim().isEmpty()) {
            etLastName.error = "Last name is required"
            isValid = false
        }

        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email address"
            isValid = false
        }

        val phoneNumber = etPhoneNumber.text.toString().trim()
        if (phoneNumber.isEmpty()) {
            etPhoneNumber.error = "Phone number is required"
            isValid = false
        } else if (phoneNumber.length < 10) {
            etPhoneNumber.error = "Please enter a valid phone number"
            isValid = false
        }

        if (etAddress.text.toString().trim().isEmpty()) {
            etAddress.error = "Address is required"
            isValid = false
        }

        // Vehicle year validation (optional but must be valid if provided)
        val vehicleYearStr = etVehicleYear.text.toString().trim()
        if (vehicleYearStr.isNotEmpty()) {
            val year = vehicleYearStr.toIntOrNull()
            if (year == null || year < 1990 || year > 2030) {
                etVehicleYear.error = "Please enter a valid year (1990-2030)"
                isValid = false
            }
        }

        // Battery capacity validation (optional but must be valid if provided)
        val batteryCapacityStr = etBatteryCapacity.text.toString().trim()
        if (batteryCapacityStr.isNotEmpty()) {
            val capacity = batteryCapacityStr.toDoubleOrNull()
            if (capacity == null || capacity <= 0 || capacity > 200) {
                etBatteryCapacity.error = "Please enter a valid battery capacity (1-200 kWh)"
                isValid = false
            }
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSaveProfile.isEnabled = !isLoading
        btnSaveProfile.text = if (isLoading) "Updating..." else "Save Profile"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

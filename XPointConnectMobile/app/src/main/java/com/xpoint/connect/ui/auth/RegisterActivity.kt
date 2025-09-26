package com.xpoint.connect.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xpoint.connect.R
import com.xpoint.connect.utils.Resource
import com.xpoint.connect.utils.SharedPreferencesManager
import com.xpoint.connect.utils.hideKeyboard
import com.xpoint.connect.utils.showToast

class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPreferencesManager = SharedPreferencesManager(this)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnRegister).setOnClickListener {
            hideKeyboard()
            performRegistration()
        }

        findViewById<View>(R.id.tvLogin).setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun performRegistration() {
        val nic =
                findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNIC)
                        .text
                        .toString()
                        .trim()
        val firstName =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etFirstName
                        )
                        .text
                        .toString()
                        .trim()
        val lastName =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etLastName
                        )
                        .text
                        .toString()
                        .trim()
        val email =
                findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEmail)
                        .text
                        .toString()
                        .trim()
        val phoneNumber =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etPhoneNumber
                        )
                        .text
                        .toString()
                        .trim()
        val password =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etPassword
                        )
                        .text
                        .toString()
                        .trim()
        val confirmPassword =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etConfirmPassword
                        )
                        .text
                        .toString()
                        .trim()
        val licenseNumber =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etLicenseNumber
                        )
                        .text
                        .toString()
                        .trim()
        val vehicleModel =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etVehicleModel
                        )
                        .text
                        .toString()
                        .trim()
        val vehicleYearStr =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etVehicleYear
                        )
                        .text
                        .toString()
                        .trim()
        val batteryCapacityStr =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                                R.id.etBatteryCapacity
                        )
                        .text
                        .toString()
                        .trim()

        val vehicleYear = vehicleYearStr.toIntOrNull() ?: 0
        val batteryCapacity = batteryCapacityStr.toDoubleOrNull() ?: 0.0

        viewModel.register(
                nic,
                firstName,
                lastName,
                email,
                phoneNumber,
                password,
                confirmPassword,
                licenseNumber,
                vehicleModel,
                vehicleYear,
                batteryCapacity
        )
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    setLoadingState(true)
                }
                is Resource.Success -> {
                    setLoadingState(false)
                    showToast(getString(R.string.registration_successful))

                    // Go back to login screen
                    finish()
                }
                is Resource.Error -> {
                    setLoadingState(false)
                    showToast(resource.message ?: "Registration failed")
                }
            }
        }

        viewModel.validationErrors.observe(this) { errors ->
            clearFieldErrors()
            errors.forEach { (field, message) ->
                when (field) {
                    "nic" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilNIC
                                    )
                                    .error = message
                    "firstName" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilFirstName
                                    )
                                    .error = message
                    "lastName" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilLastName
                                    )
                                    .error = message
                    "email" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilEmail
                                    )
                                    .error = message
                    "phoneNumber" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilPhoneNumber
                                    )
                                    .error = message
                    "password" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilPassword
                                    )
                                    .error = message
                    "confirmPassword" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilConfirmPassword
                                    )
                                    .error = message
                    "licenseNumber" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilLicenseNumber
                                    )
                                    .error = message
                    "vehicleModel" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilVehicleModel
                                    )
                                    .error = message
                    "vehicleYear" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilVehicleYear
                                    )
                                    .error = message
                    "batteryCapacity" ->
                            findViewById<com.google.android.material.textfield.TextInputLayout>(
                                            R.id.tilBatteryCapacity
                                    )
                                    .error = message
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        findViewById<View>(R.id.progressBar).visibility = if (isLoading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.btnRegister).isEnabled = !isLoading
    }

    private fun clearFieldErrors() {
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilNIC).error =
                null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilFirstName)
                .error = null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilLastName)
                .error = null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEmail).error =
                null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPhoneNumber)
                .error = null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPassword)
                .error = null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilConfirmPassword)
                .error = null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilLicenseNumber)
                .error = null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilVehicleModel)
                .error = null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilVehicleYear)
                .error = null
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilBatteryCapacity)
                .error = null
    }
}

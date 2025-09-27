/**
 * LoginActivity.kt
 *
 * Purpose: Handles user authentication for EV owners using NIC and password Author: XPoint Connect
 * Development Team Date: September 27, 2025
 *
 * Description: This activity provides the user interface for EV owner login functionality. It
 * validates user credentials through the backend API and manages user session state using SQLite
 * database storage. Upon successful authentication, users are redirected to the main application
 * dashboard.
 *
 * Key Features:
 * - NIC and password based authentication
 * - Real-time input validation with error display
 * - Secure token storage and session management
 * - Navigation to registration and password recovery
 * - Loading state management during API calls
 */
package com.xpoint.connect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.ui.main.MainActivity
import com.xpoint.connect.utils.Resource
import com.xpoint.connect.utils.hideKeyboard
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var userPreferencesManager: UserPreferencesManager

    /**
     * Initializes the login activity with required components and observers Sets up the user
     * interface, user preferences manager, and event listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userPreferencesManager = (application as XPointConnectApplication).userPreferencesManager

        setupClickListeners()
        observeViewModel()
    }

    /**
     * Configures click listeners for all interactive UI elements Handles login button, registration
     * navigation, and forgot password actions
     */
    private fun setupClickListeners() {
        findViewById<View>(R.id.btnLogin).setOnClickListener {
            hideKeyboard()
            performLogin()
        }

        findViewById<View>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<View>(R.id.tvForgotPassword).setOnClickListener {
            // TODO: Implement forgot password functionality
            showToast("Forgot password feature coming soon!")
        }
    }

    /**
     * Retrieves user input from form fields and initiates login process Extracts NIC and password
     * values and passes them to the AuthViewModel for validation
     */
    private fun performLogin() {
        val nic = findViewById<TextInputEditText>(R.id.etNIC).text.toString().trim()
        val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString().trim()

        viewModel.login(nic, password)
    }

    /**
     * Sets up observers for ViewModel LiveData to handle authentication responses Manages loading
     * states, successful login flow, error handling, and navigation
     */
    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    setLoadingState(true)
                }
                is Resource.Success -> {
                    setLoadingState(false)
                    resource.data?.let { loginResponse ->
                        // Save user data using coroutines
                        lifecycleScope.launch {
                            userPreferencesManager.saveLoginData(
                                    nic = loginResponse.nic,
                                    fullName = loginResponse.fullName,
                                    token = loginResponse.token
                            )

                            showToast(getString(R.string.login_successful))

                            // Navigate to main activity
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                }
                is Resource.Error -> {
                    setLoadingState(false)
                    showToast(resource.message ?: getString(R.string.invalid_credentials))
                }
            }
        }

        viewModel.validationErrors.observe(this) { errors ->
            clearFieldErrors()
            errors.forEach { (field, message) ->
                when (field) {
                    "nic" -> findViewById<TextInputLayout>(R.id.tilNIC).error = message
                    "password" -> findViewById<TextInputLayout>(R.id.tilPassword).error = message
                }
            }
        }
    }

    /**
     * Controls the UI loading state during authentication process Shows/hides progress indicator
     * and enables/disables user input controls
     * @param isLoading true to show loading state, false to hide
     */
    private fun setLoadingState(isLoading: Boolean) {
        findViewById<View>(R.id.progressBar).visibility = if (isLoading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.btnLogin).isEnabled = !isLoading
        findViewById<TextInputEditText>(R.id.etNIC).isEnabled = !isLoading
        findViewById<TextInputEditText>(R.id.etPassword).isEnabled = !isLoading
    }

    /**
     * Clears any existing validation error messages from input fields Resets error states for NIC
     * and password input fields
     */
    private fun clearFieldErrors() {
        findViewById<TextInputLayout>(R.id.tilNIC).error = null
        findViewById<TextInputLayout>(R.id.tilPassword).error = null
    }
}

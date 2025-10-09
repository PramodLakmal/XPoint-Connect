package com.xpoint.connect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.data.model.OperatorLoginResponse
import com.xpoint.connect.ui.operator.OperatorDashboardActivity
import com.xpoint.connect.utils.Resource
import com.xpoint.connect.utils.hideKeyboard
import com.xpoint.connect.utils.showToast

/**
 * OperatorLoginActivity - Professional login interface for charging station operators
 * 
 * Features:
 * - Material Design 3 UI with proper validation
 * - Real-time input validation with visual feedback
 * - Comprehensive error handling with toast messages
 * - Professional user experience with loading states
 * - Accessibility support and keyboard navigation
 */
class OperatorLoginActivity : AppCompatActivity() {

    private val viewModel: OperatorLoginViewModel by viewModels()
    private lateinit var userPreferencesManager: UserPreferencesManager

    // UI Components
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var cbRememberMe: MaterialCheckBox
    private lateinit var progressBar: View
    private lateinit var tvContactSupport: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_login)

        userPreferencesManager = (application as XPointConnectApplication).userPreferencesManager

        initializeViews()
        setupValidation()
        setupClickListeners()
        observeViewModel()
        
        // Check for existing session and auto-login
        checkForAutoLogin()
    }

    private fun initializeViews() {
        tilUsername = findViewById(R.id.tilOperatorUsername)
        tilPassword = findViewById(R.id.tilOperatorPassword)
        etUsername = findViewById(R.id.etOperatorUsername)
        etPassword = findViewById(R.id.etOperatorPassword)
        btnLogin = findViewById(R.id.btnOperatorLogin)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        progressBar = findViewById(R.id.progressBarOperator)
        tvContactSupport = findViewById(R.id.tvContactSupport)
        tvContactSupport = findViewById(R.id.tvContactSupport)
    }

    private fun setupValidation() {
        // Username validation
        etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateUsername()
                updateLoginButtonState()
            }
        })

        // Password validation
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePassword()
                updateLoginButtonState()
            }
        })
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            if (validateInputs()) {
                hideKeyboard()
                performLogin()
            }
        }

        // Handle "Done" action on password field
        etPassword.setOnEditorActionListener { _, _, _ ->
            if (validateInputs()) {
                hideKeyboard()
                performLogin()
            }
            true
        }

        tvContactSupport.setOnClickListener {
            showToast("📞 Please contact your system administrator for login assistance")
        }
    }

    private fun validateUsername(): Boolean {
        val username = etUsername.text?.toString()?.trim() ?: ""
        
        return when {
            username.isEmpty() -> {
                tilUsername.error = "Username is required"
                tilUsername.isErrorEnabled = true
                false
            }
            username.length < 3 -> {
                tilUsername.error = "Username must be at least 3 characters"
                tilUsername.isErrorEnabled = true
                false
            }
            username.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(username).matches() -> {
                tilUsername.error = "Please enter a valid email address"
                tilUsername.isErrorEnabled = true
                false
            }
            else -> {
                tilUsername.error = null
                tilUsername.isErrorEnabled = false
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = etPassword.text?.toString() ?: ""
        
        return when {
            password.isEmpty() -> {
                tilPassword.error = "Password is required"
                tilPassword.isErrorEnabled = true
                false
            }
            password.length < 6 -> {
                tilPassword.error = "Password must be at least 6 characters"
                tilPassword.isErrorEnabled = true
                false
            }
            else -> {
                tilPassword.error = null
                tilPassword.isErrorEnabled = false
                true
            }
        }
    }

    private fun validateInputs(): Boolean {
        val isUsernameValid = validateUsername()
        val isPasswordValid = validatePassword()
        return isUsernameValid && isPasswordValid
    }

    private fun updateLoginButtonState() {
        val username = etUsername.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""
        
        btnLogin.isEnabled = username.isNotEmpty() && password.length >= 6
        btnLogin.alpha = if (btnLogin.isEnabled) 1.0f else 0.6f
    }

    private fun performLogin() {
        val username = etUsername.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""
        
        if (!validateInputs()) {
            showToast("⚠️ Please check your input and try again")
            return
        }

        viewModel.login(username, password)
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    setLoading(true)
                }
                is Resource.Success -> {
                    setLoading(false)
                    handleLoginSuccess(result.data)
                }
                is Resource.Error -> {
                    setLoading(false)
                    handleLoginError(result.message)
                }
            }
        }
    }

    private fun handleLoginSuccess(response: OperatorLoginResponse?) {
        if (response == null) {
            showToast("❌ Invalid login response. Please try again.")
            return
        }

        try {
            val roleValue = when (val role = response.role) {
                is Number -> role.toInt()
                is String -> if (role.equals("StationOperator", true)) 1 else 0
                else -> 0
            }

            if (roleValue == 1) {
                // Save authentication data
                lifecycleScope.launch {
                    userPreferencesManager.apply {
                        saveAuthToken(response.token)
                        saveUserId(response.userId)
                        saveUserType("StationOperator")
                    }
                    
                    // Save session if remember me is checked
                    if (cbRememberMe.isChecked) {
                        val username = etUsername.text?.toString()?.trim() ?: ""
                        val password = etPassword.text?.toString() ?: ""
                        userPreferencesManager.saveOperatorSession(
                            username = username,
                            password = password,
                            rememberMe = true,
                            userId = response.userId,
                            authToken = response.token
                        )
                    }
                }

                showToast("✅ Welcome back, ${response.username}!")
                
                // Navigate to operator dashboard
                val intent = Intent(this, OperatorDashboardActivity::class.java).apply {
                    putExtra(OperatorDashboardActivity.EXTRA_OPERATOR_ID, response.userId)
                    putExtra(OperatorDashboardActivity.EXTRA_OPERATOR_USERNAME, response.username)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                startActivity(intent)
                finish()
            } else {
                showToast("🚫 You are not authorized as a Station Operator. Please contact your administrator.")
            }
        } catch (e: Exception) {
            showToast("❌ Login processing failed. Please try again.")
        }
    }

    private fun handleLoginError(errorMessage: String?) {
        val message = when {
            errorMessage?.contains("401") == true || errorMessage?.contains("Unauthorized") == true -> 
                "🔐 Invalid username or password. Please check your credentials and try again."
            errorMessage?.contains("network") == true || errorMessage?.contains("connection") == true -> 
                "🌐 Network connection issue. Please check your internet connection and try again."
            errorMessage?.contains("timeout") == true -> 
                "⏱️ Connection timeout. Please try again."
            errorMessage?.contains("400") == true -> 
                "⚠️ Invalid request. Please check your input and try again."
            errorMessage?.contains("500") == true -> 
                "🔧 Server error. Please try again later or contact support."
            !errorMessage.isNullOrBlank() -> 
                "❌ $errorMessage"
            else -> 
                "❌ Login failed. Please check your credentials and try again."
        }
        
        showToast(message)
        
        // Clear sensitive input on auth failure
        if (errorMessage?.contains("401") == true || errorMessage?.contains("credentials") == true) {
            etPassword.text?.clear()
            etPassword.requestFocus()
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        etUsername.isEnabled = !loading
        etPassword.isEnabled = !loading
        
        if (loading) {
            btnLogin.text = "Signing In..."
            btnLogin.icon = null
        } else {
            btnLogin.text = "Sign In"
            btnLogin.icon = ContextCompat.getDrawable(this, R.drawable.ic_login)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Clear any sensitive data when leaving
        etPassword.text?.clear()
    }
    
    /**
     * Checks for existing operator session and performs auto-login if valid
     */
    private fun checkForAutoLogin() {
        lifecycleScope.launch {
            try {
                if (userPreferencesManager.hasValidOperatorSession()) {
                    val credentials = userPreferencesManager.getStoredOperatorCredentials()
                    
                    if (credentials != null) {
                        val (username, password) = credentials
                        
                        // Auto-fill the form
                        etUsername.setText(username)
                        etPassword.setText(password)
                        cbRememberMe.isChecked = true
                        
                        // Show auto-login message
                        showToast("🔄 Logging in automatically...")
                        
                        // Perform auto-login
                        performLogin()
                    }
                }
            } catch (e: Exception) {
                // If auto-login fails, just continue with normal login
                showToast("Please enter your credentials")
            }
        }
    }
}



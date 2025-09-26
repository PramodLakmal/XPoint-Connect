package com.xpoint.connect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.xpoint.connect.R
import com.xpoint.connect.ui.main.MainActivity
import com.xpoint.connect.utils.Resource
import com.xpoint.connect.utils.SharedPreferencesManager
import com.xpoint.connect.utils.hideKeyboard
import com.xpoint.connect.utils.showToast

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferencesManager = SharedPreferencesManager(this)

        setupClickListeners()
        observeViewModel()
    }

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

    private fun performLogin() {
        val nic = findViewById<TextInputEditText>(R.id.etNIC).text.toString().trim()
        val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString().trim()

        viewModel.login(nic, password)
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    setLoadingState(true)
                }
                is Resource.Success -> {
                    setLoadingState(false)
                    resource.data?.let { loginResponse ->
                        // Save user data
                        sharedPreferencesManager.saveAuthToken(loginResponse.token)
                        sharedPreferencesManager.saveRefreshToken(loginResponse.refreshToken)
                        sharedPreferencesManager.saveUserType(loginResponse.userType)
                        sharedPreferencesManager.saveUserData(loginResponse.evOwner)

                        showToast(getString(R.string.login_successful))

                        // Navigate to main activity
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
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

    private fun setLoadingState(isLoading: Boolean) {
        findViewById<View>(R.id.progressBar).visibility = if (isLoading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.btnLogin).isEnabled = !isLoading
        findViewById<TextInputEditText>(R.id.etNIC).isEnabled = !isLoading
        findViewById<TextInputEditText>(R.id.etPassword).isEnabled = !isLoading
    }

    private fun clearFieldErrors() {
        findViewById<TextInputLayout>(R.id.tilNIC).error = null
        findViewById<TextInputLayout>(R.id.tilPassword).error = null
    }
}

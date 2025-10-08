package com.xpoint.connect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.ui.operator.OperatorDashboardActivity
import com.xpoint.connect.utils.Resource
import com.xpoint.connect.utils.hideKeyboard
import com.xpoint.connect.utils.showToast

class OperatorLoginActivity : AppCompatActivity() {

    private val viewModel: OperatorLoginViewModel by viewModels()
    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_login)

        userPreferencesManager = (application as XPointConnectApplication).userPreferencesManager

        findViewById<View>(R.id.btnOperatorLogin).setOnClickListener {
            hideKeyboard()
            performLogin()
        }

        observeViewModel()
    }

    private fun performLogin() {
        val username = findViewById<TextInputEditText>(R.id.etOperatorUsername).text?.toString()?.trim().orEmpty()
        val password = findViewById<TextInputEditText>(R.id.etOperatorPassword).text?.toString()?.trim().orEmpty()
        viewModel.login(username, password)
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    val resp = result.data
                    if (resp == null) {
                        showToast("Invalid login response")
                        return@observe
                    }
                    val roleValue = when (val r = resp.role) {
                        is Number -> r.toInt()
                        is String -> if (r.equals("StationOperator", true)) 1 else 0
                        else -> 0
                    }
                    if (roleValue == 1) {
                        // Save token and role for subsequent calls
                        lifecycleScope.launch {
                            (application as XPointConnectApplication)
                                .userPreferencesManager.apply {
                                    saveAuthToken(resp.token)
                                    saveUserType("StationOperator")
                                }
                        }
                        // Navigate to dashboard with operator identity
                        val intent = Intent(this, OperatorDashboardActivity::class.java)
                        intent.putExtra(OperatorDashboardActivity.EXTRA_OPERATOR_ID, resp.userId)
                        intent.putExtra(OperatorDashboardActivity.EXTRA_OPERATOR_USERNAME, resp.username)
                        startActivity(intent)
                        finish()
                    } else {
                        showToast("You are not authorized as Station Operator")
                    }
                }
                is Resource.Error -> {
                    setLoading(false)
                    showToast(result.message ?: "Login failed")
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        findViewById<View>(R.id.progressBarOperator).visibility = if (loading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.btnOperatorLogin).isEnabled = !loading
    }
}



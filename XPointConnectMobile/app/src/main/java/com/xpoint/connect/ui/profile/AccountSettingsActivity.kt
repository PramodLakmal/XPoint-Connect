/**
 * AccountSettingsActivity.kt
 *
 * Purpose: Activity for managing account security and status Author: XPoint Connect Development
 * Team Date: September 28, 2025
 *
 * Description: This activity provides account management functionality including password changes
 * and account deactivation. It includes proper security measures, confirmation dialogs, and safe
 * handling of sensitive operations with appropriate user feedback.
 *
 * Key Features:
 * - Secure password change with current password verification
 * - Account deactivation with confirmation and reason collection
 * - Input validation and security checks
 * - Confirmation dialogs for destructive actions
 * - Direct API integration with error handling
 * - Secure logout and session cleanup
 */
package com.xpoint.connect.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.api.ApiService
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.data.model.ChangePasswordRequest
import com.xpoint.connect.data.model.DeactivateAccountRequest
import com.xpoint.connect.ui.auth.LoginActivity
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var preferencesManager: UserPreferencesManager

    // Password change views
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var progressBarPassword: ProgressBar

    // Account deactivation views
    private lateinit var btnDeactivateAccount: Button
    private lateinit var progressBarDeactivate: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Account Settings"

        // Initialize API service and preferences
        apiService = ApiClient.apiService
        preferencesManager = (application as XPointConnectApplication).userPreferencesManager

        setupViews()
    }

    private fun setupViews() {
        // Password change section
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        progressBarPassword = findViewById(R.id.progressBarPassword)

        // Account deactivation section
        btnDeactivateAccount = findViewById(R.id.btnDeactivateAccount)
        progressBarDeactivate = findViewById(R.id.progressBarDeactivate)

        // Set click listeners
        btnChangePassword.setOnClickListener { changePassword() }
        btnDeactivateAccount.setOnClickListener { showDeactivateAccountDialog() }
    }

    private fun changePassword() {
        if (!validatePasswordForm()) {
            return
        }

        showPasswordLoading(true)

        lifecycleScope.launch {
            try {
                val userNIC = preferencesManager.getUserNIC()
                if (userNIC != null) {
                    val changePasswordRequest =
                            ChangePasswordRequest(
                                    currentPassword = etCurrentPassword.text.toString(),
                                    newPassword = etNewPassword.text.toString()
                            )

                    val response = apiService.changePassword(userNIC, changePasswordRequest)

                    if (response.isSuccessful) {
                        showToast("Password changed successfully!")
                        clearPasswordFields()
                    } else {
                        showToast("Failed to change password: ${response.message()}")
                    }
                } else {
                    showToast("User not logged in")
                    finish()
                }
            } catch (e: Exception) {
                showToast("Error changing password: ${e.message}")
            } finally {
                showPasswordLoading(false)
            }
        }
    }

    private fun validatePasswordForm(): Boolean {
        var isValid = true

        val currentPassword = etCurrentPassword.text.toString()
        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "Current password is required"
            isValid = false
        }

        val newPassword = etNewPassword.text.toString()
        if (newPassword.isEmpty()) {
            etNewPassword.error = "New password is required"
            isValid = false
        } else if (newPassword.length < 8) {
            etNewPassword.error = "Password must be at least 8 characters long"
            isValid = false
        } else if (!newPassword.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"))) {
            etNewPassword.error = "Password must contain uppercase, lowercase, and numbers"
            isValid = false
        }

        val confirmPassword = etConfirmPassword.text.toString()
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your new password"
            isValid = false
        } else if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        if (currentPassword == newPassword) {
            etNewPassword.error = "New password must be different from current password"
            isValid = false
        }

        return isValid
    }

    private fun clearPasswordFields() {
        etCurrentPassword.setText("")
        etNewPassword.setText("")
        etConfirmPassword.setText("")
    }

    private fun showDeactivateAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_deactivate_account, null)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etReason = dialogView.findViewById<EditText>(R.id.etReason)

        AlertDialog.Builder(this)
                .setTitle("Deactivate Account")
                .setMessage(
                        "Are you sure you want to deactivate your account? This action can be reversed by reactivating your account."
                )
                .setView(dialogView)
                .setPositiveButton("Deactivate") { _, _ ->
                    val password = etPassword.text.toString()
                    val reason = etReason.text.toString()

                    if (password.isEmpty()) {
                        showToast("Password is required to deactivate account")
                        return@setPositiveButton
                    }

                    if (reason.isEmpty()) {
                        showToast("Please provide a reason for deactivation")
                        return@setPositiveButton
                    }

                    showFinalDeactivationConfirmation(password, reason)
                }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun showFinalDeactivationConfirmation(password: String, reason: String) {
        AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage(
                        "This will deactivate your account and log you out. You can reactivate your account later by logging in again. Are you absolutely sure?"
                )
                .setPositiveButton("Yes, Deactivate") { _, _ ->
                    deactivateAccount(password, reason)
                }
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show()
    }

    private fun deactivateAccount(password: String, reason: String) {
        showDeactivateLoading(true)

        lifecycleScope.launch {
            try {
                val userNIC = preferencesManager.getUserNIC()
                if (userNIC != null) {
                    val deactivateRequest =
                            DeactivateAccountRequest(password = password, reason = reason)

                    val response = apiService.deactivateAccount(userNIC, deactivateRequest)

                    if (response.isSuccessful) {
                        showToast("Account deactivated successfully")

                        // Clear all user data and redirect to login
                        preferencesManager.logout()

                        val intent = Intent(this@AccountSettingsActivity, LoginActivity::class.java)
                        intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        showToast("Failed to deactivate account: ${response.message()}")
                    }
                } else {
                    showToast("User not logged in")
                    finish()
                }
            } catch (e: Exception) {
                showToast("Error deactivating account: ${e.message}")
            } finally {
                showDeactivateLoading(false)
            }
        }
    }

    private fun showPasswordLoading(isLoading: Boolean) {
        progressBarPassword.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnChangePassword.isEnabled = !isLoading
        btnChangePassword.text = if (isLoading) "Changing..." else "Change Password"
    }

    private fun showDeactivateLoading(isLoading: Boolean) {
        progressBarDeactivate.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnDeactivateAccount.isEnabled = !isLoading
        btnDeactivateAccount.text = if (isLoading) "Deactivating..." else "Deactivate Account"
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

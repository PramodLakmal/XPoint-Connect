/**
 * UserPreferencesManagerTemp.kt
 *
 * Temporary fallback implementation using SharedPreferences while Room database issues are resolved
 */
package com.xpoint.connect.data.database

import android.content.Context
import android.content.SharedPreferences
import com.xpoint.connect.data.model.EVOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserPreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences =
            context.getSharedPreferences("xpoint_prefs", Context.MODE_PRIVATE)

    suspend fun initialize() {
        // No initialization needed for SharedPreferences
    }

    suspend fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    suspend fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    suspend fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    suspend fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    suspend fun saveUserType(userType: String) {
        prefs.edit().putString("user_type", userType).apply()
    }

    suspend fun getUserType(): String? {
        return prefs.getString("user_type", null)
    }

    suspend fun saveUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    suspend fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    suspend fun saveUserName(userName: String) {
        prefs.edit().putString("user_name", userName).apply()
    }

    suspend fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    suspend fun saveEVOwnerProfile(profile: EVOwner) {
        prefs.edit().apply {
            putString("ev_owner_id", profile.id)
            putString("ev_owner_name", profile.firstName)
            putString("ev_owner_email", profile.email)
            putString("ev_owner_phone", profile.phoneNumber)
            putString("ev_owner_address", profile.address)
            apply()
        }
    }

    suspend fun getEVOwnerProfile(): EVOwner? {
        val id = prefs.getString("ev_owner_id", null) ?: return null
        val name = prefs.getString("ev_owner_name", "") ?: ""
        val email = prefs.getString("ev_owner_email", "") ?: ""
        val phone = prefs.getString("ev_owner_phone", "") ?: ""
        val address = prefs.getString("ev_owner_address", "") ?: ""

        return EVOwner(
                id = id,
                firstName = name,
                email = email,
                phoneNumber = phone,
                address = address
        )
    }

    suspend fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    suspend fun clearUserData() {
        prefs.edit().clear().apply()
    }

    fun getUserFlow(): Flow<EVOwner?> = flow { emit(getEVOwnerProfile()) }

    suspend fun saveRememberMe(remember: Boolean) {
        prefs.edit().putBoolean("remember_me", remember).apply()
    }

    suspend fun getRememberMe(): Boolean {
        return prefs.getBoolean("remember_me", false)
    }

    suspend fun saveAutoLogin(autoLogin: Boolean) {
        prefs.edit().putBoolean("auto_login", autoLogin).apply()
    }

    suspend fun getAutoLogin(): Boolean {
        return prefs.getBoolean("auto_login", false)
    }

    // Additional methods needed by the app
    suspend fun saveLoginData(token: String, userId: String, userType: String) {
        prefs.edit().apply {
            putString("auth_token", token)
            putString("user_id", userId)
            putString("user_type", userType)
            apply()
        }
    }

    suspend fun getUserNIC(): String? {
        return prefs.getString("user_nic", null)
    }

    suspend fun saveUserNIC(nic: String) {
        prefs.edit().putString("user_nic", nic).apply()
    }

    suspend fun getVehicleModel(): String? {
        return prefs.getString("vehicle_model", null)
    }

    suspend fun getBatteryCapacity(): Double {
        return prefs.getString("battery_capacity", "0")?.toDoubleOrNull() ?: 0.0
    }

    suspend fun saveUserData(profile: EVOwner) {
        saveEVOwnerProfile(profile)
        saveUserNIC(profile.nic)
        prefs.edit().apply {
            putString("vehicle_model", profile.vehicleModel)
            putString("battery_capacity", profile.batteryCapacity.toString())
            putString("license_number", profile.licenseNumber)
            putInt("vehicle_year", profile.vehicleYear)
            apply()
        }
    }

    suspend fun logout() {
        clearUserData()
    }

    // Operator session management methods
    suspend fun saveOperatorSession(
        username: String,
        password: String,
        rememberMe: Boolean,
        userId: String? = null,
        authToken: String? = null
    ) {
        if (rememberMe) {
            prefs.edit().apply {
                putString("operator_username", username)
                putString("operator_password", password) // Note: In production, this should be encrypted
                putString("operator_user_id", userId)
                putString("operator_auth_token", authToken)
                putBoolean("operator_remember_me", true)
                putLong("operator_session_created", System.currentTimeMillis())
                apply()
            }
        } else {
            clearOperatorSession()
        }
    }

    suspend fun getOperatorSession(): Pair<String, String>? {
        val rememberMe = prefs.getBoolean("operator_remember_me", false)
        if (!rememberMe) return null

        val username = prefs.getString("operator_username", null) ?: return null
        val password = prefs.getString("operator_password", null) ?: return null

        // Check if session is still valid (30 days)
        val sessionCreated = prefs.getLong("operator_session_created", 0)
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        if (System.currentTimeMillis() - sessionCreated > thirtyDaysInMillis) {
            clearOperatorSession()
            return null
        }

        return Pair(username, password)
    }

    suspend fun clearOperatorSession() {
        prefs.edit().apply {
            remove("operator_username")
            remove("operator_password")
            remove("operator_user_id")
            remove("operator_auth_token")
            remove("operator_remember_me")
            remove("operator_session_created")
            apply()
        }
    }

    suspend fun isAutoLoginEnabled(): Boolean {
        return prefs.getBoolean("operator_remember_me", false)
    }

    suspend fun hasValidOperatorSession(): Boolean {
        val session = getOperatorSession()
        return session != null
    }

    suspend fun getStoredOperatorCredentials(): Pair<String, String>? {
        return getOperatorSession()
    }
}
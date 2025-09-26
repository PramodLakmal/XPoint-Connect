package com.xpoint.connect.utils

import android.content.SharedPreferences
import com.xpoint.connect.data.model.EVOwner
import android.content.Context

class SharedPreferencesManager(context: Context) {

    companion object {
        private const val PREF_NAME = "xpoint_connect_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USER_NIC = "user_nic"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_FIRST_TIME = "first_time"
        private const val KEY_VEHICLE_MODEL = "vehicle_model"
        private const val KEY_BATTERY_CAPACITY = "battery_capacity"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_LICENSE_NUMBER = "license_number"
    }

    private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUserType(userType: String) {
        sharedPreferences.edit().putString(KEY_USER_TYPE, userType).apply()
    }

    fun getUserType(): String? {
        return sharedPreferences.getString(KEY_USER_TYPE, null)
    }

    fun saveUserData(evOwner: EVOwner) {
        with(sharedPreferences.edit()) {
            putString(KEY_USER_NIC, evOwner.nic)
            putString(KEY_USER_NAME, "${evOwner.firstName} ${evOwner.lastName}")
            putString(KEY_USER_EMAIL, evOwner.email)
            putString(KEY_PHONE_NUMBER, evOwner.phoneNumber)
            putString(KEY_LICENSE_NUMBER, evOwner.licenseNumber)
            putString(KEY_VEHICLE_MODEL, evOwner.vehicleModel)
            putFloat(KEY_BATTERY_CAPACITY, evOwner.batteryCapacity.toFloat())
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserNIC(): String? {
        return sharedPreferences.getString(KEY_USER_NIC, null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun getPhoneNumber(): String? {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, null)
    }

    fun getLicenseNumber(): String? {
        return sharedPreferences.getString(KEY_LICENSE_NUMBER, null)
    }

    fun getVehicleModel(): String? {
        return sharedPreferences.getString(KEY_VEHICLE_MODEL, null)
    }

    fun getBatteryCapacity(): Double {
        return sharedPreferences.getFloat(KEY_BATTERY_CAPACITY, 0f).toDouble()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setFirstTime(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME, isFirstTime).apply()
    }

    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true)
    }

    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }

    fun logout() {
        with(sharedPreferences.edit()) {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_NIC)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_TYPE)
            apply()
        }
    }
}

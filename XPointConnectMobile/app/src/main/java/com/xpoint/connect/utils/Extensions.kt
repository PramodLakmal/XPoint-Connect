package com.xpoint.connect.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

// Extension functions for easier usage
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = currentFocus ?: View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}

// Date formatting utilities
object DateUtils {

    private const val API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val DISPLAY_DATE_FORMAT = "MMM dd, yyyy"
    private const val DISPLAY_TIME_FORMAT = "hh:mm a"
    private const val DISPLAY_DATE_TIME_FORMAT = "MMM dd, yyyy 'at' hh:mm a"

    fun formatApiDateToDisplay(apiDate: String): String {
        return try {
            val apiFormat = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
            val displayFormat = SimpleDateFormat(DISPLAY_DATE_TIME_FORMAT, Locale.getDefault())
            val date = apiFormat.parse(apiDate)
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            apiDate
        }
    }

    fun formatApiDateToDisplayDate(apiDate: String): String {
        return try {
            val apiFormat = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
            val displayFormat = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            val date = apiFormat.parse(apiDate)
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            apiDate
        }
    }

    fun formatApiDateToDisplayTime(apiDate: String): String {
        return try {
            val apiFormat = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
            val displayFormat = SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault())
            val date = apiFormat.parse(apiDate)
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            apiDate
        }
    }

    fun formatDateTimeToApi(date: Date): String {
        val apiFormat = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
        return apiFormat.format(date)
    }

    fun getCurrentDateTimeForApi(): String {
        return formatDateTimeToApi(Date())
    }
}

// Validation utilities
object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidNIC(nic: String): Boolean {
        // Basic NIC validation for Sri Lankan format
        return nic.length == 10 || nic.length == 12
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        // Basic phone number validation
        return phone.length >= 10 &&
                phone.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}

// UI utilities
object UiUtils {

    fun getStatusColor(status: String): Int {
        return when (status.lowercase()) {
            "pending" -> android.R.color.holo_orange_dark
            "approved" -> android.R.color.holo_green_dark
            "checkedin" -> android.R.color.holo_blue_dark
            "completed" -> android.R.color.holo_green_light
            "cancelled" -> android.R.color.holo_red_dark
            "noshow" -> android.R.color.darker_gray
            else -> android.R.color.black
        }
    }

    fun getStatusDisplayText(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "Pending Approval"
            "approved" -> "Approved"
            "checkedin" -> "Checked In"
            "completed" -> "Completed"
            "cancelled" -> "Cancelled"
            "noshow" -> "No Show"
            else -> status
        }
    }
}

// Distance calculation utility
object LocationUtils {

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) *
                                Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon / 2) *
                                Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1) {
            "${(distanceKm * 1000).toInt()} m"
        } else {
            String.format("%.1f km", distanceKm)
        }
    }
}

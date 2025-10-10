/**
 * EditBookingActivity.kt
 *
 * Purpose: Activity for editing existing bookings (pending bookings only) Author: XPoint Connect
 * Development Team Date: October 6, 2025
 *
 * Description: This activity allows users to modify their pending bookings including changing the
 * reservation date/time, duration, and charging station. Only bookings with PENDING status can be
 * edited to maintain booking integrity.
 */
package com.xpoint.connect.ui.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.api.ApiService
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.data.model.*
import com.xpoint.connect.utils.showToast
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class EditBookingActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var preferencesManager: UserPreferencesManager

    // UI Components
    private lateinit var currentStationCard: MaterialCardView
    private lateinit var currentStationText: TextView
    private lateinit var changeStationButton: MaterialButton

    private lateinit var dateTimeLayout: TextInputLayout
    private lateinit var dateTimeInput: TextInputEditText
    private lateinit var selectDateTimeButton: MaterialButton

    private lateinit var durationLayout: TextInputLayout
    private lateinit var durationInput: TextInputEditText
    private lateinit var durationSeekBar: SeekBar
    private lateinit var durationText: TextView

    private lateinit var costEstimateCard: MaterialCardView
    private lateinit var estimatedCostText: TextView
    private lateinit var costDetailsText: TextView

    private lateinit var updateBookingButton: MaterialButton
    private lateinit var cancelBookingButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Data
    private var currentBooking: Booking? = null
    private var selectedStation: ChargingStation? = null
    private var selectedDateTime: Calendar = Calendar.getInstance()
    private var selectedDurationMinutes: Int = 120

    companion object {
        const val EXTRA_BOOKING_ID = "extra_booking_id"
        const val REQUEST_SELECT_STATION = 1001
        private const val MIN_DURATION_MINUTES = 30
        private const val MAX_DURATION_MINUTES = 480
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_booking)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Booking"

        initializeServices()
        initializeViews()
        setupClickListeners()

        val bookingId = intent.getStringExtra("booking_id")
        if (bookingId != null) {
            loadBooking(bookingId)
        } else {
            showToast("Invalid booking ID")
            finish()
        }
    }

    private fun initializeServices() {
        apiService = ApiClient.apiService
        preferencesManager = (application as XPointConnectApplication).userPreferencesManager
    }

    private fun initializeViews() {
        // Station selection
        currentStationCard = findViewById(R.id.currentStationCard)
        currentStationText = findViewById(R.id.currentStationText)
        changeStationButton = findViewById(R.id.changeStationButton)

        // Date/Time selection
        dateTimeLayout = findViewById(R.id.dateTimeLayout)
        dateTimeInput = findViewById(R.id.dateTimeInput)
        selectDateTimeButton = findViewById(R.id.selectDateTimeButton)

        // Duration selection
        durationLayout = findViewById(R.id.durationLayout)
        durationInput = findViewById(R.id.durationInput)
        durationSeekBar = findViewById(R.id.durationSeekBar)
        durationText = findViewById(R.id.durationText)

        // Cost estimation
        costEstimateCard = findViewById(R.id.costEstimateCard)
        estimatedCostText = findViewById(R.id.estimatedCostText)
        costDetailsText = findViewById(R.id.costDetailsText)

        // Actions
        updateBookingButton = findViewById(R.id.updateBookingButton)
        cancelBookingButton = findViewById(R.id.cancelBookingButton)
        progressBar = findViewById(R.id.progressBar)

        // Setup duration SeekBar
        setupDurationSeekBar()
    }

    private fun setupDurationSeekBar() {
        durationSeekBar.max = MAX_DURATION_MINUTES - MIN_DURATION_MINUTES

        durationSeekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                    ) {
                        if (fromUser) {
                            selectedDurationMinutes = MIN_DURATION_MINUTES + progress
                            updateDuration()
                            updateCostEstimate()
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                }
        )
    }

    private fun setupClickListeners() {
        changeStationButton.setOnClickListener { selectStation() }
        selectDateTimeButton.setOnClickListener { showDateTimePicker() }
        dateTimeInput.setOnClickListener { showDateTimePicker() }
        updateBookingButton.setOnClickListener { updateBooking() }
        cancelBookingButton.setOnClickListener { cancelBooking() }
    }

    private fun loadBooking(bookingId: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = apiService.getBookingById(bookingId)
                if (response.isSuccessful) {
                    currentBooking = response.body()
                    currentBooking?.let { booking ->
                        if (booking.bookingStatus != BookingStatus.Pending) {
                            showToast("Only pending bookings can be edited")
                            finish()
                            return@launch
                        }
                        populateBookingData(booking)
                    }
                            ?: run {
                                showToast("Booking data is empty")
                                finish()
                            }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showToast(
                            "Failed to load booking: ${response.code()} - ${errorBody ?: response.message()}"
                    )
                    finish()
                }
            } catch (e: Exception) {
                showToast("Error loading booking: ${e.message}")
                e.printStackTrace()
                finish()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun populateBookingData(booking: Booking) {
        // Set current station info
        currentStationText.text = booking.chargingStationName

        // Load station details if needed
        loadStationDetails(booking.chargingStationId)

        // Set date/time
        try {
            val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = apiFormat.parse(booking.reservationDateTime)
            if (date != null) {
                selectedDateTime.time = date
            }
        } catch (e: Exception) {
            selectedDateTime.time = booking.startTime
        }
        updateDateTime()

        // Set duration
        selectedDurationMinutes = booking.durationMinutes
        durationSeekBar.progress = selectedDurationMinutes - MIN_DURATION_MINUTES
        updateDuration()

        updateCostEstimate()
    }

    private fun loadStationDetails(stationId: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.getStationById(stationId)
                if (response.isSuccessful) {
                    selectedStation = response.body()
                    updateCostEstimate()
                }
            } catch (e: Exception) {
                println("Failed to load station details: ${e.message}")
            }
        }
    }

    private fun selectStation() {
        val intent = Intent(this, StationSelectionActivity::class.java)
        startActivityForResult(intent, REQUEST_SELECT_STATION)
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
                        this,
                        { _, year, month, dayOfMonth ->
                            selectedDateTime.set(Calendar.YEAR, year)
                            selectedDateTime.set(Calendar.MONTH, month)
                            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            showTimePicker()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                )
                .show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            selectedDateTime.set(Calendar.MINUTE, minute)
                            selectedDateTime.set(Calendar.SECOND, 0)
                            updateDateTime()
                            updateCostEstimate()
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                )
                .show()
    }

    private fun updateDateTime() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        dateTimeInput.setText(dateFormat.format(selectedDateTime.time))
    }

    private fun updateDuration() {
        val hours = selectedDurationMinutes / 60
        val minutes = selectedDurationMinutes % 60
        val durationText =
                when {
                    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                    hours > 0 -> "${hours}h"
                    else -> "${minutes}m"
                }
        durationInput.setText(durationText)
        this.durationText.text = "Duration: $durationText"
    }

    private fun updateCostEstimate() {
        selectedStation?.let { station ->
            val estimatedCost =
                    calculateEstimatedCost(station.chargingRate, selectedDurationMinutes)
            estimatedCostText.text = "Rs. %.2f".format(estimatedCost)
            val hours = selectedDurationMinutes / 60.0
            costDetailsText.text = "Rs. %.2f/hour × %.1f hours".format(station.chargingRate, hours)
        }
                ?: run {
                    estimatedCostText.text = "Calculating..."
                    costDetailsText.text = "Cost will be updated after station selection"
                }
    }

    private fun calculateEstimatedCost(ratePerHour: Double, durationMinutes: Int): Double {
        val hours = durationMinutes / 60.0
        return ratePerHour * hours
    }

    private fun updateBooking() {
        if (!validateForm()) return

        val booking = currentBooking ?: return

        // Check if booking can be modified based on status and time restrictions
        if (!canModifyBooking(booking)) {
            val statusMsg =
                    when (booking.bookingStatus) {
                        BookingStatus.Pending ->
                                "Booking is too close to reservation time (less than 30 minutes away)"
                        BookingStatus.Approved ->
                                "Approved booking is too close to reservation time (less than 2 hours away)"
                        else -> "Booking cannot be modified due to its current status"
                    }
            showToast("Cannot modify booking: $statusMsg")
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val reservationDateTime = formatDateTimeForAPI(selectedDateTime.time)
                val updateRequest =
                        UpdateBookingRequest(
                                reservationDateTime = reservationDateTime,
                                durationMinutes = selectedDurationMinutes
                        )

                val response = apiService.updateBooking(booking.id, updateRequest)
                if (response.isSuccessful) {
                    showToast("Booking updated successfully")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    showToast(
                            "Failed to update booking: ${response.code()} - ${errorBody ?: response.message()}"
                    )
                }
            } catch (e: Exception) {
                showToast("Error updating booking: ${e.message}")
                e.printStackTrace()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun cancelBooking() {
        androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage(
                        "Are you sure you want to cancel this booking? This action cannot be undone."
                )
                .setPositiveButton("Yes, Cancel") { _, _ -> performCancelBooking() }
                .setNegativeButton("No", null)
                .show()
    }

    private fun performCancelBooking() {
        val booking = currentBooking ?: return
        showLoading(true)

        lifecycleScope.launch {
            try {
                val cancelRequest =
                        CancelBookingRequest(
                                cancellationReason = "Cancelled by user from mobile app"
                        )
                val response = apiService.cancelBooking(booking.id, cancelRequest)
                if (response.isSuccessful) {
                    showToast("Booking cancelled successfully")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    showToast(
                            "Failed to cancel booking: ${response.code()} - ${errorBody ?: response.message()}"
                    )
                }
            } catch (e: Exception) {
                showToast("Error cancelling booking: ${e.message}")
                e.printStackTrace()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun validateForm(): Boolean {
        val now = Calendar.getInstance()
        if (selectedDateTime.before(now)) {
            showToast("Please select a future date and time")
            return false
        }
        return true
    }

    private fun formatDateTimeForAPI(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(date)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        updateBookingButton.isEnabled = !show
        cancelBookingButton.isEnabled = !show
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_STATION && resultCode == RESULT_OK) {
            data?.getStringExtra(StationSelectionActivity.EXTRA_SELECTED_STATION_ID)?.let {
                    stationId ->
                val stationName =
                        data.getStringExtra(StationSelectionActivity.EXTRA_SELECTED_STATION_NAME)
                loadSelectedStation(stationId, stationName)
            }
        }
    }

    private fun loadSelectedStation(stationId: String, stationName: String?) {
        currentStationText.text = "Loading station..."
        lifecycleScope.launch {
            try {
                val response = apiService.getStationById(stationId)
                if (response.isSuccessful) {
                    selectedStation = response.body()
                    currentStationText.text = selectedStation?.name ?: stationName
                    updateCostEstimate()
                } else {
                    currentStationText.text = stationName ?: "Selected Station"
                    showToast("Failed to load station details")
                }
            } catch (e: Exception) {
                currentStationText.text = stationName ?: "Selected Station"
                showToast("Error loading station: ${e.message}")
            }
        }
    }

    /** Validation methods adapted from ReservationManager */
    private fun canModifyBooking(booking: Booking): Boolean {
        Log.d("EditBookingActivity", "Checking if booking can be modified:")
        Log.d("EditBookingActivity", "  - Booking ID: ${booking.id}")
        Log.d("EditBookingActivity", "  - Booking Status: ${booking.bookingStatus}")
        Log.d("EditBookingActivity", "  - Reservation DateTime: ${booking.reservationDateTime}")

        val canModify =
                when (booking.bookingStatus) {
                    BookingStatus.Pending -> {
                        // Pending bookings can be modified if more than 30 minutes away
                        val moreThan30Min = isMoreThanMinutesAway(booking.reservationDateTime, 30)
                        Log.d(
                                "EditBookingActivity",
                                "  - Pending booking, more than 30 min away: $moreThan30Min"
                        )
                        moreThan30Min
                    }
                    BookingStatus.Approved -> {
                        // Approved bookings can be modified if more than 2 hours away
                        val moreThan2Hours = isMoreThanHoursAway(booking.reservationDateTime, 2)
                        Log.d(
                                "EditBookingActivity",
                                "  - Approved booking, more than 2 hours away: $moreThan2Hours"
                        )
                        moreThan2Hours
                    }
                    else -> {
                        Log.d(
                                "EditBookingActivity",
                                "  - Booking status does not allow modification"
                        )
                        false
                    }
                }

        Log.d("EditBookingActivity", "  - Can modify: $canModify")
        return canModify
    }

    private fun isMoreThanMinutesAway(dateTimeString: String, minutes: Int): Boolean {
        try {
            // Try multiple date formats to handle different API responses
            val dateFormats =
                    listOf(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'", Locale.getDefault()),
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    )

            var reservationDate: Date? = null

            // Try each format until one works
            for (format in dateFormats) {
                format.timeZone = TimeZone.getTimeZone("UTC")
                try {
                    reservationDate = format.parse(dateTimeString)
                    break
                } catch (e: Exception) {
                    // Continue to next format
                }
            }

            if (reservationDate == null) {
                Log.d("EditBookingActivity", "Unable to parse reservation date: $dateTimeString")
                return false // Unable to parse date
            }

            val currentTime = Date()
            val timeDifference = reservationDate.time - currentTime.time
            val minutesInMillis = minutes * 60L * 1000L

            Log.d(
                    "EditBookingActivity",
                    "Time difference: ${timeDifference / 60000} minutes (required: $minutes)"
            )
            return timeDifference > minutesInMillis
        } catch (e: Exception) {
            Log.e("EditBookingActivity", "Error parsing date: ${e.message}")
            return false
        }
    }

    private fun isMoreThanHoursAway(dateTimeString: String, hours: Int): Boolean {
        try {
            // Try multiple date formats to handle different API responses
            val dateFormats =
                    listOf(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'", Locale.getDefault()),
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    )

            var reservationDate: Date? = null

            // Try each format until one works
            for (format in dateFormats) {
                format.timeZone = TimeZone.getTimeZone("UTC")
                try {
                    reservationDate = format.parse(dateTimeString)
                    break
                } catch (e: Exception) {
                    // Continue to next format
                }
            }

            if (reservationDate == null) {
                Log.d("EditBookingActivity", "Unable to parse reservation date: $dateTimeString")
                return false // Unable to parse date
            }

            val currentTime = Date()
            val timeDifference = reservationDate.time - currentTime.time
            val hoursInMillis = hours * 60L * 60L * 1000L

            Log.d(
                    "EditBookingActivity",
                    "Time difference: ${timeDifference / 3600000} hours (required: $hours)"
            )
            return timeDifference > hoursInMillis
        } catch (e: Exception) {
            Log.e("EditBookingActivity", "Error parsing date: ${e.message}")
            return false
        }
    }
}

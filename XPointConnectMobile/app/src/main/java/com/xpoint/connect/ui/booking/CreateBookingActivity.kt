/**
 * CreateBookingActivity.kt
 *
 * Purpose: Activity for creating new charging station reservations Author: XPoint Connect
 * Development Team Date: September 28, 2025
 *
 * Description: This activity provides a comprehensive interface for creating new charging station
 * reservations. It includes station selection, date/time picking, duration selection, cost
 * estimation, and booking confirmation functionality. It integrates with the ReservationManager for
 * complete booking lifecycle management.
 */
package com.xpoint.connect.ui.booking

// import com.xpoint.connect.ui.stations.StationSelectionActivity // TODO: Create this activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
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
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.model.ChargingStation
import com.xpoint.connect.data.model.CreateBookingRequest
import com.xpoint.connect.data.model.Location
import com.xpoint.connect.utils.showToast
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class CreateBookingActivity : AppCompatActivity() {

    // API Service for direct calls
    private lateinit var apiService: ApiService
    private lateinit var preferencesManager: UserPreferencesManager

    // UI Components
    private lateinit var stationSelectionCard: MaterialCardView
    private lateinit var selectedStationText: TextView
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

    private lateinit var createBookingButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Data
    private var selectedStation: ChargingStation? = null
    private var selectedDateTime: Calendar = Calendar.getInstance()
    private var selectedDurationMinutes: Int = 120 // Default 2 hours

    companion object {
        const val EXTRA_STATION_ID = "extra_station_id"
        const val REQUEST_SELECT_STATION = 1001
        private const val MIN_DURATION_MINUTES = 30
        private const val MAX_DURATION_MINUTES = 480 // 8 hours
        private const val DEFAULT_DURATION_MINUTES = 120 // 2 hours
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_booking)

        // Setup toolbar with back button
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Booking"

        initializeViewModel()
        initializeViews()
        setupObservers()
        setupClickListeners()
        handleIntentData()

        // Set default values
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 12) // Default to 1 hour from now
        selectedDurationMinutes = DEFAULT_DURATION_MINUTES
        updateDuration()
        updateDateTime()
    }

    private fun initializeViewModel() {
        // Initialize API service and preferences manager for direct API calls
        apiService = ApiClient.apiService
        preferencesManager = (application as XPointConnectApplication).userPreferencesManager
    }

    private fun initializeViews() {
        // Station selection
        stationSelectionCard = findViewById(R.id.stationSelectionCard)
        selectedStationText = findViewById(R.id.selectedStationText)
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
        createBookingButton = findViewById(R.id.createBookingButton)
        progressBar = findViewById(R.id.progressBar)

        // Setup duration SeekBar
        setupDurationSeekBar()
    }

    private fun setupDurationSeekBar() {
        durationSeekBar.max = MAX_DURATION_MINUTES - MIN_DURATION_MINUTES
        durationSeekBar.progress = DEFAULT_DURATION_MINUTES - MIN_DURATION_MINUTES

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

    private fun setupObservers() {
        // TODO: Implement proper ViewModel initialization with dependency injection
        // Currently commented out to prevent crash - needs proper ReservationManager setup
        /*
        reservationViewModel.createReservationState.observe(
                this,
                Observer { result ->
                    when (result) {
                        is Resource.Loading -> {
                            showLoading(true)
                            createBookingButton.isEnabled = false
                        }
                        is Resource.Success -> {
                            showLoading(false)
                            createBookingButton.isEnabled = true
                            showBookingCreatedDialog(result.data!!)
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            createBookingButton.isEnabled = true
                            showToast(result.message ?: "Failed to create booking")
                        }
                    }
                }
        )

        reservationViewModel.errorMessage.observe(
                this,
                Observer { error ->
                    error?.let {
                        showToast(it)
                        reservationViewModel.clearErrorMessage()
                    }
                }
        )
        */
    }

    private fun setupClickListeners() {
        // Station selection
        stationSelectionCard.setOnClickListener { selectStation() }

        changeStationButton.setOnClickListener { selectStation() }

        // Date/Time selection
        selectDateTimeButton.setOnClickListener { showDateTimePicker() }

        dateTimeInput.setOnClickListener { showDateTimePicker() }

        // Create booking
        createBookingButton.setOnClickListener { createBooking() }
    }

    private fun handleIntentData() {
        intent.getStringExtra(EXTRA_STATION_ID)?.let { _ ->
            // Load station by ID instead of passing the full object
            // For now, we'll handle this in the station selection flow
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
                        true // 24-hour format
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
            val estimatedCost = calculateEstimatedCost(station.costPerKWh, selectedDurationMinutes)
            estimatedCostText.text = "Rs. %.2f".format(estimatedCost)

            val costPerHour = station.costPerKWh
            val hours = selectedDurationMinutes / 60.0

            costDetailsText.text = "Rs. %.2f/hour × %.1f hours".format(costPerHour, hours)
        }
                ?: run {
                    estimatedCostText.text = "Select station first"
                    costDetailsText.text = "Cost will be calculated after station selection"
                }
    }

    private fun calculateEstimatedCost(ratePerHour: Double, durationMinutes: Int): Double {
        val hours = durationMinutes / 60.0
        return ratePerHour * hours
    }

    private fun setSelectedStation(station: ChargingStation) {
        selectedStation = station
        selectedStationText.text = station.name
        selectedStationText.visibility = View.VISIBLE
        changeStationButton.text = "Change Station"

        updateCostEstimate()
        validateForm()
    }

    private fun createBooking() {
        if (!validateForm()) {
            return
        }

        val station = selectedStation!!
        val reservationDateTime = formatDateTimeForAPI(selectedDateTime.time)

        // Show loading state
        showLoading(true)
        createBookingButton.isEnabled = false

        // Make direct API call
        lifecycleScope.launch {
            try {
                val userNIC = preferencesManager.getUserNIC()
                if (userNIC.isNullOrEmpty()) {
                    showToast("User not logged in")
                    showLoading(false)
                    createBookingButton.isEnabled = true
                    return@launch
                }

                val bookingRequest =
                        CreateBookingRequest(
                                evOwnerNIC = userNIC,
                                chargingStationId = station.id,
                                reservationDateTime = reservationDateTime,
                                durationMinutes = selectedDurationMinutes
                        )

                val response = apiService.createBooking(bookingRequest)

                showLoading(false)
                createBookingButton.isEnabled = true

                if (response.isSuccessful) {
                    val booking = response.body()
                    if (booking != null) {
                        showBookingCreatedDialog(booking)
                    } else {
                        showToast("Booking created but response was empty")
                    }
                } else {
                    showToast("Failed to create booking: ${response.message()}")
                }
            } catch (e: Exception) {
                showLoading(false)
                createBookingButton.isEnabled = true
                showToast("Error creating booking: ${e.message}")
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate station selection
        if (selectedStation == null) {
            showToast("Please select a charging station")
            isValid = false
        }

        // Validate date/time selection
        val now = Calendar.getInstance()
        if (selectedDateTime.before(now)) {
            showToast("Please select a future date and time")
            isValid = false
        }

        // Validate duration
        if (selectedDurationMinutes < MIN_DURATION_MINUTES ||
                        selectedDurationMinutes > MAX_DURATION_MINUTES
        ) {
            showToast(
                    "Duration must be between $MIN_DURATION_MINUTES and $MAX_DURATION_MINUTES minutes"
            )
            isValid = false
        }

        createBookingButton.isEnabled = isValid
        return isValid
    }

    private fun formatDateTimeForAPI(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(date)
    }

    private fun showBookingCreatedDialog(booking: Booking) {
        androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Booking Created Successfully")
                .setMessage(
                        "Your booking has been created and is pending approval.\n\n" +
                                "Booking ID: ${booking.id}\n" +
                                "Station: ${booking.chargingStationName}\n" +
                                "Date/Time: ${formatDisplayDateTime(booking.reservationDateTime)}\n" +
                                "Duration: ${formatDuration(booking.durationMinutes)}\n" +
                                "Estimated Cost: Rs. %.2f".format(booking.totalAmount)
                )
                .setPositiveButton("View Booking") { _, _ ->
                    val intent = Intent(this, BookingDetailsActivity::class.java)
                    intent.putExtra("booking_id", booking.id)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Create Another") { _, _ -> resetForm() }
                .setCancelable(false)
                .show()
    }

    private fun formatDisplayDateTime(dateTimeString: String): String {
        return try {
            val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val displayFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            val date = apiFormat.parse(dateTimeString)
            displayFormat.format(date!!)
        } catch (e: Exception) {
            dateTimeString
        }
    }

    private fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    private fun resetForm() {
        selectedStation = null
        selectedStationText.text = "Select a charging station"
        changeStationButton.text = "Select Station"

        selectedDateTime = Calendar.getInstance()
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1)
        selectedDurationMinutes = DEFAULT_DURATION_MINUTES

        updateDateTime()
        updateDuration()
        updateCostEstimate()
        validateForm()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        createBookingButton.text = if (show) "Creating Booking..." else "Create Booking"
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
        // Show loading state
        selectedStationText.text = "Loading station..."

        lifecycleScope.launch {
            try {
                val response = apiService.getStationById(stationId)

                if (response.isSuccessful) {
                    val station = response.body()
                    if (station != null) {
                        setSelectedStation(station)
                    } else {
                        // Fallback if station details aren't available
                        showToast("Station selected: ${stationName ?: "Unknown"}")
                        selectedStationText.text = stationName ?: "Selected Station"
                        // Create a simple station object for now
                        selectedStation =
                                ChargingStation(
                                        id = stationId,
                                        name = stationName ?: "Selected Station",
                                        location = Location(),
                                        description = "",
                                        chargingRate = 25.0 // Default rate
                                )
                        updateCostEstimate()
                        validateForm()
                    }
                } else {
                    showToast("Failed to load station details")
                    selectedStationText.text = stationName ?: "Selected Station"
                }
            } catch (e: Exception) {
                showToast("Error loading station: ${e.message}")
                selectedStationText.text = stationName ?: "Selected Station"
            }
        }
    }
}

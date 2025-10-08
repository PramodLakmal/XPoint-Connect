/**
 * BookingDetailsActivity.kt
 *
 * Purpose: Activity for displaying booking details and managing booking operations Author: XPoint
 * Connect Development Team Date: September 28, 2025
 *
 * Description: This activity shows comprehensive booking information and provides options for
 * booking management including modification, cancellation, and QR code generation for approved
 * bookings. It integrates with the ReservationManager for complete booking lifecycle operations.
 */
package com.xpoint.connect.ui.booking

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.xpoint.connect.R
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.api.ApiService
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.model.BookingStatus
import com.xpoint.connect.data.model.CancelBookingRequest
import com.xpoint.connect.utils.QRCodeGenerator
import com.xpoint.connect.utils.showToast
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class BookingDetailsActivity : AppCompatActivity() {

    // API Service for direct calls
    private lateinit var apiService: ApiService
    private lateinit var qrCodeGenerator: QRCodeGenerator

    // UI Components
    private lateinit var bookingIdText: TextView
    private lateinit var stationNameText: TextView
    private lateinit var stationLocationText: TextView
    private lateinit var reservationDateTimeText: TextView
    private lateinit var durationText: TextView
    private lateinit var statusChip: Chip
    private lateinit var totalAmountText: TextView
    private lateinit var bookingDateText: TextView
    private lateinit var checkInTimeText: TextView
    private lateinit var checkOutTimeText: TextView
    private lateinit var cancellationReasonText: TextView
    private lateinit var operatorNotesText: TextView

    private lateinit var qrCodeCard: MaterialCardView
    private lateinit var qrCodeImage: ImageView
    private lateinit var generateQRButton: MaterialButton
    private lateinit var shareQRButton: MaterialButton

    private lateinit var modifyBookingButton: MaterialButton
    private lateinit var cancelBookingButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var currentBooking: Booking? = null
    private var bookingId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)

        // Initialize API service for direct calls
        apiService = ApiClient.apiService
        qrCodeGenerator = QRCodeGenerator()

        setupActionBar()
        initializeViews()
        setupObservers()
        setupClickListeners()
        handleIntentData()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Booking Details"
        }
    }

    private fun initializeViews() {
        // Booking information
        bookingIdText = findViewById(R.id.bookingIdText)
        stationNameText = findViewById(R.id.stationNameText)
        stationLocationText = findViewById(R.id.stationLocationText)
        reservationDateTimeText = findViewById(R.id.reservationDateTimeText)
        durationText = findViewById(R.id.durationText)
        statusChip = findViewById(R.id.statusChip)
        totalAmountText = findViewById(R.id.totalAmountText)
        bookingDateText = findViewById(R.id.bookingDateText)
        checkInTimeText = findViewById(R.id.checkInTimeText)
        checkOutTimeText = findViewById(R.id.checkOutTimeText)
        cancellationReasonText = findViewById(R.id.cancellationReasonText)
        operatorNotesText = findViewById(R.id.operatorNotesText)

        // QR Code section
        qrCodeCard = findViewById(R.id.qrCodeCard)
        qrCodeImage = findViewById(R.id.qrCodeImage)
        generateQRButton = findViewById(R.id.generateQRButton)
        shareQRButton = findViewById(R.id.shareQRButton)

        // Action buttons
        modifyBookingButton = findViewById(R.id.modifyBookingButton)
        cancelBookingButton = findViewById(R.id.cancelBookingButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupObservers() {
        // No longer using ViewModel observers - using direct API calls
        /*
        reservationViewModel.currentBooking.observe(
                this,
                Observer { booking ->
                    booking?.let {
                        currentBooking = it
                        updateUI(it)
                    }
                }
        )

        reservationViewModel.qrCodeState.observe(
                this,
                Observer { result ->
                    when (result) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }
                        is Resource.Success -> {
                            showLoading(false)
                            displayQRCode(result.data!!)
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            showToast(result.message ?: "Failed to generate QR code")
                        }
                    }
                }
        )

        reservationViewModel.modifyReservationState.observe(
                this,
                Observer { result ->
                    when (result) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }
                        is Resource.Success -> {
                            showLoading(false)
                            showToast("Booking modified successfully")
                            updateUI(result.data!!)
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            showToast(result.message ?: "Failed to modify booking")
                        }
                    }
                }
        )

        reservationViewModel.cancelReservationState.observe(
                this,
                Observer { result ->
                    when (result) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }
                        is Resource.Success -> {
                            showLoading(false)
                            showToast("Booking cancelled successfully")
                            finish()
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            showToast(result.message ?: "Failed to cancel booking")
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
        generateQRButton.setOnClickListener { generateQRCode() }

        shareQRButton.setOnClickListener { shareQRCode() }

        modifyBookingButton.setOnClickListener { modifyBooking() }

        cancelBookingButton.setOnClickListener { showCancelBookingDialog() }

        qrCodeImage.setOnClickListener { showFullScreenQRCode() }
    }

    private fun handleIntentData() {
        bookingId = intent.getStringExtra("booking_id") ?: ""
        if (bookingId.isNotEmpty()) {
            loadBookingDetails(bookingId)
        }

        // Handle booking passed as JSON string
        intent.getStringExtra("booking_json")?.let { _ ->
            // Parse JSON to Booking object if needed
            // For now, we'll rely on the booking ID approach
        }
    }

    private fun loadBookingDetails(id: String) {
        // Show loading state
        showLoading(true)

        // Make direct API call to load booking details
        lifecycleScope.launch {
            try {
                val response = apiService.getBookingById(id)

                showLoading(false)

                if (response.isSuccessful) {
                    val booking = response.body()
                    if (booking != null) {
                        currentBooking = booking
                        updateUI(booking)

                        // Auto-generate and display QR code if booking is approved
                        if (booking.bookingStatus == BookingStatus.Approved) {
                            handleApprovedBookingQR(booking)
                            showApprovalMessage()
                        }
                    } else {
                        showToast("Booking not found")
                        finish()
                    }
                } else {
                    showToast("Failed to load booking: ${response.message()}")
                    finish()
                }
            } catch (e: Exception) {
                showLoading(false)
                showToast("Error loading booking: ${e.message}")
                finish()
            }
        }
    }

    private fun generateQRCode(booking: Booking) {
        try {
            val qrCodeData =
                    "BOOKING:${booking.id}:${booking.chargingStationId}:${booking.evOwnerNIC}"
            val qrCodeBitmap = qrCodeGenerator.generateQRCode(qrCodeData)

            // Set QR code image bitmap
            qrCodeImage.setImageBitmap(qrCodeBitmap)
            qrCodeImage.visibility = View.VISIBLE

            // Show QR code card
            qrCodeCard.visibility = View.VISIBLE
        } catch (e: Exception) {
            showToast("Failed to generate QR code: ${e.message}")
        }
    }

    private fun showLoading(show: Boolean) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        showToast(message)
    }

    private fun updateUI(booking: Booking) {
        // Update booking information
        bookingIdText.text = "ID: ${booking.id}"
        stationNameText.text = booking.chargingStationName
        stationLocationText.text = "Charging Station" // Could be enhanced with actual location
        reservationDateTimeText.text = formatDisplayDateTime(booking.reservationDateTime)
        durationText.text = formatDuration(booking.durationMinutes)
        totalAmountText.text = "Rs. %.2f".format(booking.totalAmount)

        // Update status
        updateStatusChip(booking.bookingStatus)

        // Update timestamps
        bookingDateText.text = formatDisplayDateTime(booking.bookingDate)

        booking.checkInTime?.let {
            checkInTimeText.text = "Check-in: ${formatDisplayDateTime(it)}"
            checkInTimeText.visibility = View.VISIBLE
        }
                ?: run { checkInTimeText.visibility = View.GONE }

        booking.checkOutTime?.let {
            checkOutTimeText.text = "Check-out: ${formatDisplayDateTime(it)}"
            checkOutTimeText.visibility = View.VISIBLE
        }
                ?: run { checkOutTimeText.visibility = View.GONE }

        booking.cancellationReason?.let {
            cancellationReasonText.text = "Cancellation Reason: $it"
            cancellationReasonText.visibility = View.VISIBLE
        }
                ?: run { cancellationReasonText.visibility = View.GONE }

        booking.operatorNotes?.let {
            operatorNotesText.text = "Operator Notes: $it"
            operatorNotesText.visibility = View.VISIBLE
        }
                ?: run { operatorNotesText.visibility = View.GONE }

        // Update action buttons
        updateActionButtons(booking)

        // Show QR code if available
        if (booking.qrCode.isNotEmpty()) {
            displayQRCode(booking.qrCode)
        }
    }

    private fun updateStatusChip(status: BookingStatus) {
        statusChip.text =
                when (status) {
                    BookingStatus.Pending -> "Pending"
                    BookingStatus.Approved -> "Approved"
                    BookingStatus.CheckedIn -> "Checked In"
                    BookingStatus.Completed -> "Completed"
                    BookingStatus.Cancelled -> "Cancelled"
                    BookingStatus.NoShow -> "No Show"
                }

        val colorRes =
                when (status) {
                    BookingStatus.Pending -> R.color.status_pending
                    BookingStatus.Approved -> R.color.status_approved
                    BookingStatus.CheckedIn -> R.color.status_checked_in
                    BookingStatus.Completed -> R.color.status_completed
                    BookingStatus.Cancelled -> R.color.status_cancelled
                    BookingStatus.NoShow -> R.color.status_no_show
                }

        statusChip.setChipBackgroundColorResource(colorRes)
    }

    private fun updateActionButtons(booking: Booking) {
        // Show/hide QR code section - only show for approved bookings
        val isApproved = booking.bookingStatus == BookingStatus.Approved
        qrCodeCard.visibility = if (isApproved) View.VISIBLE else View.GONE

        // Update QR code button text based on booking status and QR availability
        if (isApproved) {
            generateQRButton.text =
                    if (booking.qrCode.isNotEmpty()) {
                        "View QR Code"
                    } else {
                        "Generate QR Code"
                    }
            generateQRButton.isEnabled = true
            generateQRButton.alpha = 1.0f
        } else {
            generateQRButton.text = "QR Code Unavailable"
            generateQRButton.isEnabled = false
            generateQRButton.alpha = 0.5f
        }

        // Update modify button - can only edit pending bookings
        val canModify = booking.bookingStatus == BookingStatus.Pending
        modifyBookingButton.isEnabled = canModify
        modifyBookingButton.alpha = if (canModify) 1.0f else 0.5f
        modifyBookingButton.text = if (canModify) "Edit Booking" else "Cannot Edit"

        // Update cancel button - can cancel pending and approved bookings
        val canCancel =
                booking.bookingStatus == BookingStatus.Pending ||
                        booking.bookingStatus == BookingStatus.Approved
        cancelBookingButton.isEnabled = canCancel
        cancelBookingButton.alpha = if (canCancel) 1.0f else 0.5f
        cancelBookingButton.text = if (canCancel) "Cancel Booking" else "Cannot Cancel"
    }

    /**
     * Handles QR code generation for approved bookings Uses server-generated QR code if available,
     * otherwise generates locally
     */
    private fun handleApprovedBookingQR(booking: Booking) {
        if (booking.qrCode.isNotEmpty()) {
            // Use server-generated QR code
            displayServerQRCode(booking.qrCode)
        } else {
            // Generate local QR code as fallback
            generateLocalQRCode(booking)
        }
    }

    /** Manual QR code generation triggered by user */
    private fun generateQRCode() {
        currentBooking?.let { booking ->
            if (booking.bookingStatus == BookingStatus.Approved) {
                // Use server QR code if available, otherwise generate locally
                handleApprovedBookingQR(booking)
            } else {
                showToast("QR code can only be generated for approved bookings")
            }
        }
    }

    /** Displays server-generated QR code from the booking */
    private fun displayServerQRCode(qrCodeData: String) {
        try {
            // Server QR code is base64 encoded, decode and display
            val decodedQRData =
                    String(android.util.Base64.decode(qrCodeData, android.util.Base64.DEFAULT))
            val bitmap = qrCodeGenerator.generateQRCode(decodedQRData, 512)

            bitmap?.let {
                qrCodeImage.setImageBitmap(it)
                qrCodeImage.visibility = View.VISIBLE
                shareQRButton.visibility = View.VISIBLE
                generateQRButton.text = "Refresh QR Code"
                qrCodeCard.visibility = View.VISIBLE

                showToast("QR Code ready for scanning")
            }
                    ?: run {
                        // Fallback to local generation
                        currentBooking?.let { generateLocalQRCode(it) }
                    }
        } catch (e: Exception) {
            // If server QR code is invalid, generate local one
            currentBooking?.let { generateLocalQRCode(it) }
        }
    }

    /** Generates QR code locally for approved booking */
    private fun generateLocalQRCode(booking: Booking) {
        val qrCodeData =
                "XPOINT_BOOKING:${booking.id}:${booking.evOwnerNIC}:${booking.chargingStationId}:${System.currentTimeMillis()}"
        val bitmap = qrCodeGenerator.generateQRCode(qrCodeData, 512)

        bitmap?.let {
            displayQRCode(qrCodeData)
            showToast("QR Code generated successfully")
        }
                ?: run { showToast("Failed to generate QR code") }
    }

    private fun displayQRCode(qrCodeData: String) {
        val bitmap =
                if (qrCodeData.startsWith("data:image") || qrCodeData.length > 1000) {
                    // It's a base64 encoded image
                    qrCodeGenerator.base64ToBitmap(qrCodeData)
                } else {
                    // It's raw data, generate QR code
                    qrCodeGenerator.generateQRCode(qrCodeData, 512)
                }

        bitmap?.let {
            qrCodeImage.setImageBitmap(it)
            qrCodeImage.visibility = View.VISIBLE
            shareQRButton.visibility = View.VISIBLE
            generateQRButton.text = "Refresh QR Code"
        }
    }

    private fun shareQRCode() {
        currentBooking?.let { booking ->
            if (booking.bookingStatus == BookingStatus.Approved && qrCodeImage.drawable != null) {
                try {
                    // Create share intent with QR code image and booking details
                    val shareText =
                            """
                        XPoint Connect - Booking QR Code
                        
                        Booking ID: ${booking.id}
                        Station: ${booking.chargingStationName}
                        Date: ${booking.reservationDateTime}
                        Duration: ${booking.durationMinutes} minutes
                        
                        Use this QR code for easy check-in at the charging station.
                        
                        Generated by XPoint Connect Mobile App
                    """.trimIndent()

                    val shareIntent =
                            Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(Intent.EXTRA_SUBJECT, "XPoint Connect - Booking QR Code")
                            }

                    startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                } catch (e: Exception) {
                    showToast("Failed to share QR code: ${e.message}")
                }
            } else {
                showToast("QR code not available for sharing")
            }
        }
    }

    private fun showFullScreenQRCode() {
        currentBooking?.let { booking ->
            if (booking.bookingStatus == BookingStatus.Approved && qrCodeImage.drawable != null) {
                // Create dialog with full-screen QR code
                val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                dialog.setContentView(R.layout.dialog_fullscreen_qr)

                val fullscreenQR = dialog.findViewById<ImageView>(R.id.fullscreen_qr_image)
                val closeButton = dialog.findViewById<ImageButton>(R.id.close_button)
                val bookingInfo = dialog.findViewById<TextView>(R.id.booking_info_text)

                // Set the same QR code bitmap
                fullscreenQR.setImageDrawable(qrCodeImage.drawable)

                // Set booking information
                bookingInfo.text =
                        """
                    Booking ID: ${booking.id}
                    Station: ${booking.chargingStationName}
                    Time: ${booking.reservationDateTime}
                    Duration: ${booking.durationMinutes} minutes
                """.trimIndent()

                closeButton.setOnClickListener { dialog.dismiss() }

                dialog.show()
            } else {
                showToast("QR code not available")
            }
        }
    }

    private fun modifyBooking() {
        currentBooking?.let { booking ->
            // Only allow editing of pending bookings
            if (booking.bookingStatus == BookingStatus.Pending) {
                val intent = Intent(this, EditBookingActivity::class.java)
                intent.putExtra("booking_id", booking.id)
                startActivity(intent)
            } else {
                showToast("Only pending bookings can be edited")
            }
        }
    }

    private fun showCancelBookingDialog() {
        val reasons =
                arrayOf(
                        "Change of plans",
                        "Found closer station",
                        "No longer needed",
                        "Technical issues",
                        "Other"
                )

        AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setItems(reasons) { _, which ->
                    val selectedReason = reasons[which]
                    if (selectedReason == "Other") {
                        showCustomReasonDialog()
                    } else {
                        cancelBooking(selectedReason)
                    }
                }
                .setNegativeButton("Back", null)
                .show()
    }

    private fun showCustomReasonDialog() {
        val editText = EditText(this)
        editText.hint = "Enter cancellation reason"

        AlertDialog.Builder(this)
                .setTitle("Cancellation Reason")
                .setView(editText)
                .setPositiveButton("Cancel Booking") { _, _ ->
                    val reason = editText.text.toString().trim()
                    if (reason.isNotEmpty()) {
                        cancelBooking(reason)
                    } else {
                        showToast("Please enter a cancellation reason")
                    }
                }
                .setNegativeButton("Back", null)
                .show()
    }

    private fun cancelBooking(reason: String) {
        currentBooking?.let { booking ->
            // Show loading state
            showLoading(true)

            lifecycleScope.launch {
                try {
                    val cancelRequest = CancelBookingRequest(cancellationReason = reason)
                    val response = apiService.cancelBooking(booking.id, cancelRequest)

                    showLoading(false)

                    if (response.isSuccessful) {
                        showToast("Booking cancelled successfully")
                        // Reload booking details to show updated status
                        loadBookingDetails(booking.id)
                    } else {
                        showToast("Failed to cancel booking: ${response.message()}")
                    }
                } catch (e: Exception) {
                    showLoading(false)
                    showToast("Error cancelling booking: ${e.message}")
                }
            }
        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_booking_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_refresh -> {
                currentBooking?.let { loadBookingDetails(it.id) }
                true
            }
            R.id.action_share -> {
                shareBookingDetails()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareBookingDetails() {
        currentBooking?.let { booking ->
            val shareText =
                    """
                XPoint Connect Booking Details
                
                Booking ID: ${booking.id}
                Station: ${booking.chargingStationName}
                Date/Time: ${formatDisplayDateTime(booking.reservationDateTime)}
                Duration: ${formatDuration(booking.durationMinutes)}
                Status: ${booking.bookingStatus}
                Amount: Rs. %.2f
                
                Download XPoint Connect app for easy EV charging!
            """
                            .trimIndent()
                            .format(booking.totalAmount)

            val shareIntent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "XPoint Connect Booking - ${booking.id}")
                    }

            startActivity(Intent.createChooser(shareIntent, "Share Booking Details"))
        }
    }

    /** Shows approval message when booking is approved */
    private fun showApprovalMessage() {
        val message =
                "🎉 Great news! Your booking has been approved!\n\n" +
                        "✅ QR code is now available for easy check-in\n" +
                        "📱 Present the QR code at the charging station\n" +
                        "⚡ Start charging when you arrive"

        showToast(message)
    }
}

package com.xpoint.connect.ui.operator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.xpoint.connect.R
import com.xpoint.connect.utils.showToast

class ScannerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPECTED_BOOKING_ID = "EXPECTED_BOOKING_ID"
        const val RESULT_SCANNED_BOOKING_ID = "SCANNED_BOOKING_ID"
    }

    private lateinit var barcodeView: DecoratedBarcodeView
    private var capture: CaptureManager? = null
    private var expectedBookingId: String? = null

    private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isGranted: Boolean ->
                if (isGranted) startScanning()
                else {
                    showToast("Camera permission is required to scan QR codes")
                    finish()
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_zxing)

        // Setup toolbar with back button
        setupToolbar()

        barcodeView = findViewById(R.id.zxing_barcode_scanner)
        
        // Get expected booking ID if provided
        expectedBookingId = intent.getStringExtra(EXTRA_EXPECTED_BOOKING_ID)
        
        checkPermissionAndStart(savedInstanceState)
    }

    private fun checkPermissionAndStart(savedInstanceState: Bundle?) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> startScanning(savedInstanceState)
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanning(savedInstanceState: Bundle? = null) {
        capture = CaptureManager(this, barcodeView)
        capture?.initializeFromIntent(intent, savedInstanceState)
        capture?.decode()
        barcodeView.decodeContinuous { result ->
            // Handle decoded text here
            val scannedText = result.text ?: return@decodeContinuous
            
            // Extract booking ID from QR code (assuming QR contains booking ID)
            val scannedBookingId = extractBookingIdFromQR(scannedText)
            
            if (scannedBookingId != null) {
                // Return the scanned booking ID
                val resultIntent = Intent().apply {
                    putExtra(RESULT_SCANNED_BOOKING_ID, scannedBookingId)
                }
                setResult(RESULT_OK, resultIntent)
                showToast("QR code scanned successfully!")
            } else {
                showToast("Invalid QR code format")
                setResult(RESULT_CANCELED)
            }
            
            finish()
        }
    }

    private fun extractBookingIdFromQR(qrText: String): String? {
        // Implement QR code parsing logic here
        // For now, assume the QR code contains the booking ID directly
        // You might need to parse JSON or extract from a specific format
        
        return try {
            // If QR contains JSON, parse it
            // If QR contains just the booking ID, return it directly
            // For this implementation, assume QR contains booking ID directly
            if (qrText.isNotBlank() && qrText.length >= 8) {
                qrText.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onDestroy() {
        capture?.onDestroy()
        super.onDestroy()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "QR Code Scanner"
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

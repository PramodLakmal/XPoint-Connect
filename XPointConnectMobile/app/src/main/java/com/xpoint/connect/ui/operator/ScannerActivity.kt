package com.xpoint.connect.ui.operator

import android.Manifest
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

    private lateinit var barcodeView: DecoratedBarcodeView
    private var capture: CaptureManager? = null

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
            val text = result.text ?: return@decodeContinuous
            showToast("Scanned: $text")
            setResult(RESULT_OK, android.content.Intent().putExtra("qr_result", text))
            finish()
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

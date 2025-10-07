/**
 * NearbyStationsActivity.kt
 *
 * Purpose: Activity to display nearby charging stations on a Google Map
 * Author: XPoint Connect Development Team
 * Date: October 7, 2025
 *
 * Description: This activity shows nearby charging stations on an interactive map,
 * allowing users to view station locations, details, and availability in their vicinity.
 * It integrates with Google Maps SDK and location services to provide a comprehensive
 * map-based station discovery experience.
 *
 * Key Features:
 * - Interactive Google Map with station markers
 * - Current location detection and display
 * - Station information popup on marker click
 * - Distance calculation from user location
 * - Real-time data from API integration
 * - Navigation integration for directions
 */
package com.xpoint.connect.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.xpoint.connect.R
import com.xpoint.connect.data.model.ChargingStation
import com.xpoint.connect.data.repository.StationRepository
import com.xpoint.connect.ui.booking.CreateBookingActivity
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.launch

class NearbyStationsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var stationRepository: StationRepository
    private lateinit var progressBar: ProgressBar
    private lateinit var fabStationList: FloatingActionButton
    private lateinit var searchEditText: EditText
    private lateinit var clearSearchButton: ImageView
    private var userLocation: LatLng? = null
    private val nearbyStations = mutableListOf<ChargingStation>()
    private var allStations = mutableListOf<ChargingStation>()
    // Sri Lanka center coordinates and bounds
    private val sriLankaCenter = LatLng(7.8731, 80.7718) // Center of Sri Lanka
    private val defaultLocation = LatLng(6.9271, 79.8612) // Colombo, Sri Lanka

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                enableMyLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                enableMyLocation()
            }
            else -> {
                // No location access granted.
                Toast.makeText(this, "Location permission required to show nearby stations", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_stations)

        // Initialize components
        initializeComponents()
        
        // Setup toolbar
        setupToolbar()
        
        // Initialize map
        initializeMap()
        
        // Request location permission
        requestLocationPermission()
    }
    
    private fun initializeComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        stationRepository = StationRepository()
        progressBar = findViewById(R.id.progressBar)
        fabStationList = findViewById(R.id.fabStationList)
        searchEditText = findViewById(R.id.etSearch)
        clearSearchButton = findViewById(R.id.ivClearSearch)
        
        // Setup search functionality
        setupSearchFunctionality()
        
        // Setup FAB click listener for future station list view
        fabStationList.setOnClickListener {
            showStationsInList()
        }
    }
    
    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    clearSearchButton.visibility = View.GONE
                    // Show all stations
                    nearbyStations.clear()
                    nearbyStations.addAll(allStations)
                } else {
                    clearSearchButton.visibility = View.VISIBLE
                    // Filter stations based on search query
                    filterStations(query)
                }
                displayStationsOnMap()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        clearSearchButton.setOnClickListener {
            searchEditText.text.clear()
        }
    }
    
    private fun filterStations(query: String) {
        val filteredStations = allStations.filter { station ->
            station.name.contains(query, ignoreCase = true) ||
            station.location.address.contains(query, ignoreCase = true) ||
            station.location.city.contains(query, ignoreCase = true) ||
            station.description.contains(query, ignoreCase = true) ||
            station.amenities.any { amenity -> amenity.contains(query, ignoreCase = true) }
        }
        
        nearbyStations.clear()
        nearbyStations.addAll(filteredStations)
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Nearby Charging Stations"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configure map settings
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
        }

        // Set map style
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Set initial camera position to Sri Lanka
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(sriLankaCenter, 7f) // Zoom level 7 shows most of Sri Lanka
        )

        // Enable location if permission is granted
        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            // If no location permission, load stations around default location
            loadNearbyStations()
        }
    }

    private fun requestLocationPermission() {
        if (!hasLocationPermission()) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (!hasLocationPermission()) {
            return
        }

        try {
            googleMap.isMyLocationEnabled = true
            getCurrentLocation()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun getCurrentLocation() {
        if (!hasLocationPermission()) {
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                    
                    // Move camera to user location
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
                    )
                    
                    // Load nearby stations relative to user location
                    loadNearbyStations()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun loadNearbyStations() {
        val location = userLocation ?: defaultLocation
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val result = stationRepository.getNearbyStations(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radiusKm = 15.0 // 15km radius
                )
                
                when (result) {
                    is Resource.Success -> {
                        allStations.clear()
                        nearbyStations.clear()
                        result.data?.let { stations ->
                            allStations.addAll(stations)
                            nearbyStations.addAll(stations)
                            runOnUiThread {
                                displayStationsOnMap()
                                showLoading(false)
                            }
                        } ?: run {
                            showError("No stations found nearby")
                        }
                    }
                    is Resource.Error -> {
                        showError(result.message ?: "Failed to load nearby stations")
                    }
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                }
            } catch (e: Exception) {
                showError("Error loading stations: ${e.message}")
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
        }
    }
    
    private fun showError(message: String) {
        runOnUiThread {
            showLoading(false)
            Toast.makeText(this@NearbyStationsActivity, message, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showStationsInList() {
        // TODO: Implement bottom sheet or dialog with station list
        Toast.makeText(this, "Station list view - Coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun displayStationsOnMap() {
        googleMap.clear() // Clear existing markers
        
        if (nearbyStations.isEmpty()) {
            Toast.makeText(this, "No nearby charging stations found", Toast.LENGTH_SHORT).show()
            return
        }
        
        nearbyStations.forEach { station ->
            val position = LatLng(station.location.latitude, station.location.longitude)
            
            // Choose marker color based on availability and status
            val markerColor = when {
                !station.isActive -> BitmapDescriptorFactory.HUE_AZURE  // Light blue-gray for inactive
                station.availableSlots == 0 -> BitmapDescriptorFactory.HUE_RED
                station.availableSlots <= 2 -> BitmapDescriptorFactory.HUE_ORANGE
                else -> BitmapDescriptorFactory.HUE_GREEN
            }
            
            // Create station type indicator
            val stationType = if (station.type == 1) "DC Fast" else "AC Standard"
            val availabilityText = if (station.isActive) {
                "Available: ${station.availableSlots}/${station.totalSlots}"
            } else {
                "Inactive"
            }
            
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(station.name)
                    .snippet("$stationType • $availabilityText • ${String.format("%.1f", station.distance ?: 0.0)} km")
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )
            
            marker?.tag = station
        }

        // Set info window click listener for station details
        googleMap.setOnInfoWindowClickListener { marker ->
            val station = marker.tag as? ChargingStation
            station?.let {
                showStationDetails(it)
            }
        }
        
        // If we have user location, adjust camera to show all stations
        userLocation?.let { userPos ->
            if (nearbyStations.isNotEmpty()) {
                val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                bounds.include(userPos)
                nearbyStations.forEach { station ->
                    bounds.include(LatLng(station.location.latitude, station.location.longitude))
                }
                
                val boundsToShow = bounds.build()
                val padding = 100 // padding in pixels
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(boundsToShow, padding)
                )
            }
        }
    }

    private fun showStationDetails(station: ChargingStation) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_station_details, null)
        bottomSheetDialog.setContentView(view)

        // Set station information
        view.findViewById<TextView>(R.id.tvStationName).text = station.name
        view.findViewById<TextView>(R.id.tvStationAddress).text = "${station.location.address}, ${station.location.city}"
        
        // Set status
        val statusTextView = view.findViewById<TextView>(R.id.tvStationStatus)
        if (station.isActive) {
            statusTextView.text = "Active"
            statusTextView.setBackgroundResource(R.drawable.badge_success)
        } else {
            statusTextView.text = "Inactive"
            statusTextView.setBackgroundResource(R.drawable.badge_danger)
        }
        
        // Set distance
        station.distance?.let { distance ->
            view.findViewById<TextView>(R.id.tvDistance).text = "${String.format("%.1f", distance)} km away"
        }
        
        // Set charging type
        val chargingType = if (station.type == 1) "DC Fast" else "AC Standard"
        view.findViewById<TextView>(R.id.tvChargingType).text = chargingType
        
        // Set availability
        view.findViewById<TextView>(R.id.tvAvailability).text = "${station.availableSlots}/${station.totalSlots} Available"
        
        // Set charging rate
        view.findViewById<TextView>(R.id.tvChargingRate).text = "Rs. ${station.chargingRate}/kWh"
        
        // Set description
        if (station.description.isNotEmpty()) {
            view.findViewById<View>(R.id.layoutDescription).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tvDescription).text = station.description
        }
        
        // Set amenities
        if (station.amenities.isNotEmpty()) {
            view.findViewById<View>(R.id.layoutAmenities).visibility = View.VISIBLE
            val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupAmenities)
            station.amenities.forEach { amenity ->
                val chip = Chip(this)
                chip.text = amenity
                chip.isClickable = false
                chipGroup.addView(chip)
            }
        }
        
        // Set button actions
        val btnBook = view.findViewById<MaterialButton>(R.id.btnBookStation)
        val btnDirections = view.findViewById<MaterialButton>(R.id.btnDirections)
        
        // Enable/disable book button based on availability
        if (station.isActive && station.availableSlots > 0) {
            btnBook.isEnabled = true
            btnBook.setOnClickListener {
                bottomSheetDialog.dismiss()
                // Navigate to booking creation with pre-selected station
                val intent = Intent(this, CreateBookingActivity::class.java).apply {
                    putExtra("selected_station_id", station.id)
                    putExtra("selected_station_name", station.name)
                }
                startActivity(intent)
            }
        } else {
            btnBook.isEnabled = false
            btnBook.text = if (!station.isActive) "Station Inactive" else "No Slots Available"
        }
        
        // Directions button
        btnDirections.setOnClickListener {
            bottomSheetDialog.dismiss()
            openDirections(station.location.latitude, station.location.longitude)
        }
        
        bottomSheetDialog.show()
    }
    
    private fun openDirections(latitude: Double, longitude: Double) {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Fallback to browser if Google Maps is not installed
            val browserIntent = Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"))
            startActivity(browserIntent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
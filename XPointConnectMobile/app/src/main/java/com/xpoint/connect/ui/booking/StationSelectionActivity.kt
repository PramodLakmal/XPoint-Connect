/**
 * StationSelectionActivity.kt
 *
 * Purpose: Activity for selecting charging stations for booking creation Author: XPoint Connect
 * Development Team Date: September 28, 2025
 *
 * Description: This activity provides a comprehensive interface for browsing and selecting charging
 * stations. It includes search functionality, filtering, station details view, and integration with
 * the booking system. Users can view station information, availability, and pricing before making
 * their selection.
 */
package com.xpoint.connect.ui.booking

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.xpoint.connect.R
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.api.ApiService
import com.xpoint.connect.data.model.ChargingStation
import kotlinx.coroutines.launch

class StationSelectionActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    // UI Components
    private lateinit var searchInput: TextInputEditText
    private lateinit var searchButton: MaterialButton
    private lateinit var stationsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noStationsText: TextView
    private lateinit var errorCard: MaterialCardView
    private lateinit var errorText: TextView
    private lateinit var retryButton: MaterialButton

    // Data
    private lateinit var stationsAdapter: StationsAdapter
    private var allStations: List<ChargingStation> = emptyList()
    private var filteredStations: List<ChargingStation> = emptyList()

    companion object {
        const val EXTRA_SELECTED_STATION_ID = "selected_station_id"
        const val EXTRA_SELECTED_STATION_NAME = "selected_station_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_selection)

        // Setup toolbar with back button
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Charging Station"

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadStations()
    }

    private fun initializeViews() {
        apiService = ApiClient.apiService

        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        stationsRecyclerView = findViewById(R.id.stationsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        noStationsText = findViewById(R.id.noStationsText)
        errorCard = findViewById(R.id.errorCard)
        errorText = findViewById(R.id.errorText)
        retryButton = findViewById(R.id.retryButton)
    }

    private fun setupRecyclerView() {
        stationsAdapter = StationsAdapter { station -> selectStation(station) }

        stationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@StationSelectionActivity)
            adapter = stationsAdapter
        }
    }

    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            filterStations(query)
        }

        retryButton.setOnClickListener { loadStations() }

        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = searchInput.text.toString().trim()
            filterStations(query)
            true
        }
    }

    private fun loadStations() {
        showLoading(true)
        hideError()

        lifecycleScope.launch {
            try {
                println("StationSelection: Making API call to getAllStations()")
                val response = apiService.getAllStations()
                println("StationSelection: API response code: ${response.code()}")

                if (response.isSuccessful) {
                    val stations = response.body() ?: emptyList()
                    println("StationSelection: Retrieved ${stations.size} stations")
                    stations.forEach { station ->
                        println(
                                "StationSelection: Station - ID: ${station.id}, Name: ${station.name}, Active: ${station.isActive}"
                        )
                    }

                    allStations = stations
                    filteredStations = stations

                    showLoading(false)

                    if (stations.isEmpty()) {
                        showNoStations()
                    } else {
                        showStations(stations)
                    }
                } else {
                    showLoading(false)
                    val errorMessage =
                            "Failed to load stations: ${response.code()} - ${response.message()}"
                    println("StationSelection: $errorMessage")
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                showLoading(false)
                val errorMessage = "Error loading stations: ${e.message}"
                println("StationSelection: Exception - $errorMessage")
                e.printStackTrace()
                showError(errorMessage)
            }
        }
    }

    private fun filterStations(query: String) {
        filteredStations =
                if (query.isEmpty()) {
                    allStations
                } else {
                    allStations.filter { station ->
                        station.name.contains(query, ignoreCase = true) ||
                                station.location.address.contains(query, ignoreCase = true) ||
                                station.location.city.contains(query, ignoreCase = true) ||
                                station.description.contains(query, ignoreCase = true)
                    }
                }

        if (filteredStations.isEmpty()) {
            showNoStations()
        } else {
            showStations(filteredStations)
        }
    }

    private fun selectStation(station: ChargingStation) {
        val resultIntent =
                Intent().apply {
                    putExtra(EXTRA_SELECTED_STATION_ID, station.id)
                    putExtra(EXTRA_SELECTED_STATION_NAME, station.name)
                }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun showStations(stations: List<ChargingStation>) {
        stationsAdapter.updateStations(stations)
        stationsRecyclerView.visibility = View.VISIBLE
        noStationsText.visibility = View.GONE
    }

    private fun showNoStations() {
        stationsRecyclerView.visibility = View.GONE
        noStationsText.visibility = View.VISIBLE
        noStationsText.text =
                if (allStations.isEmpty()) {
                    "No charging stations available"
                } else {
                    "No stations found matching your search"
                }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        stationsRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        errorCard.visibility = View.VISIBLE
        errorText.text = message
        stationsRecyclerView.visibility = View.GONE
        noStationsText.visibility = View.GONE
    }

    private fun hideError() {
        errorCard.visibility = View.GONE
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

// Adapter for stations list
class StationsAdapter(private val onStationSelected: (ChargingStation) -> Unit) :
        RecyclerView.Adapter<StationsAdapter.StationViewHolder>() {

    private var stations: List<ChargingStation> = emptyList()

    fun updateStations(newStations: List<ChargingStation>) {
        stations = newStations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
    ): StationViewHolder {
        val view =
                android.view.LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_charging_station, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        holder.bind(stations[position])
    }

    override fun getItemCount(): Int = stations.size

    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stationCard: MaterialCardView = itemView.findViewById(R.id.stationCard)
        private val stationName: TextView = itemView.findViewById(R.id.stationName)
        private val stationLocation: TextView = itemView.findViewById(R.id.stationLocation)
        private val stationDescription: TextView = itemView.findViewById(R.id.stationDescription)
        private val costPerKWh: TextView = itemView.findViewById(R.id.costPerKWh)
        private val availabilityStatus: TextView = itemView.findViewById(R.id.availabilityStatus)
        private val selectButton: MaterialButton = itemView.findViewById(R.id.selectButton)

        fun bind(station: ChargingStation) {
            stationName.text = station.name
            stationLocation.text =
                    "${station.location.address}, ${station.location.city}".takeIf { it != ", " }
                            ?: "Location not specified"
            stationDescription.text = station.description
            costPerKWh.text = "Rs. %.2f/hour".format(station.costPerKWh)

            // Set availability status
            availabilityStatus.text =
                    if (station.isActive) {
                        "Available"
                    } else {
                        "Unavailable"
                    }

            availabilityStatus.setTextColor(
                    itemView.context.getColor(
                            if (station.isActive) R.color.success else R.color.error
                    )
            )

            // Handle station selection
            selectButton.setOnClickListener {
                if (station.isActive) {
                    onStationSelected(station)
                } else {
                    android.widget.Toast.makeText(
                                    itemView.context,
                                    "This station is currently unavailable",
                                    android.widget.Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }

            stationCard.setOnClickListener {
                if (station.isActive) {
                    onStationSelected(station)
                } else {
                    android.widget.Toast.makeText(
                                    itemView.context,
                                    "This station is currently unavailable",
                                    android.widget.Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }

            // Disable selection for non-operational stations
            selectButton.isEnabled = station.isActive
            stationCard.isEnabled = station.isActive
            stationCard.alpha = if (station.isActive) 1.0f else 0.6f
        }
    }
}

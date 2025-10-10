package com.xpoint.connect.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.xpoint.connect.R
import com.xpoint.connect.data.model.ChargingStation

class StationsAdapter(private val onStationClick: (ChargingStation) -> Unit) :
        ListAdapter<ChargingStation, StationsAdapter.StationViewHolder>(StationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_charging_station, parent, false)
        return StationViewHolder(view, onStationClick)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StationViewHolder(itemView: View, private val onStationClick: (ChargingStation) -> Unit) :
            RecyclerView.ViewHolder(itemView) {

        private val stationName: TextView = itemView.findViewById(R.id.stationName)
        private val stationLocation: TextView = itemView.findViewById(R.id.stationLocation)
        private val stationDescription: TextView = itemView.findViewById(R.id.stationDescription)
        private val costPerKWh: TextView = itemView.findViewById(R.id.costPerKWh)
        private val availabilityStatus: TextView = itemView.findViewById(R.id.availabilityStatus)
        private val availabilityStatusCard: MaterialCardView =
                itemView.findViewById(R.id.availabilityStatusCard)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)

        fun bind(station: ChargingStation) {
            stationName.text = station.name
            stationLocation.text =
                    "${station.location.address}, ${station.location.city}".takeIf { it != ", " }
                            ?: "Location not specified"
            stationDescription.text =
                    station.description.ifEmpty {
                        "Fast charging station with modern amenities and reliable service"
                    }
            costPerKWh.text = "Rs. %.2f/hour".format(station.costPerKWh)

            // Set distance if available
            if (station.distance > 0) {
                tvDistance.text = "%.1f km away".format(station.distance)
            } else {
                tvDistance.text = "Distance unknown"
            }

            // Set availability status with slot information
            updateAvailabilityStatus(station)

            itemView.setOnClickListener { onStationClick(station) }
        }

        private fun updateAvailabilityStatus(station: ChargingStation) {
            if (!station.isActive) {
                // Station is inactive
                availabilityStatus.text = "Offline"
                availabilityStatusCard.setCardBackgroundColor(
                        itemView.context.getColor(R.color.text_secondary)
                )
            } else if (station.totalSlots > 0) {
                // Show available slots
                val availableSlots = station.availableSlots
                val totalSlots = station.totalSlots

                availabilityStatus.text = "$availableSlots/$totalSlots Available"

                // Set color based on availability percentage
                val availabilityPercentage =
                        if (totalSlots > 0) {
                            (availableSlots.toFloat() / totalSlots.toFloat()) * 100
                        } else 0f

                val backgroundColor =
                        when {
                            availableSlots == 0 -> R.color.error // Full
                            availabilityPercentage <= 25 -> R.color.warning // Low availability
                            availabilityPercentage <= 50 -> R.color.secondary // Medium availability
                            else -> R.color.success // Good availability
                        }

                availabilityStatusCard.setCardBackgroundColor(
                        itemView.context.getColor(backgroundColor)
                )
            } else {
                // Fallback for stations without slot information
                availabilityStatus.text = "Available"
                availabilityStatusCard.setCardBackgroundColor(
                        itemView.context.getColor(R.color.success)
                )
            }
        }
    }

    private class StationDiffCallback : DiffUtil.ItemCallback<ChargingStation>() {
        override fun areItemsTheSame(oldItem: ChargingStation, newItem: ChargingStation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
                oldItem: ChargingStation,
                newItem: ChargingStation
        ): Boolean {
            return oldItem == newItem
        }
    }
}

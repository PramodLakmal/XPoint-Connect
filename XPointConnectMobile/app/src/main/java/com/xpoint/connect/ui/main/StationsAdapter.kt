package com.xpoint.connect.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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

            itemView.setOnClickListener { onStationClick(station) }
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

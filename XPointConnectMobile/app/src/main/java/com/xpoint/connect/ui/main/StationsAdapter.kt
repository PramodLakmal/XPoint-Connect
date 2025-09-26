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

        private val tvStationName: TextView = itemView.findViewById(R.id.tvStationName)
        private val tvStationAddress: TextView = itemView.findViewById(R.id.tvStationAddress)
        private val tvStationType: TextView = itemView.findViewById(R.id.tvStationType)
        private val tvCostPerKWh: TextView = itemView.findViewById(R.id.tvCostPerKWh)
        private val tvMaxPower: TextView = itemView.findViewById(R.id.tvMaxPower)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)

        fun bind(station: ChargingStation) {
            tvStationName.text = station.name
            tvStationAddress.text = station.location.address
            tvStationType.text = station.chargingType.name
            tvCostPerKWh.text = "Rs. ${String.format("%.2f", station.costPerKWh)}/kWh"
            tvMaxPower.text = "${station.maxPowerKW} kW"

            // For now, distance will be calculated when location is available
            tvDistance.text = "-- km"

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

package com.xpoint.connect.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xpoint.connect.R
import com.xpoint.connect.data.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class BookingsAdapter(private val onBookingClick: (Booking) -> Unit) :
        ListAdapter<Booking, BookingsAdapter.BookingViewHolder>(BookingDiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view, onBookingClick)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position), dateFormat, timeFormat)
    }

    class BookingViewHolder(itemView: View, private val onBookingClick: (Booking) -> Unit) :
            RecyclerView.ViewHolder(itemView) {

        private val tvStationName: TextView = itemView.findViewById(R.id.tvBookingStationName)
        private val tvBookingDate: TextView = itemView.findViewById(R.id.tvBookingDate)
        private val tvBookingTime: TextView = itemView.findViewById(R.id.tvBookingTime)
        private val tvBookingStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)
        private val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)

        fun bind(booking: Booking, dateFormat: SimpleDateFormat, timeFormat: SimpleDateFormat) {
            tvStationName.text = booking.stationName ?: "Unknown Station"
            tvBookingDate.text = dateFormat.format(booking.startTime)
            tvBookingTime.text =
                    "${timeFormat.format(booking.startTime)} - ${timeFormat.format(booking.endTime)}"
            tvBookingStatus.text = booking.status.name
            tvBookingId.text = "ID: ${booking.id}"

            // Set status color based on booking status
            val statusColor =
                    when (booking.status.name.lowercase()) {
                        "confirmed" -> R.color.md_theme_primary
                        "completed" -> R.color.md_theme_tertiary
                        "cancelled" -> R.color.md_theme_error
                        else -> R.color.md_theme_onSurfaceVariant
                    }
            tvBookingStatus.setTextColor(itemView.context.getColor(statusColor))

            itemView.setOnClickListener { onBookingClick(booking) }
        }
    }

    private class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem == newItem
        }
    }
}

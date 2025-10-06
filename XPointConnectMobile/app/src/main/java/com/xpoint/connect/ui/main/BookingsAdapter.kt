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
import com.xpoint.connect.data.model.BookingStatus
import java.text.SimpleDateFormat
import java.util.*

class BookingsAdapter(private val onBookingClick: (Booking) -> Unit) :
    ListAdapter<Booking, BookingsAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStationName: TextView = itemView.findViewById(R.id.tvBookingStationName)
        private val tvBookingDate: TextView = itemView.findViewById(R.id.tvBookingDate)
        private val tvBookingTime: TextView = itemView.findViewById(R.id.tvBookingTime)
        private val tvBookingStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)
        private val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)

        fun bind(booking: Booking) {
            tvStationName.text = booking.chargingStationName
            tvBookingDate.text = formatDate(booking.reservationDateTime)
            tvBookingTime.text = formatTime(booking.reservationDateTime)
            tvBookingStatus.text = when (booking.bookingStatus) {
                BookingStatus.Pending -> "Pending"
                BookingStatus.Approved -> "Approved"
                BookingStatus.CheckedIn -> "Checked In"
                BookingStatus.Completed -> "Completed"
                BookingStatus.Cancelled -> "Cancelled"
                BookingStatus.NoShow -> "No Show"
            }
            tvBookingId.text = "ID: ${booking.id.take(8)}..."

            // Set status color based on booking status
            val statusColor = when (booking.bookingStatus) {
                BookingStatus.Pending -> R.color.warning
                BookingStatus.Approved -> R.color.success
                BookingStatus.CheckedIn -> R.color.primary
                BookingStatus.Completed -> R.color.success
                BookingStatus.Cancelled -> R.color.error
                BookingStatus.NoShow -> R.color.error
            }
            
            tvBookingStatus.setTextColor(itemView.context.getColor(statusColor))
            
            // Make pending bookings more prominent
            if (booking.bookingStatus == BookingStatus.Pending) {
                itemView.alpha = 1.0f
                itemView.setBackgroundResource(R.drawable.pending_booking_background)
            } else {
                itemView.alpha = 0.9f
                itemView.setBackgroundResource(R.drawable.booking_background)
            }

            itemView.setOnClickListener { onBookingClick(booking) }
        }

        private fun formatDate(dateTime: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateTime)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateTime // Return original string if parsing fails
            }
        }

        private fun formatTime(dateTime: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(dateTime)
                val endTime = Calendar.getInstance()
                endTime.time = date ?: Date()
                // This would need to add booking duration, but for now just show start time
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateTime // Return original string if parsing fails
            }
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem == newItem
        }
    }
}

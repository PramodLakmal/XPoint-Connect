package com.xpoint.connect.ui.operator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xpoint.connect.R
import com.xpoint.connect.data.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class OperatorBookingsAdapter(private var bookings: List<Booking>) : RecyclerView.Adapter<OperatorBookingsAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCustomerInfo: TextView = itemView.findViewById(R.id.tvTitle)
        val tvBookingDetails: TextView = itemView.findViewById(R.id.tvSubtitle)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_operator_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun getItemCount(): Int = bookings.size

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        
        // Customer information
        holder.tvCustomerInfo.text = "Customer: ${booking.evOwnerNIC}"
        
        // Booking details with duration
        val durationHours = booking.durationMinutes / 60.0
        val durationText = if (durationHours == durationHours.toInt().toDouble()) {
            "${durationHours.toInt()}h"
        } else {
            String.format("%.1fh", durationHours)
        }
        
        holder.tvBookingDetails.text = "Duration: $durationText • ID: ${booking.id.take(8)}"
        
        // Status with appropriate styling
        val statusText = getStatusText(booking.status)
        holder.tvStatus.text = statusText
        holder.tvStatus.setBackgroundResource(getStatusBackground(booking.status))
        
        // Set text color based on status for better visibility
        val textColor = if (booking.status == 5) { // No Show
            ContextCompat.getColor(holder.itemView.context, R.color.white)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.white)
        }
        holder.tvStatus.setTextColor(textColor)
        
        // Time information
        holder.tvTime.text = formatBookingTime(booking)
    }

    private fun getStatusText(status: Int): String {
        return when (status) {
            0 -> "⏳ Pending"
            1 -> "✅ Approved"
            2 -> "� Checked In"
            3 -> "✅ Completed"
            4 -> "❌ Cancelled"
            5 -> "⚠️ No Show"
            else -> "❓ Unknown"
        }
    }

    private fun getStatusColor(context: Context, status: Int): Int {
        return when (status) {
            0 -> ContextCompat.getColor(context, R.color.operator_warning) // Pending
            1 -> ContextCompat.getColor(context, R.color.operator_success) // Approved
            2 -> ContextCompat.getColor(context, R.color.operator_info) // Checked In
            3 -> ContextCompat.getColor(context, R.color.operator_success) // Completed
            4 -> ContextCompat.getColor(context, R.color.operator_error) // Cancelled
            5 -> ContextCompat.getColor(context, R.color.text_secondary) // No Show
            else -> ContextCompat.getColor(context, R.color.text_secondary) // Unknown
        }
    }

    private fun getStatusBackground(status: Int): Int {
        return when (status) {
            0 -> R.drawable.bg_operator_gradient_orange // Pending
            1 -> R.drawable.bg_operator_gradient_green // Approved
            2 -> R.drawable.bg_operator_gradient_blue // Checked In
            3 -> R.drawable.bg_operator_gradient_green // Completed
            4 -> R.drawable.bg_status_cancelled // Cancelled
            5 -> R.drawable.bg_status_no_show // No Show
            else -> R.drawable.bg_operator_card // Unknown
        }
    }

    private fun formatBookingTime(booking: Booking): String {
        return try {
            val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            val startFormatted = dateTimeFormat.format(booking.startTime)
            val endFormatted = timeFormat.format(booking.endTime)

            "$startFormatted - $endFormatted"
        } catch (e: Exception) {
            // Fallback to basic representation
            try {
                val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                val startFormatted = dateTimeFormat.format(booking.startTime)
                "$startFormatted"
            } catch (_: Exception) {
                ""
            }
        }
    }

    fun submit(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}



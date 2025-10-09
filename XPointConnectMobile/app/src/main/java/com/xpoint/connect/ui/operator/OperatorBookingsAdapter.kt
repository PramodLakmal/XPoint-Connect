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

class OperatorBookingsAdapter : RecyclerView.Adapter<OperatorBookingsAdapter.BookingViewHolder>() {

    private var bookings: List<Booking> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_operator_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        
        // Customer Information (evOwnerName or evOwnerNIC)
        val customerInfo = if (booking.evOwnerName.isNotEmpty()) {
            "Customer: ${booking.evOwnerName}"
        } else {
            "Customer: ${booking.evOwnerNIC}"
        }
        holder.tvTitle.text = customerInfo
        
        // Calculate and display duration
        val durationHours = booking.durationMinutes / 60.0
        val durationText = if (durationHours % 1 == 0.0) {
            "${durationHours.toInt()}h"
        } else {
            String.format("%.1fh", durationHours)
        }
        
        // Booking details with duration, station info and full booking ID
        holder.tvSubtitle.text = "Duration: $durationText • Station: ${booking.chargingStationName}\nBooking ID: ${booking.id}"
        
        // Total Amount
        holder.tvAmount.text = "Amount: Rs. ${String.format("%.2f", booking.totalAmount)}"
        
        // Booking Date (from bookingDate field)
        val bookingDateText = try {
            if (booking.bookingDate.isNotEmpty()) {
                // Parse ISO 8601 date format from backend
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(booking.bookingDate)
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    "No date"
                }
            } else {
                "No date"
            }
        } catch (e: Exception) {
            "Invalid date"
        }
        holder.tvBookingDate.text = bookingDateText
        
        // Status with appropriate styling
        val statusText = getStatusText(booking.status)
        holder.tvStatus.text = statusText
        holder.tvStatus.setBackgroundResource(getStatusBackground(booking.status))
        
        // Set text color based on status for better visibility
        val textColor = if (booking.status == "NoShow") {
            ContextCompat.getColor(holder.itemView.context, R.color.white)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.white)
        }
        holder.tvStatus.setTextColor(textColor)
        
        // Reservation Date/Time (from reservationDateTime field)
        holder.tvTime.text = formatBookingTime(booking)
    }

    override fun getItemCount(): Int = bookings.size

    private fun getStatusText(status: String): String {
        return when (status) {
            "Pending" -> "⏳ Pending"
            "Approved" -> "✅ Approved"
            "CheckedIn" -> "🔌 Checked In"
            "Completed" -> "✅ Completed"
            "Cancelled" -> "❌ Cancelled"
            "NoShow" -> "⚠️ No Show"
            else -> "❓ Unknown"
        }
    }

    private fun getStatusColor(context: Context, status: String): Int {
        return when (status) {
            "Pending" -> ContextCompat.getColor(context, R.color.operator_warning)
            "Approved" -> ContextCompat.getColor(context, R.color.operator_success)
            "CheckedIn" -> ContextCompat.getColor(context, R.color.operator_info)
            "Completed" -> ContextCompat.getColor(context, R.color.operator_success)
            "Cancelled" -> ContextCompat.getColor(context, R.color.operator_error)
            "NoShow" -> ContextCompat.getColor(context, R.color.text_secondary)
            else -> ContextCompat.getColor(context, R.color.text_secondary)
        }
    }

    private fun getStatusBackground(status: String): Int {
        return when (status) {
            "Pending" -> R.drawable.bg_operator_gradient_orange
            "Approved" -> R.drawable.bg_operator_gradient_green
            "CheckedIn" -> R.drawable.bg_operator_gradient_blue
            "Completed" -> R.drawable.bg_operator_gradient_green
            "Cancelled" -> R.drawable.bg_status_cancelled
            "NoShow" -> R.drawable.bg_status_no_show
            else -> R.drawable.bg_operator_card
        }
    }

    private fun formatBookingTime(booking: Booking): String {
        return try {
            // Use reservationDateTime for display since it's the main booking time
            if (booking.reservationDateTime.isNotEmpty()) {
                // Parse ISO 8601 date format from backend
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                
                val date = inputFormat.parse(booking.reservationDateTime)
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    "Reservation time not available"
                }
            } else {
                // Fallback to check-in time if available
                if (!booking.checkInTime.isNullOrEmpty()) {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                    
                    val date = inputFormat.parse(booking.checkInTime)
                    if (date != null) {
                        "Check-in: ${outputFormat.format(date)}"
                    } else {
                        "Time not available"
                    }
                } else {
                    "Time not available"
                }
            }
        } catch (e: Exception) {
            // If parsing fails, try to display raw reservation date
            if (booking.reservationDateTime.isNotEmpty()) {
                "Reservation: ${booking.reservationDateTime.take(10)}" // Just show date part
            } else {
                "Time not available"
            }
        }
    }

    fun submit(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvBookingDate: TextView = itemView.findViewById(R.id.tvBookingDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }
}



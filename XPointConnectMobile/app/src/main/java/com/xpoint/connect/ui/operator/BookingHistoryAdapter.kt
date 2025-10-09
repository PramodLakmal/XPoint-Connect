package com.xpoint.connect.ui.operator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xpoint.connect.R
import com.xpoint.connect.data.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class BookingHistoryAdapter : RecyclerView.Adapter<BookingHistoryAdapter.BookingHistoryViewHolder>() {

    private var bookings: List<Booking> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun submit(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_history, parent, false)
        return BookingHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingHistoryViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    inner class BookingHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCustomerNIC: TextView = itemView.findViewById(R.id.tvCustomerNIC)
        private val tvSessionDate: TextView = itemView.findViewById(R.id.tvSessionDate)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(booking: Booking) {
            // Display shortened booking ID
            tvBookingId.text = "Booking #${booking.id.takeLast(8)}"
            
            // Status with appropriate styling
            val statusText = when (booking.status) {
                "Completed" -> "Completed"
                "Cancelled" -> "Cancelled"
                "NoShow" -> "No Show"
                else -> "Unknown"
            }
            tvStatus.text = statusText
            
            when (booking.status) {
                "Completed" -> { // Completed
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                }
                "Cancelled" -> { // Cancelled
                    tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                }
                "NoShow" -> { // No Show
                    tvStatus.setBackgroundResource(R.drawable.bg_status_no_show)
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_default)
                }
            }
            
            // Customer info
            tvCustomerNIC.text = "Customer: ${booking.evOwnerNIC}"
            
            // Session date
            val sessionDate = try {
                if (booking.reservationDateTime.isNotEmpty()) {
                    // Parse ISO 8601 date format from backend
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val date = inputFormat.parse(booking.reservationDateTime)
                    if (date != null) {
                        dateFormat.format(date)
                    } else {
                        "Unknown date"
                    }
                } else {
                    dateFormat.format(booking.startTime)
                }
            } catch (e: Exception) {
                "Unknown date"
            }
            tvSessionDate.text = "Session: $sessionDate"
            
            // Duration
            tvDuration.text = "Duration: ${booking.durationMinutes} minutes"
            
            // Amount
            tvAmount.text = "Amount: Rs. ${String.format("%.2f", booking.totalAmount)}"
        }
    }
}
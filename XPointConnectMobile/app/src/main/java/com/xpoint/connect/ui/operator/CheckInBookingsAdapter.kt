package com.xpoint.connect.ui.operator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xpoint.connect.R
import com.xpoint.connect.data.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class CheckInBookingsAdapter(
    private val onQRScanClick: (Booking) -> Unit
) : RecyclerView.Adapter<CheckInBookingsAdapter.CheckInBookingViewHolder>() {

    private var bookings: List<Booking> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    fun submit(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckInBookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_check_in_booking, parent, false)
        return CheckInBookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckInBookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    inner class CheckInBookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCustomerNIC: TextView = itemView.findViewById(R.id.tvCustomerNIC)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val btnScanQR: MaterialButton = itemView.findViewById(R.id.btnScanQR)

        fun bind(booking: Booking) {
            // Display shortened booking ID
            tvBookingId.text = "Booking #${booking.id?.takeLast(8) ?: "Unknown"}"
            
            // Status with appropriate styling
            tvStatus.text = when (booking.status) {
                "Pending" -> "Pending"
                "Approved" -> "Approved"
                "CheckedIn" -> "Checked In"
                "Completed" -> "Completed"
                "Cancelled" -> "Cancelled"
                "NoShow" -> "No Show"
                else -> "Unknown"
            }
            
            when (booking.status) {
                "Pending" -> { // Pending
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                "Approved" -> { // Approved
                    tvStatus.setBackgroundResource(R.drawable.bg_status_approved)
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_default)
                }
            }
            
            // Customer info
            tvCustomerNIC.text = "Customer: ${booking.evOwnerNIC}"
            
            // Date and time
            val formattedDate = try {
                if (booking.reservationDateTime.isNotEmpty()) {
                    // Parse ISO 8601 date format from backend
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val date = inputFormat.parse(booking.reservationDateTime)
                    if (date != null) {
                        dateFormat.format(date)
                    } else {
                        "Invalid date"
                    }
                } else {
                    "No date available"
                }
            } catch (e: Exception) {
                "Invalid date"
            }
            tvDateTime.text = formattedDate
            
            // Duration
            tvDuration.text = "Duration: ${booking.durationMinutes} minutes"
            
            // QR scan button click
            btnScanQR.setOnClickListener {
                onQRScanClick(booking)
            }
        }
    }
}
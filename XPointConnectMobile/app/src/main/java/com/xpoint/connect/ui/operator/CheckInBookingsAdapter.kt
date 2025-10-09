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
                0 -> "Pending"
                1 -> "Approved"
                2 -> "Checked In"
                3 -> "Completed"
                4 -> "Cancelled"
                5 -> "No Show"
                else -> "Unknown"
            }
            
            when (booking.status) {
                0 -> { // Pending
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                1 -> { // Approved
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
                dateFormat.format(booking.reservationDateTime)
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
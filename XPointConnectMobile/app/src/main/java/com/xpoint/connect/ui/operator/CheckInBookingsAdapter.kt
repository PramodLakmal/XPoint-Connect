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
        private val tvStationName: TextView = itemView.findViewById(R.id.tvStationName)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvBookingDate: TextView = itemView.findViewById(R.id.tvBookingDate)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val btnScanQR: MaterialButton = itemView.findViewById(R.id.btnScanQR)

        fun bind(booking: Booking) {
            // Display full booking ID
            tvBookingId.text = "Booking ID: ${booking.id}"
            
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
            
            // Customer info (evOwnerName or evOwnerNIC)
            val customerText = if (booking.evOwnerName.isNotEmpty()) {
                "Customer: ${booking.evOwnerName} (${booking.evOwnerNIC})"
            } else {
                "Customer: ${booking.evOwnerNIC}"
            }
            tvCustomerNIC.text = customerText
            
            // Charging Station Name
            tvStationName.text = "Station: ${booking.chargingStationName}"
            
            // Total Amount
            tvAmount.text = "Amount: Rs. ${String.format("%.2f", booking.totalAmount)}"
            
            // Booking Date
            val bookingDateText = try {
                if (booking.bookingDate.isNotEmpty()) {
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
            tvBookingDate.text = bookingDateText
            
            // Reservation Date and time
            val formattedDate = try {
                if (booking.reservationDateTime.isNotEmpty()) {
                    // Parse ISO 8601 date format from backend
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                    val date = inputFormat.parse(booking.reservationDateTime)
                    if (date != null) {
                        outputFormat.format(date)
                    } else {
                        "Invalid date"
                    }
                } else {
                    "No date available"
                }
            } catch (e: Exception) {
                "Invalid date"
            }
            tvDateTime.text = "Reservation: $formattedDate"
            
            // Duration in minutes
            tvDuration.text = "Duration: ${booking.durationMinutes} minutes"
            
            // QR scan button click
            btnScanQR.setOnClickListener {
                onQRScanClick(booking)
            }
        }
    }
}
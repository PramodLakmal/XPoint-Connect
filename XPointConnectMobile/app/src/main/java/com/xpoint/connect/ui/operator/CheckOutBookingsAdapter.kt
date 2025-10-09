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

class CheckOutBookingsAdapter(
    private val onDoneClick: (Booking) -> Unit
) : RecyclerView.Adapter<CheckOutBookingsAdapter.CheckOutBookingViewHolder>() {

    private var bookings: List<Booking> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    fun submit(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckOutBookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_check_out_booking, parent, false)
        return CheckOutBookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckOutBookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    inner class CheckOutBookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCustomerNIC: TextView = itemView.findViewById(R.id.tvCustomerNIC)
        private val tvCheckInTime: TextView = itemView.findViewById(R.id.tvCheckInTime)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val btnDone: MaterialButton = itemView.findViewById(R.id.btnDone)

        fun bind(booking: Booking) {
            // Display shortened booking ID
            tvBookingId.text = "Booking #${booking.id.takeLast(8)}"
            
            // Status with appropriate styling
            tvStatus.text = "Checked In"
            tvStatus.setBackgroundResource(R.drawable.bg_status_checked_in)
            
            // Customer info
            tvCustomerNIC.text = "Customer: ${booking.evOwnerNIC}"
            
            // Check-in time
            val checkInTime = booking.checkInTime ?: "Unknown"
            tvCheckInTime.text = "Checked in: $checkInTime"
            
            // Duration
            tvDuration.text = "Session duration: ${booking.durationMinutes} minutes"
            
            // Done button click
            btnDone.setOnClickListener {
                onDoneClick(booking)
            }
        }
    }
}
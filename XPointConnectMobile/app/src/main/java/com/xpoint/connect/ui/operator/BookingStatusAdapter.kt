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

class BookingStatusAdapter(
    private val onBookingClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingStatusAdapter.BookingViewHolder>() {
    
    private var bookings = listOf<Booking>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    fun updateBookings(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_status, parent, false)
        return BookingViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }
    
    override fun getItemCount(): Int = bookings.size
    
    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEvOwnerName: TextView = itemView.findViewById(R.id.tvEvOwnerName)
        private val tvBookingStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)
        private val tvReservationTime: TextView = itemView.findViewById(R.id.tvReservationTime)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        
        fun bind(booking: Booking) {
            tvEvOwnerName.text = booking.evOwnerName.ifEmpty { booking.evOwnerNIC }
            tvBookingStatus.text = booking.status
            
            // Format reservation date time
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(booking.reservationDateTime)
                tvReservationTime.text = date?.let { dateFormat.format(it) } ?: booking.reservationDateTime
            } catch (e: Exception) {
                tvReservationTime.text = booking.reservationDateTime
            }
            
            tvDuration.text = "${booking.durationMinutes} mins"
            tvAmount.text = "Rs. ${String.format("%.2f", booking.totalAmount)}"
            
            // Set status-specific styling
            when (booking.status) {
                "Pending" -> {
                    tvBookingStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                "Approved" -> {
                    tvBookingStatus.setTextColor(itemView.context.getColor(android.R.color.holo_blue_dark))
                }
                "CheckedIn" -> {
                    tvBookingStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
                "Completed" -> {
                    tvBookingStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
                "Cancelled" -> {
                    tvBookingStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
                "NoShow" -> {
                    tvBookingStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_light))
                }
            }
            
            itemView.setOnClickListener {
                onBookingClick(booking)
            }
        }
    }
}
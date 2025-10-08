package com.xpoint.connect.ui.operator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xpoint.connect.R
import com.xpoint.connect.data.model.Booking

class OperatorBookingsAdapter(private var bookings: List<Booking>) : RecyclerView.Adapter<OperatorBookingsAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val subtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_operator_booking, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = bookings.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = bookings[position]
        holder.title.text = "${b.evOwnerNIC} - ${b.status}"
        holder.subtitle.text = "${b.startTime} → ${b.endTime}"
    }

    fun submit(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}



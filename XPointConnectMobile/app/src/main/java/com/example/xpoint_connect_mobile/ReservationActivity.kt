package com.example.xpoint_connect_mobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReservationActivity : AppCompatActivity() {

    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        db = DBHelper(this)

        val etDate = findViewById<EditText>(R.id.etDate)
        val etTime = findViewById<EditText>(R.id.etTime)
        val etStation = findViewById<EditText>(R.id.etStation)
        val btnReserve = findViewById<Button>(R.id.btnReserve)

        btnReserve.setOnClickListener {
            val date = etDate.text.toString()
            val time = etTime.text.toString()
            val station = etStation.text.toString()

            val userEmail = "test@example.com" // TODO: Replace with logged-in user

            val success = db.addReservation(userEmail, date, time, station)
            if (success) {
                Toast.makeText(this, "Reservation Added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to Add Reservation", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

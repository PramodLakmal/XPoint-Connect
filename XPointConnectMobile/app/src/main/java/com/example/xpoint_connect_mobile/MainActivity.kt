package com.example.xpoint_connect_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnGoRegister = findViewById<Button>(R.id.btnLogin)
        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, ReservationActivity::class.java))
        }
    }
}

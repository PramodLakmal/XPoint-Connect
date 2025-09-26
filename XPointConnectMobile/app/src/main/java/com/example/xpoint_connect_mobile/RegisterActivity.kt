/**
 * File: RegisterActivity.kt
 * Author: Your Name (ITXXXXXXXX)
 * Date: 2025-09-25
 * Purpose: Register new EV owner and save to local SQLite
 */

package com.example.xpoint_connect_mobile

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val dbHelper = DBHelper(this)
        val db = dbHelper.writableDatabase

        val etNic = findViewById<EditText>(R.id.etNic)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val values = ContentValues().apply {
                put("nic", etNic.text.toString())
                put("name", etName.text.toString())
                put("email", etEmail.text.toString())
                put("password", etPassword.text.toString())
                put("status", 1) // active
            }

            val result = db.insert("ev_owner", null, values)

            if (result != -1L) {
                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to register", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


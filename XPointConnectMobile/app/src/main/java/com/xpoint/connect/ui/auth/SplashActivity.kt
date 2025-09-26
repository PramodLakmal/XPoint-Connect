package com.xpoint.connect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.xpoint.connect.ui.main.MainActivity
import com.xpoint.connect.utils.SharedPreferencesManager
import com.xpoint.connect.R

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPreferencesManager = SharedPreferencesManager(this)

        // Delay for splash screen
        Handler(Looper.getMainLooper())
                .postDelayed({ navigateToNextScreen() }, 2000) // 2 seconds delay
    }

    private fun navigateToNextScreen() {
        val intent =
                if (sharedPreferencesManager.isLoggedIn()) {
                    Intent(this, MainActivity::class.java)
                } else {
                    Intent(this, LoginActivity::class.java)
                }

        startActivity(intent)
        finish()
    }
}

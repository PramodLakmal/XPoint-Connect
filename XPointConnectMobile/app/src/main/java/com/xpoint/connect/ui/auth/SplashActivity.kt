package com.xpoint.connect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xpoint.connect.R
import com.xpoint.connect.XPointConnectApplication
import com.xpoint.connect.data.database.UserPreferencesManager
import com.xpoint.connect.ui.main.MainActivity
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferencesManager = (application as XPointConnectApplication).userPreferencesManager

        // Delay for splash screen
        Handler(Looper.getMainLooper())
                .postDelayed({ navigateToNextScreen() }, 2000) // 2 seconds delay
    }

    private fun navigateToNextScreen() {
        lifecycleScope.launch {
            val intent =
                    if (userPreferencesManager.isLoggedIn()) {
                        Intent(this@SplashActivity, MainActivity::class.java)
                    } else {
                        Intent(this@SplashActivity, LoginActivity::class.java)
                    }

            startActivity(intent)
            finish()
        }
    }
}

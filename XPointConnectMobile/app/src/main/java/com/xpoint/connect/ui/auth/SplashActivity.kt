/**
 * SplashActivity.kt
 *
 * Purpose: Initial splash screen activity for app branding and user session validation Author:
 * XPoint Connect Development Team Date: September 27, 2025
 *
 * Description: This activity serves as the application's entry point, displaying the XPoint Connect
 * branding while checking user authentication status. It determines whether to navigate to the
 * login screen for new users or directly to the main application for authenticated users, providing
 * a smooth onboarding experience.
 *
 * Key Features:
 * - Application branding and splash screen display
 * - User authentication status validation
 * - Automatic navigation based on login state
 * - SQLite-based session management integration
 * - Smooth transition to appropriate application screens
 * - Background initialization of application components
 */
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

    /**
     * Initializes the splash activity and sets up navigation timer Configures user preferences
     * manager and schedules navigation to next screen
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferencesManager = (application as XPointConnectApplication).userPreferencesManager

        // Delay for splash screen
        Handler(Looper.getMainLooper())
                .postDelayed({ navigateToNextScreen() }, 2000) // 2 seconds delay
    }

    /**
     * Determines and navigates to the appropriate next screen based on user authentication status
     * Checks login state and redirects to MainActivity for authenticated users or LoginActivity for
     * new users
     */
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

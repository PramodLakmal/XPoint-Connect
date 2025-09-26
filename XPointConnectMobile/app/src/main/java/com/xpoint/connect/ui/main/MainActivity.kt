package com.xpoint.connect.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xpoint.connect.R
import com.xpoint.connect.ui.auth.LoginActivity
import com.xpoint.connect.ui.bookings.BookingsFragment
import com.xpoint.connect.ui.profile.ProfileFragment
import com.xpoint.connect.utils.SharedPreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferencesManager = SharedPreferencesManager(this)

        // Check if user is logged in
        if (!sharedPreferencesManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize with Dashboard fragment
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, DashboardFragment())
                    .commit()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment =
                    when (item.itemId) {
                        R.id.nav_home -> DashboardFragment()
                        R.id.nav_stations -> StationsFragment()
                        R.id.nav_bookings -> BookingsFragment()
                        R.id.nav_profile -> ProfileFragment()
                        else -> DashboardFragment()
                    }

            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit()

            true
        }
    }
}

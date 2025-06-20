package com.example.droiddevs

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class PocketPagesApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Read the saved theme preference from SharedPreferences.
        val sharedPreferences = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getString("app_theme", "system")

        // Apply the theme. This must be done before any Activities are created.
        applyTheme(theme)
    }

    companion object {
        fun applyTheme(theme: String?) {
            when (theme) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}
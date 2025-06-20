package com.example.droiddevs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        radioGroupTheme = findViewById(R.id.rgTheme)
        sharedPreferences = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        loadSettings()

        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.rbLight -> "light"
                R.id.rbDark -> "dark"
                else -> "system"
            }
            saveAndApplyTheme(theme)
        }
    }

    private fun loadSettings() {
        val currentTheme = sharedPreferences.getString("app_theme", "system")
        when (currentTheme) {
            "light" -> radioGroupTheme.check(R.id.rbLight)
            "dark" -> radioGroupTheme.check(R.id.rbDark)
            else -> radioGroupTheme.check(R.id.rbSystem)
        }
    }

    private fun saveAndApplyTheme(theme: String) {
        sharedPreferences.edit().putString("app_theme", theme).apply()
        PocketPagesApp.applyTheme(theme)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
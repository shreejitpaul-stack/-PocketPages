package com.example.droiddevs

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

class SignInActivity : AppCompatActivity() {
    private lateinit var tvAppTitle: MaterialTextView
    private lateinit var tvSubtitle: MaterialTextView
    private lateinit var tilPin: TextInputLayout
    private lateinit var etPin: TextInputEditText
    private lateinit var btnSignIn: MaterialButton
    private lateinit var tvForgotPin: MaterialTextView
    private lateinit var tvCreatePin: MaterialTextView
    private lateinit var sharedPreferences: SharedPreferences

    // Default PIN for demo purposes - in a real app, this would be securely stored
    private val DEFAULT_PIN = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        sharedPreferences = getSharedPreferences("notion_prefs", MODE_PRIVATE)

        if (isUserSignedIn()) {
            navigateToMain()
            return
        }

        initializeViews()
        setupClickListeners()
        setupPinInput()
    }

    private fun initializeViews() {
        tvAppTitle = findViewById(R.id.tvAppTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        tilPin = findViewById(R.id.tilPin)
        etPin = findViewById(R.id.etPin)
        btnSignIn = findViewById(R.id.btnSignIn)
        tvForgotPin = findViewById(R.id.tvForgotPin)
        tvCreatePin = findViewById(R.id.tvCreatePin)

        tvAppTitle.text = getString(R.string.app_name)
        tvSubtitle.text = "Enter your PIN to access your workspace"

        // Initially disable sign in button
        btnSignIn.isEnabled = false
    }

    private fun setupClickListeners() {
        btnSignIn.setOnClickListener {
            val enteredPin = etPin.text.toString().trim()
            validateAndSignIn(enteredPin)
        }

        tvForgotPin.setOnClickListener {
            showForgotPinDialog()
        }

        tvCreatePin.setOnClickListener {
            showCreatePinDialog()
        }
    }

    private fun setupPinInput() {
        etPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val pin = s.toString().trim()
                btnSignIn.isEnabled = pin.length >= 4

                // Clear any previous error
                if (pin.isNotEmpty()) {
                    tilPin.error = null
                }

                // Auto-submit when PIN is 4 digits and user stops typing
                if (pin.length == 4) {
                    etPin.postDelayed({
                        if (etPin.text.toString().trim() == pin) {
                            validateAndSignIn(pin)
                        }
                    }, 500) // Small delay to allow user to continue typing if needed
                }
            }
        })
    }

    private fun validateAndSignIn(pin: String) {
        when {
            pin.isEmpty() -> {
                tilPin.error = "Please enter your PIN"
                return
            }
            pin.length < 4 -> {
                tilPin.error = "PIN must be at least 4 digits"
                return
            }
            !pin.all { it.isDigit() } -> {
                tilPin.error = "PIN can only contain numbers"
                return
            }
            pin == getStoredPin() -> {
                // Successful authentication
                performSignIn()
            }
            else -> {
                // Wrong PIN
                tilPin.error = "Incorrect PIN. Please try again."
                etPin.selectAll() // Select all text for easy correction

                // Track failed attempts (optional security feature)
                incrementFailedAttempts()
            }
        }
    }

    private fun getStoredPin(): String {
        // In a real app, this would be retrieved from secure storage
        return sharedPreferences.getString("user_pin", DEFAULT_PIN) ?: DEFAULT_PIN
    }

    private fun performSignIn() {
        // Clear any errors
        tilPin.error = null

        // Reset failed attempts counter
        sharedPreferences.edit().putInt("failed_pin_attempts", 0).apply()

        // Store sign-in state
        sharedPreferences.edit()
            .putBoolean("is_signed_in", true)
            .putString("user_email", "user@example.com")
            .putString("user_name", "Demo User")
            .putString("user_id", "demo_user_123")
            .putLong("last_signin_time", System.currentTimeMillis())
            .apply()

        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }

    private fun incrementFailedAttempts() {
        val currentAttempts = sharedPreferences.getInt("failed_pin_attempts", 0)
        val newAttempts = currentAttempts + 1
        sharedPreferences.edit().putInt("failed_pin_attempts", newAttempts).apply()

        // Optional: Lock out after too many failed attempts
        if (newAttempts >= 5) {
            tilPin.error = "Too many failed attempts. Please try again later."
            btnSignIn.isEnabled = false
            etPin.isEnabled = false

            // In a real app, you might want to implement a timeout here
            etPin.postDelayed({
                btnSignIn.isEnabled = true
                etPin.isEnabled = true
                tilPin.error = null
                sharedPreferences.edit().putInt("failed_pin_attempts", 0).apply()
            }, 30000) // 30 second timeout
        }
    }

    private fun showForgotPinDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Forgot PIN?")
            .setMessage("For demo purposes, the default PIN is: $DEFAULT_PIN\n\nIn a real app, you would verify identity through email or other secure methods.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                etPin.setText(DEFAULT_PIN)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCreatePinDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_pin, null)
        val etNewPin = dialogView.findViewById<TextInputEditText>(R.id.etNewPin)
        val etConfirmPin = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPin)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Create New PIN")
            .setView(dialogView)
            .setPositiveButton("Create") { dialog, _ ->
                val newPin = etNewPin.text.toString().trim()
                val confirmPin = etConfirmPin.text.toString().trim()

                when {
                    newPin.isEmpty() || confirmPin.isEmpty() -> {
                        Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                    }
                    newPin.length < 4 -> {
                        Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                    }
                    !newPin.all { it.isDigit() } -> {
                        Toast.makeText(this, "PIN can only contain numbers", Toast.LENGTH_SHORT).show()
                    }
                    newPin != confirmPin -> {
                        Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Save new PIN
                        sharedPreferences.edit().putString("user_pin", newPin).apply()
                        Toast.makeText(this, "PIN created successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isUserSignedIn(): Boolean {
        val isSignedIn = sharedPreferences.getBoolean("is_signed_in", false)

        // Optional: Check if sign-in has expired (for additional security)
        if (isSignedIn) {
            val lastSignIn = sharedPreferences.getLong("last_signin_time", 0)
            val currentTime = System.currentTimeMillis()
            val sessionDuration = 24 * 60 * 60 * 1000 // 24 hours in milliseconds

            if (currentTime - lastSignIn > sessionDuration) {
                // Session expired, require re-authentication
                sharedPreferences.edit().putBoolean("is_signed_in", false).apply()
                return false
            }
        }

        return isSignedIn
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
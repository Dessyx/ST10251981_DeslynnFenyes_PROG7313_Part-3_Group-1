package com.example.prog7313_groupwork

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.User
import com.example.prog7313_groupwork.models.MainActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var database: AstraDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize database
        database = AstraDatabase.getDatabase(this)

        // Initialize views
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)
        backButton = findViewById(R.id.backButton)

        // Set click listeners
        registerButton.setOnClickListener {
            registerUser()
        }

        backButton.setOnClickListener {
            finish() // Go back to previous activity
        }
    }

    private fun registerUser() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phoneStr = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Validation
        if (name.isEmpty() || email.isEmpty() || phoneStr.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val phone = phoneStr.toInt()
            
            // Hash the password
            val hashedPassword = hashPassword(password)

            // Create user object
            val user = User(
                NameSurname = name,
                PhoneNumber = phone,
                userEmail = email,
                passwordHash = hashedPassword
            )

            // Save user to database
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Check if user already exists
                    val existingUser = database.userDAO().getUserByEmail(email)
                    if (existingUser != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    // Insert new user
                    database.userDAO().insertUser(user)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to login or main activity
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
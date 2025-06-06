package com.example.prog7313_groupwork

// imports
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.entities.User
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.prog7313_groupwork.firebase.FirebaseUserService

// ----------------------------- Functionality of activity_register.xml ----------------------------------
class RegisterActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var passwordInput: EditText            // Variable declaration
    private lateinit var registerButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var firebaseUserService: FirebaseUserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //------------------------------------------------------------------------------------------
        // Initialize Firebase service
        firebaseUserService = FirebaseUserService()

        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)
        backButton = findViewById(R.id.backButton)

        //------------------------------------------------------------------------------------------
        // on click listeners
        registerButton.setOnClickListener {
            registerUser()
        }

        backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        //-------------------------------------------------------------------------------------------
    }

    //----------------------------------------------------------------------------------------------
    // Captures the users details and inserts the data into the database, creating an account
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
            
            // Hash the password using BCrypt for data security
            val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

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
                    val existingUser = firebaseUserService.getUserByEmail(email)
                    if (existingUser != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    firebaseUserService.insertUser(user)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
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
}

// -----------------------------------<<< End Of File >>>------------------------------------------
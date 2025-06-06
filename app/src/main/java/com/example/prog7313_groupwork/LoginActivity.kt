package com.example.prog7313_groupwork

// imports
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.repository.MainActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.prog7313_groupwork.firebase.FirebaseUserService

// ----------------------------- Functionality of login.xml ----------------------------------
class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: MaterialButton       // Variable declaration
    private lateinit var registerButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var firebaseUserService: FirebaseUserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Initialize Firebase service
        firebaseUserService = FirebaseUserService()

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        backButton = findViewById(R.id.back_button)

        // -----------------------------------------------------------------------------------------
        // on click listeners
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        // -----------------------------------------------------------------------------------------
            Log.d("LoginActivity", "Attempting login for email: $email")

            lifecycleScope.launch {
                try {
                    val user = withContext(Dispatchers.IO) {
                        firebaseUserService.getUserByEmail(email)
                    }

                    // Handles the login checks and functionality
                    if (user != null) {
                        Log.d("LoginActivity", "User found: ${user.userEmail}")
                        val result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash)
                        if (result.verified) {
                            Log.d("LoginActivity", "Password verification successful.")
                            getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit()
                                .putLong("current_user_id", user.id)
                                .apply()

                            Log.d("LoginActivity", "Login successful, navigating to HomeActivity.")
                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {   // Error handling display
                            Log.d("LoginActivity", "Password verification failed.")
                            Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d("LoginActivity", "User not found for email: $email")
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Login failed", e)
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
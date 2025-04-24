package com.example.prog7313_groupwork.repository

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.User
import kotlinx.coroutines.launch

class ProfileMainClass : AppCompatActivity() {

    private lateinit var db: AstraDatabase
    private lateinit var btnSaveProfile: Button
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private var currentUserId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        // Initialize views
        initializeViews()

        // Initialize AstraDatabase
        db = AstraDatabase.getDatabase(this)

        // Load existing profile if any
        loadExistingProfile()

        btnSaveProfile.setOnClickListener {
            validateAndSaveProfile()
        }
    }

    private fun initializeViews() {
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etEmail = findViewById(R.id.etEmail)
    }

    private fun loadExistingProfile() {
        lifecycleScope.launch {
            try {
                val existingUser = db.userDAO().getLatestUser()
                existingUser?.let { user ->
                    currentUserId = user.id
                    runOnUiThread {
                        etName.setText(user.name)
                        etSurname.setText(user.surname)
                        etEmail.setText(user.userEmail)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@ProfileMainClass,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun validateAndSaveProfile() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        saveUserProfile(name, surname, email)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveUserProfile(name: String, surname: String, email: String) {
        lifecycleScope.launch {
            try {
                val user = User(
                    id = if (currentUserId != -1L) currentUserId else 0,
                    name = name,
                    surname = surname,
                    userEmail = email
                )

                db.userDAO().insertUser(user)

                runOnUiThread {
                    Toast.makeText(
                        this@ProfileMainClass,
                        if (currentUserId != -1L) "Profile updated!" else "Profile created!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@ProfileMainClass,
                        "Failed to save profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
package com.example.prog7313_groupwork.repository

// Imports
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.HomeActivity
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//--------------------------------------- Profile Main Class -----------------------------------------------
// This class handles the profile management functionality including viewing, editing and deleting user profiles
class ProfileMainClass : AppCompatActivity() {

    // Variable declarations
    private lateinit var database: AstraDatabase
    private lateinit var btnSaveProfile: Button
    private lateinit var btnDeleteProfile: Button
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private var currentUserId: Long = -1
    private lateinit var savings: TextView

    // ------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        // Initializes views and database
        initializeViews()
        database = AstraDatabase.getDatabase(this)


        loadExistingProfile()
        updateSavingsDisplay()
        //----------------------------------------------------------------------------------
        // On CLick listeners
        btnSaveProfile.setOnClickListener {
            validateAndSaveProfile()
        }
        
        btnDeleteProfile.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        //------------------------------------------------------------------------------------
        // Navigation
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    // ------------------------------------------------------------------------------------
    // Initializes views
    private fun initializeViews() {
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnDeleteProfile = findViewById(R.id.btnDeleteProfile)
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etEmail = findViewById(R.id.etEmail)
        savings = findViewById(R.id.tvTotalSaved)
    }

    // ------------------------------------------------------------------------------------
    // Loads the existing user profile if one exists
    private fun loadExistingProfile() {
        lifecycleScope.launch {
            try {
                val existingUser = database.userDAO().getLatestUser()
                existingUser?.let { user ->
                    currentUserId = user.id
                    runOnUiThread {
                        val nameParts = user.NameSurname.split(" ", limit = 2)
                        etName.setText(nameParts.getOrNull(0) ?: "")
                        etSurname.setText(nameParts.getOrNull(1) ?: "")
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

    // ------------------------------------------------------------------------------------
    // Validates the input fields before saving the profile
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

    // ------------------------------------------------------------------------------------
    // Validates if the email address is in a correct format
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // ------------------------------------------------------------------------------------
    // Saves the user profile to the database
    private fun saveUserProfile(name: String, surname: String, email: String) {
        lifecycleScope.launch {
            try {
                val user = User(
                    id = if (currentUserId != -1L) currentUserId else 0,
                    NameSurname = "$name $surname",
                    PhoneNumber = 0,
                    userEmail = email,
                    passwordHash = ""
                )

                database.userDAO().insertUser(user)

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
    
    // ------------------------------------------------------------------------------------
    // Shows a confirmation dialog before deleting the profile
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteUserProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    // ------------------------------------------------------------------------------------
    // Deletes the user profile from the database
    private fun deleteUserProfile() {
        if (currentUserId == -1L) {
            Toast.makeText(this, "No profile to delete", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                database.userDAO().deleteUserById(currentUserId)
                
                runOnUiThread {
                    Toast.makeText(
                        this@ProfileMainClass,
                        "Profile deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Navigate back to home screen
                    val intent = Intent(this@ProfileMainClass, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@ProfileMainClass,
                        "Failed to delete profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // Updates the savings display with the current total savings
    private fun updateSavingsDisplay() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val totalSavings = database.savingsDAO().getTotalSavings(currentUserId) ?: 0.0

                withContext(Dispatchers.Main) {
                    savings.text = String.format("R %.2f", totalSavings)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    savings.text = "R 0.00"
                }
            }
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
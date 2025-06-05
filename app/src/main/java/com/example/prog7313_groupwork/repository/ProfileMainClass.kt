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
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.HomeActivity
import com.example.prog7313_groupwork.LoginActivity
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.User
import com.example.prog7313_groupwork.firebase.FirebaseSavingsService
import com.example.prog7313_groupwork.firebase.FirebaseUserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//--------------------------------------- Profile Main Class -----------------------------------------------
// This class handles the profile management functionality including viewing, editing and deleting user profiles
class ProfileMainClass : AppCompatActivity() {

    // Variable declarations
    private lateinit var database: AstraDatabase
    private lateinit var savingsService: FirebaseSavingsService
    private lateinit var firebaseUserService: FirebaseUserService
    private lateinit var btnSaveProfile: Button
    private lateinit var btnDeleteProfile: Button
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var savings: TextView
    private var existingEmail: String = ""       // keep the original email under the hood
    private var currentUserId: Long = -1L

    // ------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        // Initialize Firebase service
        firebaseUserService = FirebaseUserService()

        // Initializes views and database
        initializeViews()
        database = AstraDatabase.getDatabase(this)
        savingsService = FirebaseSavingsService()

        loadExistingProfile()

        // On Click listeners
        btnSaveProfile.setOnClickListener { validateAndSaveProfile() }
        btnDeleteProfile.setOnClickListener { showDeleteConfirmationDialog() }

        // Navigation back to Home
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(this)
                finish()
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // Initializes views
    private fun initializeViews() {
        btnSaveProfile   = findViewById(R.id.btnSaveProfile)
        btnDeleteProfile = findViewById(R.id.btnDeleteProfile)
        etName           = findViewById(R.id.etName)
        etSurname        = findViewById(R.id.etSurname)
        savings          = findViewById(R.id.tvTotalSaved)
    }

    // ------------------------------------------------------------------------------------
    // Loads the existing user profile if one exists
    private fun loadExistingProfile() {
        lifecycleScope.launch {
            try {
                val existingUser = firebaseUserService.getLatestUser()
                existingUser?.let { user ->
                    currentUserId = user.id
                    existingEmail = user.userEmail

                    // store user_id so SavingsMainClass can read it
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .putLong("user_id", currentUserId)
                        .apply()

                    withContext(Dispatchers.Main) {
                        val parts = user.NameSurname.split(" ", limit = 2)
                        etName.setText(parts.getOrNull(0) ?: "")
                        etSurname.setText(parts.getOrNull(1) ?: "")

                        // now that currentUserId is set, show their savings
                        updateSavingsDisplay()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
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
        val name    = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()

        if (name.isEmpty() || surname.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        saveUserProfile(name, surname)
    }

    // ------------------------------------------------------------------------------------
    // Saves the user profile to the database
    private fun saveUserProfile(name: String, surname: String) {
        lifecycleScope.launch {
            try {
                val user = User(
                    id           = if (currentUserId != -1L) currentUserId else 0,
                    NameSurname  = "$name $surname",
                    PhoneNumber  = 0,
                    userEmail    = existingEmail,
                    passwordHash = ""
                )
                val newId = firebaseUserService.insertUser(user)

                if (currentUserId == -1L) {
                    // this was a new user → capture their new ID
                    currentUserId = newId
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .putLong("user_id", currentUserId)
                        .apply()
                }

                runOnUiThread {
                    Toast.makeText(
                        this@ProfileMainClass,
                        if (user.id != 0L) "Profile updated!" else "Profile created!",
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
            .setPositiveButton("Delete") { _, _ -> deleteUserProfile() }
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
                firebaseUserService.deleteUserById(currentUserId)
                savingsService.deleteSavings(currentUserId)

                runOnUiThread {
                    Toast.makeText(
                        this@ProfileMainClass,
                        "Profile deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@ProfileMainClass, LoginActivity::class.java)
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
                val totalSavings = savingsService.getTotalSavings(currentUserId)

                withContext(Dispatchers.Main) {
                    savings.text = String.format("R %.2f", totalSavings)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    savings.text = "R 0.00"
                    Toast.makeText(
                        this@ProfileMainClass,
                        "Error loading savings: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
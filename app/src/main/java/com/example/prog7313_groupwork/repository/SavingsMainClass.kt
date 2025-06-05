// SavingsMainClass.kt
package com.example.prog7313_groupwork.repository

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.HomeActivity
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Savings
import com.example.prog7313_groupwork.firebase.FirebaseSavingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

// -------------------------- Handles savings_page.xml functionality ----------------------------
class SavingsMainClass : AppCompatActivity() {

    private lateinit var db: AstraDatabase
    private lateinit var savingsService: FirebaseSavingsService
    private lateinit var etSaveAmount: EditText
    private lateinit var savingsGoalInput: EditText
    private lateinit var btnAddSavings: Button
    private lateinit var btnSetGoal: Button
    private lateinit var tvTotalSavings: TextView
    private lateinit var progressBarSavings: ProgressBar
    private lateinit var tvProgressPercent: TextView

    private var currentUserId: Long = -1L
    private var monthlySavingsGoal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.savings_page)

        // Initialize database and services
        db = AstraDatabase.getDatabase(this)
        savingsService = FirebaseSavingsService()

        // Get user ID from SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", -1L)

        if (currentUserId == -1L) {
            Toast.makeText(this, "Please log in to use savings feature", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        updateSavingsDisplay()

        // Set up click listeners
        btnAddSavings.setOnClickListener {
            validateAndAddSavings()
        }

        btnSetGoal.setOnClickListener {
            validateAndSetGoal()
        }

        // Back button navigation
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(this)
                finish()
            }
        }
    }

    private fun initializeViews() {
        etSaveAmount = findViewById(R.id.etSaveAmount)
        savingsGoalInput = findViewById(R.id.savingsGoalInput)
        btnAddSavings = findViewById(R.id.btnAddSavings)
        btnSetGoal = findViewById(R.id.btnSetGoal)
        tvTotalSavings = findViewById(R.id.tvTotalSavings)
        progressBarSavings = findViewById(R.id.progressBarSavings)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
    }

    private fun validateAndSetGoal() {
        val input = savingsGoalInput.text.toString()
        if (input.isBlank()) {
            Toast.makeText(this, "Enter a goal amount", Toast.LENGTH_SHORT).show()
            return
        }
        val goal = input.toDoubleOrNull()
        if (goal == null || goal <= 0) {
            Toast.makeText(this, "Enter a valid goal amount", Toast.LENGTH_SHORT).show()
            return
        }
        setMonthlyGoal(goal)
    }

    private fun setMonthlyGoal(goal: Double) {
        lifecycleScope.launch {
            try {
                // Save the goal to SharedPreferences
                getSharedPreferences("user_prefs", MODE_PRIVATE).edit().apply {
                    putFloat("monthly_savings_goal", goal.toFloat())
                    apply()
                }
                monthlySavingsGoal = goal
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavingsMainClass,
                        "Monthly savings goal set to R$goal",
                        Toast.LENGTH_SHORT
                    ).show()
                    savingsGoalInput.text.clear()
                    updateSavingsDisplay()
                }
            } catch (e: Exception) {
                Log.e("SavingsMainClass", "Error setting goal: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavingsMainClass,
                        "Failed to set goal: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun validateAndAddSavings() {
        val input = etSaveAmount.text.toString()
        if (input.isBlank()) {
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = input.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        addSavings(amount)
    }

    private fun addSavings(amount: Double) {
        if (currentUserId == -1L) {
            Toast.makeText(this, "Please log in to save money", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val savings = Savings(
                    userId = currentUserId,
                    amount = amount,
                    date = System.currentTimeMillis()
                )

                // Add to Firebase
                savingsService.saveSavings(savings)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavingsMainClass,
                        "Added R$amount to savings",
                        Toast.LENGTH_SHORT
                    ).show()
                    etSaveAmount.text.clear()
                    updateSavingsDisplay()
                }
            } catch (e: Exception) {
                Log.e("SavingsMainClass", "Error adding savings: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavingsMainClass,
                        "Failed to save: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateSavingsDisplay() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val total = savingsService.getTotalSavings(currentUserId)
                
                // Get the monthly goal from SharedPreferences
                monthlySavingsGoal = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getFloat("monthly_savings_goal", 0f).toDouble()

                val percent = if (monthlySavingsGoal > 0) {
                    ((total / monthlySavingsGoal) * 100)
                        .coerceAtMost(100.0)
                        .toInt()
                } else 0

                withContext(Dispatchers.Main) {
                    // Display both total savings and monthly goal
                    tvTotalSavings.text = String.format("R %.2f / R %.2f", total, monthlySavingsGoal)
                    progressBarSavings.progress = percent
                    tvProgressPercent.text = "$percent%"
                }
            } catch (e: Exception) {
                Log.e("SavingsMainClass", "Error updating display: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavingsMainClass,
                        "Error updating display: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateSavingsDisplay()
    }
}

// -----------------------------------<<< End Of File >>>------------------------------------------
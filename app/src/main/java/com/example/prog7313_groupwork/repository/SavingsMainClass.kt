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
import kotlinx.coroutines.launch


// -------------------------- Handles savings_page.xml functionality ----------------------------
class SavingsMainClass : AppCompatActivity() {

    private lateinit var db: AstraDatabase
    private lateinit var etSaveAmount: EditText
    private lateinit var btnAddSavings: Button
    private lateinit var tvTotalSavings: TextView
    private lateinit var progressBarSavings: ProgressBar
    private lateinit var tvProgressPercent: TextView

    private val savingsGoal = 6000.0
    private var currentUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.savings_page)

        // 1) pull the real user_id from prefs
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("user_id", -1L)

        initializeViews()
        db = AstraDatabase.getDatabase(this) // initialize database

        updateSavingsDisplay()
        //----------------------------------------------------------------------------------
        // on click listener
        btnAddSavings.setOnClickListener {
            validateAndAddSavings()
        }
        //----------------------------------------------------------------------------------
        //                              Page navigation section

        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(this)
                finish()
            }
        }
    }

    private fun initializeViews() {
        etSaveAmount      = findViewById(R.id.etSaveAmount)
        btnAddSavings     = findViewById(R.id.btnAddSavings)
        tvTotalSavings    = findViewById(R.id.tvTotalSavings)
        progressBarSavings= findViewById(R.id.progressBarSavings)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
    }
    //---------------------------------------------------------------------------------------
    // Checks and adds savings input
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
    //----------------------------------------------------------------------------------------
    // Inserts the savings amount to the savings table
    private fun addSavings(amount: Double) {
        lifecycleScope.launch {
            try {
                val savings = Savings(
                    userId = currentUserId,
                    amount = amount,
                    date   = System.currentTimeMillis()
                )

                // Add to database
                db.savingsDAO().insertSavings(savings)

                runOnUiThread {
                    Toast.makeText(
                        this@SavingsMainClass,
                        "Added R$amount",
                        Toast.LENGTH_SHORT
                    ).show()
                    etSaveAmount.text.clear()
                    updateSavingsDisplay()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SavingsMainClass,
                        "Failed to save",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    //------------------------------------------------------------------------------------------
    // Updates the savings display to the most recent
    private fun updateSavingsDisplay() {
        lifecycleScope.launch {
            try {
                val total = db.savingsDAO().getTotalSavings(currentUserId) ?: 0.0
                val percent = ((total / savingsGoal) * 100)
                    .coerceAtMost(100.0)
                    .toInt()

                runOnUiThread {
                    tvTotalSavings.text   = String.format("R %.2f", total)
                    progressBarSavings.progress = percent
                    tvProgressPercent.text     = "$percent%"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SavingsMainClass,
                        "Failed to update display",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
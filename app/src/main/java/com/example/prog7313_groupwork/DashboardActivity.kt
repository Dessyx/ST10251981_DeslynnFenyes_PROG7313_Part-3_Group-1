package com.example.prog7313_groupwork

import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.AdapterView
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {
    private lateinit var monthSpinner: Spinner
    private lateinit var spendingTrendsText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var dashSavingsText: TextView
    private lateinit var totalSpentText: TextView
    private lateinit var database: AstraDatabase
    private var currentUserId: Long = 1 // Should be set from login session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Initialize database
        database = AstraDatabase.getDatabase(this)

        // Initialize views
        monthSpinner = findViewById(R.id.monthSpinner)
        spendingTrendsText = findViewById(R.id.spendingTrendsTitle)
        backButton = findViewById(R.id.back_button)
        dashSavingsText = findViewById(R.id.dash_savings)
        totalSpentText = findViewById(R.id.totalSpent)

//----------------------------------------------------------------------------------
//                              Page navigation section 

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
// ----------------------------------------------------------------------------------

        // Set up month spinner listener
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = parent?.getItemAtPosition(position).toString()
                spendingTrendsText.text = "Spending trends for $selectedMonth"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Update displays
        updateSavingsDisplay()
        updateTotalSpentDisplay()
    }

    private fun updateSavingsDisplay() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get total savings from database
                val totalSavings = database.savingsDAO().getTotalSavings(currentUserId) ?: 0.0
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    dashSavingsText.text = String.format("R %.2f", totalSavings)
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    private fun updateTotalSpentDisplay() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get total expenses from database
                val totalExpenses = database.expenseDAO().getTotalExpenseForUser(currentUserId) ?: 0.0
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    totalSpentText.text = String.format("R %.2f", totalExpenses)
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update displays whenever the activity resumes
        updateSavingsDisplay()
        updateTotalSpentDisplay()
    }
} 
package com.example.prog7313_groupwork

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Budget
import com.example.prog7313_groupwork.entities.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class SpendingGoalsActivity : AppCompatActivity() {
    private lateinit var monthlyGoalText: TextView
    private lateinit var totalSpentText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var categoriesText: TextView
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    private val db by lazy { AstraDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spending_goals)

        // Initialize views
        monthlyGoalText = findViewById(R.id.monthlyGoalText)
        totalSpentText = findViewById(R.id.totalSpentText)
        progressBar = findViewById(R.id.spendingProgressBar)
        progressText = findViewById(R.id.progressText)
        categoriesText = findViewById(R.id.categoriesText)
        
        // Set up back button
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Load the spending data
        loadSpendingData()
    }

    private fun loadSpendingData() {
        val currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L).toInt()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get budget and categories
                val budget = db.budgetDAO().getCurrentBudget(currentUserId).first()
                val categories = db.categoryDAO().getAllCategories()
                
                // Calculate total spent and categories within limit
                var totalSpent = 0.0
                var categoriesWithinLimit = 0
                val categoryDetails = StringBuilder()
                
                categories.forEach { category ->
                    val spent = category.spent ?: 0.0
                    val limit = category.categoryLimit.toDoubleOrNull() ?: 0.0
                    totalSpent += spent
                    
                    // Add category details to the text
                    categoryDetails.append("${category.categoryName}:\n")
                    categoryDetails.append("  Spent: ${currencyFormat.format(spent)}\n")
                    categoryDetails.append("  Limit: ${currencyFormat.format(limit)}\n")
                    categoryDetails.append("  Status: ${if (spent <= limit) "✅ Within limit" else "⚠️ Over limit"}\n\n")
                    
                    if (spent <= limit) {
                        categoriesWithinLimit++
                    }
                }

                val monthlyGoal = budget?.monthlyGoal ?: 0.0
                val progressPercentage = if (monthlyGoal > 0) {
                    ((totalSpent / monthlyGoal) * 100).toInt()
                } else 0

                // Update UI on the main thread
                withContext(Dispatchers.Main) {
                    // Update monthly goal
                    monthlyGoalText.text = currencyFormat.format(monthlyGoal)
                    
                    // Update total spent
                    totalSpentText.text = currencyFormat.format(totalSpent)
                    
                    // Update progress bar
                    progressBar.max = 100
                    progressBar.progress = progressPercentage.coerceAtMost(100)
                    
                    // Update progress text
                    progressText.text = "$progressPercentage% of budget used"
                    
                    // Update categories text
                    categoriesText.text = categoryDetails.toString()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Show error message if something goes wrong
                    categoriesText.text = "Error loading data. Please try again."
                }
            }
        }
    }
} 
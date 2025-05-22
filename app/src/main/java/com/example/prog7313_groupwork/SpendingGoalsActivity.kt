package com.example.prog7313_groupwork

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
        
        // Back button
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
                val categoryDetails = SpannableStringBuilder()
                
                categories.forEach { category ->
                    val spent = category.spent ?: 0.0
                    val limit = category.categoryLimit.toDoubleOrNull() ?: 0.0
                    totalSpent += spent
                    
                    // Add category name
                    val categoryName = SpannableString("${category.categoryName}:\n")
                    categoryDetails.append(categoryName)
                    
                    // Add spent amount
                    val spentText = "  Spent: ${currencyFormat.format(spent)}\n"
                    val spentSpannable = SpannableString(spentText)
                    if (spent > limit) {
                        spentSpannable.setSpan(ForegroundColorSpan(Color.RED), 0, spentText.length, 0)
                        spentSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, spentText.length, 0)
                    }
                    categoryDetails.append(spentSpannable)
                    
                    // Add limit
                    categoryDetails.append("  Limit: ${currencyFormat.format(limit)}\n")
                    
                    // Add status with warning for overspent categories
                    val statusText = if (spent <= limit) {
                        "  Status: ✅ Within limit\n\n"
                    } else {
                        "  Status: ⚠️ OVER LIMIT!\n\n"
                    }
                    val statusSpannable = SpannableString(statusText)
                    if (spent > limit) {
                        statusSpannable.setSpan(ForegroundColorSpan(Color.RED), 0, statusText.length, 0)
                        statusSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, statusText.length, 0)
                    }
                    categoryDetails.append(statusSpannable)
                    
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
                    
                    // Update categories text with formatted content
                    categoriesText.text = categoryDetails
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
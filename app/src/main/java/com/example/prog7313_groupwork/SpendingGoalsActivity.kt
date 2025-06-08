package com.example.prog7313_groupwork

// Imports
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.entities.Budget
import com.example.prog7313_groupwork.entities.Category
import com.example.prog7313_groupwork.firebase.FirebaseCategoryService
import com.example.prog7313_groupwork.firebase.FirebaseBudgetService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

// ------------------------------------ Spending Goals Activity Class ----------------------------------------
// This activity displays the user's spending goals, budget progress, and category-wise spending status
class SpendingGoalsActivity : AppCompatActivity() {
    // UI Components
    private lateinit var monthlyGoalText: TextView
    private lateinit var totalSpentText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var categoriesText: TextView
    
    // Firebase Services
    private lateinit var categoryService: FirebaseCategoryService
    private lateinit var firebaseBudgetService: FirebaseBudgetService
    
    // Currency formatter for South African Rand
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    // ------------------------------------------------------------------------------------
    // Initialize the activity and set up the UI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spending_goals)

        // Initialize Firebase services
        categoryService = FirebaseCategoryService()
        firebaseBudgetService = FirebaseBudgetService()

        // Initialize UI components
        initializeViews()
        
        // Set up back button
        setupBackButton()

        // Load spending data
        loadSpendingData()
    }

    // ------------------------------------------------------------------------------------
    // Initialize all UI components
    private fun initializeViews() {
        monthlyGoalText = findViewById(R.id.monthlyGoalText)
        totalSpentText = findViewById(R.id.totalSpentText)
        progressBar = findViewById(R.id.spendingProgressBar)
        progressText = findViewById(R.id.progressText)
        categoriesText = findViewById(R.id.categoriesText)
    }

    // ------------------------------------------------------------------------------------
    // Set up back button functionality
    private fun setupBackButton() {
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    // ------------------------------------------------------------------------------------
    // Load and display spending data including budget, categories, and progress
    private fun loadSpendingData() {
        val currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Check if user is logged in
                if (currentUserId == -1L) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SpendingGoalsActivity, "Please log in to view spending goals", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Fetch budget and categories from Firebase
                val budget = firebaseBudgetService.getCurrentBudget(currentUserId.toInt()).first()
                val categories = categoryService.getCategoriesForUser(currentUserId)
                
                // Calculate total spending and track categories within limit
                var totalSpent = 0.0
                var categoriesWithinLimit = 0
                val categoryDetails = SpannableStringBuilder()
                
                // Process each category
                categories.forEach { category ->
                    val spent = category.spent ?: 0.0
                    val limit = category.categoryLimit.toDoubleOrNull() ?: 0.0
                    totalSpent += spent
                    
                    // Add category name
                    val categoryName = SpannableString("${category.categoryName}:\n")
                    categoryDetails.append(categoryName)
                    
                    // Add spent amount with color coding for overspending
                    val spentText = "  Spent: ${currencyFormat.format(spent)}\n"
                    val spentSpannable = SpannableString(spentText)
                    if (spent > limit) {
                        spentSpannable.setSpan(ForegroundColorSpan(Color.RED), 0, spentText.length, 0)
                        spentSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, spentText.length, 0)
                    }
                    categoryDetails.append(spentSpannable)
                    
                    // Add category limit
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

                // Calculate progress percentage
                val monthlyGoal = budget?.monthlyGoal ?: 0.0
                val progressPercentage = if (monthlyGoal > 0) {
                    ((totalSpent / monthlyGoal) * 100).toInt()
                } else 0

                // Update UI on the main thread
                withContext(Dispatchers.Main) {
                    updateUI(monthlyGoal, totalSpent, progressPercentage, categoryDetails)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    categoriesText.text = "Error loading data. Please try again."
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // Update all UI components with the calculated values
    private fun updateUI(monthlyGoal: Double, totalSpent: Double, progressPercentage: Int, categoryDetails: SpannableStringBuilder) {
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
}
// -----------------------------------<<< End Of File >>>------------------------------------------ 
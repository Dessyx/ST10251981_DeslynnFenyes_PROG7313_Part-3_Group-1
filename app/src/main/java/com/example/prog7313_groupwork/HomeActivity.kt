package com.example.prog7313_groupwork

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.ProgressBar
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeActivity : AppCompatActivity() {
    private lateinit var budgetGoalText: TextView
    private lateinit var overspentCategoriesText: TextView
    private lateinit var savingProgressBar: ProgressBar
    private lateinit var progressPercentageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Initialize views
        budgetGoalText = findViewById(R.id.textView3)
        overspentCategoriesText = findViewById(R.id.overspentCategories)
        savingProgressBar = findViewById(R.id.savingProgressBar)
        progressPercentageText = findViewById(R.id.progressPercentage)

        // Get DAOs
        val db = AstraDatabase.getDatabase(this)
        val budgetDAO = db.budgetDAO()
        val categoryDAO = db.CategoryDAO()

        // Observe current budget and categories
        lifecycleScope.launch {
            // Get current month's budget
            budgetDAO.getCurrentBudget().collect { budget ->
                val budgetAmount = budget?.monthlyGoal ?: 0.0
                budgetGoalText.text = "R%.2f".format(budgetAmount)

                // Get all categories
                val categories = categoryDAO.getAllCategories()
                
                // Calculate overspent categories
                val overspentCategories = categories.filter { category ->
                    val spent = category.spent ?: 0.0
                    val limit = category.categoryLimit.toDoubleOrNull() ?: 0.0
                    spent > limit
                }

                // Display overspent categories
                if (overspentCategories.isNotEmpty()) {
                    val overspentText = overspentCategories.joinToString(", ") { it.categoryName }
                    overspentCategoriesText.text = overspentText
                    overspentCategoriesText.setTextColor(getColor(R.color.red))
                } else {
                    overspentCategoriesText.text = "None"
                    overspentCategoriesText.setTextColor(getColor(android.R.color.black))
                }

                // Calculate saving progress
                val totalSpent = categories.sumOf { it.spent ?: 0.0 }
                val progress = if (budgetAmount > 0) {
                    ((1 - (totalSpent / budgetAmount)) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }

                // Update progress bar and percentage
                savingProgressBar.progress = progress
                progressPercentageText.text = "$progress%"
            }
        }
    }
}

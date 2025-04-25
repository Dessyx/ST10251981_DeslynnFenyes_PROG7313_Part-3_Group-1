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
import android.content.Intent
import android.widget.ImageButton
import android.widget.Button
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {
    private lateinit var budgetGoalText: TextView
    private lateinit var overspentCategoriesText: TextView
    private lateinit var savingProgressBar: ProgressBar
    private lateinit var progressPercentageText: TextView
    private lateinit var activeBalanceValue: TextView
    private lateinit var database: AstraDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Initialize views
        budgetGoalText = findViewById(R.id.textView3)
        overspentCategoriesText = findViewById(R.id.overspentCategories)
        savingProgressBar = findViewById(R.id.savingProgressBar)
        progressPercentageText = findViewById(R.id.progressPercentage)
        activeBalanceValue = findViewById(R.id.activeBalanceValue)
        database = AstraDatabase.getDatabase(this)

        // Calculate and update active balance
        updateActiveBalance()

//----------------------------------------------------------------------------------
//                              Page navigation section 

        // Setup Add Category button click
        val addCategoryButton = findViewById<ImageButton>(R.id.btnAddCategory)
        addCategoryButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }

        // Setup Dashboard button click
        val dashboardButton = findViewById<Button>(R.id.dashboardbtn)
        dashboardButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

// ----------------------------------------------------------------------------------


        // Get DAOs
        val db = AstraDatabase.getDatabase(this)
        val budgetDAO = db.budgetDAO()
        val categoryDAO = db.categoryDAO()

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
                    overspentCategoriesText.setTextColor(getColor(android.R.color.holo_red_dark))
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

    private fun updateActiveBalance() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get all active expenses
                val expenses = database.expenseDAO().getAllActiveExpenses()
                val totalExpenses = expenses.sumOf { it.amount }

                // Get all active income
                val incomes = database.incomeDAO().getAllActiveIncome()
                val totalIncome = incomes.sumOf { it.amount }

                // Calculate active balance (income - expenses)
                val activeBalance = totalIncome - totalExpenses

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    activeBalanceValue.text = "R %.2f".format(activeBalance)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error calculating balance: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update active balance whenever the activity resumes
        updateActiveBalance()
    }
}

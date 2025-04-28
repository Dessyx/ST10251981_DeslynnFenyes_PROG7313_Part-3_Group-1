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
import com.example.prog7313_groupwork.repository.AwardsMainClass
import com.example.prog7313_groupwork.repository.ProfileMainClass
import com.example.prog7313_groupwork.repository.SavingsMainClass
import com.example.prog7313_groupwork.repository.SettingsMainClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.adapters.HistoryAdapter
import com.example.prog7313_groupwork.adapters.HistoryItem
import com.example.prog7313_groupwork.entities.IncomeDAO
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var budgetGoalText: TextView
    private lateinit var overspentCategoriesText: TextView
    private lateinit var savingProgressBar: ProgressBar
    private lateinit var progressPercentageText: TextView
    private lateinit var activeBalanceValue: TextView
    private lateinit var greetingText: TextView
    private lateinit var database: AstraDatabase
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private var currentUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Get user ID from shared preferences
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L)

        if (currentUserId == -1L) {
            // If not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize views
        budgetGoalText = findViewById(R.id.textView3)
        overspentCategoriesText = findViewById(R.id.overspentCategories)
        savingProgressBar = findViewById(R.id.savingProgressBar)
        progressPercentageText = findViewById(R.id.progressPercentage)
        activeBalanceValue = findViewById(R.id.activeBalanceValue)
        greetingText = findViewById(R.id.greetingText)
        database = AstraDatabase.getDatabase(this)

        // Setup history RecyclerView
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        historyAdapter = HistoryAdapter()
        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = historyAdapter
        }

        // Update user greeting
        updateUserGreeting()

        // Calculate and update active balance
        updateActiveBalance()

        // Load transaction history
        loadTransactionHistory()

//----------------------------------------------------------------------------------
//                              Page navigation section 

        // Setup Add Expense button click
        val addExpenseButton = findViewById<ImageButton>(R.id.btnAddExpense)
        addExpenseButton.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        // Setup Add Income button click
        val addIncomeButton = findViewById<ImageButton>(R.id.btnAddIncome)
        addIncomeButton.setOnClickListener {
            val intent = Intent(this, AddIncome::class.java)
            startActivity(intent)
        }

        // Setup Set Budget button click
        val setBudgetButton = findViewById<ImageButton>(R.id.btnSetBudget)
        setBudgetButton.setOnClickListener {
            val intent = Intent(this, SetBudgetActivity::class.java)
            startActivity(intent)
        }

        // Setup Debt Planner button click
        val debtPlannerButton = findViewById<ImageButton>(R.id.btnDebtPlanner)
        debtPlannerButton.setOnClickListener {
            val intent = Intent(this, DebtPlanner::class.java)
            startActivity(intent)
        }

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

        // Setup Savings button click
        val savingsButton = findViewById<ImageButton>(R.id.btnSavings)
        savingsButton.setOnClickListener {
            val intent = Intent(this, SavingsMainClass::class.java)
            startActivity(intent)
        }

        // Setup Awards button click
        val awardsButton = findViewById<ImageButton>(R.id.btnAwards)
        awardsButton.setOnClickListener {
            val intent = Intent(this, AwardsMainClass::class.java)
            startActivity(intent)
        }

        // Setup Profile button click
        val profileButton = findViewById<ImageButton>(R.id.btnProfile)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileMainClass::class.java)
            startActivity(intent)
        }

        // Setup Settings button click
        val settingsButton = findViewById<ImageButton>(R.id.btnSettings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsMainClass::class.java)
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

    private fun updateUserGreeting() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get user from database
                val user = database.userDAO().getUserById(currentUserId)
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    greetingText.text = "Hello ${user?.NameSurname ?: "User"}"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    greetingText.text = "Hello User"
                }
            }
        }
    }

    private fun updateActiveBalance() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get all active expenses for the current user
                val expenses = database.expenseDAO().getAllExpensesForUser(currentUserId).first()
                val totalExpenses = expenses.sumOf { it.amount }

                // Get all active income for the current user
                val incomes = database.incomeDAO().getAllIncomeForUser(currentUserId).first()
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

    private fun loadTransactionHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get all expenses and incomes
                val expenses = database.expenseDAO().getAllExpensesForUser(currentUserId).first()
                val incomes = database.incomeDAO().getAllIncomeForUser(currentUserId).first()

                // Convert to HistoryItems
                val historyItems = mutableListOf<HistoryItem>()
                val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

                // Add expenses
                expenses.forEach { expense ->
                    historyItems.add(HistoryItem(
                        title = expense.description,
                        category = expense.category,
                        amount = expense.amount,
                        date = dateFormatter.format(Date(expense.date)),
                        isExpense = true
                    ))
                }

                // Add incomes
                incomes.forEach { income ->
                    historyItems.add(HistoryItem(
                        title = income.description,
                        category = income.category,
                        amount = income.amount,
                        date = dateFormatter.format(Date(income.date)),
                        isExpense = false
                    ))
                }

                // Sort by date (most recent first)
                val sortedItems = historyItems.sortedByDescending { 
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(it.date)?.time ?: 0L 
                }

                // Update the adapter on the main thread
                withContext(Dispatchers.Main) {
                    historyAdapter.updateHistory(sortedItems)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error loading history: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update displays whenever the activity resumes
        updateActiveBalance()
        updateUserGreeting()
        loadTransactionHistory() // Reload history when returning to the activity
    }
}

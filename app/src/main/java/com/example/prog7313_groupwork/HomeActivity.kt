package com.example.prog7313_groupwork

// imports
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.adapters.HistoryAdapter
import com.example.prog7313_groupwork.adapters.HistoryItem
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.repository.AwardsMainClass
import com.example.prog7313_groupwork.repository.ProfileMainClass
import com.example.prog7313_groupwork.repository.SavingsMainClass
import com.example.prog7313_groupwork.repository.SettingsMainClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var budgetGoalText: TextView
    private lateinit var overspentCategoriesText: TextView
    private lateinit var savingProgressBar: ProgressBar       // Declaring variables
    private lateinit var progressPercentageText: TextView
    private lateinit var activeBalanceValue: TextView
    private lateinit var greetingText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    private val db by lazy { AstraDatabase.getDatabase(this) }
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    private var currentUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        budgetGoalText          = findViewById(R.id.textView3)
        overspentCategoriesText = findViewById(R.id.overspentCategories)
        savingProgressBar       = findViewById(R.id.savingProgressBar)
        progressPercentageText  = findViewById(R.id.progressPercentage)
        activeBalanceValue      = findViewById(R.id.activeBalanceValue)
        greetingText            = findViewById(R.id.greetingText)
        historyRecyclerView     = findViewById(R.id.historyRecyclerView)

        historyAdapter = HistoryAdapter()
        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter       = historyAdapter
        }

        // Gets the current user
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L)
        if (currentUserId == -1L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // -----------------------------------------------------------------------------------------
        // Navigation section
        findViewById<ImageButton>(R.id.btnAddExpense).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnAddIncome).setOnClickListener {
            startActivity(Intent(this, AddIncome::class.java))
        }
        findViewById<ImageButton>(R.id.btnSetBudget).setOnClickListener {
            startActivity(Intent(this, SetBudgetActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnDebtPlanner).setOnClickListener {
            startActivity(Intent(this, DebtPlanner::class.java))
        }
        findViewById<ImageButton>(R.id.btnAddCategory).setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }
        findViewById<Button>(R.id.dashboardbtn).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSavings).setOnClickListener {
            startActivity(Intent(this, SavingsMainClass::class.java))
        }
        findViewById<ImageButton>(R.id.btnAwards).setOnClickListener {
            startActivity(Intent(this, AwardsMainClass::class.java))
        }
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileMainClass::class.java))
        }
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsMainClass::class.java))
        }

        loadBudgetAndCategories()
        updateActiveBalance()
        updateGreeting()
        loadTransactionHistory()
    }

    //----------------------------------------------------------------------------------------------
    // Displays overspent categories and savings progress
    private fun loadBudgetAndCategories() {
        val userInt   = currentUserId.toInt()
        val budgetDAO = db.budgetDAO()
        val catDAO    = db.categoryDAO()

        lifecycleScope.launch {
            val categories = catDAO.getAllCategories()

            // fetches category data
            budgetDAO.getCurrentBudget(userInt).collect { budget ->
                val goal       = budget?.monthlyGoal ?: 0.0
                val totalSpent = categories.sumOf { it.spent ?: 0.0 }
                val progress   = if (goal > 0.0) {
                    ((1 - (totalSpent / goal)) * 100).toInt().coerceIn(0, 100)
                } else 0

                val overspentList = categories
                    .filter { (it.spent ?: 0.0) > (it.categoryLimit.toDoubleOrNull() ?: 0.0) }
                    .joinToString(", ") { it.categoryName }

                withContext(Dispatchers.Main) {
                    budgetGoalText.text = currencyFormat.format(goal)

                    // Update savings progress
                    updateSavingsProgress()

                    // Flags overspent categories red
                    if (overspentList.isNotEmpty()) {
                        overspentCategoriesText.text = overspentList
                        overspentCategoriesText.setTextColor(
                            getColor(android.R.color.holo_red_dark)
                        )
                    } else {
                        overspentCategoriesText.text = "None"
                        overspentCategoriesText.setTextColor(
                            getColor(android.R.color.black)
                        )
                    }
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // Calculates and display savings progress
    private fun updateSavingsProgress() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val total = db.savingsDAO().getTotalSavings(currentUserId) ?: 0.0

                val monthlySavingsGoal = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getFloat("monthly_savings_goal", 0f).toDouble()

                val percent = if (monthlySavingsGoal > 0) {
                    ((total / monthlySavingsGoal) * 100)
                        .coerceAtMost(100.0)
                        .toInt()
                } else 0

                withContext(Dispatchers.Main) {
                    savingProgressBar.progress = percent
                    progressPercentageText.text = "$percent%"
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error updating savings progress: ${e.message}", e)
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // Displays the current balance as the user spends money and recieves income
    private fun updateActiveBalance() {
        lifecycleScope.launch(Dispatchers.IO) {
            val expenses = db.expenseDAO()
                .getAllExpensesForUser(currentUserId).first()
            val incomes  = db.incomeDAO()
                .getAllIncomeForUser(currentUserId).first()
            val balance  = incomes.sumOf { it.amount } - expenses.sumOf { it.amount }

            withContext(Dispatchers.Main) {
                activeBalanceValue.text = "R %.2f".format(balance)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Greets user using their entered name and surname
    private fun updateGreeting() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDAO().getUserById(currentUserId)
            withContext(Dispatchers.Main) {
                greetingText.text = "Hello ${user?.NameSurname ?: "User"}"
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // Displays the users income and expense history
    private fun loadTransactionHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            val expenses = db.expenseDAO()
                .getAllExpensesForUser(currentUserId).first()
            val incomes  = db.incomeDAO()
                .getAllIncomeForUser(currentUserId).first()
            val fmt      = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val items    = mutableListOf<HistoryItem>()

            for (e in expenses) {
                items += HistoryItem(
                    title     = e.description,
                    category  = e.category,
                    amount    = e.amount,
                    date      = fmt.format(Date(e.date)),
                    isExpense = true
                )
            }
            for (i in incomes) {
                items += HistoryItem(
                    title     = i.description,
                    category  = i.category,
                    amount    = i.amount,
                    date      = fmt.format(Date(i.date)),
                    isExpense = false
                )
            }
            items.sortByDescending { hi -> fmt.parse(hi.date)?.time ?: 0L }

            withContext(Dispatchers.Main) {
                historyAdapter.updateHistory(items)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateActiveBalance()
        updateGreeting()
        loadTransactionHistory()
        loadBudgetAndCategories()
        updateSavingsProgress()
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
package com.example.prog7313_groupwork

// imports
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.adapters.HistoryAdapter
import com.example.prog7313_groupwork.adapters.HistoryItem
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.firebase.FirebaseCategoryService
import com.example.prog7313_groupwork.firebase.FirebaseExpenseService
import com.example.prog7313_groupwork.firebase.FirebaseSavingsService
import com.example.prog7313_groupwork.firebase.FirebaseIncomeService
import com.example.prog7313_groupwork.firebase.FirebaseUserService
import com.example.prog7313_groupwork.firebase.FirebaseBudgetService
import com.example.prog7313_groupwork.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var budgetGoalText: TextView
    private lateinit var overspentCategoriesText: TextView
    private lateinit var savingProgressBar: ProgressBar
    private lateinit var progressPercentageText: TextView
    private lateinit var activeBalanceValue: TextView
    private lateinit var greetingText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    private val db by lazy { AstraDatabase.getDatabase(this) }
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    private var currentUserId: Long = -1L

    // Firebase service instances
    private val firebaseCategoryService = FirebaseCategoryService()
    private val firebaseExpenseService = FirebaseExpenseService()
    private val firebaseSavingsService = FirebaseSavingsService()
    private val firebaseIncomeService = FirebaseIncomeService()
    private val firebaseUserService = FirebaseUserService()
    private val firebaseBudgetService = FirebaseBudgetService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        budgetGoalText = findViewById(R.id.textView3)
        overspentCategoriesText = findViewById(R.id.overspentCategories)
        savingProgressBar = findViewById(R.id.savingProgressBar)
        progressPercentageText = findViewById(R.id.progressPercentage)
        activeBalanceValue = findViewById(R.id.activeBalanceValue)
        greetingText = findViewById(R.id.greetingText)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        historyAdapter = HistoryAdapter()
        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = historyAdapter
        }

        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L)
        if (currentUserId == -1L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

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

    private fun loadBudgetAndCategories() {
        val userInt = currentUserId.toInt()

        lifecycleScope.launch {
            try {
                // Using FirebaseCategoryService instead of local DAO
                val firebaseCategories = withContext(Dispatchers.IO) {
                    firebaseCategoryService.getCategoriesForUser(currentUserId)
                }

                firebaseBudgetService.getCurrentBudget(userInt).collect { budget ->
                    val goal = budget?.monthlyGoal ?: 0.0
                    val totalSpent = firebaseCategories.sumOf { it.spent ?: 0.0 }

                    val progress = if (goal > 0.0) {
                        ((1 - (totalSpent / goal)) * 100).toInt().coerceIn(0, 100)
                    } else 0

                    val overspentList = firebaseCategories
                        .filter { (it.spent ?: 0.0) > (it.categoryLimit.toDoubleOrNull() ?: 0.0) }
                        .joinToString(", ") { it.categoryName }

                    withContext(Dispatchers.Main) {
                        budgetGoalText.text = currencyFormat.format(goal)
                        updateSavingsProgress()

                        if (overspentList.isNotEmpty()) {
                            overspentCategoriesText.text = overspentList
                            overspentCategoriesText.setTextColor(getColor(android.R.color.holo_red_dark))
                        } else {
                            overspentCategoriesText.text = "None"
                            overspentCategoriesText.setTextColor(getColor(android.R.color.black))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error loading categories: ${e.message}", e)
            }
        }
    }

    private fun updateSavingsProgress() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val total = firebaseSavingsService.getTotalSavings(currentUserId)
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error updating savings progress: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateActiveBalance() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getLong("current_user_id", -1L)

                if (userId == -1L) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeActivity, "Please log in to view balance", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val totalSpent = firebaseExpenseService.getTotalSpending(userId)
                val income = firebaseIncomeService.getTotalIncomeForUser(userId) ?: 0.0
                val savings = firebaseSavingsService.getTotalSavings(userId)
                val activeBalance = income - totalSpent - savings

                withContext(Dispatchers.Main) {
                    activeBalanceValue.text = "R %.2f".format(activeBalance)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error updating balance: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateGreeting() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = firebaseUserService.getUserById(currentUserId)
            withContext(Dispatchers.Main) {
                greetingText.text = "Hello ${user?.NameSurname ?: "User"}"
            }
        }
    }

    private fun loadTransactionHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val expenses = firebaseExpenseService.getExpensesByUser(currentUserId)
                val incomes = firebaseIncomeService.getAllIncomeForUser(currentUserId).first()
                val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val items = mutableListOf<HistoryItem>()

                for (e in expenses) {
                    items += HistoryItem(
                        title = e.description,
                        category = e.category,
                        amount = e.amount,
                        date = fmt.format(Date(e.date)),
                        isExpense = true
                    )
                }
                for (i in incomes) {
                    items += HistoryItem(
                        title = i.description,
                        category = i.category,
                        amount = i.amount,
                        date = fmt.format(Date(i.date)),
                        isExpense = false
                    )
                }
                items.sortByDescending { hi -> fmt.parse(hi.date)?.time ?: 0L }

                withContext(Dispatchers.Main) {
                    historyAdapter.updateHistory(items)
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error loading transaction history: ${e.message}", e)
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

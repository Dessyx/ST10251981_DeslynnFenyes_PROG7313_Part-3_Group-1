package com.example.prog7313_groupwork

// imports
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.adapters.CategorySpendingAdapter
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// --------------------- Functionality for activity_category_spending.xml ------------------------
class CategorySpendingActivity : AppCompatActivity() {

    private lateinit var categorySpendingAdapter: CategorySpendingAdapter
    private lateinit var periodText: TextView
    private lateinit var totalSpentText: TextView
    private var startDate: Calendar = Calendar.getInstance()   // Declaring varibales
    private var endDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_spending)

        periodText = findViewById(R.id.periodText)
        totalSpentText = findViewById(R.id.totalSpentText)

        // Sets the initial date range (current month)
        startDate.set(Calendar.DAY_OF_MONTH, 1)
        startDate.set(Calendar.HOUR_OF_DAY, 0)
        startDate.set(Calendar.MINUTE, 0)
        startDate.set(Calendar.SECOND, 0)
        startDate.set(Calendar.MILLISECOND, 0)

        endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDate.set(Calendar.HOUR_OF_DAY, 23)
        endDate.set(Calendar.MINUTE, 59)
        endDate.set(Calendar.SECOND, 59)
        endDate.set(Calendar.MILLISECOND, 999)

        updateDateDisplay()

        val recyclerView = findViewById<RecyclerView>(R.id.categorySpendingRecyclerView)
        categorySpendingAdapter = CategorySpendingAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategorySpendingActivity)
            adapter = categorySpendingAdapter
        }

        // --------------------------------------------------------------------------------------
        // On click listener
        val filterButton = findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener {
            showDateRangePicker()
        }

        // --------------------------------------------------------------------------------------
        // navigation
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        loadCategorySpending()
    }

    private fun showDateRangePicker() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        DatePickerDialog(
            this,
            { _, year, month, day ->
                startDate.set(year, month, day)
                startDate.set(Calendar.HOUR_OF_DAY, 0)
                startDate.set(Calendar.MINUTE, 0)
                startDate.set(Calendar.SECOND, 0)
                startDate.set(Calendar.MILLISECOND, 0)
                
                // Show second date picker for end date
                DatePickerDialog(
                    this,
                    { _, year, month, day ->
                        endDate.set(year, month, day)
                        endDate.set(Calendar.HOUR_OF_DAY, 23)
                        endDate.set(Calendar.MINUTE, 59)
                        endDate.set(Calendar.SECOND, 59)
                        endDate.set(Calendar.MILLISECOND, 999)
                        
                        updateDateDisplay()
                        loadCategorySpending()
                    },
                    endDate.get(Calendar.YEAR),
                    endDate.get(Calendar.MONTH),
                    endDate.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        periodText.text = "${dateFormat.format(startDate.time)} - ${dateFormat.format(endDate.time)}"
    }

    private fun loadCategorySpending() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getLong("current_user_id", -1L)

                if (userId == -1L) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CategorySpendingActivity, "Please log in to view spending", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val db = AstraDatabase.getDatabase(this@CategorySpendingActivity)
                val categories = db.categoryDAO().getAllCategories()
                val expenseDAO = db.expenseDAO()

                // Calculate spending for each category in the period that the user chose
                val categoriesWithSpending = categories.map { category ->
                    val expenses = expenseDAO.getExpensesByDateRange(
                        userId,
                        startDate.timeInMillis,
                        endDate.timeInMillis
                    ).first().filter { it.category == category.categoryName }
                    
                    val totalSpent = expenses.sumOf { it.amount }
                    category.copy(spent = totalSpent)
                }

                // Calculates the total spent in all categories
                val totalSpending = categoriesWithSpending.sumOf { it.spent ?: 0.0 }

                withContext(Dispatchers.Main) {
                    categorySpendingAdapter.updateCategories(categoriesWithSpending)
                    totalSpentText.text = "Total: R %.2f".format(totalSpending)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategorySpendingActivity, "Error loading spending: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
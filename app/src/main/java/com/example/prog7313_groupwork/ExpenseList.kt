package com.example.prog7313_groupwork

// imports
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.adapters.ExpenseAdapter
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------------------- Functionality for activity_expense_list.xml --------------------------------
class ExpenseList : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var filterButton: ImageButton
    private lateinit var periodText: TextView               // Declaration of variables
    private lateinit var database: AstraDatabase
    
    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)
        
        database = AstraDatabase.getDatabase(this)  // initialize database
        
        // Initialize views
        recyclerView = findViewById(R.id.expenseRecyclerView)
        filterButton = findViewById(R.id.filterButton)
        periodText = findViewById(R.id.periodText)

        expenseAdapter = ExpenseAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExpenseList)
            adapter = expenseAdapter
        }
        
        // Set default date range (current month)
        startDate.set(Calendar.DAY_OF_MONTH, 1)
        updatePeriodText()

        //-------------------------------------------------------------------------------------------
        // on click listeners
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        loadExpenses()

        //-------------------------------------------------------------------------------------------
        findViewById<ImageButton>(R.id.backButton)?.setOnClickListener {
            finish()
        }
    }

    //----------------------------------------------------------------------------------------------
    // Displays the option sto choose last month, last 3 months or custom period
    // to filter expenses by
    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_date_filter, null)
        dialog.setContentView(view)
        
        view.findViewById<View>(R.id.lastMonthButton).setOnClickListener {
            setLastMonthPeriod()
            dialog.dismiss()
        }
        
        view.findViewById<View>(R.id.last3MonthsButton).setOnClickListener {
            setLast3MonthsPeriod()
            dialog.dismiss()
        }
        
        view.findViewById<View>(R.id.customPeriodButton).setOnClickListener {
            showDateRangePicker()
            dialog.dismiss()
        }
        
        dialog.show()
    }

    // ---------------------------------------------------------------------------------------------
    // Filters expenses to show the last months
    private fun setLastMonthPeriod() {
        startDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        endDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.DATE, -1)
        }
        updatePeriodText()
        loadExpenses()
    }

    // ---------------------------------------------------------------------------------------------
    // Filters expenses to show the 3 last months
    private fun setLast3MonthsPeriod() {
        startDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -3)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        endDate = Calendar.getInstance()
        updatePeriodText()
        loadExpenses()
    }

    // ---------------------------------------------------------------------------------------------
    // Filters expenses to show the expenses within the users selected period
    private fun showDateRangePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                startDate.set(year, month, day)
                showEndDatePicker()
            },
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun showEndDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                endDate.set(year, month, day)
                updatePeriodText()
                loadExpenses()
            },
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    //----------------------------------------------------------------------------------------------
    private fun updatePeriodText() {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val startText = dateFormat.format(startDate.time)
        val endText = dateFormat.format(endDate.time)
        
        periodText.text = if (startText == endText) {
            startText
        } else {
            "$startText - $endText"
        }
    }

    // --------------------------------------------------------------------------------------------
    // Displays the expenses to the user
    private fun loadExpenses() {
        lifecycleScope.launch {
            try {
                val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getLong("current_user_id", -1L)

                if (userId == -1L) {
                    Toast.makeText(this@ExpenseList, "Please log in to view expenses", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val startDateLong = startDate.timeInMillis
                val endDateLong = endDate.timeInMillis
                
                database.expenseDAO().getExpensesByDateRange(userId, startDateLong, endDateLong)
                    .collect { expenses ->
                        if (expenses.isEmpty()) {
                            Toast.makeText(this@ExpenseList, "No expenses found for this period", Toast.LENGTH_SHORT).show()
                        }
                        expenseAdapter.submitList(expenses.sortedByDescending { it.date })
                    }
            } catch (e: Exception) {
                Toast.makeText(this@ExpenseList, "Error loading expenses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
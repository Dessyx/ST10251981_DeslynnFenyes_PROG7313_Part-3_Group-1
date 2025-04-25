package com.example.prog7313_groupwork

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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

class ExpenseList : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var filterButton: ImageButton
    private lateinit var periodText: TextView
    private lateinit var database: AstraDatabase
    
    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)
        
        database = AstraDatabase.getDatabase(this)
        
        // Initialize views
        recyclerView = findViewById(R.id.expenseRecyclerView)
        filterButton = findViewById(R.id.filterButton)
        periodText = findViewById(R.id.periodText)
        
        // Setup RecyclerView
        expenseAdapter = ExpenseAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExpenseList)
            adapter = expenseAdapter
        }
        
        // Set default date range (current month)
        startDate.set(Calendar.DAY_OF_MONTH, 1)
        updatePeriodText()
        
        // Setup click listeners
        filterButton.setOnClickListener {
            showFilterDialog()
        }
        
        // Load initial data
        loadExpenses()
        
        // Setup back button
        findViewById<ImageButton>(R.id.backButton)?.setOnClickListener {
            finish()
        }
    }
    
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
    
    private fun setLast3MonthsPeriod() {
        startDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -3)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        endDate = Calendar.getInstance()
        updatePeriodText()
        loadExpenses()
    }
    
    private fun showDateRangePicker() {
        // Show start date picker
        DatePickerDialog(
            this,
            { _, year, month, day ->
                startDate.set(year, month, day)
                // After selecting start date, show end date picker
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
    
    private fun loadExpenses() {
        lifecycleScope.launch {
            try {
                // Get the latest user from the database
                val currentUser = database.userDAO().getLatestUser()
                val userId = currentUser?.id ?: return@launch
                
                // Convert Calendar dates to Long timestamps
                val startDateLong = startDate.timeInMillis
                val endDateLong = endDate.timeInMillis
                
                database.expenseDAO().getExpensesByDateRange(userId, startDateLong, endDateLong)
                    .collect { expenses ->
                        expenseAdapter.submitList(expenses.sortedByDescending { it.date })
                    }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
package com.example.prog7313_groupwork

// imports
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.AdapterView
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import java.text.SimpleDateFormat
import java.util.*

//testing
// ----------------------- Functionality for dashboard.xml ------------------------------
class DashboardActivity : AppCompatActivity() {
    private lateinit var monthSpinner: Spinner
    private lateinit var spendingTrendsText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var dashSavingsText: TextView
    private lateinit var totalSpentText: TextView
    private lateinit var database: AstraDatabase
    private lateinit var barChart: BarChart
    private lateinit var secondBarChart: BarChart
    private lateinit var secondGraphText: TextView
    private var currentUserId: Long = 1
    private var selectedDate: Calendar = Calendar.getInstance()

    //-------------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Initialize database and views
        database = AstraDatabase.getDatabase(this)

        // Get current user ID from SharedPreferences
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L)

        if (currentUserId == -1L) {
            Toast.makeText(this, "Please log in to view dashboard", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        monthSpinner = findViewById(R.id.monthSpinner)
        spendingTrendsText = findViewById(R.id.spendingTrendsTitle)
        backButton = findViewById(R.id.back_button)
        dashSavingsText = findViewById(R.id.dash_savings)
        totalSpentText = findViewById(R.id.totalSpent)
        barChart = findViewById(R.id.CatGraph)
        secondBarChart = findViewById(R.id.secondGraph)
        secondGraphText = findViewById(R.id.spendingTrendsTitle)

        // Set up back button click listener
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupBarChart()
        setupSecondBarChart()
        setupDatePicker()

        // Set current month as default selection
        val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
        val months = resources.getStringArray(R.array.months_array)
        val currentMonthIndex = months.indexOf(currentMonth)
        if (currentMonthIndex != -1) {
            monthSpinner.setSelection(currentMonthIndex)
        }

        // Month spinner listener
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = parent?.getItemAtPosition(position).toString()
                updateBarChart(selectedMonth)
                updateTotalSpentDisplay(selectedMonth)
                // Reset selected date to first day of selected month
                val calendar = Calendar.getInstance()
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
                calendar.time = monthFormat.parse(selectedMonth) ?: Date()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                selectedDate = calendar
                updateSecondBarChart()
                updateDateDisplay()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        // Update displays
        updateSavingsDisplay()
        updateTotalSpentDisplay(monthSpinner.selectedItem.toString())
        updateBarChart(monthSpinner.selectedItem.toString())
        updateSecondBarChart()
        updateDateDisplay()
    }

    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setScaleEnabled(true)
            setPinchZoom(false)
            
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                isEnabled = true
                textSize = 12f
            }
        }
    }

    private fun setupSecondBarChart() {
        secondBarChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setScaleEnabled(true)
            setPinchZoom(false)
            
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                isEnabled = true
                textSize = 12f
            }
        }
    }

    private fun setupDatePicker() {
        spendingTrendsText.setOnClickListener {
            val year = selectedDate.get(Calendar.YEAR)
            val month = selectedDate.get(Calendar.MONTH)
            val day = selectedDate.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateSecondBarChart()
                updateDateDisplay()
            }, year, month, day).show()
        }
    }

    private fun updateBarChart(selectedMonth: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get all categories
                val categories = database.categoryDAO().getAllCategories()
                val entries = mutableListOf<BarEntry>()
                val limitEntries = mutableListOf<BarEntry>()
                val labels = mutableListOf<String>()

                // Get the month number (1-12) from the month name
                val calendar = Calendar.getInstance()
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
                calendar.time = monthFormat.parse(selectedMonth) ?: Date()
                val monthNumber = calendar.get(Calendar.MONTH) + 1 // Adding 1 because Calendar.MONTH is 0-based

                // Get the year
                val year = Calendar.getInstance().get(Calendar.YEAR)

                // Calculate start and end timestamps for the selected month
                calendar.set(year, monthNumber - 1, 1, 0, 0, 0)
                val startTimestamp = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                val endTimestamp = calendar.timeInMillis

                // Get expenses for the selected month
                val expenses = database.expenseDAO().getExpensesForUserInDateRange(currentUserId, startTimestamp, endTimestamp)

                // Calculate spent amount for each category
                categories.forEachIndexed { index, category ->
                    val categoryExpenses = expenses.filter { it.category == category.categoryName }
                    val spent = categoryExpenses.sumOf { it.amount }
                    val limit = category.categoryLimit.toDoubleOrNull() ?: 0.0
                    
                    entries.add(BarEntry(index.toFloat(), spent.toFloat()))
                    limitEntries.add(BarEntry(index.toFloat(), limit.toFloat()))
                    labels.add(category.categoryName)
                }

                val spentDataSet = BarDataSet(entries, "Spent").apply {
                    color = android.graphics.Color.parseColor("#E91E63")
                }

                val limitDataSet = BarDataSet(limitEntries, "Limit").apply {
                    color = android.graphics.Color.parseColor("#2196F3")
                }

                val data = BarData(spentDataSet, limitDataSet).apply {
                    barWidth = 0.3f
                    groupBars(0f, 0.1f, 0.1f)
                }

                withContext(Dispatchers.Main) {
                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    barChart.data = data
                    barChart.invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error updating chart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateSecondBarChart() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get all categories
                val categories = database.categoryDAO().getAllCategories()
                val entries = mutableListOf<BarEntry>()
                val limitEntries = mutableListOf<BarEntry>()
                val labels = mutableListOf<String>()

                // Calculate start and end timestamps for the selected day
                val startOfDay = Calendar.getInstance().apply {
                    time = selectedDate.time
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfDay = Calendar.getInstance().apply {
                    time = selectedDate.time
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                // Get expenses for the selected day
                val expenses = database.expenseDAO().getExpensesForUserInDateRange(currentUserId, startOfDay, endOfDay)

                // Calculate spent amount for each category
                categories.forEachIndexed { index, category ->
                    val categoryExpenses = expenses.filter { it.category == category.categoryName }
                    val spent = categoryExpenses.sumOf { it.amount }
                    val limit = category.categoryLimit.toDoubleOrNull() ?: 0.0
                    
                    entries.add(BarEntry(index.toFloat(), spent.toFloat()))
                    limitEntries.add(BarEntry(index.toFloat(), limit.toFloat()))
                    labels.add(category.categoryName)
                }

                val spentDataSet = BarDataSet(entries, "Spent").apply {
                    color = android.graphics.Color.parseColor("#fdcfe5") // Green color for spent
                    valueTextColor = android.graphics.Color.parseColor("#f7529d")
                    valueTextSize = 10f
                }

                val limitDataSet = BarDataSet(limitEntries, "Limit").apply {
                    color = android.graphics.Color.parseColor("#cae5bb") // Orange color for limit
                    valueTextColor = android.graphics.Color.parseColor("#0ccd17")
                    valueTextSize = 10f
                }

                val data = BarData(spentDataSet, limitDataSet).apply {
                    barWidth = 0.3f
                    groupBars(0f, 0.1f, 0.1f)
                }

                withContext(Dispatchers.Main) {
                    secondBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    secondBarChart.data = data
                    secondBarChart.invalidate()

                    // Update the title to show the selected date
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    spendingTrendsText.text = dateFormat.format(selectedDate.time)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error updating daily chart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTotalSpentDisplay(selectedMonth: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get the month number (1-12) from the month name
                val calendar = Calendar.getInstance()
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
                calendar.time = monthFormat.parse(selectedMonth) ?: Date()
                val monthNumber = calendar.get(Calendar.MONTH) + 1

                // Get the year
                val year = Calendar.getInstance().get(Calendar.YEAR)

                // Calculate start and end timestamps for the selected month
                calendar.set(year, monthNumber - 1, 1, 0, 0, 0)
                val startTimestamp = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                val endTimestamp = calendar.timeInMillis

                // Get total expenses for the selected month
                val totalExpenses = database.expenseDAO()
                    .getTotalExpenseForUserInDateRange(currentUserId, startTimestamp, endTimestamp) ?: 0.0

                withContext(Dispatchers.Main) {
                    totalSpentText.text = String.format("R %.2f", totalExpenses)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error updating total spent: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // Fetches and displays the total savings
    private fun updateSavingsDisplay() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val totalSavings = database.savingsDAO().getTotalSavings(currentUserId) ?: 0.0
                withContext(Dispatchers.Main) {
                    dashSavingsText.text = String.format("R %.2f", totalSavings)
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        // Set the year to current year
        selectedDate.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
        spendingTrendsText.text = dateFormat.format(selectedDate.time)
    }

    override fun onResume() {
        super.onResume()
        updateSavingsDisplay()
        updateTotalSpentDisplay(monthSpinner.selectedItem.toString())
        updateBarChart(monthSpinner.selectedItem.toString())
        updateSecondBarChart()
    }
}

// -----------------------------------<<< End Of File >>>------------------------------------------
package com.example.prog7313_groupwork

// imports
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import android.widget.ImageButton
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
    private var currentUserId: Long = 1

    //-------------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Initialize database and views
        database = AstraDatabase.getDatabase(this)

        monthSpinner = findViewById(R.id.monthSpinner)
        spendingTrendsText = findViewById(R.id.spendingTrendsTitle)
        backButton = findViewById(R.id.back_button)
        dashSavingsText = findViewById(R.id.dash_savings)
        totalSpentText = findViewById(R.id.totalSpent)
        barChart = findViewById(R.id.CatGraph)

        setupBarChart()
//----------------------------------------------------------------------------------
//                              Page navigation section 

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
// ----------------------------------------------------------------------------------

        //  month spinner listener
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = parent?.getItemAtPosition(position).toString()
                spendingTrendsText.text = "Spending trends for $selectedMonth"
                updateBarChart(selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // p3
            }
        }

        // Update displays
        updateSavingsDisplay()
        updateTotalSpentDisplay()
        updateBarChart(monthSpinner.selectedItem.toString())
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

    private fun updateBarChart(selectedMonth: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val categories = database.categoryDAO().getAllCategories()
                val entries = mutableListOf<BarEntry>()
                val limitEntries = mutableListOf<BarEntry>()
                val labels = mutableListOf<String>()

                categories.forEachIndexed { index, category ->
                    val spent = category.spent ?: 0.0
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

    // ------------------------------------------------------------------------------------------
    // Fetches and displays the total spent
    private fun updateTotalSpentDisplay() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val totalExpenses = database.expenseDAO().getTotalExpenseForUser(currentUserId) ?: 0.0
                withContext(Dispatchers.Main) {
                    totalSpentText.text = String.format("R %.2f", totalExpenses)
                }
            } catch (e: Exception) {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateSavingsDisplay()
        updateTotalSpentDisplay()
        updateBarChart(monthSpinner.selectedItem.toString())
    }
}

// -----------------------------------<<< End Of File >>>------------------------------------------
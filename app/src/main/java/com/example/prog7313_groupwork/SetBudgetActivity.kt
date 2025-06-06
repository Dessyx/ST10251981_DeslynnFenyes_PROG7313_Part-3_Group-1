package com.example.prog7313_groupwork
// import
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.entities.Budget
import com.example.prog7313_groupwork.entities.Category
import com.example.prog7313_groupwork.firebase.FirebaseBudgetService
import com.example.prog7313_groupwork.firebase.FirebaseCategoryService
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

// --------------- Functionality for activity_set_budget ------------------------------------------
class SetBudgetActivity : AppCompatActivity() {
    private lateinit var firebaseBudgetService: FirebaseBudgetService
    private lateinit var categoryService: FirebaseCategoryService
    private lateinit var maxSlider: Slider
    private lateinit var minSlider: Slider                      // Declaring variables
    private lateinit var saveButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var pieChart: PieChart
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    // ------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        // Initialize services
        firebaseBudgetService = FirebaseBudgetService()
        categoryService = FirebaseCategoryService()

        maxSlider  = findViewById(R.id.totalBudgetSlider)
        minSlider  = findViewById(R.id.totalBudgetSlider2)
        saveButton = findViewById(R.id.saveBudgetButton)
        backButton = findViewById(R.id.backButton)
        pieChart = findViewById(R.id.budgetPieChart)

        setupPieChart()
        setupSliders()
        loadExistingBudgets()
        setupClickListeners()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 75f
            transparentCircleRadius = 80f
            setDrawCenterText(true)
            centerText = "Budget\nDistribution"
            setCenterTextSize(16f)
            setDrawEntryLabels(false)
            setExtraOffsets(32f, 32f, 32f, 48f)
            legend.isEnabled = true
            legend.textSize = 12f
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.yEntrySpace = 12f
            legend.xEntrySpace = 16f
            legend.formSize = 16f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    private fun setupSliders() {
        listOf(maxSlider, minSlider).forEach { slider ->
            slider.valueFrom = 0f
            slider.valueTo = 100_000f
            slider.setLabelFormatter { v ->
                currencyFormat.format(v.toDouble())
            }
        }

        maxSlider.addOnChangeListener { _, _, _ ->
            updatePieChart()
        }
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            val maxAmt = maxSlider.value
            val minAmt = minSlider.value

            if (maxAmt <= 0f) {
                Toast.makeText(this, "Maximum budget must be > 0", Toast.LENGTH_SHORT).show()
            } else {
                saveBudgets(maxAmt.toDouble(), minAmt.toDouble())
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.viewSpendingGoalsButton).setOnClickListener {
            startActivity(Intent(this, SpendingGoalsActivity::class.java))
        }
    }

    //----------------------------------------------------------------------------------------------
    // Displays the most recent budgets
    private fun loadExistingBudgets() {
        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L).toInt()

        lifecycleScope.launch {
            firebaseBudgetService
                .getCurrentBudget(userId)
                .first()            // just gets the latest ones
                ?.monthlyGoal
                ?.toFloat()
                ?.let { maxSlider.value = it }

            val minPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getFloat("min_monthly_budget", 0f)
            minSlider.value = minPref

            updatePieChart()
        }
    }

    private fun updatePieChart() {
        val maxBudget = maxSlider.value.toDouble()
        if (maxBudget <= 0) {
            pieChart.clear()
            pieChart.invalidate()
            pieChart.visibility = android.view.View.INVISIBLE
            return
        }
        pieChart.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getLong("current_user_id", -1L)
            
            if (userId == -1L) {
                Toast.makeText(this@SetBudgetActivity, "Please log in to view budget distribution", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val categories = categoryService.getCategoriesForUser(userId)
            val spentByCategory = categories.mapNotNull { cat ->
                val spent = cat.spent ?: 0.0
                if (spent > 0) Pair(cat.categoryName, spent) else null
            }
            val totalSpent = spentByCategory.sumOf { it.second }
            val entries = mutableListOf<PieEntry>()
            spentByCategory.forEach { (name, spent) ->
                entries.add(PieEntry(spent.toFloat(), name))
            }
            val unallocated = maxBudget - totalSpent
            if (unallocated > 0) {
                entries.add(PieEntry(unallocated.toFloat(), "Unallocated"))
            }
            if (entries.isEmpty()) {
                entries.add(PieEntry(maxBudget.toFloat(), "Unallocated"))
            }
            // Assign colors: light grey for Unallocated, palette for others
            val colors = mutableListOf<Int>()
            var paletteIndex = 0
            for (entry in entries) {
                if (entry.label == "Unallocated") {
                    colors.add(android.graphics.Color.LTGRAY)
                } else {
                    colors.add(com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS[paletteIndex % com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS.size])
                    paletteIndex++
                }
            }
            val dataSet = PieDataSet(entries, "Budget Distribution")
            dataSet.colors = colors
            dataSet.valueTextSize = 8f
            // Custom ValueFormatter for 'k' format
            val kFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value >= 1000f) String.format("%.0fk", value / 1000f) else value.toInt().toString()
                }
            }
            dataSet.valueFormatter = kFormatter
            dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSet.sliceSpace = 2f
            dataSet.valueLinePart1Length = 0.4f
            dataSet.valueLinePart2Length = 0.4f
            dataSet.valueLinePart1OffsetPercentage = 100f
            dataSet.valueLineColor = Color.DKGRAY
            dataSet.setDrawValues(true)
            val data = PieData(dataSet)
            data.setValueTextSize(14f)
            data.setValueTextColor(Color.BLACK)
            pieChart.data = data
            pieChart.invalidate()
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Sets the min and max budgets to what the user has selected
    private fun saveBudgets(maxAmt: Double, minAmt: Double) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = prefs.getLong("current_user_id", -1L).toInt()
        val cal    = Calendar.getInstance()

        val budget = Budget(
            userId      = userId,
            totalAmount = maxAmt,
            monthlyGoal = maxAmt,
            month       = cal.get(Calendar.MONTH),
            year        = cal.get(Calendar.YEAR),
            isActive    = true
        )
        lifecycleScope.launch {
            firebaseBudgetService.saveBudget(budget)
        }

        prefs.edit()
            .putFloat("min_monthly_budget", minAmt.toFloat())
            .apply()

        Toast.makeText(
            this,
            "Budget saved:\nMax ${currencyFormat.format(maxAmt)}\nMin ${currencyFormat.format(minAmt)}",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}

// -----------------------------------<<< End Of File >>>------------------------------------------
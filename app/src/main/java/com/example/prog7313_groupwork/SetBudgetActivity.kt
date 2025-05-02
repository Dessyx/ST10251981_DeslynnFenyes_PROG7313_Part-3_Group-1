
package com.example.prog7313_groupwork

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Budget
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class SetBudgetActivity : AppCompatActivity() {
    private val db by lazy { AstraDatabase.getDatabase(this) }
    private lateinit var maxSlider: Slider
    private lateinit var minSlider: Slider
    private lateinit var saveButton: MaterialButton
    private lateinit var backButton: ImageButton
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        maxSlider  = findViewById(R.id.totalBudgetSlider)
        minSlider  = findViewById(R.id.totalBudgetSlider2)
        saveButton = findViewById(R.id.saveBudgetButton)
        backButton = findViewById(R.id.backButton)

        // Configure both sliders
        listOf(maxSlider, minSlider).forEach { slider ->
            slider.valueFrom = 0f
            slider.valueTo   = 15_000f
            slider.setLabelFormatter { v ->
                currencyFormat.format(v.toDouble())
            }
        }

        // Load existing Max (from DB) and Min (from prefs) on entry
        loadExistingBudgets()

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
    }

    private fun loadExistingBudgets() {
        // 1) Max Budget from Room
        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L).toInt()

        lifecycleScope.launch {
            db.budgetDAO()
                .getCurrentBudget(userId)
                .first()            // just get the latest once
                ?.monthlyGoal
                ?.toFloat()
                ?.let { maxSlider.value = it }

            // 2) Min Budget from SharedPreferences
            val minPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getFloat("min_monthly_budget", 0f)
            minSlider.value = minPref
        }
    }

    private fun saveBudgets(maxAmt: Double, minAmt: Double) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = prefs.getLong("current_user_id", -1L).toInt()
        val cal    = Calendar.getInstance()

        // 1) Save Max into your Room DB
        val budget = Budget(
            userId      = userId,
            totalAmount = maxAmt,
            monthlyGoal = maxAmt,
            month       = cal.get(Calendar.MONTH),
            year        = cal.get(Calendar.YEAR),
            isActive    = true
        )
        lifecycleScope.launch {
            db.budgetDAO().saveBudget(budget)
        }

        // 2) Save Min into SharedPreferences
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


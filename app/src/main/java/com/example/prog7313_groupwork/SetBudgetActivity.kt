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
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class SetBudgetActivity : AppCompatActivity() {
    private val db by lazy { AstraDatabase.getDatabase(this) }
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        val slider = findViewById<Slider>(R.id.totalBudgetSlider).apply {
            valueFrom = 0f
            valueTo   = 15_000f
            setLabelFormatter { v ->
                // e.g. "R 5 000,00"
                currencyFormat.format(v.toDouble())
            }
        }

        findViewById<MaterialButton>(R.id.saveBudgetButton)
            .setOnClickListener {
                val amt = slider.value
                if (amt <= 0f) {
                    Toast.makeText(this, "Total budget must be > 0", Toast.LENGTH_SHORT).show()
                } else {
                    saveBudget(amt.toDouble())
                }
            }

        findViewById<ImageButton>(R.id.backButton)
            .setOnClickListener { finish() }
    }

    private fun saveBudget(amount: Double) {
        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L).toInt()
        val cal    = Calendar.getInstance()

        val budget = Budget(
            userId      = userId,
            totalAmount = amount,
            monthlyGoal = amount,
            month       = cal.get(Calendar.MONTH),
            year        = cal.get(Calendar.YEAR),
            isActive    = true
        )

        lifecycleScope.launch {
            db.budgetDAO().saveBudget(budget)
            Toast.makeText(
                this@SetBudgetActivity,
                "Budget saved: ${currencyFormat.format(amount)}",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}

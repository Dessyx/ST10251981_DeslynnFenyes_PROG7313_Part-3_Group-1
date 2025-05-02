
package com.example.prog7313_groupwork
// import
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

// --------------- Functionality for activity_set_budget ------------------------------------------
class SetBudgetActivity : AppCompatActivity() {
    private val db by lazy { AstraDatabase.getDatabase(this) }
    private lateinit var maxSlider: Slider
    private lateinit var minSlider: Slider                      // Declaring variables
    private lateinit var saveButton: MaterialButton
    private lateinit var backButton: ImageButton
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    // ------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        maxSlider  = findViewById(R.id.totalBudgetSlider)
        minSlider  = findViewById(R.id.totalBudgetSlider2)
        saveButton = findViewById(R.id.saveBudgetButton)
        backButton = findViewById(R.id.backButton)

        // Configuring sliders
        listOf(maxSlider, minSlider).forEach { slider ->
            slider.valueFrom = 0f
            slider.valueTo   = 100_000f
            slider.setLabelFormatter { v ->
                currencyFormat.format(v.toDouble())
            }
        }

        loadExistingBudgets()

        // ------------------------------------------------------------------------------------------
        // on click listeners
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
    //----------------------------------------------------------------------------------------------
    // Displays the most recent budgets
    private fun loadExistingBudgets() {
        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L).toInt()

        lifecycleScope.launch {
            db.budgetDAO()
                .getCurrentBudget(userId)
                .first()            // just gets the latest ones
                ?.monthlyGoal
                ?.toFloat()
                ?.let { maxSlider.value = it }

            val minPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getFloat("min_monthly_budget", 0f)
            minSlider.value = minPref
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
            db.budgetDAO().saveBudget(budget)
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
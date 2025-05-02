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

// ----------------------- Functionality for dashboard.xml ------------------------------
class DashboardActivity : AppCompatActivity() {
    private lateinit var monthSpinner: Spinner
    private lateinit var spendingTrendsText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var dashSavingsText: TextView
    private lateinit var totalSpentText: TextView
    private lateinit var database: AstraDatabase
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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // p3
            }
        }

        // Update displays
        updateSavingsDisplay()
        updateTotalSpentDisplay()
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
    }
}

// -----------------------------------<<< End Of File >>>------------------------------------------
package com.example.prog7313_groupwork

// imports
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.example.prog7313_groupwork.entities.DebtPlan
import com.example.prog7313_groupwork.firebase.FirebaseDebtPlannerService
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

// -------------------- Functionality for activity_debt_planner.xml -------------------------------
class DebtPlanner : AppCompatActivity() {
    private lateinit var totalDebtInput: TextInputEditText
    private lateinit var monthlySalaryInput: TextInputEditText
    private lateinit var paymentPeriodInput: TextInputEditText   // variable declaration
    private lateinit var monthlyPaymentText: TextView
    private lateinit var calculateButton: Button
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var debtPlannerService: FirebaseDebtPlannerService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_planner)

        // Initialize Firebase service
        debtPlannerService = FirebaseDebtPlannerService()

        initializeViews()
        setupClickListeners()
        loadLatestDebtPlan()
    }

    private fun initializeViews() {
        totalDebtInput = findViewById(R.id.totalDebtInput)
        monthlySalaryInput = findViewById(R.id.monthlySalaryInput)
        paymentPeriodInput = findViewById(R.id.paymentPeriodInput)
        monthlyPaymentText = findViewById(R.id.monthlyPaymentText)
        calculateButton = findViewById(R.id.calculateButton)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
        monthlyPaymentText.text = formatCurrency(0.0)
    }

    // ---------------------------------------------------------------------------------
    // on click listeners
    private fun setupClickListeners() {
        calculateButton.setOnClickListener {
            calculateMonthlyPayment()
        }

        saveButton.setOnClickListener {
            saveDebtPlan()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    //-------------------------------------------------------------------------------------
    // Performs a calculation using your total debt and Salary to show you how much pm to pay.
    private fun calculateMonthlyPayment(): Double? {
        try {
            val totalDebt = totalDebtInput.text.toString().toDoubleOrNull()
            val monthlySalary = monthlySalaryInput.text.toString().toDoubleOrNull()
            val paymentPeriod = paymentPeriodInput.text.toString().toIntOrNull()

            // Validation
            if (totalDebt == null || monthlySalary == null || paymentPeriod == null) {
                Toast.makeText(this, "Please fill in all fields with valid numbers", Toast.LENGTH_SHORT).show()
                return null
            }

            if (totalDebt <= 0 || monthlySalary <= 0 || paymentPeriod <= 0) {
                Toast.makeText(this, "Please enter positive numbers", Toast.LENGTH_SHORT).show()
                return null
            }

            val monthlyPayment = totalDebt / paymentPeriod

            // Check if monthly payment is more than 30% of salary
            if (monthlyPayment > monthlySalary * 0.3) {
                Toast.makeText(
                    this,
                    "Warning: Monthly payment (${formatCurrency(monthlyPayment)}) exceeds 30% of your salary (${formatCurrency(monthlySalary * 0.3)})",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Display the monthly payment
            monthlyPaymentText.text = formatCurrency(monthlyPayment)
            return monthlyPayment

        } catch (e: Exception) {
            Toast.makeText(this, "Error calculating payment: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    // --------------------------------------------------------------------------------------------
    // Saves the debt plan into the database
    private fun saveDebtPlan() {
        val monthlyPayment = calculateMonthlyPayment() ?: return

        lifecycleScope.launch {
            try {
                val totalDebt = totalDebtInput.text.toString().toDouble()
                val monthlySalary = monthlySalaryInput.text.toString().toDouble()
                val paymentPeriod = paymentPeriodInput.text.toString().toInt()

                // TODO: Get actual user ID from session
                val userId = 1

                val debtPlan = DebtPlan(
                    userId = userId,
                    totalDebt = totalDebt,
                    monthlySalary = monthlySalary,
                    paymentPeriod = paymentPeriod,
                    monthlyPayment = monthlyPayment,
                    createdDate = System.currentTimeMillis()
                )

                // Save to Firebase
                debtPlannerService.insertDebtPlan(debtPlan)
                Toast.makeText(this@DebtPlanner, "Debt plan saved successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@DebtPlanner, "Error saving debt plan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ----------------------------------------------------------------------------------------------
    // Displays the most recent debt plan
    private fun loadLatestDebtPlan() {
        lifecycleScope.launch {
            try {
                val userId = 1
                val latestPlan = debtPlannerService.getLatestDebtPlanForUser(userId)
                
                latestPlan?.let { plan ->
                    totalDebtInput.setText(plan.totalDebt.toString())
                    monthlySalaryInput.setText(plan.monthlySalary.toString())
                    paymentPeriodInput.setText(plan.paymentPeriod.toString())
                    monthlyPaymentText.text = formatCurrency(plan.monthlyPayment)
                }
            } catch (e: Exception) {
                Toast.makeText(this@DebtPlanner, "Error loading latest plan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // formats the currency
    private fun formatCurrency(amount: Double): String {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return currencyFormat.format(amount)
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
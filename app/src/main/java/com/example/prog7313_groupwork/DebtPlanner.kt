package com.example.prog7313_groupwork

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import java.text.NumberFormat
import java.util.*

class DebtPlanner : AppCompatActivity() {
    private lateinit var totalDebtInput: TextInputEditText
    private lateinit var monthlySalaryInput: TextInputEditText
    private lateinit var paymentPeriodInput: TextInputEditText
    private lateinit var monthlyPaymentText: TextView
    private lateinit var calculateButton: Button
    private lateinit var saveButton: Button
    private lateinit var dbHelper: AstraDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_planner)

        // Initialize database helper
        dbHelper = AstraDatabase(this)

        // Initialize views
        totalDebtInput = findViewById(R.id.totalDebtInput)
        monthlySalaryInput = findViewById(R.id.monthlySalaryInput)
        paymentPeriodInput = findViewById(R.id.paymentPeriodInput)
        monthlyPaymentText = findViewById(R.id.monthlyPaymentText)
        calculateButton = findViewById(R.id.calculateButton)
        saveButton = findViewById(R.id.saveButton)

        // Set click listeners
        calculateButton.setOnClickListener {
            calculateMonthlyPayment()
        }

        saveButton.setOnClickListener {
            saveDebtPlan()
        }

        // Load latest debt plan if exists
        loadLatestDebtPlan()
    }

    private fun calculateMonthlyPayment() {
        try {
            val totalDebt = totalDebtInput.text.toString().toDouble()
            val monthlySalary = monthlySalaryInput.text.toString().toDouble()
            val paymentPeriod = paymentPeriodInput.text.toString().toInt()

            if (totalDebt <= 0 || monthlySalary <= 0 || paymentPeriod <= 0) {
                Toast.makeText(this, "Please enter valid positive numbers", Toast.LENGTH_SHORT).show()
                return
            }

            val monthlyPayment = totalDebt / paymentPeriod

            if (monthlyPayment > monthlySalary * 0.3) {
                Toast.makeText(this, "Warning: Monthly payment exceeds 30% of your salary", Toast.LENGTH_LONG).show()
            }

            // Format the monthly payment as currency
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            monthlyPaymentText.text = currencyFormat.format(monthlyPayment)
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDebtPlan() {
        try {
            val totalDebt = totalDebtInput.text.toString().toDouble()
            val monthlySalary = monthlySalaryInput.text.toString().toDouble()
            val paymentPeriod = paymentPeriodInput.text.toString().toInt()

            if (totalDebt <= 0 || monthlySalary <= 0 || paymentPeriod <= 0) {
                Toast.makeText(this, "Please enter valid positive numbers", Toast.LENGTH_SHORT).show()
                return
            }

            val monthlyPayment = dbHelper.saveDebtPlan(totalDebt, monthlySalary, paymentPeriod)
            Toast.makeText(this, "Debt plan saved successfully!", Toast.LENGTH_SHORT).show()

            // Update the monthly payment display
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            monthlyPaymentText.text = currencyFormat.format(monthlyPayment)
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLatestDebtPlan() {
        val latestPlan = dbHelper.getLatestDebtPlan()
        if (latestPlan != null) {
            totalDebtInput.setText(latestPlan.debtAmount.toString())
            monthlySalaryInput.setText(latestPlan.salary.toString())
            paymentPeriodInput.setText(latestPlan.paymentPeriod.toString())
            
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            monthlyPaymentText.text = currencyFormat.format(latestPlan.monthlyPayment)
        }
    }
}
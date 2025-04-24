package com.example.prog7313_groupwork

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Income
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddIncome : AppCompatActivity() {
    private lateinit var dateInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var addIncomeButton: MaterialButton
    private lateinit var database: AstraDatabase
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        // Initialize database
        database = AstraDatabase.getDatabase(this)

        // Initialize views
        initializeViews()
        setupDatePicker()
        setupCategorySpinner()
        setupAddIncomeButton()
    }

    private fun initializeViews() {
        dateInput = findViewById(R.id.dateInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        addIncomeButton = findViewById(R.id.addExpenseButton)
    }

    private fun setupDatePicker() {
        dateInput.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dateInput.setText(dateFormatter.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "Salary",
            "Freelance",
            "Investments",
            "Gifts",
            "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupAddIncomeButton() {
        addIncomeButton.setOnClickListener {
            saveIncome()
        }
    }

    private fun saveIncome() {
        val amount = amountInput.text.toString()
        val description = descriptionInput.text.toString()
        val date = dateInput.text.toString()
        val category = categorySpinner.selectedItem.toString()

        // Validation
        if (amount.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val amountValue = amount.toDouble()
            if (amountValue <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                return
            }

            // TODO: Replace with actual user ID from session
            val userId = 1 // Temporary user ID

            val income = Income(
                userId = userId,
                amount = amountValue,
                description = description,
                date = date,
                category = category
            )

            lifecycleScope.launch {
                try {
                    database.incomeDAO().insertIncome(income)
                    Toast.makeText(this@AddIncome, "Income added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@AddIncome, "Error saving income: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
        }
    }
}
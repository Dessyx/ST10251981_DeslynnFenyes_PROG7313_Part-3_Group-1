package com.example.prog7313_groupwork

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Expense
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var dateInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var attachImageInput: EditText
    private lateinit var addExpenseButton: MaterialButton
    private lateinit var viewExpenseButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var database: AstraDatabase
    private var selectedDate: Calendar = Calendar.getInstance()

    // Sample categories - you can replace these with categories from your database
    private val categories = arrayOf(
        "Food",
        "Transportation",
        "Housing",
        "Utilities",
        "Entertainment",
        "Healthcare",
        "Shopping",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Initialize database
        database = AstraDatabase.getDatabase(this)

        // Initialize views
        dateInput = findViewById(R.id.dateInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.expenseDescription)
        attachImageInput = findViewById(R.id.attachImageInput)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        viewExpenseButton = findViewById(R.id.viewExpenseButton)
        backButton = findViewById(R.id.backButton)

        // Set up category spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Set up date picker
        setupDatePicker()

        // Set click listeners
        addExpenseButton.setOnClickListener {
            addExpense()
        }

        viewExpenseButton.setOnClickListener {
            // Navigate to expense list activity
            startActivity(Intent(this, ExpenseList::class.java))
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        dateInput.setOnClickListener {
            val year = selectedDate.get(Calendar.YEAR)
            val month = selectedDate.get(Calendar.MONTH)
            val day = selectedDate.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateDateDisplay()
            }, year, month, day).show()
        }

        // Set initial date display
        updateDateDisplay()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateInput.setText(dateFormat.format(selectedDate.time))
    }

    private fun addExpense() {
        val amount = amountInput.text.toString()
        val description = descriptionInput.text.toString()
        val category = categorySpinner.selectedItem.toString()

        // Validation
        if (amount.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val expenseAmount = amount.toDouble()
            if (expenseAmount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return
            }

            // Create expense object
            val expense = Expense(
                date = selectedDate.timeInMillis,
                category = category,
                amount = expenseAmount,
                description = description,
                imagePath = null, // TODO: Implement image attachment
                userId = 1 // TODO: Get actual user ID from shared preferences or login session
            )

            // Save expense to database
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    database.expenseDAO().insertExpense(expense)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpenseActivity, "Expense added successfully!", Toast.LENGTH_SHORT).show()
                        clearInputs()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpenseActivity, "Failed to add expense: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputs() {
        amountInput.text.clear()
        descriptionInput.text.clear()
        categorySpinner.setSelection(0)
        selectedDate = Calendar.getInstance()
        updateDateDisplay()
    }
}
package com.example.prog7313_groupwork

//imports
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Income
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.prog7313_groupwork.firebase.FirebaseIncomeService

// --------------------------- Functionaloty for activity_add_income.xml -------------------------
class AddIncome : AppCompatActivity() {
    private lateinit var dateInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var addIncomeButton: MaterialButton   // Variable declaration
    private lateinit var database: AstraDatabase
    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var firebaseIncomeService: FirebaseIncomeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        // Initialize database
        database = AstraDatabase.getDatabase(this)

        // Initialize Firebase service
        firebaseIncomeService = FirebaseIncomeService()

        initializeViews()
        setupDatePicker()   // Initialize views
        setupAddIncomeButton()

        // --------------------------------------------------------------------------------------
        // Navigation section
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    //--------------------------------------------------------------------------------------------
    private fun initializeViews() {
        dateInput = findViewById(R.id.dateInput)
        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.expenseDescription)
        addIncomeButton = findViewById(R.id.addExpenseButton)
    }

    // ---------------------------------------------------------------------------------
    // Note: AI (ChatGPT) was used in this section of the date picker
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

        updateDateDisplay()
    }

    //------------------------------------------------------------------------------------
    // refreshes
    private fun updateDateDisplay() {
        dateInput.setText(dateFormatter.format(selectedDate.time))
    }

    // -------------------------------------------------------------------------------------
    // on click listener
    private fun setupAddIncomeButton() {
        addIncomeButton.setOnClickListener {
            saveIncome()
        }
    }

    //---------------------------------------------------------------------------------------
    // Saves the users entered income information into the database
    private fun saveIncome() {
        val amount = amountInput.text.toString()
        val description = descriptionInput.text.toString()

        // Validation
        if (amount.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val amountValue = amount.toDouble()
            if (amountValue <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                return
            }

            // Get user ID from shared preferences
            val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getLong("current_user_id", -1L)

            if (userId == -1L) {
                Toast.makeText(this, "Please log in to add income", Toast.LENGTH_SHORT).show()
                return
            }

            val income = Income(
                userId = userId,
                amount = amountValue,
                description = description,
                date = selectedDate.timeInMillis,
                category = "General" // Default category
            )

            lifecycleScope.launch {
                try {
                    firebaseIncomeService.insertIncome(income)
                    Toast.makeText(this@AddIncome, "Income added successfully!", Toast.LENGTH_SHORT).show()
                    
                    // Return to home activity and refresh it
                    val intent = Intent(this@AddIncome, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
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
// -----------------------------------<<< End Of File >>>------------------------------------------
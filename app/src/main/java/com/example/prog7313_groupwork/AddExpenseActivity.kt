package com.example.prog7313_groupwork

// imports
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.entities.Expense
import com.example.prog7313_groupwork.firebase.FirebaseCategoryService
import com.example.prog7313_groupwork.firebase.FirebaseExpenseService
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ---------------------- Functionality for activity_add_expense. --------------------------------
class AddExpenseActivity : AppCompatActivity() {
    private lateinit var dateInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var attachImageInput: EditText
    private lateinit var addExpenseButton: MaterialButton
    private lateinit var viewExpenseButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var categoryService: FirebaseCategoryService
    private lateinit var expenseService: FirebaseExpenseService
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedImageUri: Uri? = null
    private var selectedImagePath: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imagePath = result.data?.getStringExtra("image_path")
            imagePath?.let {
                selectedImagePath = it
                attachImageInput.setText("Image attached")
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Initialize services
        categoryService = FirebaseCategoryService()
        expenseService = FirebaseExpenseService()

        // Initialize views
        dateInput = findViewById(R.id.dateInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.expenseDescription)
        attachImageInput = findViewById(R.id.attachImageInput)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        viewExpenseButton = findViewById(R.id.viewExpenseButton)
        backButton = findViewById(R.id.backButton)

        loadCategories()
        setupDatePicker()
        setupImageAttachment()

        // -------------------------------------------------------------------------
        // on click listeners
        addExpenseButton.setOnClickListener {
            addExpense()
        }

        viewExpenseButton.setOnClickListener {
            try {
                val intent = Intent(this, ExpenseList::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening expense list: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupImageAttachment() {
        attachImageInput.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            getContent.launch(intent)
        }
    }

    private fun copyImageToPrivateStorage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val fileName = "expense_image_${System.currentTimeMillis()}.jpg"
                val file = File(filesDir, fileName)
                
                FileOutputStream(file).use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                
                selectedImagePath = file.absolutePath
                withContext(Dispatchers.Main) {
                    attachImageInput.setText(fileName)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddExpenseActivity, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getLong("current_user_id", -1L)
                
                if (userId == -1L) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpenseActivity, "Please log in to view categories", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val categoriesList = categoryService.getCategoriesForUser(userId)
                val categoryNames = categoriesList.map { it.categoryName }

                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(
                        this@AddExpenseActivity,
                        android.R.layout.simple_spinner_item,
                        categoryNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddExpenseActivity, "Failed to load categories: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

// -------------------------------------------------------------------------------------------
    // refreshes
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateInput.setText(dateFormat.format(selectedDate.time))
    }
// --------------------------------------------------------------------------------------------
    // Inserts all fields into the database
    private fun addExpense() {
        val amount = amountInput.text.toString()
        val description = descriptionInput.text.toString()
        val category = categorySpinner.selectedItem.toString()

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

            val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getLong("current_user_id", -1L)

            if (userId == -1L) {
                Toast.makeText(this, "Please log in to add expenses", Toast.LENGTH_SHORT).show()
                return
            }

            val expense = Expense(
                date = selectedDate.timeInMillis,
                category = category,
                amount = expenseAmount,
                description = description,
                imagePath = selectedImagePath,
                userId = userId
            )

            // Save expense to Firebase and update category spent amount
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val success = expenseService.addExpense(expense)
                    if (!success) {
                        throw Exception("Failed to add expense to Firebase")
                    }

                    val categories = categoryService.getCategoriesForUser(userId)
                    val matchingCategory = categories.find { it.categoryName == category }
                    
                    if (matchingCategory != null) {
                        val currentSpent = matchingCategory.spent ?: 0.0
                        val newSpent = currentSpent + expenseAmount
                        val updatedCategory = matchingCategory.copy(spent = newSpent)
                        categoryService.saveCategory(updatedCategory)
                        
                        Log.d("AddExpenseActivity", "Updated category ${category} spent from $currentSpent to $newSpent")
                    } else {
                        Log.e("AddExpenseActivity", "Category not found: $category")
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpenseActivity, "Expense added successfully!", Toast.LENGTH_SHORT).show()
                        clearInputs()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpenseActivity, "Failed to add expense: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("AddExpenseActivity", "Error adding expense", e)
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
        selectedImageUri = null
        selectedImagePath = null
        attachImageInput.text.clear()
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
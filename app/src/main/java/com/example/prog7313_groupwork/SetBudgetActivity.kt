package com.example.prog7313_groupwork

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Budget
import com.example.prog7313_groupwork.entities.BudgetCategory
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class SetBudgetActivity : AppCompatActivity() {
    private lateinit var database: AstraDatabase
    private lateinit var totalBudgetSlider: Slider
    private lateinit var categoryContainer: LinearLayout
    private lateinit var addCategoryButton: LinearLayout
    private lateinit var saveBudgetButton: MaterialButton
    private lateinit var backButton: ImageButton
    
    private val categorySliders = mutableMapOf<String, Slider>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        database = AstraDatabase.getDatabase(this)
        initializeViews()
        setupListeners()
        loadExistingBudget()
    }

    private fun initializeViews() {
        totalBudgetSlider = findViewById(R.id.totalBudgetSlider)
        categoryContainer = findViewById(R.id.categoryContainer)
        addCategoryButton = findViewById(R.id.addCategoryButton)
        saveBudgetButton = findViewById(R.id.saveBudgetButton)
        backButton = findViewById(R.id.backButton)

        // Initialize existing category sliders
        setupCategorySlider(R.id.foodSlider, "Food")
        setupCategorySlider(R.id.transportSlider, "Transport")
        setupCategorySlider(R.id.petSlider, "Pet")
        setupCategorySlider(R.id.entertainmentSlider, "Entertainment")
        setupCategorySlider(R.id.luxuriesSlider, "Luxuries")

        // Setup total budget slider
        totalBudgetSlider.apply {
            addOnChangeListener { _, value, _ ->
                updateSliderLabel(this, value)
            }
            setLabelFormatter { value ->
                currencyFormat.format(value)
            }
        }
    }

    private fun setupCategorySlider(sliderId: Int, categoryName: String) {
        findViewById<Slider>(sliderId)?.let { slider ->
            categorySliders[categoryName] = slider
            slider.apply {
                addOnChangeListener { _, value, _ ->
                    updateSliderLabel(this, value)
                }
                setLabelFormatter { value ->
                    currencyFormat.format(value)
                }
            }
        }
    }

    private fun setupListeners() {
        addCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }

        saveBudgetButton.setOnClickListener {
            if (validateInput()) {
                saveBudget()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(): Boolean {
        if (totalBudgetSlider.value <= 0) {
            showError("Total budget must be greater than 0")
            return false
        }

        if (categorySliders.isEmpty()) {
            showError("Please add at least one category")
            return false
        }

        return true
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = dialogView.findViewById<TextView>(R.id.categoryNameInput).text.toString()
                if (categoryName.isNotBlank()) {
                    addNewCategory(categoryName)
                } else {
                    showError("Category name cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun addNewCategory(categoryName: String) {
        if (categorySliders.containsKey(categoryName)) {
            showError("Category already exists")
            return
        }

        val categoryLayout = LayoutInflater.from(this)
            .inflate(R.layout.item_budget_category, categoryContainer, false)

        categoryLayout.findViewById<TextView>(R.id.categoryName).text = categoryName
        val slider = categoryLayout.findViewById<Slider>(R.id.categorySlider).apply {
            setLabelFormatter { value ->
                currencyFormat.format(value)
            }
            addOnChangeListener { _, value, _ ->
                updateSliderLabel(this, value)
            }
        }

        categorySliders[categoryName] = slider
        categoryContainer.addView(categoryLayout, categoryContainer.childCount - 1)
    }

    private fun updateSliderLabel(slider: Slider, value: Float) {
        slider.contentDescription = currencyFormat.format(value)
    }

    private fun loadExistingBudget() {
        lifecycleScope.launch {
            try {
                // TODO: Replace with actual user ID
                val userId = 1
                val latestBudget = database.budgetDAO().getLatestBudget(userId)
                
                latestBudget?.let { budget ->
                    totalBudgetSlider.value = budget.totalAmount.toFloat()
                    
                    database.budgetDAO().getBudgetCategories(budget.id)
                        .catch { e ->
                            showError("Error loading categories: ${e.message}")
                        }
                        .collect { categories ->
                            categories.forEach { category ->
                                categorySliders[category.name]?.value = category.limit.toFloat()
                            }
                        }
                }
            } catch (e: Exception) {
                showError("Error loading budget: ${e.message}")
            }
        }
    }

    private fun saveBudget() {
        lifecycleScope.launch {
            try {
                // TODO: Replace with actual user ID
                val userId = 1
                val calendar = Calendar.getInstance()
                
                val budget = Budget(
                    userId = userId,
                    totalAmount = totalBudgetSlider.value.toDouble(),
                    createdDate = System.currentTimeMillis(),
                    monthlyGoal = totalBudgetSlider.value.toDouble(), // Setting monthly goal same as total amount
                    month = calendar.get(Calendar.MONTH),
                    year = calendar.get(Calendar.YEAR)
                )

                val categories = categorySliders.map { (name, slider) ->
                    BudgetCategory(
                        budgetId = 0, // Will be set by DAO
                        userId = userId,
                        name = name,
                        icon = getCategoryIcon(name),
                        limit = slider.value.toDouble()
                    )
                }

                database.budgetDAO().saveBudgetWithCategories(budget, categories)
                showSuccess("Budget saved successfully!")
                finish()
            } catch (e: Exception) {
                showError("Error saving budget: ${e.message}")
            }
        }
    }

    private fun showSuccess(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCategoryIcon(categoryName: String): String {
        return when (categoryName.toLowerCase(Locale.ROOT)) {
            "food" -> "ic_food"
            "transport" -> "ic_transport"
            "pet" -> "ic_pet"
            "entertainment" -> "ic_entertainment"
            "luxuries" -> "ic_luxuries"
            else -> "ic_misc"
        }
    }
}
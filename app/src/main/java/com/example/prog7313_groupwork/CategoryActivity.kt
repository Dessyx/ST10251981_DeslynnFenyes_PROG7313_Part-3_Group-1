package com.example.prog7313_groupwork

// imports
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.adapters.CategoryAdapter
import com.example.prog7313_groupwork.entities.Category
import com.example.prog7313_groupwork.firebase.FirebaseCategoryService
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --------------------- Functionality for category.xml --------------------------------------
class CategoryActivity : AppCompatActivity() {

    private val firebaseCategoryService = FirebaseCategoryService()

    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category)

      /*  val db = AstraDatabase.getDatabase(this)
        categoryDAO = db.categoryDAO()*/

        val recyclerView = findViewById<RecyclerView>(R.id.categoriesRecyclerView)
        categoryAdapter = CategoryAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoryActivity)
            adapter = categoryAdapter
        }

        val nameEditText = findViewById<EditText>(R.id.editTextCategoryName)
        val limitEditText = findViewById<EditText>(R.id.editTextCategoryLimit)
        val saveButton = findViewById<Button>(R.id.saveButton)

        loadCategories()  // loads categories in database

        //------------------------------------------------------------------------------------
        // on click listener
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val limit = limitEditText.text.toString().trim()
        // ------------------------------------------------------------------------------------
            // adds category information entered
            if (name.isNotEmpty() && limit.isNotEmpty()) {
                val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getLong("current_user_id", -1L)

                if (userId == -1L) {
                    Toast.makeText(this, "Please log in to create categories", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val category = Category(
                    categoryName = name, 
                    categoryLimit = limit,
                    userId = userId
                )

                lifecycleScope.launch {
                    try {
                        firebaseCategoryService.saveCategory(category)
                        Log.d("CategoryActivity", "Category created: $name with limit $limit and spent 0.0")
                        loadCategories() // Reload categories after saving
                    } catch (e: Exception) {
                        Log.e("CategoryActivity", "Error creating category", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CategoryActivity, "Error creating category: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                sendBroadcast(Intent("CATEGORY_UPDATED"))

                Toast.makeText(this, "Category saved!", Toast.LENGTH_SHORT).show()
                nameEditText.text.clear()
                limitEditText.text.clear()
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }

        //----------------------------------------------------------------------------------
        //                              Page navigation section 

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP 
            startActivity(intent)
            finish()
        }

        // Setup view spending button
        val viewSpendingButton = findViewById<Button>(R.id.viewSpendingButton)
        viewSpendingButton.setOnClickListener {
            val intent = Intent(this, CategorySpendingActivity::class.java)
            startActivity(intent)
        }
        //----------------------------------------------------------------------------------
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getLong("current_user_id", -1L)
                
                if (userId == -1L) {
                    Toast.makeText(this@CategoryActivity, "Please log in to view categories", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val categories = firebaseCategoryService.getCategoriesForUser(userId)
                categoryAdapter.updateCategories(categories)
            } catch (e: Exception) {
                Toast.makeText(this@CategoryActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// -----------------------------------<<< End Of File >>>------------------------------------------
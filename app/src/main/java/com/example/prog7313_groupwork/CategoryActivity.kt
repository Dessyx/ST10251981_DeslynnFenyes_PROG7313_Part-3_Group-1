package com.example.prog7313_groupwork

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prog7313_groupwork.entities.Category
import com.example.prog7313_groupwork.entities.CategoryDAO
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var categoryDAO: CategoryDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category)

        val db = AstraDatabase.getDatabase(this)
        categoryDAO = db.CategoryDAO()

        val nameEditText = findViewById<EditText>(R.id.editTextCategoryName)
        val limitEditText = findViewById<EditText>(R.id.editTextCategoryLimit)
        val saveButton = findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val limit = limitEditText.text.toString().trim()

            if (name.isNotEmpty() && limit.isNotEmpty()) {
                val category = Category(categoryName = name, categoryLimit = limit)

                GlobalScope.launch {
                    categoryDAO.insertCategory(category)
                }

                Toast.makeText(this, "Category saved!", Toast.LENGTH_SHORT).show()
                nameEditText.text.clear()
                limitEditText.text.clear()
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

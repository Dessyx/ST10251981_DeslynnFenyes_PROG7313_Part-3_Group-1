package com.example.prog7313_groupwork.adapters

import android.app.Dialog
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.entities.Expense
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val expenseName: TextView = itemView.findViewById(R.id.expenseName)
        private val expenseDate: TextView = itemView.findViewById(R.id.expenseDate)
        private val expenseAmount: TextView = itemView.findViewById(R.id.expenseAmount)
        private val imageButton: ImageButton = itemView.findViewById(R.id.attachImageInput)
        private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(expense: Expense) {
            categoryName.text = expense.category
            expenseName.text = expense.description
            expenseDate.text = dateFormatter.format(Date(expense.date))
            expenseAmount.text = "-R%.2f".format(expense.amount)

            // Handle image button visibility and click
            if (expense.imagePath != null) {
                imageButton.visibility = View.VISIBLE
                imageButton.setOnClickListener {
                    showImageDialog(expense.imagePath)
                }
            } else {
                imageButton.visibility = View.GONE
            }

            // Set category icon based on category
            val iconResource = when (expense.category.toLowerCase(Locale.ROOT)) {
                "transport", "petrol" -> R.drawable.ic_transport
                "food" -> R.drawable.ic_food
                "entertainment", "dstv" -> R.drawable.ic_entertainment
                "utilities", "electricity", "water & rates" -> R.drawable.ic_utilities
                "pet" -> R.drawable.ic_pet
                "luxuries" -> R.drawable.ic_luxuries
                else -> R.drawable.ic_home_category
            }
            categoryIcon.setImageResource(iconResource)
        }

        private fun showImageDialog(imagePath: String) {
            val dialog = Dialog(itemView.context)
            dialog.setContentView(R.layout.dialog_image)
            
            val imageView = dialog.findViewById<ImageView>(R.id.fullscreen_image)
            val closeButton = dialog.findViewById<ImageButton>(R.id.close_button)
            
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("ExpenseAdapter", "Error loading image: ${e.message}")
            }
            
            closeButton.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        }
    }

    private class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
} 
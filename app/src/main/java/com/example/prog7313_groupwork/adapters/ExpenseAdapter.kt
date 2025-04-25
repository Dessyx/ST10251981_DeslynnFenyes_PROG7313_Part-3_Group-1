package com.example.prog7313_groupwork.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.entities.Expense
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
        private val expenseDescription: TextView = itemView.findViewById(R.id.expenseDescription)
        private val expenseDate: TextView = itemView.findViewById(R.id.expenseDate)
        private val expenseAmount: TextView = itemView.findViewById(R.id.expenseAmount)

        fun bind(expense: Expense) {
            categoryName.text = expense.category
            expenseDescription.text = expense.description
            expenseDate.text = expense.date
            expenseAmount.text = "-R%.2f".format(expense.amount)

            // Set category icon based on category
            val iconResource = when (expense.category.toLowerCase(Locale.ROOT)) {
                "transport", "petrol" -> R.drawable.ic_transport
                "food" -> R.drawable.ic_food
                "entertainment", "dstv" -> R.drawable.ic_entertainment
                "utilities", "electricity", "water & rates" -> R.drawable.ic_utilities
                "pet" -> R.drawable.ic_pet
                "luxuries" -> R.drawable.ic_luxuries
                else -> R.drawable.ic_misc
            }
            categoryIcon.setImageResource(iconResource)
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
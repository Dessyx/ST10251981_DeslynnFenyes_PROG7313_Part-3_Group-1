package com.example.prog7313_groupwork.adapters
// Imports
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.entities.Category

// ------------------------------------ Category Spending Adaptor ----------------------------------------
// This Adapter handles the display of the filtered categories on the activity_category_spending.xml page.
class CategorySpendingAdapter : RecyclerView.Adapter<CategorySpendingAdapter.CategorySpendingViewHolder>() {
    private var categories: List<Category> = emptyList() // Declaring the list for categories

    class CategorySpendingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryIcon: ImageView = view.findViewById(R.id.categoryIcon)
        val categoryName: TextView = view.findViewById(R.id.categoryName)
        val spentAmount: TextView = view.findViewById(R.id.spentAmount)
        val limitAmount: TextView = view.findViewById(R.id.limitAmount)
        val percentageText: TextView = view.findViewById(R.id.percentageText)
    }

    // ------------------------------------------------------------------------------------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySpendingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_spending, parent, false)
        return CategorySpendingViewHolder(view)
    }

    // ------------------------------------------------------------------------------------
    override fun onBindViewHolder(holder: CategorySpendingViewHolder, position: Int) {
        val category = categories[position]
        val spent = category.spent ?: 0.0
        val limit = category.categoryLimit.toDoubleOrNull() ?: 0.0
        val percentage = if (limit > 0) ((spent / limit) * 100).toInt() else 0

        holder.categoryName.text = category.categoryName
        holder.spentAmount.text = "Spent: R %.2f".format(spent)
        holder.limitAmount.text = "Limit: R %.2f".format(limit)
        holder.percentageText.text = "$percentage%"

        // Set text color to red if overspent
        if (spent > limit) {
            holder.categoryName.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            holder.spentAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            holder.percentageText.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        } else {
            holder.categoryName.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
            holder.spentAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
            holder.percentageText.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
        }

        // Set appropriate icon based on category name
        val iconResId = when (category.categoryName.lowercase()) {
            "food" -> R.drawable.ic_food
            "transport" -> R.drawable.ic_transport
            "pet" -> R.drawable.ic_pet
            "luxuries" -> R.drawable.ic_luxuries
            "entertainment" -> R.drawable.ic_entertainment
            "utilities" -> R.drawable.ic_utilities
            else -> R.drawable.ic_home_category
        }
        holder.categoryIcon.setImageResource(iconResId)
    }

    // ------------------------------------------------------------------------------------
    override fun getItemCount() = categories.size

    // ------------------------------------------------------------------------------------
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
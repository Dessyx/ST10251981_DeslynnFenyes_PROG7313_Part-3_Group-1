package com.example.prog7313_groupwork.adapters

// Imports
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.entities.Category

//--------------------------------------- Category Adapter class -----------------------------------------------
// This adapter handles the fetching and displaying of Categories on the Category.xml page
class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private var categories: List<Category> = emptyList() // Declaring the list of categories

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {  // Variable declaration
        val categoryIcon: ImageView = view.findViewById(R.id.categoryIcon)
        val categoryName: TextView = view.findViewById(R.id.categoryName)
        val categoryProgress: TextView = view.findViewById(R.id.categoryProgress)
        val progressBar: ProgressBar = view.findViewById(R.id.categoryProgressBar)
    }

    // ------------------------------------------------------------------------------------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    // ------------------------------------------------------------------------------------
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val spent = category.spent ?: 0.0
        val limit = category.categoryLimit.toDoubleOrNull() ?: 0.0
        val progress = if (limit > 0) ((spent / limit) * 100).toInt() else 0

        holder.categoryName.text = category.categoryName
        holder.categoryProgress.text = "R${spent.toInt()} of R${limit.toInt()}"
        holder.progressBar.progress = progress

        // This sets the text color to red if overspent
        if (spent > limit) {
            holder.categoryName.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            holder.categoryProgress.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        } else {
            holder.categoryName.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
            holder.categoryProgress.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
        }

        // Chooses the category icon based on the name
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

    fun updateCategories(newCategories: List<Category>) { // Updates the list of categories
        categories = newCategories
        notifyDataSetChanged()
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
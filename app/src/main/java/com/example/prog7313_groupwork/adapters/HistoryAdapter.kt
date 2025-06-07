package com.example.prog7313_groupwork.adapters

// Imports
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_groupwork.R

// ------------------------------------ History Item Data Class ----------------------------------------
// This data class represents a single history item in the transaction history
data class HistoryItem(
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val timestamp: Long,
    val isExpense: Boolean
)

// ------------------------------------ History Adapter Class ----------------------------------------
// This adapter handles the display of transaction history items in a RecyclerView
class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private var historyItems: List<HistoryItem> = emptyList() // List to store history items

    // ViewHolder class to hold references to the views
    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.historyIcon)
        val title: TextView = view.findViewById(R.id.historyTitle)
        val category: TextView = view.findViewById(R.id.historyCategory)
        val amount: TextView = view.findViewById(R.id.historyAmount)
        val date: TextView = view.findViewById(R.id.historyDate)
    }

    // ------------------------------------------------------------------------------------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    // ------------------------------------------------------------------------------------
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyItems[position]
        
        // Set basic text fields
        holder.title.text = item.title
        holder.category.text = item.category
        holder.date.text = item.date

        // Format and color the amount based on whether it's an expense or income
        val amountText = if (item.isExpense) {
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            "-R%.2f".format(item.amount)
        } else { // Sets amount to green if income
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            "+R%.2f".format(item.amount)
        }
        holder.amount.text = amountText

        // Set the appropriate icon based on the category
        val iconResId = when (item.category.lowercase()) {
            "transport" -> R.drawable.ic_transport
            "luxuries" -> R.drawable.ic_luxuries
            "food" -> R.drawable.ic_food
            "entertainment" -> R.drawable.ic_entertainment
            "utilities" -> R.drawable.ic_utilities
            "income" -> R.drawable.ic_income
            else -> R.drawable.ic_home_category // Default icon for other categories
        }
        holder.icon.setImageResource(iconResId)
    }

    // ------------------------------------------------------------------------------------
    override fun getItemCount() = historyItems.size

    // ------------------------------------------------------------------------------------
    // Updates the list of history items and sorts them by timestamp
    fun updateHistory(newItems: List<HistoryItem>) {
        historyItems = newItems.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
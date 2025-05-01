package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

// ------------------------------------ Budget Entity Class ----------------------------------------
// This class represents a user's monthly budget with its total amount and goals
@Entity(tableName = "budget")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated unique identifier
    val userId: Int, // ID of the user who owns this budget
    val totalAmount: Double, // Total budget amount for the month
    val createdDate: Long = System.currentTimeMillis(), // Timestamp when the budget was created
    val monthlyGoal: Double, // Target savings goal for the month
    val month: Int = Calendar.getInstance().get(Calendar.MONTH), // Current month (0-11)
    val year: Int = Calendar.getInstance().get(Calendar.YEAR), // Current year
    val isActive: Boolean = true // Whether this budget is currently active
)
// -----------------------------------<<< End Of File >>>------------------------------------------ 
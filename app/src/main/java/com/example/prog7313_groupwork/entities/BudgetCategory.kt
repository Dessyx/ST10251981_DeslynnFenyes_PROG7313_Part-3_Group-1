package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ------------------------------------ Budget Category Entity Class ----------------------------------------
// This class represents a category within a budget with its spending limits and tracking
@Entity(
    tableName = "budget_category",
    foreignKeys = [
        ForeignKey(
            entity = Budget::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("budgetId")]
)
data class BudgetCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated unique identifier
    val budgetId: Int, // ID of the budget
    val userId: Int, // ID of the user who owns this category
    val name: String, // Name of the budget category
    val icon: String, // Icon for the category
    val limit: Double, // Maximum spending limit for this category
    val spent: Double = 0.0, // Current amount spent in this category
    val isActive: Boolean = true // Whether this category is currently active
)
// -----------------------------------<<< End Of File >>>------------------------------------------ 
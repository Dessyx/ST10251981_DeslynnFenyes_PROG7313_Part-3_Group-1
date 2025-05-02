package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ------------------------------------ Income Entity Class ----------------------------------------
// This class represents a user's income record with amount and category details
@Entity(
    tableName = "income",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Auto-generated unique identifier
    val userId: Long, // ID of the user who received this income
    val amount: Double, // Amount of income received
    val description: String, // Description or source of the income
    val date: Long = System.currentTimeMillis(), // Date when the income was received
    val category: String // Category of the income (e.g., Salary, Bonus, Investment)
)
// -----------------------------------<<< End Of File >>>------------------------------------------ 
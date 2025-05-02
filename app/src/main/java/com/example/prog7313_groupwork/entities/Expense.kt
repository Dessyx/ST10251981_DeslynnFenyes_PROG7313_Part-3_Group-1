package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ------------------------------------ Expense Entity Class ----------------------------------------
// This class represents a user's expense record with category and amount details
@Entity(
    tableName = "expenses",
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
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Auto-generated unique identifier
    val userId: Long, // ID of the user who made this expense
    val date: Long = System.currentTimeMillis(), // Date when the expense was made
    val category: String, // Category of the expense (e.g., Food, Transport)
    val amount: Double, // Amount spent
    val description: String, // Description or note about the expense
    val imagePath: String? = null, // Optional path to receipt image
    val isActive: Boolean = true, // Whether this expense record is active
    val created: Long = System.currentTimeMillis() // Timestamp when the record was created
)
// -----------------------------------<<< End Of File >>>------------------------------------------ 
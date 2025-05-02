package com.example.prog7313_groupwork.entities
// imports
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ------------------------------------ Expense Entity Class ----------------------------------------
// This class represents a user's expense record with category and amount details
@Entity(
    tableName = "savings", // the savings table
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

data class Savings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val description: String = "",
    val category: String = "General"
)
// -----------------------------------<<< End Of File >>>------------------------------------------
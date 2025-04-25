package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: Long = System.currentTimeMillis(),
    val category: String,
    val amount: Double,
    val description: String,
    val imagePath: String? = null,
    val isActive: Boolean = true,
    val created: Long = System.currentTimeMillis()
) 
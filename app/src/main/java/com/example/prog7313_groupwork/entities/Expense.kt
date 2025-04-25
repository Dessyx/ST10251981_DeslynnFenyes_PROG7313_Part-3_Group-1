package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val category: String,
    val amount: Double,
    val description: String,
    val imagePath: String?,
    val userId: Long, // Foreign key to link expense with user
    val created: Long = System.currentTimeMillis()
) 
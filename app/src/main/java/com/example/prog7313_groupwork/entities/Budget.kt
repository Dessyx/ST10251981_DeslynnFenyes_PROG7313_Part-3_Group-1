package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthlyGoal: Double,
    val month: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
    val year: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
) 
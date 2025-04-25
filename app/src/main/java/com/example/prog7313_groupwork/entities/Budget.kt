package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "budget")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val totalAmount: Double,
    val createdDate: Long = System.currentTimeMillis(),
    val monthlyGoal: Double,
    val month: Int = Calendar.getInstance().get(Calendar.MONTH),
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isActive: Boolean = true
) 
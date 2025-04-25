package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debt_plan")
data class DebtPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val totalDebt: Double,
    val monthlySalary: Double,
    val paymentPeriod: Int,
    val monthlyPayment: Double,
    val createdDate: Long
) 
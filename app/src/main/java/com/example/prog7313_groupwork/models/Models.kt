package com.example.prog7313_groupwork.models

data class Expense(
    val id: Int,
    val amount: Double,
    val category: String,
    val date: String,
    val description: String?
)

data class DebtPlan(
    val id: Int,
    val debtAmount: Double,
    val salary: Double,
    val paymentPeriod: Int,
    val monthlyPayment: Double
) 
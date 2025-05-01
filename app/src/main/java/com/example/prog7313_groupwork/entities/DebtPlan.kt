package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Entity
import androidx.room.PrimaryKey

// ------------------------------------ Debt Plan Entity Class ----------------------------------------
// This class represents a user's debt repayment plan with payment calculations
@Entity(tableName = "debt_plan")
data class DebtPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated unique identifier
    val userId: Int, // ID of the user who owns this debt plan
    val totalDebt: Double, // Total amount of debt to be repaid
    val monthlySalary: Double, // User's monthly income for payment calculations
    val paymentPeriod: Int, // Number of months to repay the debt
    val monthlyPayment: Double, // Calculated monthly payment amount
    val createdDate: Long // Timestamp when the debt plan was created
)
// -----------------------------------<<< End Of File >>>------------------------------------------ 
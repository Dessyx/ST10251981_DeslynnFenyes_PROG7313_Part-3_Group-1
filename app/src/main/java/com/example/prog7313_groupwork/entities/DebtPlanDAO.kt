package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ------------------------------------ Debt Plan DAO Interface ----------------------------------------
// This interface defines the database operations for the DebtPlan entity
@Dao
interface DebtPlanDAO {
    // ------------------------------------------------------------------------------------
    // Inserts a new debt plan
    @Insert
    suspend fun insertDebtPlan(debtPlan: DebtPlan)

    // ------------------------------------------------------------------------------------
    // Updates an existing debt plan
    @Update
    suspend fun updateDebtPlan(debtPlan: DebtPlan)

    // ------------------------------------------------------------------------------------
    // Deletes a debt plan
    @Delete
    suspend fun deleteDebtPlan(debtPlan: DebtPlan)

    // ------------------------------------------------------------------------------------
    // Retrieves all debt plans for a specific user, ordered by creation date
    @Query("SELECT * FROM debt_plan WHERE userId = :userId ORDER BY createdDate DESC")
    fun getAllDebtPlansForUser(userId: Int): Flow<List<DebtPlan>>

    // ------------------------------------------------------------------------------------
    // Retrieves the most recent debt plan for a user
    @Query("SELECT * FROM debt_plan WHERE userId = :userId ORDER BY createdDate DESC LIMIT 1")
    suspend fun getLatestDebtPlanForUser(userId: Int): DebtPlan?

    // ------------------------------------------------------------------------------------
    // Retrieves a specific debt plan by its ID
    @Query("SELECT * FROM debt_plan WHERE id = :id")
    suspend fun getDebtPlanById(id: Int): DebtPlan?
}
// -----------------------------------<<< End Of File >>>------------------------------------------ 
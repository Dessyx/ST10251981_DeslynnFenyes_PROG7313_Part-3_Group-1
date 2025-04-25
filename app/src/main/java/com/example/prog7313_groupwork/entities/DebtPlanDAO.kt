package com.example.prog7313_groupwork.entities

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtPlanDAO {
    @Insert
    suspend fun insertDebtPlan(debtPlan: DebtPlan)

    @Update
    suspend fun updateDebtPlan(debtPlan: DebtPlan)

    @Delete
    suspend fun deleteDebtPlan(debtPlan: DebtPlan)

    @Query("SELECT * FROM debt_plan WHERE userId = :userId ORDER BY createdDate DESC")
    fun getAllDebtPlansForUser(userId: Int): Flow<List<DebtPlan>>

    @Query("SELECT * FROM debt_plan WHERE userId = :userId ORDER BY createdDate DESC LIMIT 1")
    suspend fun getLatestDebtPlanForUser(userId: Int): DebtPlan?

    @Query("SELECT * FROM debt_plan WHERE id = :id")
    suspend fun getDebtPlanById(id: Int): DebtPlan?
} 
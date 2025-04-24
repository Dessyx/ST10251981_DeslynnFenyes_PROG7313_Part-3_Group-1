package com.example.prog7313_groupwork.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    fun getCurrentBudget(
        month: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
        year: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    ): Flow<Budget?>
} 
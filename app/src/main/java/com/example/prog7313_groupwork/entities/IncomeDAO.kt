package com.example.prog7313_groupwork.entities

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDAO {
    @Insert
    suspend fun insertIncome(income: Income)

    @Update
    suspend fun updateIncome(income: Income)

    @Delete
    suspend fun deleteIncome(income: Income)

    @Query("SELECT * FROM income WHERE userId = :userId")
    fun getAllIncomeForUser(userId: Long): Flow<List<Income>>

    @Query("SELECT * FROM income WHERE id = :id")
    suspend fun getIncomeById(id: Long): Income?

    @Query("SELECT SUM(amount) FROM income WHERE userId = :userId")
    suspend fun getTotalIncomeForUser(userId: Long): Double?

    @Query("SELECT * FROM income WHERE userId = :userId AND category = :category")
    fun getIncomeByCategory(userId: Long, category: String): Flow<List<Income>>

    @Query("SELECT * FROM income")
    suspend fun getAllActiveIncome(): List<Income>
} 
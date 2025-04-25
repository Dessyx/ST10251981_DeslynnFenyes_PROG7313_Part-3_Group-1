package com.example.prog7313_groupwork.entities

import androidx.room.*
import com.example.prog7313_groupwork.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDAO {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expense WHERE userId = :userId")
    fun getAllExpensesForUser(userId: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun getExpenseById(id: Int): Expense?

    @Query("SELECT * FROM expense WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getExpensesByDateRange(userId: Int, startDate: String, endDate: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expense WHERE userId = :userId")
    suspend fun getTotalExpenseForUser(userId: Int): Double?

    @Query("SELECT * FROM expense WHERE userId = :userId AND category = :category")
    fun getExpensesByCategory(userId: Int, category: String): Flow<List<Expense>>
} 
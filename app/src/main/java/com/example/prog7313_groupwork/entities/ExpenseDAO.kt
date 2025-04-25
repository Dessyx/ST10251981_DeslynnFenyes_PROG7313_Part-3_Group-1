package com.example.prog7313_groupwork.entities

import androidx.room.*
import com.example.prog7313_groupwork.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId AND isActive = 1 ORDER BY date DESC")
    fun getAllExpensesForUser(userId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id AND isActive = 1")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND isActive = 1")
    fun getExpensesByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalExpenseForUser(userId: Long): Double?

    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = :category AND isActive = 1")
    fun getExpensesByCategory(userId: Long, category: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND category = :category AND isActive = 1")
    suspend fun getTotalExpenseByCategory(userId: Long, category: String): Double?

    @Query("UPDATE expenses SET isActive = 0 WHERE id = :expenseId")
    suspend fun deactivateExpense(expenseId: Long)

    @Query("DELETE FROM expenses WHERE isActive = 0")
    suspend fun deleteInactiveExpenses()

    @Transaction
    suspend fun deactivateAndDeleteExpense(expenseId: Long) {
        val expense = getExpenseById(expenseId) ?: return
        deactivateExpense(expenseId)
        deleteInactiveExpenses()
    }

    @Query("SELECT DISTINCT category FROM expenses WHERE userId = :userId AND isActive = 1")
    fun getExpenseCategories(userId: Long): Flow<List<String>>

    @Query("SELECT * FROM expenses WHERE isActive = 1")
    suspend fun getAllActiveExpenses(): List<Expense>
} 
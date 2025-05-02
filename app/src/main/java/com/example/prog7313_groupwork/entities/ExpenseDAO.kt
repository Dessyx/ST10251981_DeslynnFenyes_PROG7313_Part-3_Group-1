package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.*
import com.example.prog7313_groupwork.entities.Expense
import kotlinx.coroutines.flow.Flow

// ------------------------------------ Expense DAO Interface ----------------------------------------
// This interface defines the database operations for the Expense entity
@Dao
interface ExpenseDAO {
    // ------------------------------------------------------------------------------------
    // Inserts a new expense or replaces an existing one, returns the ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    // ------------------------------------------------------------------------------------
    // Updates an existing expense
    @Update
    suspend fun updateExpense(expense: Expense)

    // ------------------------------------------------------------------------------------
    // Deletes an expense
    @Delete
    suspend fun deleteExpense(expense: Expense)

    // ------------------------------------------------------------------------------------
    // Retrieves all active expenses for a user, ordered by date
    @Query("SELECT * FROM expenses WHERE userId = :userId AND isActive = 1 ORDER BY date DESC")
    fun getAllExpensesForUser(userId: Long): Flow<List<Expense>>

    // ------------------------------------------------------------------------------------
    // Retrieves a specific expense by its ID
    @Query("SELECT * FROM expenses WHERE id = :id AND isActive = 1")
    suspend fun getExpenseById(id: Long): Expense?

    // ------------------------------------------------------------------------------------
    // Retrieves expenses within a specific date range for a user
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND isActive = 1")
    fun getExpensesByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Expense>>

    // ------------------------------------------------------------------------------------
    // Calculates the total amount spent by a user
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalExpenseForUser(userId: Long): Double?

    // ------------------------------------------------------------------------------------
    // Retrieves all expenses for a specific category
    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = :category AND isActive = 1")
    fun getExpensesByCategory(userId: Long, category: String): Flow<List<Expense>>

    // ------------------------------------------------------------------------------------
    // Calculates the total amount spent in a specific category
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND category = :category AND isActive = 1")
    suspend fun getTotalExpenseByCategory(userId: Long, category: String): Double?

    // ------------------------------------------------------------------------------------
    // Deactivates an expense by setting isActive to false
    @Query("UPDATE expenses SET isActive = 0 WHERE id = :expenseId")
    suspend fun deactivateExpense(expenseId: Long)

    // ------------------------------------------------------------------------------------
    // Permanently deletes all inactive expenses
    @Query("DELETE FROM expenses WHERE isActive = 0")
    suspend fun deleteInactiveExpenses()

    // ------------------------------------------------------------------------------------
    // Transaction to deactivate and delete an expense
    @Transaction
    suspend fun deactivateAndDeleteExpense(expenseId: Long) {
        val expense = getExpenseById(expenseId) ?: return
        deactivateExpense(expenseId)
        deleteInactiveExpenses()
    }

    // ------------------------------------------------------------------------------------
    // Retrieves a list of unique expense categories for a user
    @Query("SELECT DISTINCT category FROM expenses WHERE userId = :userId AND isActive = 1")
    fun getExpenseCategories(userId: Long): Flow<List<String>>

    // ------------------------------------------------------------------------------------
    // Retrieves all active expenses across all users
    @Query("SELECT * FROM expenses WHERE isActive = 1")
    suspend fun getAllActiveExpenses(): List<Expense>
}
// -----------------------------------<<< End Of File >>>------------------------------------------ 
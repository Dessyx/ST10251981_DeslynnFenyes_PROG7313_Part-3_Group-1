package com.example.prog7313_groupwork.entities

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

// ------------------------------------ Budget DAO Interface ----------------------------------------
// This interface defines the database operations for Budget and BudgetCategory entities
@Dao
interface BudgetDAO {
    // ------------------------------------------------------------------------------------
    // Inserts or replaces a budget
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: Budget)

    // ------------------------------------------------------------------------------------
    // Inserts a new budget and returns its ID
    @Insert
    suspend fun insertBudget(budget: Budget): Long

    // ------------------------------------------------------------------------------------
    // Inserts a new budget category
    @Insert
    suspend fun insertBudgetCategory(category: BudgetCategory)

    // ------------------------------------------------------------------------------------
    // Updates an existing budget
    @Update
    suspend fun updateBudget(budget: Budget)

    // ------------------------------------------------------------------------------------
    // Updates an existing budget category
    @Update
    suspend fun updateBudgetCategory(category: BudgetCategory)

    // ------------------------------------------------------------------------------------
    // Deletes a budget
    @Delete
    suspend fun deleteBudget(budget: Budget)

    // ------------------------------------------------------------------------------------
    // Deletes a budget category
    @Delete
    suspend fun deleteBudgetCategory(category: BudgetCategory)

    // ------------------------------------------------------------------------------------
    // Retrieves the current active budget for a user in a specific month and year
    @Query("""
        SELECT * 
          FROM budget
         WHERE userId   = :userId
           AND month    = :month
           AND year     = :year
           AND isActive = 1
         LIMIT 1
    """)
    fun getCurrentBudget(
        userId: Int,
        month:  Int = Calendar.getInstance().get(Calendar.MONTH),
        year:   Int = Calendar.getInstance().get(Calendar.YEAR)
    ): Flow<Budget?>

    // ------------------------------------------------------------------------------------
    // Retrieves the most recent active budget for a user
    @Query("SELECT * FROM budget WHERE userId = :userId AND isActive = 1 ORDER BY createdDate DESC LIMIT 1")
    suspend fun getLatestBudget(userId: Int): Budget?

    // ------------------------------------------------------------------------------------
    // Retrieves all active categories for a specific budget
    @Query("SELECT * FROM budget_category WHERE budgetId = :budgetId AND isActive = 1")
    fun getBudgetCategories(budgetId: Int): Flow<List<BudgetCategory>>

    // ------------------------------------------------------------------------------------
    // Retrieves the most recent active category by name for a user
    @Query("SELECT * FROM budget_category WHERE userId = :userId AND name = :categoryName AND isActive = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getLatestCategoryByName(userId: Int, categoryName: String): BudgetCategory?

    // ------------------------------------------------------------------------------------
    // Updates the spent amount for a specific category
    @Query("UPDATE budget_category SET spent = spent + :amount WHERE id = :categoryId")
    suspend fun updateCategorySpent(categoryId: Int, amount: Double)

    // ------------------------------------------------------------------------------------
    // Calculates the total amount spent across all categories in a budget
    @Query("SELECT SUM(spent) FROM budget_category WHERE budgetId = :budgetId AND isActive = 1")
    suspend fun getTotalSpent(budgetId: Int): Double?

    // ------------------------------------------------------------------------------------
    // Transaction to save a budget
    @Transaction
    suspend fun saveBudget(budget: Budget) {
        setBudget(budget)
    }

    // ------------------------------------------------------------------------------------
    // Transaction to deactivate a budget
    @Transaction
    suspend fun deactivateBudget(budgetId: Int) {
        val b = getLatestBudget(budgetId) ?: return
        updateBudget(b.copy(isActive = false))
    }

    // ------------------------------------------------------------------------------------
    // Transaction to deactivate a budget category
    @Transaction
    suspend fun deactivateBudgetCategory(categoryId: Int) {
        val c = getLatestCategoryByName(categoryId, "") ?: return
        updateBudgetCategory(c.copy(isActive = false))
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------

package com.example.prog7313_groupwork.entities

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

@Dao
interface BudgetDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: Budget)

    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Insert
    suspend fun insertBudgetCategory(category: BudgetCategory)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Update
    suspend fun updateBudgetCategory(category: BudgetCategory)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Delete
    suspend fun deleteBudgetCategory(category: BudgetCategory)

    /**
     * Now filters by userId, month & year.
     * Returns a Flow so HomeActivity can collect and auto-update.
     */
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

    @Query("SELECT * FROM budget WHERE userId = :userId AND isActive = 1 ORDER BY createdDate DESC LIMIT 1")
    suspend fun getLatestBudget(userId: Int): Budget?

    @Query("SELECT * FROM budget_category WHERE budgetId = :budgetId AND isActive = 1")
    fun getBudgetCategories(budgetId: Int): Flow<List<BudgetCategory>>

    @Query("SELECT * FROM budget_category WHERE userId = :userId AND name = :categoryName AND isActive = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getLatestCategoryByName(userId: Int, categoryName: String): BudgetCategory?

    @Query("UPDATE budget_category SET spent = spent + :amount WHERE id = :categoryId")
    suspend fun updateCategorySpent(categoryId: Int, amount: Double)

    @Query("SELECT SUM(spent) FROM budget_category WHERE budgetId = :budgetId AND isActive = 1")
    suspend fun getTotalSpent(budgetId: Int): Double?

    @Transaction
    suspend fun saveBudget(budget: Budget) {
        setBudget(budget)
    }

    @Transaction
    suspend fun deactivateBudget(budgetId: Int) {
        val b = getLatestBudget(budgetId) ?: return
        updateBudget(b.copy(isActive = false))
    }

    @Transaction
    suspend fun deactivateBudgetCategory(categoryId: Int) {
        val c = getLatestCategoryByName(categoryId, "") ?: return
        updateBudgetCategory(c.copy(isActive = false))
    }
}

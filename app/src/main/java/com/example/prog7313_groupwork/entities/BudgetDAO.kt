package com.example.prog7313_groupwork.entities

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM budget WHERE month = :month AND year = :year LIMIT 1")
    fun getCurrentBudget(
        month: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
        year: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    ): Flow<Budget?>

    @Query("SELECT * FROM budget WHERE userId = :userId ORDER BY createdDate DESC LIMIT 1")
    suspend fun getLatestBudget(userId: Int): Budget?

    @Query("SELECT * FROM budget_category WHERE budgetId = :budgetId")
    fun getBudgetCategories(budgetId: Int): Flow<List<BudgetCategory>>

    @Query("SELECT * FROM budget_category WHERE userId = :userId AND name = :categoryName ORDER BY id DESC LIMIT 1")
    suspend fun getLatestCategoryByName(userId: Int, categoryName: String): BudgetCategory?

    @Transaction
    suspend fun saveBudgetWithCategories(budget: Budget, categories: List<BudgetCategory>) {
        val budgetId = insertBudget(budget)
        categories.forEach { category ->
            insertBudgetCategory(category.copy(budgetId = budgetId.toInt()))
        }
    }
} 
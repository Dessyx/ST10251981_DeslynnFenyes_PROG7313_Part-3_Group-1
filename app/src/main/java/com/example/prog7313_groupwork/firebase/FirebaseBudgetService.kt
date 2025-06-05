package com.example.prog7313_groupwork.firebase

import com.example.prog7313_groupwork.entities.Budget
import com.example.prog7313_groupwork.entities.BudgetCategory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class FirebaseBudgetService {
    private val db = FirebaseFirestore.getInstance()
    private val budgetCollection = db.collection("budgets")
    private val budgetCategoryCollection = db.collection("budget_categories")

    suspend fun setBudget(budget: Budget) = withContext(Dispatchers.IO) {
        try {
            val budgetMap: HashMap<String, Any?> = hashMapOf(
                "userId" to budget.userId,
                "totalAmount" to budget.totalAmount,
                "monthlyGoal" to budget.monthlyGoal,
                "month" to budget.month,
                "year" to budget.year,
                "isActive" to budget.isActive,
                "createdDate" to budget.createdDate
            )
            budgetCollection.document(budget.id.toString()).set(budgetMap).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun insertBudget(budget: Budget): Long = withContext(Dispatchers.IO) {
        try {
            val budgetMap: HashMap<String, Any?> = hashMapOf(
                "userId" to budget.userId,
                "totalAmount" to budget.totalAmount,
                "monthlyGoal" to budget.monthlyGoal,
                "month" to budget.month,
                "year" to budget.year,
                "isActive" to budget.isActive,
                "createdDate" to budget.createdDate
            )
            val docRef = budgetCollection.add(budgetMap).await()
            docRef.id.toLong()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun insertBudgetCategory(category: BudgetCategory) = withContext(Dispatchers.IO) {
        try {
            val categoryMap: HashMap<String, Any?> = hashMapOf(
                "budgetId" to category.budgetId,
                "userId" to category.userId,
                "name" to category.name,
                "icon" to category.icon,
                "limit" to category.limit,
                "spent" to category.spent,
                "isActive" to category.isActive
            )
            budgetCategoryCollection.add(categoryMap).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateBudget(budget: Budget) = withContext(Dispatchers.IO) {
        try {
            val budgetMap: HashMap<String, Any?> = hashMapOf(
                "userId" to budget.userId,
                "totalAmount" to budget.totalAmount,
                "monthlyGoal" to budget.monthlyGoal,
                "month" to budget.month,
                "year" to budget.year,
                "isActive" to budget.isActive,
                "createdDate" to budget.createdDate
            )
            budgetCollection.document(budget.id.toString()).update(budgetMap).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateBudgetCategory(category: BudgetCategory) = withContext(Dispatchers.IO) {
        try {
            val categoryMap: HashMap<String, Any?> = hashMapOf(
                "budgetId" to category.budgetId,
                "userId" to category.userId,
                "name" to category.name,
                "icon" to category.icon,
                "limit" to category.limit,
                "spent" to category.spent,
                "isActive" to category.isActive
            )
            budgetCategoryCollection.document(category.id.toString()).update(categoryMap).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteBudget(budget: Budget) = withContext(Dispatchers.IO) {
        try {
            budgetCollection.document(budget.id.toString()).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteBudgetCategory(category: BudgetCategory) = withContext(Dispatchers.IO) {
        try {
            budgetCategoryCollection.document(category.id.toString()).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    fun getCurrentBudget(
        userId: Int,
        month: Int = Calendar.getInstance().get(Calendar.MONTH),
        year: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): Flow<Budget?> = flow {
        try {
            val snapshot = budgetCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()

            val budget = snapshot.documents.firstOrNull()?.let { doc ->
                Budget(
                    id = doc.id.toIntOrNull() ?: 0,
                    userId = doc.getLong("userId")?.toInt() ?: 0,
                    totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                    monthlyGoal = doc.getDouble("monthlyGoal") ?: 0.0,
                    month = doc.getLong("month")?.toInt() ?: 0,
                    year = doc.getLong("year")?.toInt() ?: 0,
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdDate = doc.getLong("createdDate") ?: System.currentTimeMillis()
                )
            }
            emit(budget)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getLatestBudget(userId: Int): Budget? = withContext(Dispatchers.IO) {
        try {
            val snapshot = budgetCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("createdDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.let { doc ->
                Budget(
                    id = doc.id.toIntOrNull() ?: 0,
                    userId = doc.getLong("userId")?.toInt() ?: 0,
                    totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                    monthlyGoal = doc.getDouble("monthlyGoal") ?: 0.0,
                    month = doc.getLong("month")?.toInt() ?: 0,
                    year = doc.getLong("year")?.toInt() ?: 0,
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdDate = doc.getLong("createdDate") ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getBudgetCategories(budgetId: Int): Flow<List<BudgetCategory>> = flow {
        try {
            val snapshot = budgetCategoryCollection
                .whereEqualTo("budgetId", budgetId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                BudgetCategory(
                    id = doc.id.toIntOrNull() ?: 0,
                    budgetId = doc.getLong("budgetId")?.toInt() ?: 0,
                    userId = doc.getLong("userId")?.toInt() ?: 0,
                    name = doc.getString("name") ?: "",
                    icon = doc.getString("icon") ?: "",
                    limit = doc.getDouble("limit") ?: 0.0,
                    spent = doc.getDouble("spent") ?: 0.0,
                    isActive = doc.getBoolean("isActive") ?: true
                )
            }
            emit(categories)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getLatestCategoryByName(userId: Int, categoryName: String): BudgetCategory? = withContext(Dispatchers.IO) {
        try {
            val snapshot = budgetCategoryCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("name", categoryName)
                .whereEqualTo("isActive", true)
                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.let { doc ->
                BudgetCategory(
                    id = doc.id.toIntOrNull() ?: 0,
                    budgetId = doc.getLong("budgetId")?.toInt() ?: 0,
                    userId = doc.getLong("userId")?.toInt() ?: 0,
                    name = doc.getString("name") ?: "",
                    icon = doc.getString("icon") ?: "",
                    limit = doc.getDouble("limit") ?: 0.0,
                    spent = doc.getDouble("spent") ?: 0.0,
                    isActive = doc.getBoolean("isActive") ?: true
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateCategorySpent(categoryId: Int, amount: Double) = withContext(Dispatchers.IO) {
        try {
            val doc = budgetCategoryCollection.document(categoryId.toString()).get().await()
            val currentSpent = doc.getDouble("spent") ?: 0.0
            budgetCategoryCollection.document(categoryId.toString())
                .update("spent", currentSpent + amount)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getTotalSpent(budgetId: Int): Double? = withContext(Dispatchers.IO) {
        try {
            val snapshot = budgetCategoryCollection
                .whereEqualTo("budgetId", budgetId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            snapshot.documents.sumOf { doc ->
                doc.getDouble("spent") ?: 0.0
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveBudget(budget: Budget) = withContext(Dispatchers.IO) {
        try {
            setBudget(budget)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deactivateBudget(budgetId: Int) = withContext(Dispatchers.IO) {
        try {
            val budget = getLatestBudget(budgetId) ?: return@withContext
            updateBudget(budget.copy(isActive = false))
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deactivateBudgetCategory(categoryId: Int) = withContext(Dispatchers.IO) {
        try {
            val category = getLatestCategoryByName(categoryId, "") ?: return@withContext
            updateBudgetCategory(category.copy(isActive = false))
        } catch (e: Exception) {
            throw e
        }
    }
} 
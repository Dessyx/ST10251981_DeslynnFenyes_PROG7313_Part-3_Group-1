package com.example.prog7313_groupwork.firebase

import com.example.prog7313_groupwork.entities.Expense
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseExpenseService {
    private val db = FirebaseFirestore.getInstance()
    private val expensesCollection = db.collection("expenses")

    // Add an expense to Firebase
    suspend fun addExpense(expense: Expense): Boolean {
        return try {
            val expenseMap = hashMapOf(
                "userId" to expense.userId,
                "date" to expense.date,
                "category" to expense.category,
                "amount" to expense.amount,
                "description" to expense.description,
                "imagePath" to expense.imagePath
            )
            expensesCollection.add(expenseMap).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get all expenses for a user
    suspend fun getExpensesByUser(userId: Long): List<Expense> {
        return try {
            val snapshot = expensesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Expense(
                    id = doc.id.toLongOrNull() ?: 0L,
                    userId = doc.getLong("userId") ?: userId,
                    date = doc.getLong("date") ?: System.currentTimeMillis(),
                    category = doc.getString("category") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    imagePath = doc.getString("imagePath")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get expenses for a user within a date range
    suspend fun getExpensesByDateRange(userId: Long, startDate: Long, endDate: Long): List<Expense> {
        return try {
            val snapshot = expensesCollection
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Expense(
                    id = doc.id.toLongOrNull() ?: 0L,
                    userId = doc.getLong("userId") ?: userId,
                    date = doc.getLong("date") ?: System.currentTimeMillis(),
                    category = doc.getString("category") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    imagePath = doc.getString("imagePath")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get expenses for a user by category
    suspend fun getExpensesByCategory(userId: Long, category: String): List<Expense> {
        return try {
            val snapshot = expensesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", category)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Expense(
                    id = doc.id.toLongOrNull() ?: 0L,
                    userId = doc.getLong("userId") ?: userId,
                    date = doc.getLong("date") ?: System.currentTimeMillis(),
                    category = doc.getString("category") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    imagePath = doc.getString("imagePath")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get total spending for a user
    suspend fun getTotalSpending(userId: Long): Double {
        return try {
            val expenses = getExpensesByUser(userId)
            expenses.sumOf { it.amount }
        } catch (e: Exception) {
            0.0
        }
    }

    // Get total spending for a user within a date range
    suspend fun getTotalSpendingByDateRange(userId: Long, startDate: Long, endDate: Long): Double {
        return try {
            val expenses = getExpensesByDateRange(userId, startDate, endDate)
            expenses.sumOf { it.amount }
        } catch (e: Exception) {
            0.0
        }
    }

    // Get total spending for a user by category
    suspend fun getTotalSpendingByCategory(userId: Long, category: String): Double {
        return try {
            val expenses = getExpensesByCategory(userId, category)
            expenses.sumOf { it.amount }
        } catch (e: Exception) {
            0.0
        }
    }

    // Update an expense
    suspend fun updateExpense(expenseId: String, updatedExpense: Expense): Boolean {
        return try {
            val expenseMap = hashMapOf(
                "userId" to updatedExpense.userId,
                "date" to updatedExpense.date,
                "category" to updatedExpense.category,
                "amount" to updatedExpense.amount,
                "description" to updatedExpense.description,
                "imagePath" to updatedExpense.imagePath
            )
            expensesCollection.document(expenseId)
                .set(expenseMap, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete an expense
    suspend fun deleteExpense(expenseId: String): Boolean {
        return try {
            expensesCollection.document(expenseId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Batch delete multiple expenses
    suspend fun batchDeleteExpenses(expenseIds: List<String>): Boolean {
        return try {
            val batch = db.batch()
            expenseIds.forEach { id ->
                batch.delete(expensesCollection.document(id))
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get expenses with pagination
    suspend fun getExpensesWithPagination(
        userId: Long,
        limit: Long,
        lastDocumentId: String? = null
    ): Pair<List<Expense>, String?> {
        return try {
            var query = expensesCollection
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)

            if (lastDocumentId != null) {
                val lastDoc = expensesCollection.document(lastDocumentId).get().await()
                query = query.startAfter(lastDoc)
            }

            val snapshot = query.get().await()
            val expenses = snapshot.toObjects(Expense::class.java)
            val lastDocId = if (expenses.isNotEmpty()) snapshot.documents.last().id else null

            Pair(expenses, lastDocId)
        } catch (e: Exception) {
            Pair(emptyList(), null)
        }
    }
}
package com.example.prog7313_groupwork.firebase

import com.example.prog7313_groupwork.entities.Expense
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseExpenseService {
    private val db = FirebaseFirestore.getInstance()
    private val expensesCollection = db.collection("expenses")

    // Add an expense to Firebase
    suspend fun addExpense(expense: Expense): Boolean {
        return try {
            expensesCollection.add(expense).await()
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

            snapshot.toObjects(Expense::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Update an expense
    suspend fun updateExpense(expenseId: String, updatedExpense: Expense): Boolean {
        return try {
            expensesCollection.document(expenseId)
                .set(updatedExpense, SetOptions.merge())
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
}
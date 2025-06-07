package com.example.prog7313_groupwork.firebase

import com.example.prog7313_groupwork.entities.Income
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseIncomeService {
    private val db = FirebaseFirestore.getInstance()
    private val incomeCollection = db.collection("income")

    suspend fun insertIncome(income: Income) = withContext(Dispatchers.IO) {
        try {
            val incomeMap = hashMapOf(
                "userId" to income.userId,
                "amount" to income.amount,
                "description" to income.description,
                "date" to income.date,
                "category" to income.category
            )
            incomeCollection.add(incomeMap).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateIncome(income: Income) = withContext(Dispatchers.IO) {
        try {
            val incomeMap = hashMapOf(
                "userId" to income.userId,
                "amount" to income.amount,
                "description" to income.description,
                "date" to income.date,
                "category" to income.category
            )
            incomeCollection.document(income.id.toString()).set(incomeMap).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteIncome(income: Income) = withContext(Dispatchers.IO) {
        try {
            incomeCollection.document(income.id.toString()).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    fun getAllIncomeForUser(userId: Long): Flow<List<Income>> = flow {
        try {
            val snapshot = incomeCollection.whereEqualTo("userId", userId).get().await()
            val incomes = snapshot.documents.mapNotNull { doc ->
                Income(
                    id = doc.id.toLongOrNull() ?: 0L,
                    userId = doc.getLong("userId") ?: userId,
                    amount = doc.getDouble("amount") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    date = doc.getLong("date") ?: System.currentTimeMillis(),
                    category = doc.getString("category") ?: "General"
                )
            }
            emit(incomes)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getIncomeById(id: Long): Income? = withContext(Dispatchers.IO) {
        try {
            val doc = incomeCollection.document(id.toString()).get().await()
            if (doc.exists()) {
                Income(
                    id = doc.id.toLongOrNull() ?: 0L,
                    userId = doc.getLong("userId") ?: 0L,
                    amount = doc.getDouble("amount") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    date = doc.getLong("date") ?: System.currentTimeMillis(),
                    category = doc.getString("category") ?: "General"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTotalIncomeForUser(userId: Long): Double? = withContext(Dispatchers.IO) {
        try {
            val snapshot = incomeCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.sumOf { doc ->
                doc.getDouble("amount") ?: 0.0
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getIncomeByCategory(userId: Long, category: String): Flow<List<Income>> = flow {
        try {
            val snapshot = incomeCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", category)
                .get()
                .await()
            val incomes = snapshot.documents.mapNotNull { doc ->
                Income(
                    id = doc.id.toLongOrNull() ?: 0L,
                    userId = doc.getLong("userId") ?: userId,
                    amount = doc.getDouble("amount") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    date = doc.getLong("date") ?: System.currentTimeMillis(),
                    category = doc.getString("category") ?: "General"
                )
            }
            emit(incomes)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllActiveIncome(): List<Income> = withContext(Dispatchers.IO) {
        try {
            val snapshot = incomeCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                Income(
                    id = doc.id.toLongOrNull() ?: 0L,
                    userId = doc.getLong("userId") ?: 0L,
                    amount = doc.getDouble("amount") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    date = doc.getLong("date") ?: System.currentTimeMillis(),
                    category = doc.getString("category") ?: "General"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 
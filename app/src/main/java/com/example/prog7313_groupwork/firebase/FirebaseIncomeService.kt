package com.example.prog7313_groupwork.firebase

// Imports
import com.example.prog7313_groupwork.entities.Income
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ------------------------------------ Firebase Income Service Class ----------------------------------------
// This class handles all income-related operations with Firebase Firestore
class FirebaseIncomeService {
    private val db = FirebaseFirestore.getInstance()
    private val incomeCollection = db.collection("income")

    // ------------------------------------------------------------------------------------
    // Inserts a new income record into Firestore
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

    // ------------------------------------------------------------------------------------
    // Updates an existing income record in Firestore
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

    // ------------------------------------------------------------------------------------
    // Deletes an income record from Firestore
    suspend fun deleteIncome(income: Income) = withContext(Dispatchers.IO) {
        try {
            incomeCollection.document(income.id.toString()).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    // ------------------------------------------------------------------------------------
    // Gets all income records for a specific user
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

    // ------------------------------------------------------------------------------------
    // Gets a specific income record by its ID
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

    // ------------------------------------------------------------------------------------
    // Gets the total income amount for a specific user
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

    // ------------------------------------------------------------------------------------
    // Gets all income records for a specific user and category
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

    // ------------------------------------------------------------------------------------
    // Gets all active income records
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
// -----------------------------------<<< End Of File >>>------------------------------------------ 
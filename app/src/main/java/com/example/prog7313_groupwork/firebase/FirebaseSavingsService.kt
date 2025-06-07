package com.example.prog7313_groupwork.firebase
import com.example.prog7313_groupwork.entities.Savings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseSavingsService {
    private val db = FirebaseFirestore.getInstance()
    private val savingsCollection = db.collection("savings")

    // Add or update savings for a user
    suspend fun saveSavings(savings: Savings): Boolean {
        return try {
            val savingsMap = hashMapOf(
                "userId" to savings.userId,
                "amount" to savings.amount,
                "date" to savings.date
            )
            
            // Use userId as document ID to ensure one savings record per user
            savingsCollection.document(savings.userId.toString())
                .set(savingsMap, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get savings for a user
    suspend fun getSavingsByUser(userId: Long): Savings? {
        return try {
            val doc = savingsCollection.document(userId.toString()).get().await()
            if (doc.exists()) {
                Savings(
                    userId = doc.getLong("userId") ?: userId,
                    amount = doc.getDouble("amount") ?: 0.0,
                    date = doc.getLong("date") ?: System.currentTimeMillis()
                )
            } else {
                // If no savings record exists, create one with zero amount
                val newSavings = Savings(userId = userId, amount = 0.0, date = System.currentTimeMillis())
                saveSavings(newSavings)
                newSavings
            }
        } catch (e: Exception) {
            null
        }
    }

    // Get total savings for a user
    suspend fun getTotalSavings(userId: Long): Double {
        return try {
            val savings = getSavingsByUser(userId)
            savings?.amount ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    // Update savings amount for a user
    suspend fun updateSavingsAmount(userId: Long, newAmount: Double): Boolean {
        return try {
            val savings = getSavingsByUser(userId)
            if (savings != null) {
                val updatedSavings = savings.copy(amount = newAmount)
                saveSavings(updatedSavings)
            } else {
                val newSavings = Savings(userId = userId, amount = newAmount, date = System.currentTimeMillis())
                saveSavings(newSavings)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete savings for a user
    suspend fun deleteSavings(userId: Long): Boolean {
        return try {
            savingsCollection.document(userId.toString()).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

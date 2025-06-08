package com.example.prog7313_groupwork.firebase

// Imports
import com.example.prog7313_groupwork.entities.Savings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// ------------------------------------ Firebase Savings Service Class ----------------------------------------
// This class handles all savings-related operations with Firebase Firestore
class FirebaseSavingsService {
    private val db = FirebaseFirestore.getInstance()
    private val savingsCollection = db.collection("savings")

    // ------------------------------------------------------------------------------------
    // Adds or updates savings for a user in Firestore
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

    // ------------------------------------------------------------------------------------
    // Gets savings record for a specific user
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

    // ------------------------------------------------------------------------------------
    // Gets the total savings amount for a specific user
    suspend fun getTotalSavings(userId: Long): Double {
        return try {
            val savings = getSavingsByUser(userId)
            savings?.amount ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    // ------------------------------------------------------------------------------------
    // Updates the savings amount for a specific user
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

    // ------------------------------------------------------------------------------------
    // Deletes the savings record for a specific user
    suspend fun deleteSavings(userId: Long): Boolean {
        return try {
            savingsCollection.document(userId.toString()).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------

package com.example.prog7313_groupwork.firebase
import com.example.prog7313_groupwork.entities.Savings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlin.collections.set
import kotlin.text.get

class FirebaseSavingsService {
    private val db = FirebaseFirestore.getInstance()
    private val savingsCollection = db.collection("savings")

    // Add or update savings for a user
    suspend fun saveSavings(savings: Savings): Boolean {
        return try {
            // Use userId as document ID to ensure one savings record per user
            savingsCollection.document(savings.userId.toString())
                .set(savings, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get savings for a user
    suspend fun getSavingsByUser(userId: Long): Savings? {
        return try {
            val document = savingsCollection.document(userId.toString()).get().await()
            document.toObject(Savings::class.java)
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
            savingsCollection.document(userId.toString())
                .update("amount", newAmount)
                .await()
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

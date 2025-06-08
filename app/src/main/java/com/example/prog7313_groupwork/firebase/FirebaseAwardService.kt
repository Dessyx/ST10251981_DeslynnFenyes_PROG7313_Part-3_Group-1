package com.example.prog7313_groupwork.firebase

// Imports
import com.example.prog7313_groupwork.entities.Award
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// ------------------------------------ Firebase Award Service Class ----------------------------------------
// This class handles all award-related operations with Firebase Firestore
class FirebaseAwardService {
    private val db = FirebaseFirestore.getInstance()
    private val awardsCollection = db.collection("awards")

    // ------------------------------------------------------------------------------------
    // Inserts a new award into Firestore
    suspend fun insertAward(award: Award) {
        awardsCollection.document(award.id.toString()).set(award).await()
    }

    // ------------------------------------------------------------------------------------
    // Retrieves all awards for a specific user
    suspend fun getUserAwards(userId: Long): List<Award> {
        return awardsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(Award::class.java)
    }

    // ------------------------------------------------------------------------------------
    // Updates an existing award in Firestore
    suspend fun updateAward(award: Award) {
        awardsCollection.document(award.id.toString()).set(award).await()
    }

    // ------------------------------------------------------------------------------------
    // Retrieves the next unachieved award for a user, ordered by goal amount
    suspend fun getNextUnachievedAward(userId: Long): Award? {
        return awardsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("achieved", false)
            .orderBy("goalAmount")
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(Award::class.java)
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------ 
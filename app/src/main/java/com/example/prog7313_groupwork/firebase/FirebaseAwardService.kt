package com.example.prog7313_groupwork.firebase

import com.example.prog7313_groupwork.entities.Award
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAwardService {
    private val db = FirebaseFirestore.getInstance()
    private val awardsCollection = db.collection("awards")

    suspend fun insertAward(award: Award) {
        awardsCollection.document(award.id.toString()).set(award).await()
    }

    suspend fun getUserAwards(userId: Long): List<Award> {
        return awardsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(Award::class.java)
    }

    suspend fun updateAward(award: Award) {
        awardsCollection.document(award.id.toString()).set(award).await()
    }

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
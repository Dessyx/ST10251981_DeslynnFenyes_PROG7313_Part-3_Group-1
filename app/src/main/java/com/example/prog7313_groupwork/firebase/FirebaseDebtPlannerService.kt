package com.example.prog7313_groupwork.firebase

import com.example.prog7313_groupwork.entities.DebtPlan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseDebtPlannerService {
    private val db = FirebaseFirestore.getInstance()
    private val debtPlansCollection = db.collection("debt_plans")

    suspend fun insertDebtPlan(debtPlan: DebtPlan) {
        debtPlansCollection.document(debtPlan.id.toString()).set(debtPlan).await()
    }

    suspend fun updateDebtPlan(debtPlan: DebtPlan) {
        debtPlansCollection.document(debtPlan.id.toString()).set(debtPlan).await()
    }

    suspend fun deleteDebtPlan(debtPlan: DebtPlan) {
        debtPlansCollection.document(debtPlan.id.toString()).delete().await()
    }

    fun getAllDebtPlansForUser(userId: Int): Flow<List<DebtPlan>> = flow {
        val snapshot = debtPlansCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdDate")
            .get()
            .await()
        emit(snapshot.toObjects(DebtPlan::class.java))
    }

    suspend fun getLatestDebtPlanForUser(userId: Int): DebtPlan? {
        return debtPlansCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdDate")
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(DebtPlan::class.java)
    }

    suspend fun getDebtPlanById(id: Int): DebtPlan? {
        return debtPlansCollection
            .document(id.toString())
            .get()
            .await()
            .toObject(DebtPlan::class.java)
    }
} 
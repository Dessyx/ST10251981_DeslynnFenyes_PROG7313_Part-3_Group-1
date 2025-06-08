package com.example.prog7313_groupwork.firebase

// Imports
import com.example.prog7313_groupwork.entities.DebtPlan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

// ------------------------------------ Firebase Debt Planner Service Class ----------------------------------------
// This class handles all debt plan-related operations with Firebase Firestore
class FirebaseDebtPlannerService {
    private val db = FirebaseFirestore.getInstance()
    private val debtPlansCollection = db.collection("debt_plans")

    // ------------------------------------------------------------------------------------
    // Inserts a new debt plan into Firestore
    suspend fun insertDebtPlan(debtPlan: DebtPlan) {
        debtPlansCollection.document(debtPlan.id.toString()).set(debtPlan).await()
    }

    // ------------------------------------------------------------------------------------
    // Updates an existing debt plan in Firestore
    suspend fun updateDebtPlan(debtPlan: DebtPlan) {
        debtPlansCollection.document(debtPlan.id.toString()).set(debtPlan).await()
    }

    // ------------------------------------------------------------------------------------
    // Deletes a debt plan from Firestore
    suspend fun deleteDebtPlan(debtPlan: DebtPlan) {
        debtPlansCollection.document(debtPlan.id.toString()).delete().await()
    }

    // ------------------------------------------------------------------------------------
    // Gets all debt plans for a specific user
    fun getAllDebtPlansForUser(userId: Int): Flow<List<DebtPlan>> = flow {
        val snapshot = debtPlansCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdDate")
            .get()
            .await()
        emit(snapshot.toObjects(DebtPlan::class.java))
    }

    // ------------------------------------------------------------------------------------
    // Gets the latest debt plan for a specific user
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

    // ------------------------------------------------------------------------------------
    // Gets a specific debt plan by its ID
    suspend fun getDebtPlanById(id: Int): DebtPlan? {
        return debtPlansCollection
            .document(id.toString())
            .get()
            .await()
            .toObject(DebtPlan::class.java)
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------ 
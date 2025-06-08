package com.example.prog7313_groupwork.firebase

// Imports
import com.example.prog7313_groupwork.entities.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

// ------------------------------------ Firebase Category Service Class ----------------------------------------
// This class handles all category-related operations with Firebase Firestore
class FirebaseCategoryService {
    private val db = FirebaseFirestore.getInstance()
    private val categoryCollection = db.collection("categories")

    // ------------------------------------------------------------------------------------
    // Saves or updates a category in Firestore
    suspend fun saveCategory(category: Category) {
        val categoryMap = hashMapOf(
            "categoryName" to category.categoryName,
            "categoryLimit" to category.categoryLimit,
            "spent" to (category.spent ?: 0.0),
            "userId" to category.userId
        )
        
        if (category.id.isNotEmpty()) {
            categoryCollection.document(category.id).set(categoryMap).await()
        } else {
            val newDoc = categoryCollection.document()
            categoryMap["id"] = newDoc.id
            newDoc.set(categoryMap).await()
        }
    }

    // ------------------------------------------------------------------------------------
    // Gets all categories from Firestore
    suspend fun getAllCategories(): List<Category> {
        val snapshot = categoryCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            Category(
                id = doc.id,
                categoryName = doc.getString("categoryName") ?: "",
                categoryLimit = doc.getString("categoryLimit") ?: "",
                spent = doc.getDouble("spent"),
                userId = doc.getLong("userId") ?: -1L
            )
        }
    }

    // ------------------------------------------------------------------------------------
    // Gets all categories for a specific user
    suspend fun getCategoriesForUser(userId: Long): List<Category> {
        val snapshot = categoryCollection.whereEqualTo("userId", userId).get().await()
        return snapshot.documents.mapNotNull { doc ->
            Category(
                id = doc.id,
                categoryName = doc.getString("categoryName") ?: "",
                categoryLimit = doc.getString("categoryLimit") ?: "",
                spent = doc.getDouble("spent"),
                userId = doc.getLong("userId") ?: -1L
            )
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
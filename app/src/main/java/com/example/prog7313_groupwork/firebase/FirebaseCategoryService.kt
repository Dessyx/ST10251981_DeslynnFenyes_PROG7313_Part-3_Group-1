package com.example.prog7313_groupwork.firebase
import com.example.prog7313_groupwork.entities.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class FirebaseCategoryService {

    private val db = FirebaseFirestore.getInstance()
    private val categoryCollection = db.collection("categories")

    // Add or update category
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

    // Get all categories
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

    // Get categories for a specific user
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
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
        val newDoc = categoryCollection.document()
        val categoryWithId = category.copy(id = newDoc.id)
        newDoc.set(categoryWithId).await()
    }

    // Get all categories
    suspend fun getAllCategories(): List<Category> {
        val snapshot = categoryCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject<Category>() }
    }

    // Get categories for a specific user
    suspend fun getCategoriesForUser(userId: Long): List<Category> {
        val snapshot = categoryCollection.whereEqualTo("userId", userId).get().await()
        return snapshot.documents.mapNotNull { it.toObject<Category>() }
    }
}
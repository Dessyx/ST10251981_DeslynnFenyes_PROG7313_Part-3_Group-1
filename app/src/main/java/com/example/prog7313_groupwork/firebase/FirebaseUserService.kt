package com.example.prog7313_groupwork.firebase

import com.example.prog7313_groupwork.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseUserService {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        try {
            val userMap = hashMapOf(
                "nameSurname" to user.NameSurname,
                "phoneNumber" to user.PhoneNumber,
                "userEmail" to user.userEmail,
                "passwordHash" to user.passwordHash,
                "language" to user.language,
                "currency" to user.currency,
                "themeColor" to user.themeColor
            )
            val docRef = usersCollection.add(userMap).await()
            docRef.id.toLong()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getLatestUser(): User? = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection.orderBy("nameSurname").limit(1).get().await()
            snapshot.documents.firstOrNull()?.let { doc ->
                User(
                    id = doc.id.toLong(),
                    NameSurname = doc.getString("nameSurname") ?: "",
                    PhoneNumber = doc.getLong("phoneNumber")?.toInt() ?: 0,
                    userEmail = doc.getString("userEmail") ?: "",
                    passwordHash = doc.getString("passwordHash") ?: "",
                    language = doc.getString("language") ?: "en",
                    currency = doc.getString("currency") ?: "ZAR",
                    themeColor = doc.getLong("themeColor")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection.whereEqualTo("userEmail", email).limit(1).get().await()
            snapshot.documents.firstOrNull()?.let { doc ->
                User(
                    id = doc.id.toLong(),
                    NameSurname = doc.getString("nameSurname") ?: "",
                    PhoneNumber = doc.getLong("phoneNumber")?.toInt() ?: 0,
                    userEmail = doc.getString("userEmail") ?: "",
                    passwordHash = doc.getString("passwordHash") ?: "",
                    language = doc.getString("language") ?: "en",
                    currency = doc.getString("currency") ?: "ZAR",
                    themeColor = doc.getLong("themeColor")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserById(userId: Long): User? = withContext(Dispatchers.IO) {
        try {
            val doc = usersCollection.document(userId.toString()).get().await()
            doc.toObject(User::class.java)?.copy(id = doc.id.toLong())
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserCredentials(userId: Long, email: String, passwordHash: String) = withContext(Dispatchers.IO) {
        try {
            val updates = hashMapOf<String, Any>(
                "userEmail" to email,
                "passwordHash" to passwordHash
            )
            usersCollection.document(userId.toString()).update(updates).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserLanguage(userId: Long, language: String) = withContext(Dispatchers.IO) {
        try {
            usersCollection.document(userId.toString()).update("language", language).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserCurrency(userId: Long, currency: String) = withContext(Dispatchers.IO) {
        try {
            usersCollection.document(userId.toString()).update("currency", currency).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserThemeColor(userId: Long, color: Int) = withContext(Dispatchers.IO) {
        try {
            usersCollection.document(userId.toString()).update("themeColor", color).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteUserById(userId: Long) = withContext(Dispatchers.IO) {
        try {
            usersCollection.document(userId.toString()).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }
} 
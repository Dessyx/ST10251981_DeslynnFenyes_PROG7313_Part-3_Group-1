package com.example.prog7313_groupwork.firebase

import com.example.prog7313_groupwork.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class FirebaseUserService {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        try {
            val userMap: HashMap<String, Any?> = hashMapOf(
                "nameSurname" to user.NameSurname,
                "phoneNumber" to user.PhoneNumber,
                "userEmail" to user.userEmail,
                "passwordHash" to user.passwordHash,
                "language" to user.language,
                "currency" to user.currency,
                "themeColor" to user.themeColor
                // Add a numeric ID field for app-wide use
                // If user.id is 0, generate a new one (for new users)
                // Otherwise, use the provided user.id (for updates of existing users based on numeric ID)
            )

            val userIdToUse = if (user.id == 0L) System.currentTimeMillis() else user.id
            userMap["numericUserId"] = userIdToUse

            // When inserting, let Firestore generate its own string ID.
            // When updating, find the document by numericUserId and update.

            if (user.id == 0L) {
                // New user - add a new document
                 usersCollection.add(userMap).await()
            } else {
                // Existing user (identified by numericUserId) - find and update the document
                val snapshot = usersCollection.whereEqualTo("numericUserId", user.id).limit(1).get().await()
                val docRef = snapshot.documents.firstOrNull()?.reference
                if (docRef != null) {
                    docRef.update(userMap as Map<String, Any>).await()
                } else {
                    // This case should ideally not happen if logic is correct,
                    // but handle if a user with that numeric ID isn't found for update.
                    // You might want to log an error or throw an exception here.
                     usersCollection.add(userMap).await() // Optionally re-add if not found, but careful with duplicates
                }
            }

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getLatestUser(): User? = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection.orderBy("numericUserId", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(1).get().await()
            snapshot.documents.firstOrNull()?.let { doc ->
                mapDocumentToUser(doc)
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error getting latest user", e) // Added logging
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection.whereEqualTo("userEmail", email).limit(1).get().await()
            snapshot.documents.firstOrNull()?.let { doc ->
                 mapDocumentToUser(doc)
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error getting user by email: $email", e) // Added logging
            null
        }
    }

    suspend fun getUserById(userId: Long): User? = withContext(Dispatchers.IO) {
        try {
             // Query by the numericUserId field
            val snapshot = usersCollection.whereEqualTo("numericUserId", userId).limit(1).get().await()
            snapshot.documents.firstOrNull()?.let { doc ->
                mapDocumentToUser(doc)
            }
        } catch (e: Exception) {
             Log.e("FirebaseUserService", "Error getting user by ID: $userId", e) // Added logging
            null
        }
    }

    // Helper function to map Firestore DocumentSnapshot to User object
    private fun mapDocumentToUser(doc: com.google.firebase.firestore.DocumentSnapshot): User? {
        return try {
            User(
                id = doc.getLong("numericUserId") ?: 0L, // Use the stored numeric ID
                NameSurname = doc.getString("nameSurname") ?: "",
                PhoneNumber = doc.getLong("phoneNumber")?.toInt() ?: 0,
                userEmail = doc.getString("userEmail") ?: "",
                passwordHash = doc.getString("passwordHash") ?: "",
                language = doc.getString("language") ?: "en",
                currency = doc.getString("currency") ?: "ZAR",
                themeColor = doc.getLong("themeColor")?.toInt() ?: 0
            )
        } catch (e: Exception) {
             Log.e("FirebaseUserService", "Error mapping document to User", e)
            null
        }
    }

    suspend fun updateUserCredentials(userId: Long, email: String, passwordHash: String) = withContext(Dispatchers.IO) {
        try {
            val updates = hashMapOf<String, Any>(
                "userEmail" to email,
                "passwordHash" to passwordHash
            )
             // Find the document by numericUserId and update it
            val snapshot = usersCollection.whereEqualTo("numericUserId", userId).limit(1).get().await()
            val docRef = snapshot.documents.firstOrNull()?.reference
            if (docRef != null) {
                docRef.update(updates).await()
            } else {
                 Log.e("FirebaseUserService", "User document not found for update with numeric ID: $userId")
                 // Optionally throw an exception or handle this case as needed
            }

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserLanguage(userId: Long, language: String) = withContext(Dispatchers.IO) {
        try {
             // Find the document by numericUserId and update it
            val snapshot = usersCollection.whereEqualTo("numericUserId", userId).limit(1).get().await()
            val docRef = snapshot.documents.firstOrNull()?.reference
             if (docRef != null) {
                docRef.update("language", language).await()
            } else {
                 Log.e("FirebaseUserService", "User document not found for language update with numeric ID: $userId")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserCurrency(userId: Long, currency: String) = withContext(Dispatchers.IO) {
        try {
             // Find the document by numericUserId and update it
            val snapshot = usersCollection.whereEqualTo("numericUserId", userId).limit(1).get().await()
            val docRef = snapshot.documents.firstOrNull()?.reference
             if (docRef != null) {
                docRef.update("currency", currency).await()
            } else {
                 Log.e("FirebaseUserService", "User document not found for currency update with numeric ID: $userId")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserThemeColor(userId: Long, color: Int) = withContext(Dispatchers.IO) {
        try {
             // Find the document by numericUserId and update it
            val snapshot = usersCollection.whereEqualTo("numericUserId", userId).limit(1).get().await()
            val docRef = snapshot.documents.firstOrNull()?.reference
             if (docRef != null) {
                docRef.update("themeColor", color).await()
            } else {
                 Log.e("FirebaseUserService", "User document not found for theme color update with numeric ID: $userId")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteUserById(userId: Long) = withContext(Dispatchers.IO) {
        try {
            // Find the document by numericUserId and delete it
            val snapshot = usersCollection.whereEqualTo("numericUserId", userId).limit(1).get().await()
            val docRef = snapshot.documents.firstOrNull()?.reference
            if (docRef != null) {
                docRef.delete().await()
            } else {
                 Log.e("FirebaseUserService", "User document not found for deletion with numeric ID: $userId")
            }
        } catch (e: Exception) {
            throw e
        }
    }
} 
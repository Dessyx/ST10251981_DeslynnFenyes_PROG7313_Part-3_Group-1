package com.example.prog7313_groupwork.firebase

// Imports
import com.example.prog7313_groupwork.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

// ------------------------------------ Firebase User Service Class ----------------------------------------
// This class handles all user-related operations with Firebase Firestore
class FirebaseUserService {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // ------------------------------------------------------------------------------------
    // Inserts a new user or updates an existing user in Firestore
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
            )

            val userIdToUse = if (user.id == 0L) System.currentTimeMillis() else user.id
            userMap["numericUserId"] = userIdToUse

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

                     usersCollection.add(userMap).await()
                }
            }

        } catch (e: Exception) {
            throw e
        }
    }

    // ------------------------------------------------------------------------------------
    // Retrieves the most recently created user from Firestore
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

    // ------------------------------------------------------------------------------------
    // Retrieves a user by their email address
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

    // ------------------------------------------------------------------------------------
    // Retrieves a user by their numeric ID
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

    // ------------------------------------------------------------------------------------
    // Helper function to convert Firestore document to User object
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

    // ------------------------------------------------------------------------------------
    // Updates user's email and password credentials
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

    // ------------------------------------------------------------------------------------
    // Updates user's preferred language
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

    // ------------------------------------------------------------------------------------
    // Updates user's preferred currency
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

    // ------------------------------------------------------------------------------------
    // Updates user's theme color preference
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

    // ------------------------------------------------------------------------------------
    // Deletes a user from Firestore
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
package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// ------------------------------------ User DAO Interface ----------------------------------------
// This interface defines the database operations for the User entity
@Dao
interface UserDAO {
    // ------------------------------------------------------------------------------------
    // Inserts a new user or replaces an existing one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    // ------------------------------------------------------------------------------------
    // Retrieves the most recently created user
    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    suspend fun getLatestUser(): User?

    // ------------------------------------------------------------------------------------
    // Retrieves a user by their email address
    @Query("SELECT * FROM users WHERE userEmail = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // ------------------------------------------------------------------------------------
    // Retrieves a user by their ID
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    // ------------------------------------------------------------------------------------
    // Retrieves the total savings for a specific user
    @Query("SELECT SUM(amount) FROM savings WHERE userId = :userId")
    suspend fun getTotalSavings(userId: Long): Int?

    // ------------------------------------------------------------------------------------
    // Updates a user's email and password
    @Query("UPDATE users SET userEmail = :email, passwordHash = :passwordHash WHERE id = :userId")
    suspend fun updateUserCredentials(userId: Long, email: String, passwordHash: String)

    // ------------------------------------------------------------------------------------
    // Updates a user's preferred language
    @Query("UPDATE users SET language = :language WHERE id = :userId")
    suspend fun updateUserLanguage(userId: Long, language: String)

    // ------------------------------------------------------------------------------------
    // Updates a user's preferred currency
    @Query("UPDATE users SET currency = :currency WHERE id = :userId")
    suspend fun updateUserCurrency(userId: Long, currency: String)

    // ------------------------------------------------------------------------------------
    // Updates a user's preferred theme color
    @Query("UPDATE users SET themeColor = :color WHERE id = :userId")
    suspend fun updateUserThemeColor(userId: Long, color: Int)

    // ------------------------------------------------------------------------------------
    // Deletes a user by their ID
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)
}
// -----------------------------------<<< End Of File >>>------------------------------------------
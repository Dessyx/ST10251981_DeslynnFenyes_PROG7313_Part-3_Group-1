package com.example.prog7313_groupwork.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    suspend fun getLatestUser(): User?

    @Query("SELECT * FROM users WHERE userEmail = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Delete
    suspend fun clearUsers()

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT SUM(amount) FROM savings WHERE userId = :userId")
    suspend fun getTotalSavings(userId: Long): Int?

    @Query("UPDATE users SET userEmail = :email, passwordHash = :passwordHash WHERE id = :userId")
    suspend fun updateUserCredentials(userId: Long, email: String, passwordHash: String)

    @Query("UPDATE users SET language = :language WHERE id = :userId")
    suspend fun updateUserLanguage(userId: Long, language: String)

    @Query("UPDATE users SET currency = :currency WHERE id = :userId")
    suspend fun updateUserCurrency(userId: Long, currency: String)

    @Query("UPDATE users SET themeColor = :color WHERE id = :userId")
    suspend fun updateUserThemeColor(userId: Long, color: Int)
}
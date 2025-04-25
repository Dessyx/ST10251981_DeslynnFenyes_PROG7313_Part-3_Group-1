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

    @Query("SELECT * FROM users WHERE userEmail = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Delete()
    suspend fun clearUsers()
}
package com.example.prog7313_groupwork.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavingsDAO {
    @Insert
    suspend fun insertSavings(savings: Savings)

    @Query("SELECT SUM(amount) FROM savings WHERE userId = :userId")
    suspend fun getTotalSavings(userId: Long): Int?

    @Query("SELECT * FROM savings WHERE userId = :userId ORDER BY date DESC")
    suspend fun getUserSavings(userId: Long): List<Savings>
}
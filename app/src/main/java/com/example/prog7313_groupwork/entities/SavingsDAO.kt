package com.example.prog7313_groupwork.entities

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavings(savings: Savings): Long

    @Update
    suspend fun updateSavings(savings: Savings)

    @Delete
    suspend fun deleteSavings(savings: Savings)

    @Query("SELECT * FROM savings WHERE userId = :userId AND isActive = 1 ORDER BY date DESC")
    fun getSavingsByUser(userId: Long): Flow<List<Savings>>

    @Query("SELECT * FROM savings WHERE id = :savingsId AND isActive = 1")
    suspend fun getSavingsById(savingsId: Long): Savings?

    @Query("SELECT SUM(amount) FROM savings WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalSavings(userId: Long): Double?

    @Query("SELECT * FROM savings WHERE userId = :userId AND category = :category AND isActive = 1")
    fun getSavingsByCategory(userId: Long, category: String): Flow<List<Savings>>

    @Query("UPDATE savings SET isActive = 0 WHERE id = :savingsId")
    suspend fun deactivateSavings(savingsId: Long)

    @Query("DELETE FROM savings WHERE isActive = 0")
    suspend fun deleteInactiveSavings()

    @Transaction
    suspend fun deactivateAndDeleteSavings(savingsId: Long) {
        val savings = getSavingsById(savingsId) ?: return
        deactivateSavings(savingsId)
        deleteInactiveSavings()
    }
}
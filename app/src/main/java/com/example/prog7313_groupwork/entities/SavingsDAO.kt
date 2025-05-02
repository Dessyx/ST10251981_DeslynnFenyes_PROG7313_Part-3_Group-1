package com.example.prog7313_groupwork.entities

// imports
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ------------------------------------ Savings DAO Interface ----------------------------------------
// This interface defines the database operations for the Savings entity
@Dao
interface SavingsDAO {
    // ------------------------------------------------------------------------------------
    // Inserts the saving amount entered to the table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavings(savings: Savings): Long
    // -------------------------------------------------------------------------------------
    @Update
    suspend fun updateSavings(savings: Savings)

    @Delete
    suspend fun deleteSavings(savings: Savings)

    @Query("SELECT * FROM savings WHERE userId = :userId AND isActive = 1 ORDER BY date DESC")
    fun getSavingsByUser(userId: Long): Flow<List<Savings>>

    // ------------------------------------------------------------------------------------
    // Fetches the existing savings for that specific user
    @Query("SELECT * FROM savings WHERE id = :savingsId AND isActive = 1")
    suspend fun getSavingsById(savingsId: Long): Savings?

    // ------------------------------------------------------------------------------------
    // Get the sum of all the users savings
    @Query("SELECT SUM(amount) FROM savings WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalSavings(userId: Long): Double?
    //-------------------------------------------------------------------------------------

    @Query("SELECT * FROM savings WHERE userId = :userId AND category = :category AND isActive = 1")
    fun getSavingsByCategory(userId: Long, category: String): Flow<List<Savings>>

    // ------------------------------------------------------------------------------------
    // Deactivates savings
    @Query("UPDATE savings SET isActive = 0 WHERE id = :savingsId")
    suspend fun deactivateSavings(savingsId: Long)

    // ------------------------------------------------------------------------------------
    // Delete inactive savings
    @Query("DELETE FROM savings WHERE isActive = 0")
    suspend fun deleteInactiveSavings()

    @Transaction
    suspend fun deactivateAndDeleteSavings(savingsId: Long) {
        val savings = getSavingsById(savingsId) ?: return
        deactivateSavings(savingsId)
        deleteInactiveSavings()
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------
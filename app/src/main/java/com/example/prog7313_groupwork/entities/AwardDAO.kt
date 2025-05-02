package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.*

// ------------------------------------ Award DAO Interface ----------------------------------------
// This interface defines the database operations for the Award entity
@Dao
interface AwardDAO {
    // ------------------------------------------------------------------------------------
    // Inserts a new award or replaces an existing one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAward(award: Award)

    // ------------------------------------------------------------------------------------
    // Retrieves all awards for a specific user
    @Query("SELECT * FROM awards WHERE userId = :userId")
    suspend fun getUserAwards(userId: Long): List<Award>

    // ------------------------------------------------------------------------------------
    // Updates an existing award's information
    @Update
    suspend fun updateAward(award: Award)

    // ------------------------------------------------------------------------------------
    // Retrieves the next unachieved award for a user, ordered by goal amount
    @Query("SELECT * FROM awards WHERE userId = :userId AND achieved = 0 ORDER BY goalAmount ASC LIMIT 1")
    suspend fun getNextUnachievedAward(userId: Long): Award?
}
// -----------------------------------<<< End Of File >>>------------------------------------------
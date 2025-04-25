package com.example.prog7313_groupwork.entities

import androidx.room.*

@Dao
interface AwardDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAward(award: Award)

    @Query("SELECT * FROM awards WHERE userId = :userId")
    suspend fun getUserAwards(userId: Long): List<Award>

    @Update
    suspend fun updateAward(award: Award)

    @Query("SELECT * FROM awards WHERE userId = :userId AND achieved = 0 ORDER BY goalAmount ASC LIMIT 1")
    suspend fun getNextUnachievedAward(userId: Long): Award?
}
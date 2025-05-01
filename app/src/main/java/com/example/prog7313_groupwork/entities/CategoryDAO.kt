package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// ------------------------------------ Category DAO Interface ----------------------------------------
// This interface defines the database operations for the Category entity
@Dao
interface CategoryDAO {
    // ------------------------------------------------------------------------------------
    // Inserts a new category or replaces an existing one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    // ------------------------------------------------------------------------------------
    // Retrieves all categories from the database
    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>
}
// -----------------------------------<<< End Of File >>>------------------------------------------
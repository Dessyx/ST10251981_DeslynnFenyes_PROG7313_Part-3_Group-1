package com.example.prog7313_groupwork.astraDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prog7313_groupwork.entities.User
import com.example.prog7313_groupwork.entities.Award
import com.example.prog7313_groupwork.entities.Savings
import com.example.prog7313_groupwork.entities.UserDAO
import com.example.prog7313_groupwork.entities.AwardDAO
import com.example.prog7313_groupwork.entities.SavingsDAO

@Database(
    entities = [
        User::class,
        Award::class,
        Savings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AstraDatabase : RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun awardDAO(): AwardDAO
    abstract fun savingsDAO(): SavingsDAO

    companion object {
        @Volatile
        private var INSTANCE: AstraDatabase? = null

        fun getDatabase(context: Context): AstraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstraDatabase::class.java,
                    "astra-database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
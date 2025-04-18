package com.example.prog7313_groupwork.astraDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prog7313_groupwork.entities.Category
import com.example.prog7313_groupwork.entities.CategoryDAO
import com.example.prog7313_groupwork.entities.User
import com.example.prog7313_groupwork.entities.UserDAO

@Database(entities =[User::class, Category::class], version = 1)
abstract class AstraDatabase : RoomDatabase(){

    abstract fun userDAO(): UserDAO
    abstract fun CategoryDAO(): CategoryDAO

    companion object{
        @Volatile
        private var INSTANCE: AstraDatabase? = null

        fun getDatabase(context: Context): AstraDatabase{
            return INSTANCE?:synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstraDatabase::class.java,
                    "astra-Database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
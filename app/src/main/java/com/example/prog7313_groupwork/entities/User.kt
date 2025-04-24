package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val surname: String,
    val userEmail: String,
    val passwordHash: String = "",
    val created: Long = System.currentTimeMillis(),
    val language: String = "en",
    val currency: String = "USD",
    val themeColor: String = "light"
)
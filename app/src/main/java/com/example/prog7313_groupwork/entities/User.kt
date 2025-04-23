package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true) val id: Long=0,
    var NameSurname: String,
    var PhoneNumber: Int,
    var userEmail: String,
    var passwordHash: String,
    var created: Long = System.currentTimeMillis()
)

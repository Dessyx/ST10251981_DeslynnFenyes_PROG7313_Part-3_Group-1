package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Entity
import androidx.room.PrimaryKey

// ------------------------------------ User Entity Class ----------------------------------------
// This class represents a user in the application with their personal and preference settings
@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true) val id: Long=0, // Auto-generated unique identifier
    var NameSurname: String, // User's full name
    var PhoneNumber: Int, // User's phone number
    var userEmail: String, // User's email address
    var passwordHash: String, // Hashed password for security
    var created: Long = System.currentTimeMillis(), // Timestamp of account creation
    var language: String = "en", // User's preferred language
    var currency: String = "USD", // User's preferred currency
    var themeColor: Int = 0 // User's preferred theme color
)
// -----------------------------------<<< End Of File >>>------------------------------------------


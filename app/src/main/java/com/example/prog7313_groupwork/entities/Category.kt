package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Entity
import androidx.room.PrimaryKey

// ------------------------------------ Category Entity Class ----------------------------------------
// This class represents a budget category with its spending limits and tracking
data class Category(
    val id: String = "", // Auto-generated unique identifier for each
    val categoryName: String, // Name of the budget category (e.g., Food, Transport)
    val categoryLimit: String, // Maximum spending limit for this category
    var spent: Double? = 0.0 // Current amount spent in this category
)
// -----------------------------------<<< End Of File >>>------------------------------------------

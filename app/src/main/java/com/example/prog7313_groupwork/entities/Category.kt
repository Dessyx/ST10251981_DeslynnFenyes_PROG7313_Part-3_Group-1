package com.example.prog7313_groupwork.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long=0,
    var categoryName: String,
    var categoryLimit: String


)

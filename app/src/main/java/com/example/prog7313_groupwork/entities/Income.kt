package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "income",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val category: String
) 
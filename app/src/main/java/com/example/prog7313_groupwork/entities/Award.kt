package com.example.prog7313_groupwork.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "awards",
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
data class Award(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val goalAmount: Int,
    var achieved: Boolean = false,
    var dateAchieved: Long? = null,
    val awardType: String // "BADGE" or "GIFT_CARD"
)
package com.example.prog7313_groupwork.entities

// Imports
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ------------------------------------ Award Entity Class ----------------------------------------
// This class represents an award or achievement that users can earn through their savings goals
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
    val id: Long = 0, // Auto-generated unique identifier
    val userId: Long, // ID of the user who can earn this award
    val goalAmount: Int, // Target savings amount required to earn the award
    var achieved: Boolean = false, // Whether the award has been earned
    var dateAchieved: Long? = null, // Timestamp when the award was earned (null if not achieved)
    val awardType: String // Type of award ("BADGE" or "GIFT_CARD")
)
// -----------------------------------<<< End Of File >>>------------------------------------------
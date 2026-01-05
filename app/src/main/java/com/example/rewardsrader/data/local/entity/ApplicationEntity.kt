package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "applications",
    foreignKeys = [
        ForeignKey(
            entity = ProfileCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileCardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileCardId")]
)
data class ApplicationEntity(
    @PrimaryKey val id: String,
    val profileCardId: String,
    val applicationDateUtc: String? = null,
    val decisionDateUtc: String? = null,
    val status: String,
    val creditBureau: String? = null,
    val reconsiderationNotes: String? = null,
    val welcomeOfferTerms: String? = null
)

package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "offers",
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
data class OfferEntity(
    @PrimaryKey val id: String,
    val profileCardId: String,
    val title: String,
    val note: String? = null,
    val startDateUtc: String? = null,
    val endDateUtc: String? = null,
    val type: String,
    val multiplierRate: Double? = null,
    val minSpend: Double? = null,
    val maxCashBack: Double? = null
)

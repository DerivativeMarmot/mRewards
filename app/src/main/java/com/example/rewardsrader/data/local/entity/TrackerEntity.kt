package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trackers",
    foreignKeys = [
        ForeignKey(
            entity = ProfileCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileCardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileCardBenefitEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileCardBenefitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OfferEntity::class,
            parentColumns = ["id"],
            childColumns = ["offerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profileCardId"),
        Index("profileCardBenefitId"),
        Index("offerId"),
        Index(value = ["profileCardBenefitId", "startDateUtc", "endDateUtc"], unique = true),
        Index(value = ["offerId", "startDateUtc", "endDateUtc"], unique = true)
    ]
)
data class TrackerEntity(
    @PrimaryKey val id: String,
    val profileCardId: String,
    val profileCardBenefitId: String? = null,
    val offerId: String? = null,
    val type: TrackerSourceType,
    val startDateUtc: String,
    val endDateUtc: String,
    val manualCompleted: Boolean = false,
    val notes: String? = null
)

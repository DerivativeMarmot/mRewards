package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profile_card_benefits",
    foreignKeys = [
        ForeignKey(
            entity = ProfileCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileCardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BenefitEntity::class,
            parentColumns = ["id"],
            childColumns = ["benefitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileCardId"), Index("benefitId")]
)
data class ProfileCardBenefitEntity(
    @PrimaryKey val id: String,
    val profileCardId: String,
    val benefitId: String
)

package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "card_benefits",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BenefitEntity::class,
            parentColumns = ["id"],
            childColumns = ["benefitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId"), Index("benefitId")]
)
data class CardBenefitEntity(
    @PrimaryKey val id: String,
    val cardId: String,
    val benefitId: String
)

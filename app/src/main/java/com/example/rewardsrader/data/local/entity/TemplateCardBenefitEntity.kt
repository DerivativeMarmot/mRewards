package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "template_card_benefits",
    foreignKeys = [
        ForeignKey(
            entity = TemplateCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateCardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BenefitEntity::class,
            parentColumns = ["id"],
            childColumns = ["benefitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateCardId"), Index(value = ["benefitId"], unique = true)]
)
data class TemplateCardBenefitEntity(
    @PrimaryKey val id: String,
    val templateCardId: String,
    val benefitId: String
)

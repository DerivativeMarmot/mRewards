package com.example.rewardsrader.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CardWithBenefits(
    @Embedded val card: CardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CardBenefitEntity::class,
            parentColumn = "cardId",
            entityColumn = "benefitId"
        )
    )
    val benefits: List<BenefitEntity>
)

package com.example.rewardsrader.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CardWithBenefits(
    @Embedded val card: CardEntity,
    @Relation(parentColumn = "id", entityColumn = "cardId")
    val benefits: List<BenefitEntity>
)

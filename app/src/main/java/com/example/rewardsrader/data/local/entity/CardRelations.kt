package com.example.rewardsrader.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TemplateCardWithBenefits(
    @Embedded val templateCard: TemplateCardEntity,
    @Relation(parentColumn = "cardId", entityColumn = "id")
    val card: CardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TemplateCardBenefitEntity::class,
            parentColumn = "templateCardId",
            entityColumn = "benefitId"
        )
    )
    val benefits: List<BenefitEntity>
)

data class ProfileCardBenefitWithBenefit(
    @Embedded val link: ProfileCardBenefitEntity,
    @Relation(parentColumn = "benefitId", entityColumn = "id")
    val benefit: BenefitEntity
)

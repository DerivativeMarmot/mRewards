package com.example.rewardsrader.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ProfileCardWithRelations(
    @Embedded val profileCard: ProfileCardEntity,
    @Relation(parentColumn = "cardId", entityColumn = "id")
    val card: CardEntity?,
    @Relation(parentColumn = "id", entityColumn = "profileCardId")
    val applications: List<ApplicationEntity>,
    @Relation(parentColumn = "id", entityColumn = "profileCardId")
    val offers: List<OfferEntity>,
    @Relation(parentColumn = "id", entityColumn = "profileCardId", entity = ProfileCardBenefitEntity::class)
    val benefits: List<ProfileCardBenefitWithBenefit>
)

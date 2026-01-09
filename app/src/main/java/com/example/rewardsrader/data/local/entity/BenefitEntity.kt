package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "benefits")
data class BenefitEntity(
    @PrimaryKey val id: String,
    val title: String? = null,
    val type: BenefitType = BenefitType.Credit,
    val amount: Double? = null,
    val cap: Double? = null,
    val frequency: BenefitFrequency,
    val category: List<BenefitCategory>,
    val notes: String? = null
)

package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val issuer: String,
    val nickname: String? = null,
    val productName: String,
    val network: String,
    val annualFeeUsd: Double,
    val lastFour: String? = null,
    val openDateUtc: String? = null,
    val closeDateUtc: String? = null,
    val statementCutUtc: String? = null,
    val welcomeOfferProgress: String? = null,
    val status: String,
    val notes: String? = null,
    val subSpendingUsd: Double? = null,
    val subDuration: Int? = null,
    val subDurationUnit: String? = "months"
)

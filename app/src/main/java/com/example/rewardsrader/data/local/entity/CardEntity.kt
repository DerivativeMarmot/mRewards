package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = IssuerEntity::class,
            parentColumns = ["id"],
            childColumns = ["issuerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("issuerId")]
)
data class CardEntity(
    @PrimaryKey val id: String,
    val issuerId: String,
    val productName: String,
    val network: CardNetwork = CardNetwork.Visa,
    val annualFee: Double,
    val foreignFeeTransactionFee: Double = 0.0
)

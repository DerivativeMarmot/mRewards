package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = BenefitEntity::class,
            parentColumns = ["id"],
            childColumns = ["benefitId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ProfileCardBenefitEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileCardBenefitId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("benefitId"), Index("profileCardBenefitId")]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val benefitId: String? = null,
    val notes: String? = null,
    val dateUtc: String? = null,
    val amount: Double,
    val profileCardBenefitId: String? = null
)

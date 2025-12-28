package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "benefits",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId")]
)
data class BenefitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val type: String,
    val amountUsd: Double? = null,
    val capUsd: Double? = null,
    val cadence: String,
    val category: String? = null,
    val merchant: String? = null,
    val enrollmentRequired: Boolean,
    val effectiveDateUtc: String,
    val expiryDateUtc: String? = null,
    val terms: String? = null,
    val dataSource: String? = null,
    val notes: String? = null,
    val transactionsJson: String? = null
)

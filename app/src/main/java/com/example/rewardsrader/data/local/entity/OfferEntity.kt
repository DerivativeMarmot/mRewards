package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "offers",
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
data class OfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val title: String,
    val note: String? = null,
    val startDateUtc: String? = null,
    val endDateUtc: String? = null,
    val type: String,
    val minSpendUsd: Double? = null,
    val maxCashBackUsd: Double? = null,
    val status: String = "active"
)

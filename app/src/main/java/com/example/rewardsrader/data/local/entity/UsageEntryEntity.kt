package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_entries",
    foreignKeys = [
        ForeignKey(
            entity = BenefitEntity::class,
            parentColumns = ["id"],
            childColumns = ["benefitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("benefitId")]
)
data class UsageEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val benefitId: Long,
    val dateUtc: String,
    val amountUsd: Double? = null,
    val notes: String? = null,
    val merchant: String? = null,
    val location: String? = null,
    val proofLink: String? = null
)

package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracker_transactions",
    foreignKeys = [
        ForeignKey(
            entity = TrackerEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackerId")]
)
data class TrackerTransactionEntity(
    @PrimaryKey val id: String,
    val trackerId: String,
    val amount: Double,
    val dateUtc: String,
    val notes: String? = null
)

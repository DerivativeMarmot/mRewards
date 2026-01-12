package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profile_cards",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CardFaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardFaceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("profileId"), Index("cardId"), Index("cardFaceId")]
)
data class ProfileCardEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val cardId: String? = null,
    val cardFaceId: String? = null,
    val nickname: String? = null,
    val annualFee: Double,
    val lastFour: String? = null,
    val openDateUtc: String? = null,
    val closeDateUtc: String? = null,
    val statementCutUtc: String? = null,
    val welcomeOfferProgress: String? = null,
    val status: CardStatus = CardStatus.Active,
    val notes: String? = null,
    val subSpending: Double? = null,
    val subDuration: Int? = null,
    val subDurationUnit: CardSubDurationUnit? = null
)

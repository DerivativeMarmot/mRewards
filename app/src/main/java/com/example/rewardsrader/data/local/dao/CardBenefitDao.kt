package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.CardBenefitEntity

@Dao
interface CardBenefitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: CardBenefitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<CardBenefitEntity>)

    @Query("SELECT * FROM card_benefits WHERE cardId = :cardId")
    suspend fun getForCard(cardId: String): List<CardBenefitEntity>
}

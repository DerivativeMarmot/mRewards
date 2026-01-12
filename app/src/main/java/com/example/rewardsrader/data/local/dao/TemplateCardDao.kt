package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.rewardsrader.data.local.entity.TemplateCardEntity
import com.example.rewardsrader.data.local.entity.TemplateCardWithBenefits

@Dao
interface TemplateCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templateCards: List<TemplateCardEntity>)

    @Query("SELECT * FROM template_cards")
    suspend fun getAll(): List<TemplateCardEntity>

    @Transaction
    @Query("SELECT * FROM template_cards WHERE id = :templateCardId LIMIT 1")
    suspend fun getWithBenefits(templateCardId: String): TemplateCardWithBenefits?

    @Transaction
    @Query("SELECT * FROM template_cards")
    suspend fun getAllWithBenefits(): List<TemplateCardWithBenefits>
}

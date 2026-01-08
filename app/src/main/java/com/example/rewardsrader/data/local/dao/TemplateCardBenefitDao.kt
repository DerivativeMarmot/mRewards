package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.TemplateCardBenefitEntity

@Dao
interface TemplateCardBenefitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<TemplateCardBenefitEntity>)

    @Query("SELECT * FROM template_card_benefits WHERE templateCardId = :templateCardId")
    suspend fun getForTemplateCard(templateCardId: String): List<TemplateCardBenefitEntity>
}

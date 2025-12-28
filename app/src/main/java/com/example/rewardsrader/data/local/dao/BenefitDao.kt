package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rewardsrader.data.local.entity.BenefitEntity

@Dao
interface BenefitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(benefit: BenefitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(benefits: List<BenefitEntity>)

    @Query("SELECT * FROM benefits WHERE cardId = :cardId")
    suspend fun getForCard(cardId: Long): List<BenefitEntity>

    @Query("SELECT * FROM benefits WHERE id = :benefitId LIMIT 1")
    suspend fun getById(benefitId: Long): BenefitEntity?

    @Update
    suspend fun update(benefit: BenefitEntity)

    @Query("DELETE FROM benefits WHERE id = :benefitId")
    suspend fun deleteById(benefitId: Long)
}

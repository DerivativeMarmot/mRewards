package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.UsageEntryEntity

@Dao
interface UsageEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: UsageEntryEntity): Long

    @Query("SELECT * FROM usage_entries WHERE benefitId = :benefitId")
    suspend fun getForBenefit(benefitId: Long): List<UsageEntryEntity>
}

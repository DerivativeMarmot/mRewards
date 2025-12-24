package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.NotificationRuleEntity

@Dao
interface NotificationRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: NotificationRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<NotificationRuleEntity>)

    @Query("SELECT * FROM notification_rules WHERE benefitId = :benefitId")
    suspend fun getForBenefit(benefitId: Long): List<NotificationRuleEntity>

    @Query("SELECT * FROM notification_rules WHERE benefitId IN (:benefitIds)")
    suspend fun getForBenefits(benefitIds: List<Long>): List<NotificationRuleEntity>
}

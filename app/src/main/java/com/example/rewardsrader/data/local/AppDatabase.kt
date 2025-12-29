package com.example.rewardsrader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rewardsrader.data.local.dao.ApplicationDao
import com.example.rewardsrader.data.local.dao.BenefitDao
import com.example.rewardsrader.data.local.dao.CardDao
import com.example.rewardsrader.data.local.dao.NotificationRuleDao
import com.example.rewardsrader.data.local.dao.UsageEntryDao
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.NotificationRuleEntity
import com.example.rewardsrader.data.local.entity.UsageEntryEntity

@Database(
    entities = [
        CardEntity::class,
        ApplicationEntity::class,
        BenefitEntity::class,
        UsageEntryEntity::class,
        NotificationRuleEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun benefitDao(): BenefitDao
    abstract fun usageEntryDao(): UsageEntryDao
    abstract fun notificationRuleDao(): NotificationRuleDao
}

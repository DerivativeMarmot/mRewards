package com.example.rewardsrader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rewardsrader.data.local.dao.ApplicationDao
import com.example.rewardsrader.data.local.dao.BenefitDao
import com.example.rewardsrader.data.local.dao.CardDao
import com.example.rewardsrader.data.local.dao.CardFaceDao
import com.example.rewardsrader.data.local.dao.IssuerDao
import com.example.rewardsrader.data.local.dao.NotificationRuleDao
import com.example.rewardsrader.data.local.dao.OfferDao
import com.example.rewardsrader.data.local.dao.ProfileCardBenefitDao
import com.example.rewardsrader.data.local.dao.ProfileCardDao
import com.example.rewardsrader.data.local.dao.ProfileDao
import com.example.rewardsrader.data.local.dao.TemplateCardBenefitDao
import com.example.rewardsrader.data.local.dao.TemplateCardDao
import com.example.rewardsrader.data.local.dao.TransactionDao
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardFaceEntity
import com.example.rewardsrader.data.local.entity.IssuerEntity
import com.example.rewardsrader.data.local.entity.NotificationRuleEntity
import com.example.rewardsrader.data.local.entity.OfferEntity
import com.example.rewardsrader.data.local.entity.ProfileCardBenefitEntity
import com.example.rewardsrader.data.local.entity.ProfileCardEntity
import com.example.rewardsrader.data.local.entity.ProfileEntity
import com.example.rewardsrader.data.local.entity.TemplateCardBenefitEntity
import com.example.rewardsrader.data.local.entity.TemplateCardEntity
import com.example.rewardsrader.data.local.entity.TransactionEntity

@Database(
    entities = [
        IssuerEntity::class,
        CardEntity::class,
        CardFaceEntity::class,
        ProfileEntity::class,
        ProfileCardEntity::class,
        ProfileCardBenefitEntity::class,
        BenefitEntity::class,
        TransactionEntity::class,
        NotificationRuleEntity::class,
        OfferEntity::class,
        ApplicationEntity::class,
        TemplateCardEntity::class,
        TemplateCardBenefitEntity::class
    ],
    version = 17,
    exportSchema = false
)
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun issuerDao(): IssuerDao
    abstract fun cardDao(): CardDao
    abstract fun cardFaceDao(): CardFaceDao
    abstract fun profileDao(): ProfileDao
    abstract fun profileCardDao(): ProfileCardDao
    abstract fun profileCardBenefitDao(): ProfileCardBenefitDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun benefitDao(): BenefitDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationRuleDao(): NotificationRuleDao
    abstract fun offerDao(): OfferDao
    abstract fun templateCardDao(): TemplateCardDao
    abstract fun templateCardBenefitDao(): TemplateCardBenefitDao
}

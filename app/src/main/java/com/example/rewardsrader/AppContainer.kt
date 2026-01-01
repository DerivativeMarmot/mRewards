package com.example.rewardsrader

import android.content.Context
import androidx.room.Room
import com.example.rewardsrader.data.local.MIGRATION_1_2
import com.example.rewardsrader.data.local.MIGRATION_2_3
import com.example.rewardsrader.data.local.MIGRATION_3_4
import com.example.rewardsrader.data.local.MIGRATION_4_5
import com.example.rewardsrader.data.local.MIGRATION_5_6
import com.example.rewardsrader.config.CardConfigLoader
import com.example.rewardsrader.config.CardConfigProvider
import com.example.rewardsrader.config.DefaultCardConfigProvider
import com.example.rewardsrader.data.local.AppDatabase
import com.example.rewardsrader.data.local.repository.CardRepository
import com.example.rewardsrader.template.CardTemplateImporter

class AppContainer(context: Context) {
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "rewardsrader.db"
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
        .build()

    val cardRepository: CardRepository = CardRepository(
        db.cardDao(),
        db.benefitDao(),
        db.applicationDao(),
        db.usageEntryDao(),
        db.notificationRuleDao(),
        db.offerDao()
    )

    private val cardConfigLoader = CardConfigLoader(context)
    val cardConfigProvider: CardConfigProvider = DefaultCardConfigProvider(cardConfigLoader)
    val cardTemplateImporter = CardTemplateImporter(cardRepository)
}

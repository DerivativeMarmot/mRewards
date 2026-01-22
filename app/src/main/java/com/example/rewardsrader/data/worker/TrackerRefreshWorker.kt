package com.example.rewardsrader.data.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rewardsrader.data.local.AppDatabase
import com.example.rewardsrader.data.local.MIGRATION_1_2
import com.example.rewardsrader.data.local.MIGRATION_2_3
import com.example.rewardsrader.data.local.MIGRATION_3_4
import com.example.rewardsrader.data.local.MIGRATION_4_5
import com.example.rewardsrader.data.local.MIGRATION_5_6
import com.example.rewardsrader.data.local.MIGRATION_6_7
import com.example.rewardsrader.data.local.MIGRATION_8_9
import com.example.rewardsrader.data.local.MIGRATION_9_10
import com.example.rewardsrader.data.local.MIGRATION_10_12
import com.example.rewardsrader.data.local.MIGRATION_12_13
import com.example.rewardsrader.data.local.MIGRATION_13_14
import com.example.rewardsrader.data.local.MIGRATION_14_15
import com.example.rewardsrader.data.local.MIGRATION_15_16
import com.example.rewardsrader.data.local.MIGRATION_16_17
import com.example.rewardsrader.data.local.MIGRATION_17_18
import com.example.rewardsrader.data.local.MIGRATION_18_19
import com.example.rewardsrader.data.local.MIGRATION_19_20
import com.example.rewardsrader.data.local.MIGRATION_20_21
import com.example.rewardsrader.data.local.MIGRATION_21_22
import com.example.rewardsrader.data.local.MIGRATION_22_23
import com.example.rewardsrader.data.local.MIGRATION_23_24
import com.example.rewardsrader.data.local.repository.CardRepository
import com.example.rewardsrader.notifications.TrackerReminderScheduler
import com.example.rewardsrader.ui.tracker.TrackerGenerator
import java.time.LocalDate

class TrackerRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "rewardsrader.db"
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15,
                MIGRATION_15_16,
                MIGRATION_16_17,
                MIGRATION_17_18,
                MIGRATION_18_19,
                MIGRATION_19_20,
                MIGRATION_20_21,
                MIGRATION_21_22,
                MIGRATION_22_23,
                MIGRATION_23_24
            )
            .build()

        val repository = CardRepository(
            issuerDao = db.issuerDao(),
            cardDao = db.cardDao(),
            cardFaceDao = db.cardFaceDao(),
            profileDao = db.profileDao(),
            profileCardDao = db.profileCardDao(),
            profileCardBenefitDao = db.profileCardBenefitDao(),
            benefitDao = db.benefitDao(),
            trackerDao = db.trackerDao(),
            trackerTransactionDao = db.trackerTransactionDao(),
            notificationRuleDao = db.notificationRuleDao(),
            notificationScheduleDao = db.notificationScheduleDao(),
            offerDao = db.offerDao(),
            applicationDao = db.applicationDao(),
            templateCardDao = db.templateCardDao(),
            templateCardBenefitDao = db.templateCardBenefitDao()
        )
        val reminderScheduler = TrackerReminderScheduler(applicationContext, repository)

        return runCatching {
            repository.ensureProfile(DEFAULT_PROFILE_ID, name = "Default Profile")
            val cards = repository.getProfileCardsWithRelations(DEFAULT_PROFILE_ID)
            if (cards.isNotEmpty()) {
                val existing = repository.getTrackersForProfileCards(cards.map { it.profileCard.id })
                val generator = TrackerGenerator { repository.newId() }
                val newTrackers = generator.generateMissingTrackers(cards, existing, LocalDate.now())
                repository.insertTrackers(newTrackers)
            }
            reminderScheduler.rescheduleEnabledTrackerReminders()
            Result.success()
        }.getOrElse {
            Result.retry()
        }.also {
            db.close()
        }
    }

    companion object {
        private const val DEFAULT_PROFILE_ID = "default_profile"
    }
}

package com.example.rewardsrader.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
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
import com.example.rewardsrader.ui.tracker.formatTrackerDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val trackerId = intent.getStringExtra(EXTRA_TRACKER_ID)
        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID)
        if (trackerId.isNullOrBlank()) {
            pendingResult.finish()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                context,
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
            try {
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
                val scheduler = TrackerReminderScheduler(context, repository)
                runCatching {
                    val info = scheduler.getTrackerReminderInfo(trackerId)
                    if (info == null || !info.isActive) {
                        scheduler.cancelAllTrackerReminders(trackerId)
                        return@runCatching
                    }
                    val helper = NotificationHelper(context)
                    val title = "Tracker reminder"
                    val endDateLabel = formatTrackerDate(info.endDate)
                    val body = "${info.cardName}: ${info.title} ends $endDateLabel"
                    helper.showNotification(
                        trackerId.hashCode(),
                        title,
                        body,
                        helper.trackerPendingIntent(trackerId)
                    )
                    scheduleId?.let { repository.deleteNotificationSchedule(it) }
                }
            } finally {
                db.close()
                pendingResult.finish()
            }
        }
    }
}

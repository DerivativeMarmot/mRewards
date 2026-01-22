package com.example.rewardsrader.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.rewardsrader.data.local.entity.NotificationScheduleEntity
import com.example.rewardsrader.data.local.entity.NotificationSourceType
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.entity.TrackerEntity
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import com.example.rewardsrader.data.local.repository.CardRepository
import com.example.rewardsrader.ui.tracker.formatCardDisplayName
import com.example.rewardsrader.ui.tracker.parseTrackerDate
import java.time.LocalDate
import java.time.ZoneId

data class TrackerReminderInfo(
    val trackerId: String,
    val cardName: String,
    val title: String,
    val endDate: LocalDate,
    val targetAmount: Double,
    val usedAmount: Double,
    val isActive: Boolean
)

class TrackerReminderScheduler(
    private val context: Context,
    private val repository: CardRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun addTrackerReminder(trackerId: String, daysBefore: Int) {
        val safeDays = daysBefore.coerceIn(1, 7)
        val tracker = repository.getTracker(trackerId) ?: return
        val info = buildReminderInfo(tracker) ?: return
        if (!info.isActive) {
            cancelAllTrackerReminders(trackerId)
            return
        }
        val triggerAtMillis = computeTriggerAtMillis(info.endDate, safeDays)
        val scheduleId = repository.newId()
        val schedule = NotificationScheduleEntity(
            id = scheduleId,
            sourceType = NotificationSourceType.Tracker,
            sourceId = trackerId,
            triggerAtMillis = triggerAtMillis,
            daysBefore = safeDays,
            enabled = true
        )
        repository.upsertNotificationSchedule(schedule)
        scheduleAlarm(triggerAtMillis, buildAlarmPendingIntent(trackerId, scheduleId))
    }

    suspend fun cancelTrackerReminder(trackerId: String, scheduleId: String) {
        alarmManager.cancel(buildAlarmPendingIntent(trackerId, scheduleId))
        repository.deleteNotificationSchedule(scheduleId)
    }

    suspend fun cancelAllTrackerReminders(trackerId: String) {
        val schedules = repository.getNotificationSchedules(NotificationSourceType.Tracker, trackerId)
        schedules.forEach { schedule ->
            alarmManager.cancel(buildAlarmPendingIntent(trackerId, schedule.id))
        }
        repository.deleteNotificationSchedulesForSource(NotificationSourceType.Tracker, trackerId)
    }

    suspend fun rescheduleEnabledTrackerReminders() {
        val schedules = repository.getEnabledNotificationSchedules(NotificationSourceType.Tracker)
        schedules.forEach { schedule ->
            rescheduleTrackerReminder(schedule)
        }
    }

    suspend fun refreshTrackerReminders(trackerId: String) {
        val schedules = repository.getNotificationSchedules(NotificationSourceType.Tracker, trackerId)
        if (schedules.isEmpty()) return
        schedules.forEach { schedule ->
            rescheduleTrackerReminder(schedule)
        }
    }

    suspend fun getTrackerReminderInfo(trackerId: String): TrackerReminderInfo? {
        val tracker = repository.getTracker(trackerId) ?: return null
        return buildReminderInfo(tracker)
    }

    private suspend fun rescheduleTrackerReminder(schedule: NotificationScheduleEntity) {
        val tracker = repository.getTracker(schedule.sourceId) ?: return
        val info = buildReminderInfo(tracker) ?: return
        if (!info.isActive) {
            cancelAllTrackerReminders(tracker.id)
            return
        }
        val triggerAtMillis = computeTriggerAtMillis(info.endDate, schedule.daysBefore)
        val updated = schedule.copy(triggerAtMillis = triggerAtMillis, enabled = true)
        repository.upsertNotificationSchedule(updated)
        scheduleAlarm(triggerAtMillis, buildAlarmPendingIntent(tracker.id, schedule.id))
    }

    private fun buildAlarmPendingIntent(trackerId: String, scheduleId: String): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_TRACKER_ID, trackerId)
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        }
        return PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private suspend fun buildReminderInfo(tracker: TrackerEntity): TrackerReminderInfo? {
        val profileCard = repository.getProfileCardWithRelations(tracker.profileCardId)
        val endDate = parseTrackerDate(tracker.endDateUtc) ?: return null
        val (cardName, title, targetAmount) = resolveTrackerLabel(profileCard, tracker)
        val usedAmount = repository.getTrackerTransactions(tracker.id).sumOf { it.amount }
        val today = LocalDate.now()
        val isActive = resolveActiveStatus(tracker, usedAmount, targetAmount, endDate, today)
        return TrackerReminderInfo(
            trackerId = tracker.id,
            cardName = cardName,
            title = title,
            endDate = endDate,
            targetAmount = targetAmount,
            usedAmount = usedAmount,
            isActive = isActive
        )
    }

    private fun resolveTrackerLabel(
        profileCard: ProfileCardWithRelations?,
        tracker: TrackerEntity
    ): Triple<String, String, Double> {
        val baseName = profileCard?.profileCard?.nickname?.takeIf { it.isNotBlank() }
            ?: profileCard?.card?.productName
            ?: "Card"
        val cardName = formatCardDisplayName(baseName, profileCard?.profileCard?.lastFour)
        return when (tracker.type) {
            TrackerSourceType.Benefit -> {
                val entry = profileCard?.benefits?.firstOrNull { it.link.id == tracker.profileCardBenefitId }
                val title = entry?.benefit?.title?.takeIf { it.isNotBlank() } ?: "Credit benefit"
                val amount = entry?.benefit?.amount ?: 0.0
                Triple(cardName, title, amount)
            }
            TrackerSourceType.Offer -> {
                val offer = profileCard?.offers?.firstOrNull { it.id == tracker.offerId }
                val title = offer?.title ?: "Offer"
                val amount = offer?.maxCashBack ?: offer?.minSpend ?: 0.0
                Triple(cardName, title, amount)
            }
            TrackerSourceType.Sub -> {
                val title = "Sign-up bonus"
                val amount = profileCard?.profileCard?.subSpending ?: 0.0
                Triple(cardName, title, amount)
            }
        }
    }

    private fun resolveActiveStatus(
        tracker: TrackerEntity,
        usedAmount: Double,
        targetAmount: Double,
        endDate: LocalDate,
        today: LocalDate
    ): Boolean {
        if (tracker.type == TrackerSourceType.Offer && tracker.manualCompleted) return false
        if (usedAmount >= targetAmount) return false
        if (today.isAfter(endDate)) return false
        return true
    }

    private fun computeTriggerAtMillis(endDate: LocalDate, daysBefore: Int): Long {
        val reminderDate = endDate.minusDays(daysBefore.toLong())
        val triggerTime = reminderDate.atStartOfDay(ZoneId.systemDefault())
        return triggerTime.toInstant().toEpochMilli()
    }
}

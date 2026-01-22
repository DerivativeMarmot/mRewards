package com.example.rewardsrader.notifications

internal const val TRACKER_REMINDER_CHANNEL_ID = "tracker_reminders"
internal const val TRACKER_REMINDER_CHANNEL_NAME = "Tracker Reminders"
internal const val EXTRA_TRACKER_ID = "extra_tracker_id"
internal const val EXTRA_SCHEDULE_ID = "extra_schedule_id"

internal fun trackerScheduleId(trackerId: String): String = "tracker:$trackerId"

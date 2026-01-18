package com.example.rewardsrader.ui.tracker

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val trackerDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)

internal fun parseTrackerDate(value: String?): LocalDate? =
    value?.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalDate.parse(it, trackerDateFormatter) }.getOrNull()
    }

internal fun formatTrackerDate(date: LocalDate): String = date.format(trackerDateFormatter)

internal fun formatTrackerAmount(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    return formatter.format(amount)
}

internal fun formatTimeLeftLabel(endDate: LocalDate, today: LocalDate = LocalDate.now()): String {
    val days = java.time.temporal.ChronoUnit.DAYS.between(today, endDate)
    return when {
        days < 0 -> "Ended"
        days == 0L -> "Ends today"
        days == 1L -> "1 day left"
        else -> "$days days left"
    }
}

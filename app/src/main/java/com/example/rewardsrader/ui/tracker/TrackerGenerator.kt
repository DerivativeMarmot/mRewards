package com.example.rewardsrader.ui.tracker

import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardSubDurationUnit
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.entity.TrackerEntity
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import java.time.LocalDate
import java.time.Period

class TrackerGenerator(
    private val newId: () -> String
) {
    fun generateMissingTrackers(
        cards: List<ProfileCardWithRelations>,
        existing: List<TrackerEntity>,
        today: LocalDate = LocalDate.now()
    ): List<TrackerEntity> {
        val existingKeys = existing.mapTo(mutableSetOf()) { trackerKey(it) }
        val newTrackers = mutableListOf<TrackerEntity>()

        cards.forEach { card ->
            val profileCardId = card.profileCard.id
            val fallbackStart = parseTrackerDate(card.profileCard.openDateUtc) ?: today
            card.benefits.forEach { entry ->
                if (entry.benefit.type != BenefitType.Credit) return@forEach
                val startDate = parseTrackerDate(entry.link.startDateUtc) ?: fallbackStart
                val endDate = parseTrackerDate(entry.link.endDateUtc)
                val frequency = entry.benefit.frequency
                if (frequency == BenefitFrequency.EveryTransaction) {
                    val safeEnd = endDate?.takeIf { !it.isBefore(startDate) } ?: startDate
                    if (shouldGenerateTracker(startDate, endDate, today)) {
                        addTracker(
                            existingKeys = existingKeys,
                            newTrackers = newTrackers,
                            profileCardId = profileCardId,
                            profileCardBenefitId = entry.link.id,
                            offerId = null,
                            sourceType = TrackerSourceType.Benefit,
                            startDate = startDate,
                            endDate = safeEnd
                        )
                    }
                    return@forEach
                }

                val safeEnd = endDate?.let { if (it.isBefore(startDate)) startDate else it }
                if (!shouldGenerateTracker(startDate, safeEnd, today)) return@forEach
                if (isCalendarFrequency(frequency)) {
                    val periodStart = currentCalendarPeriodStart(startDate, today, frequency)
                    val periodEnd = calendarPeriodEnd(periodStart, frequency)
                    val finalEnd = safeEnd?.let { if (periodEnd.isAfter(it)) it else periodEnd } ?: periodEnd
                    if (!periodStart.isAfter(finalEnd)) {
                        addTracker(
                            existingKeys = existingKeys,
                            newTrackers = newTrackers,
                            profileCardId = profileCardId,
                            profileCardBenefitId = entry.link.id,
                            offerId = null,
                            sourceType = TrackerSourceType.Benefit,
                            startDate = periodStart,
                            endDate = finalEnd
                        )
                    }
                } else {
                    val period = periodForFrequency(frequency)
                    if (period != null) {
                        val periodStart = currentPeriodStart(startDate, today, period)
                        val periodEnd = periodStart.plus(period).minusDays(1)
                        val finalEnd = safeEnd?.let { if (periodEnd.isAfter(it)) it else periodEnd } ?: periodEnd
                        if (!periodStart.isAfter(finalEnd)) {
                            addTracker(
                                existingKeys = existingKeys,
                                newTrackers = newTrackers,
                                profileCardId = profileCardId,
                                profileCardBenefitId = entry.link.id,
                                offerId = null,
                                sourceType = TrackerSourceType.Benefit,
                                startDate = periodStart,
                                endDate = finalEnd
                            )
                        }
                    }
                }
            }

            val subSpending = card.profileCard.subSpending
            val subDuration = card.profileCard.subDuration
            if (subSpending != null && subSpending > 0.0 && subDuration != null && subDuration > 0) {
                val startDate = parseTrackerDate(card.profileCard.openDateUtc) ?: today
                val unit = card.profileCard.subDurationUnit ?: CardSubDurationUnit.Month
                val endDate = when (unit) {
                    CardSubDurationUnit.Day -> startDate.plusDays(subDuration.toLong()).minusDays(1)
                    CardSubDurationUnit.Month -> startDate.plusMonths(subDuration.toLong()).minusDays(1)
                }
                val safeEnd = if (endDate.isBefore(startDate)) startDate else endDate
                addTracker(
                    existingKeys = existingKeys,
                    newTrackers = newTrackers,
                    profileCardId = profileCardId,
                    profileCardBenefitId = null,
                    offerId = null,
                    sourceType = TrackerSourceType.Sub,
                    startDate = startDate,
                    endDate = safeEnd
                )
            }

            card.offers.forEach { offer ->
                val startDate = parseTrackerDate(offer.startDateUtc) ?: today
                val endDate = parseTrackerDate(offer.endDateUtc) ?: startDate
                val safeEnd = if (endDate.isBefore(startDate)) startDate else endDate
                if (shouldGenerateTracker(startDate, safeEnd, today)) {
                    addTracker(
                        existingKeys = existingKeys,
                        newTrackers = newTrackers,
                        profileCardId = profileCardId,
                        profileCardBenefitId = null,
                        offerId = offer.id,
                        sourceType = TrackerSourceType.Offer,
                        startDate = startDate,
                        endDate = safeEnd
                    )
                }
            }
        }

        return newTrackers
    }

    private fun addTracker(
        existingKeys: MutableSet<TrackerKey>,
        newTrackers: MutableList<TrackerEntity>,
        profileCardId: String,
        profileCardBenefitId: String?,
        offerId: String?,
        sourceType: TrackerSourceType,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val key = TrackerKey(
            profileCardId = profileCardId,
            sourceType = sourceType,
            profileCardBenefitId = profileCardBenefitId,
            offerId = offerId,
            startDate = formatTrackerDate(startDate),
            endDate = formatTrackerDate(endDate)
        )
        if (existingKeys.add(key)) {
            newTrackers.add(
                TrackerEntity(
                    id = newId(),
                    profileCardId = profileCardId,
                    profileCardBenefitId = profileCardBenefitId,
                    offerId = offerId,
                    type = sourceType,
                    startDateUtc = key.startDate,
                    endDateUtc = key.endDate
                )
            )
        }
    }

    private fun periodForFrequency(frequency: BenefitFrequency): Period? =
        when (frequency) {
            BenefitFrequency.EveryAnniversary -> Period.ofYears(1)
            BenefitFrequency.EveryTransaction -> null
            else -> null
        }

    private fun shouldGenerateTracker(
        startDate: LocalDate,
        endDate: LocalDate?,
        today: LocalDate
    ): Boolean {
        if (today.isBefore(startDate)) return false
        if (endDate != null && today.isAfter(endDate)) return false
        return true
    }

    private fun isCalendarFrequency(frequency: BenefitFrequency): Boolean =
        when (frequency) {
            BenefitFrequency.Monthly,
            BenefitFrequency.Quarterly,
            BenefitFrequency.SemiAnnually,
            BenefitFrequency.Annually -> true
            else -> false
        }

    private fun currentCalendarPeriodStart(
        startDate: LocalDate,
        today: LocalDate,
        frequency: BenefitFrequency
    ): LocalDate {
        val todayStart = calendarPeriodStart(today, frequency)
        val startPeriodStart = calendarPeriodStart(startDate, frequency)
        return if (todayStart == startPeriodStart && startDate.isAfter(todayStart)) {
            startDate
        } else {
            todayStart
        }
    }

    private fun currentPeriodStart(
        startDate: LocalDate,
        today: LocalDate,
        period: Period
    ): LocalDate {
        var periodStart = startDate
        while (!periodStart.plus(period).isAfter(today)) {
            periodStart = periodStart.plus(period)
        }
        return periodStart
    }

    private fun calendarPeriodStart(date: LocalDate, frequency: BenefitFrequency): LocalDate =
        when (frequency) {
            BenefitFrequency.Monthly -> date.withDayOfMonth(1)
            BenefitFrequency.Quarterly -> {
                val startMonth = ((date.monthValue - 1) / 3) * 3 + 1
                LocalDate.of(date.year, startMonth, 1)
            }
            BenefitFrequency.SemiAnnually -> {
                val startMonth = if (date.monthValue <= 6) 1 else 7
                LocalDate.of(date.year, startMonth, 1)
            }
            BenefitFrequency.Annually -> LocalDate.of(date.year, 1, 1)
            else -> date
        }

    private fun calendarPeriodEnd(date: LocalDate, frequency: BenefitFrequency): LocalDate {
        val start = calendarPeriodStart(date, frequency)
        return when (frequency) {
            BenefitFrequency.Monthly -> start.plusMonths(1).minusDays(1)
            BenefitFrequency.Quarterly -> start.plusMonths(3).minusDays(1)
            BenefitFrequency.SemiAnnually -> start.plusMonths(6).minusDays(1)
            BenefitFrequency.Annually -> start.plusYears(1).minusDays(1)
            else -> start
        }
    }

    private fun trackerKey(tracker: TrackerEntity) = TrackerKey(
        profileCardId = tracker.profileCardId,
        sourceType = tracker.type,
        profileCardBenefitId = tracker.profileCardBenefitId,
        offerId = tracker.offerId,
        startDate = tracker.startDateUtc,
        endDate = tracker.endDateUtc
    )

    private data class TrackerKey(
        val profileCardId: String,
        val sourceType: TrackerSourceType,
        val profileCardBenefitId: String?,
        val offerId: String?,
        val startDate: String,
        val endDate: String
    )
}

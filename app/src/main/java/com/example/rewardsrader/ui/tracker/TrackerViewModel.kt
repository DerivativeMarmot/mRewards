package com.example.rewardsrader.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.entity.TrackerEntity
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import com.example.rewardsrader.data.local.entity.TrackerTransactionEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import java.time.LocalDate
import java.time.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val repository: CardRepository
) : ViewModel() {
    private val defaultProfileId = "default_profile"
    private val _state = MutableStateFlow(TrackerUiState())
    val state: StateFlow<TrackerUiState> = _state

    fun loadTrackers(showLoading: Boolean = true) {
        viewModelScope.launch {
            _state.update { current ->
                current.copy(
                    isLoading = if (showLoading) true else current.isLoading,
                    error = null
                )
            }
            runCatching {
                repository.ensureProfile(defaultProfileId, name = "Default Profile")
                val cards = repository.getProfileCardsWithRelations(defaultProfileId)
                val cardIds = cards.map { it.profileCard.id }
                val existing = repository.getTrackersForProfileCards(cardIds)
                val newTrackers = generateMissingTrackers(cards, existing)
                repository.insertTrackers(newTrackers)
                val trackers = repository.getTrackersForProfileCards(cardIds)
                val transactions = repository.getTrackerTransactionsForTrackers(trackers.map { it.id })
                buildUiState(cards, trackers, transactions)
            }.onSuccess { nextState ->
                _state.value = nextState.copy(selectedFilter = _state.value.selectedFilter)
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load trackers") }
            }
        }
    }

    fun setFilter(filter: TrackerStatus) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    private fun buildUiState(
        cards: List<ProfileCardWithRelations>,
        trackers: List<TrackerEntity>,
        transactions: List<TrackerTransactionEntity>
    ): TrackerUiState {
        val cardMap = cards.associateBy { it.profileCard.id }
        val benefitMap = cards.flatMap { it.benefits }.associateBy { it.link.id }
        val offerMap = cards.flatMap { it.offers }.associateBy { it.id }
        val transactionsByTracker = transactions.groupBy { it.trackerId }
        val today = LocalDate.now()
        val trackerEndDates = trackers.associate {
            it.id to (parseTrackerDate(it.endDateUtc) ?: LocalDate.MAX)
        }
        val items = trackers.mapNotNull { tracker ->
            val card = cardMap[tracker.profileCardId]
            val cardName = card?.profileCard?.nickname?.takeIf { it.isNotBlank() }
                ?: card?.card?.productName
                ?: "Card"
            val (title, amount) = when (tracker.type) {
                TrackerSourceType.Benefit -> {
                    val entry = tracker.profileCardBenefitId?.let { benefitMap[it] }
                    val name = entry?.benefit?.title?.takeIf { it.isNotBlank() } ?: "Credit benefit"
                    val value = entry?.benefit?.amount ?: 0.0
                    name to value
                }
                TrackerSourceType.Offer -> {
                    val offer = tracker.offerId?.let { offerMap[it] }
                    val name = offer?.title ?: "Offer"
                    val value = offer?.maxCashBack ?: offer?.minSpend ?: 0.0
                    name to value
                }
            }
            val endDate = parseTrackerDate(tracker.endDateUtc) ?: today
            val usedAmount = transactionsByTracker[tracker.id]?.sumOf { it.amount } ?: 0.0
            val status = resolveStatus(tracker, usedAmount, amount, endDate, today)
            TrackerItemUi(
                id = tracker.id,
                profileCardId = tracker.profileCardId,
                cardName = cardName,
                cardFaceUrl = card?.cardFace?.remoteUrl,
                title = title,
                amount = amount,
                usedAmount = usedAmount,
                timeLeftLabel = formatTimeLeftLabel(endDate, today),
                status = status,
                sourceType = tracker.type
            )
        }.sortedBy { trackerEndDates[it.id] ?: LocalDate.MAX }
        val activeCount = items.count { it.status == TrackerStatus.Active }
        val completeCount = items.count { it.status == TrackerStatus.Complete }
        val expiredCount = items.count { it.status == TrackerStatus.Expired }
        return TrackerUiState(
            isLoading = false,
            error = null,
            selectedFilter = TrackerStatus.Active,
            trackers = items,
            activeCount = activeCount,
            completeCount = completeCount,
            expiredCount = expiredCount
        )
    }

    private fun generateMissingTrackers(
        cards: List<ProfileCardWithRelations>,
        existing: List<TrackerEntity>
    ): List<TrackerEntity> {
        val existingKeys = existing.mapTo(mutableSetOf()) { trackerKey(it) }
        val today = LocalDate.now()
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
                    val key = TrackerKey(
                        profileCardBenefitId = entry.link.id,
                        offerId = null,
                        startDate = formatTrackerDate(startDate),
                        endDate = formatTrackerDate(safeEnd)
                    )
                    if (existingKeys.add(key)) {
                        newTrackers.add(
                            TrackerEntity(
                                id = repository.newId(),
                                profileCardId = profileCardId,
                                profileCardBenefitId = entry.link.id,
                                offerId = null,
                                type = TrackerSourceType.Benefit,
                                startDateUtc = key.startDate,
                                endDateUtc = key.endDate
                            )
                        )
                    }
                    return@forEach
                }
                val safeEnd = endDate?.let { if (it.isBefore(startDate)) startDate else it }
                val generationEnd = safeEnd ?: maxOf(today, startDate)
                if (isCalendarFrequency(frequency)) {
                    var periodStart = startDate
                    var periodEnd = calendarPeriodEnd(periodStart, frequency)
                    var nextPeriodStart = nextCalendarPeriodStart(periodStart, frequency)
                    while (!periodStart.isAfter(generationEnd)) {
                        val finalEnd = safeEnd?.let { if (periodEnd.isAfter(it)) it else periodEnd } ?: periodEnd
                        val key = TrackerKey(
                            profileCardBenefitId = entry.link.id,
                            offerId = null,
                            startDate = formatTrackerDate(periodStart),
                            endDate = formatTrackerDate(finalEnd)
                        )
                        if (existingKeys.add(key)) {
                            newTrackers.add(
                                TrackerEntity(
                                    id = repository.newId(),
                                    profileCardId = profileCardId,
                                    profileCardBenefitId = entry.link.id,
                                    offerId = null,
                                    type = TrackerSourceType.Benefit,
                                    startDateUtc = key.startDate,
                                    endDateUtc = key.endDate
                                )
                            )
                        }
                        periodStart = nextPeriodStart
                        periodEnd = calendarPeriodEnd(periodStart, frequency)
                        nextPeriodStart = nextCalendarPeriodStart(periodStart, frequency)
                    }
                } else {
                    val period = periodForFrequency(frequency)
                    if (period != null) {
                        var periodStart = startDate
                        while (!periodStart.isAfter(generationEnd)) {
                            val periodEnd = periodStart.plus(period).minusDays(1)
                            val finalEnd = safeEnd?.let { if (periodEnd.isAfter(it)) it else periodEnd } ?: periodEnd
                            val key = TrackerKey(
                                profileCardBenefitId = entry.link.id,
                                offerId = null,
                                startDate = formatTrackerDate(periodStart),
                                endDate = formatTrackerDate(finalEnd)
                            )
                            if (existingKeys.add(key)) {
                                newTrackers.add(
                                    TrackerEntity(
                                        id = repository.newId(),
                                        profileCardId = profileCardId,
                                        profileCardBenefitId = entry.link.id,
                                        offerId = null,
                                        type = TrackerSourceType.Benefit,
                                        startDateUtc = key.startDate,
                                        endDateUtc = key.endDate
                                    )
                                )
                            }
                            periodStart = periodStart.plus(period)
                        }
                    }
                }
            }

            card.offers.forEach { offer ->
                val startDate = parseTrackerDate(offer.startDateUtc) ?: today
                val endDate = parseTrackerDate(offer.endDateUtc) ?: startDate
                val safeEnd = if (endDate.isBefore(startDate)) startDate else endDate
                val key = TrackerKey(
                    profileCardBenefitId = null,
                    offerId = offer.id,
                    startDate = formatTrackerDate(startDate),
                    endDate = formatTrackerDate(safeEnd)
                )
                if (existingKeys.add(key)) {
                    newTrackers.add(
                        TrackerEntity(
                            id = repository.newId(),
                            profileCardId = profileCardId,
                            profileCardBenefitId = null,
                            offerId = offer.id,
                            type = TrackerSourceType.Offer,
                            startDateUtc = key.startDate,
                            endDateUtc = key.endDate
                        )
                    )
                }
            }
        }

        return newTrackers
    }

    private fun resolveStatus(
        tracker: TrackerEntity,
        usedAmount: Double,
        targetAmount: Double,
        endDate: LocalDate,
        today: LocalDate
    ): TrackerStatus {
        if (tracker.type == TrackerSourceType.Offer && tracker.manualCompleted) {
            return TrackerStatus.Complete
        }
        if (usedAmount >= targetAmount) {
            return TrackerStatus.Complete
        }
        if (today.isAfter(endDate)) {
            return TrackerStatus.Expired
        }
        return TrackerStatus.Active
    }

    private fun periodForFrequency(frequency: BenefitFrequency): Period? =
        when (frequency) {
            BenefitFrequency.EveryAnniversary -> Period.ofYears(1)
            BenefitFrequency.EveryTransaction -> null
            else -> null
        }

    private fun isCalendarFrequency(frequency: BenefitFrequency): Boolean =
        when (frequency) {
            BenefitFrequency.Monthly,
            BenefitFrequency.Quarterly,
            BenefitFrequency.SemiAnnually,
            BenefitFrequency.Annually -> true
            else -> false
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

    private fun nextCalendarPeriodStart(date: LocalDate, frequency: BenefitFrequency): LocalDate {
        val start = calendarPeriodStart(date, frequency)
        return when (frequency) {
            BenefitFrequency.Monthly -> start.plusMonths(1)
            BenefitFrequency.Quarterly -> start.plusMonths(3)
            BenefitFrequency.SemiAnnually -> start.plusMonths(6)
            BenefitFrequency.Annually -> start.plusYears(1)
            else -> start
        }
    }

    private fun trackerKey(tracker: TrackerEntity) = TrackerKey(
        profileCardBenefitId = tracker.profileCardBenefitId,
        offerId = tracker.offerId,
        startDate = tracker.startDateUtc,
        endDate = tracker.endDateUtc
    )

    private data class TrackerKey(
        val profileCardBenefitId: String?,
        val offerId: String?,
        val startDate: String,
        val endDate: String
    )

    companion object {
        fun factory(repository: CardRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return TrackerViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

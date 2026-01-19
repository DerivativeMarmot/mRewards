package com.example.rewardsrader.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.entity.CardStatus
import com.example.rewardsrader.data.local.entity.TrackerEntity
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import com.example.rewardsrader.data.local.entity.TrackerTransactionEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import java.time.LocalDate
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
    private val trackerGenerator = TrackerGenerator { repository.newId() }

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
                val activeCards = cards.filter { it.profileCard.status == CardStatus.Active }
                val cardIds = activeCards.map { it.profileCard.id }
                val existing = repository.getTrackersForProfileCards(cardIds)
                val newTrackers = trackerGenerator.generateMissingTrackers(activeCards, existing)
                repository.insertTrackers(newTrackers)
                val trackers = repository.getTrackersForProfileCards(cardIds)
                val transactions = repository.getTrackerTransactionsForTrackers(trackers.map { it.id })
                buildUiState(activeCards, trackers, transactions)
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
            val baseName = card?.profileCard?.nickname?.takeIf { it.isNotBlank() }
                ?: card?.card?.productName
                ?: "Card"
            val cardName = formatCardDisplayName(baseName, card?.profileCard?.lastFour)
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

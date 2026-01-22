package com.example.rewardsrader.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.entity.TrackerEntity
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import com.example.rewardsrader.data.local.entity.TrackerTransactionEntity
import com.example.rewardsrader.data.local.entity.NotificationSourceType
import com.example.rewardsrader.data.local.repository.CardRepository
import com.example.rewardsrader.notifications.TrackerReminderScheduler
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerEditViewModel(
    private val repository: CardRepository,
    private val reminderScheduler: TrackerReminderScheduler
) : ViewModel() {
    private val _state = MutableStateFlow(
        TrackerEditState(entryDate = formatTrackerDate(LocalDate.now()))
    )
    val state: StateFlow<TrackerEditState> = _state

    private var currentTracker: TrackerEntity? = null

    fun load(trackerId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val tracker = repository.getTracker(trackerId)
                    ?: throw IllegalStateException("Tracker not found")
                currentTracker = tracker
                val profileCard = repository.getProfileCardWithRelations(tracker.profileCardId)
                val detail = buildDetailUi(tracker, profileCard)
                val transactions = loadTransactions(tracker.id)
                val reminders = loadReminders(tracker)
                _state.update {
                    it.copy(
                        isLoading = false,
                        tracker = detail.copy(usedAmount = transactions.sumOf { entry -> entry.amount }),
                        reminders = reminders,
                        offerNotes = tracker.notes.orEmpty(),
                        offerCompleted = tracker.manualCompleted,
                        transactions = transactions,
                        entryAmount = "",
                        entryNotes = "",
                        entryDate = it.entryDate.ifBlank { formatTrackerDate(LocalDate.now()) }
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, error = error.message ?: "Failed to load tracker")
                }
            }
        }
    }

    fun setEntryAmount(value: String) {
        _state.update { it.copy(entryAmount = value) }
    }

    fun setEntryDate(value: String) {
        _state.update { it.copy(entryDate = value) }
    }

    fun setEntryNotes(value: String) {
        _state.update { it.copy(entryNotes = value) }
    }

    fun setOfferCompleted(isCompleted: Boolean) {
        _state.update { it.copy(offerCompleted = isCompleted) }
        persistOfferChanges()
    }

    fun setOfferNotes(value: String) {
        _state.update { it.copy(offerNotes = value) }
    }

    fun saveOfferNotes(value: String) {
        _state.update { it.copy(offerNotes = value) }
        persistOfferChanges()
    }

    fun addReminder(daysBefore: Int) {
        val tracker = currentTracker ?: return
        val normalizedDays = daysBefore.coerceIn(1, 7)
        if (_state.value.reminders.any { it.daysBefore == normalizedDays }) {
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isReminderUpdating = true, error = null) }
            runCatching {
                reminderScheduler.addTrackerReminder(tracker.id, normalizedDays)
            }.onSuccess {
                refreshReminders(tracker.id)
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isReminderUpdating = false,
                        error = error.message ?: "Failed to update reminder"
                    )
                }
            }
        }
    }

    fun deleteReminder(reminderId: String) {
        val tracker = currentTracker ?: return
        viewModelScope.launch {
            _state.update { it.copy(isReminderUpdating = true, error = null) }
            runCatching {
                reminderScheduler.cancelTrackerReminder(tracker.id, reminderId)
            }.onSuccess {
                refreshReminders(tracker.id)
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isReminderUpdating = false,
                        error = error.message ?: "Failed to update reminder"
                    )
                }
            }
        }
    }

    fun notifyReminderPermissionDenied() {
        _state.update { it.copy(error = "Notification permission denied") }
    }

    private fun persistOfferChanges() {
        val tracker = currentTracker ?: return
        if (tracker.type != TrackerSourceType.Offer) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val updated = tracker.copy(
                manualCompleted = _state.value.offerCompleted,
                notes = _state.value.offerNotes.trim().takeIf { it.isNotBlank() }
            )
            runCatching {
                repository.updateTracker(updated)
            }.onSuccess {
                currentTracker = updated
                _state.update { state ->
                    state.copy(
                        isSaving = false,
                        tracker = state.tracker?.copy(
                            manualCompleted = updated.manualCompleted,
                            notes = updated.notes
                        ),
                        offerCompleted = updated.manualCompleted,
                        offerNotes = updated.notes.orEmpty(),
                        error = null
                    )
                }
                refreshReminders(updated.id)
            }.onFailure { error ->
                _state.update {
                    it.copy(isSaving = false, error = error.message ?: "Failed to save tracker")
                }
            }
        }
    }

    fun addTransaction() {
        val tracker = currentTracker ?: return
        val amount = _state.value.entryAmount.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _state.update { it.copy(error = "Enter a valid amount") }
            return
        }
        val parsedDate = parseTrackerDate(_state.value.entryDate)
        if (parsedDate == null) {
            _state.update { it.copy(error = "Enter a valid date") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val transaction = TrackerTransactionEntity(
                id = repository.newId(),
                trackerId = tracker.id,
                amount = amount,
                dateUtc = formatTrackerDate(parsedDate),
                notes = _state.value.entryNotes.trim().takeIf { it.isNotBlank() }
            )
            repository.addTrackerTransaction(transaction)
            val transactions = loadTransactions(tracker.id)
            _state.update { state ->
                state.copy(
                    isSaving = false,
                    transactions = transactions,
                    tracker = state.tracker?.copy(usedAmount = transactions.sumOf { it.amount }),
                    entryAmount = "",
                    entryNotes = ""
                )
            }
            refreshReminders(tracker.id)
        }
    }

    fun deleteTransaction(transactionId: String) {
        val tracker = currentTracker ?: return
        viewModelScope.launch {
            repository.deleteTrackerTransaction(transactionId)
            val transactions = loadTransactions(tracker.id)
            _state.update { state ->
                state.copy(
                    transactions = transactions,
                    tracker = state.tracker?.copy(usedAmount = transactions.sumOf { it.amount })
                )
            }
            refreshReminders(tracker.id)
        }
    }

    private suspend fun loadTransactions(trackerId: String): List<TrackerTransactionUi> {
        val entries = repository.getTrackerTransactions(trackerId)
        return entries.map { entry ->
            TrackerTransactionUi(
                id = entry.id,
                amount = entry.amount,
                date = entry.dateUtc,
                notes = entry.notes
            )
        }.sortedBy { parseTrackerDate(it.date) ?: LocalDate.MIN }
    }

    private suspend fun loadReminders(tracker: TrackerEntity): List<TrackerReminderUi> {
        val schedules = repository.getNotificationSchedules(NotificationSourceType.Tracker, tracker.id)
        return schedules.sortedBy { it.daysBefore }.map { schedule ->
            TrackerReminderUi(
                id = schedule.id,
                daysBefore = schedule.daysBefore
            )
        }
    }

    private fun buildDetailUi(
        tracker: TrackerEntity,
        profileCard: ProfileCardWithRelations?
    ): TrackerDetailUi {
        val baseName = profileCard?.profileCard?.nickname?.takeIf { it.isNotBlank() }
            ?: profileCard?.card?.productName
            ?: "Card"
        val cardName = formatCardDisplayName(baseName, profileCard?.profileCard?.lastFour)
        val (title, amount) = when (tracker.type) {
            TrackerSourceType.Benefit -> {
                val entry = profileCard?.benefits?.firstOrNull { it.link.id == tracker.profileCardBenefitId }
                val name = entry?.benefit?.title?.takeIf { it.isNotBlank() } ?: "Credit benefit"
                val value = entry?.benefit?.amount ?: 0.0
                name to value
            }
            TrackerSourceType.Offer -> {
                val offer = profileCard?.offers?.firstOrNull { it.id == tracker.offerId }
                val name = offer?.title ?: "Offer"
                val value = offer?.maxCashBack ?: offer?.minSpend ?: 0.0
                name to value
            }
            TrackerSourceType.Sub -> {
                val name = "Sign-up bonus"
                val value = profileCard?.profileCard?.subSpending ?: 0.0
                name to value
            }
        }
        return TrackerDetailUi(
            id = tracker.id,
            cardName = cardName,
            title = title,
            amount = amount,
            usedAmount = 0.0,
            startDate = tracker.startDateUtc,
            endDate = tracker.endDateUtc,
            manualCompleted = tracker.manualCompleted,
            notes = tracker.notes,
            sourceType = tracker.type
        )
    }

    private fun refreshReminders(trackerId: String) {
        viewModelScope.launch {
            runCatching {
                reminderScheduler.refreshTrackerReminders(trackerId)
            }
            val tracker = repository.getTracker(trackerId)
            if (tracker != null) {
                val reminders = loadReminders(tracker)
                _state.update { it.copy(reminders = reminders, isReminderUpdating = false) }
            } else {
                _state.update { it.copy(reminders = emptyList(), isReminderUpdating = false) }
            }
        }
    }

    companion object {
        fun factory(
            repository: CardRepository,
            reminderScheduler: TrackerReminderScheduler
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TrackerEditViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return TrackerEditViewModel(repository, reminderScheduler) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

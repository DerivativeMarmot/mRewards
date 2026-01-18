package com.example.rewardsrader.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.entity.TrackerEntity
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import com.example.rewardsrader.data.local.entity.TrackerTransactionEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerEditViewModel(
    private val repository: CardRepository
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
                _state.update {
                    it.copy(
                        isLoading = false,
                        tracker = detail.copy(usedAmount = transactions.sumOf { entry -> entry.amount }),
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
    }

    fun setOfferNotes(value: String) {
        _state.update { it.copy(offerNotes = value) }
    }

    fun saveOfferTracker(onSaved: (() -> Unit)? = null) {
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
                onSaved?.invoke()
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

    private fun buildDetailUi(
        tracker: TrackerEntity,
        profileCard: ProfileCardWithRelations?
    ): TrackerDetailUi {
        val cardName = profileCard?.profileCard?.nickname?.takeIf { it.isNotBlank() }
            ?: profileCard?.card?.productName
            ?: "Card"
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

    companion object {
        fun factory(repository: CardRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TrackerEditViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return TrackerEditViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

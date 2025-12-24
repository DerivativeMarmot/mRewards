package com.example.rewardsrader.ui.cardedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardEditViewModel(
    private val repository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CardEditState())
    val state: StateFlow<CardEditState> = _state.asStateFlow()

    private var originalCard: CardEntity? = null

    fun load(cardId: Long) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.getCard(cardId) }
                .onSuccess { card ->
                    if (card == null) {
                        _state.value = _state.value.copy(isLoading = false, error = "Card not found")
                    } else {
                        originalCard = card
                        _state.value = CardEditState(
                            cardId = card.id,
                            issuer = card.issuer,
                            productName = card.productName,
                            nickname = card.nickname.orEmpty(),
                            annualFee = card.annualFeeUsd.toString(),
                            lastFour = card.lastFour.orEmpty(),
                            openDate = card.openDateUtc.orEmpty(),
                            statementCut = card.statementCutUtc.orEmpty(),
                            status = card.status,
                            welcomeOfferProgress = card.welcomeOfferProgress.orEmpty(),
                            notes = card.notes.orEmpty(),
                            isLoading = false,
                            isSaving = false,
                            error = null
                        )
                    }
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoading = false, error = it.message)
                }
        }
    }

    fun updateNickname(value: String) { _state.value = _state.value.copy(nickname = value) }
    fun updateAnnualFee(value: String) { _state.value = _state.value.copy(annualFee = value) }
    fun updateLastFour(value: String) { _state.value = _state.value.copy(lastFour = value.take(4)) }
    fun updateOpenDate(value: String) { _state.value = _state.value.copy(openDate = value) }
    fun updateStatementCut(value: String) { _state.value = _state.value.copy(statementCut = value) }
    fun updateStatus(value: String) { _state.value = _state.value.copy(status = value) }
    fun updateWelcomeOffer(value: String) { _state.value = _state.value.copy(welcomeOfferProgress = value) }
    fun updateNotes(value: String) { _state.value = _state.value.copy(notes = value) }

    fun save(onSuccess: () -> Unit) {
        val card = originalCard ?: run {
            _state.value = _state.value.copy(error = "Card not loaded")
            return
        }
        val updated = card.copy(
            nickname = _state.value.nickname.ifBlank { null },
            annualFeeUsd = _state.value.annualFee.toDoubleOrNull() ?: 0.0,
            lastFour = _state.value.lastFour.ifBlank { null },
            openDateUtc = _state.value.openDate.ifBlank { null },
            statementCutUtc = _state.value.statementCut.ifBlank { null },
            status = _state.value.status.ifBlank { card.status },
            welcomeOfferProgress = _state.value.welcomeOfferProgress.ifBlank { null },
            notes = _state.value.notes.ifBlank { null }
        )
        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching { repository.updateCard(updated) }
                .onSuccess {
                    _state.value = _state.value.copy(isSaving = false)
                    onSuccess()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isSaving = false, error = e.message)
                }
        }
    }

    companion object {
        fun factory(repository: CardRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CardEditViewModel(repository) as T
                }
            }
        }
    }
}

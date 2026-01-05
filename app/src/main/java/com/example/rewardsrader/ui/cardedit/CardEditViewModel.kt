package com.example.rewardsrader.ui.cardedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Legacy edit screen is no longer used; kept minimal for compilation.
class CardEditViewModel : ViewModel() {
    private val _state = MutableStateFlow(CardEditState())
    val state: StateFlow<CardEditState> = _state.asStateFlow()

    fun load(cardId: String) {
        _state.value = _state.value.copy(cardId = cardId, isLoading = false)
    }

    fun updateNickname(value: String) { _state.value = _state.value.copy(nickname = value) }
    fun updateAnnualFee(value: String) { _state.value = _state.value.copy(annualFee = value) }
    fun updateLastFour(value: String) { _state.value = _state.value.copy(lastFour = value) }
    fun updateOpenDate(value: String) { _state.value = _state.value.copy(openDate = value) }
    fun updateStatementCut(value: String) { _state.value = _state.value.copy(statementCut = value) }
    fun updateStatus(value: String) { _state.value = _state.value.copy(status = value) }
    fun updateWelcomeOffer(value: String) { _state.value = _state.value.copy(welcomeOfferProgress = value) }
    fun updateNotes(value: String) { _state.value = _state.value.copy(notes = value) }

    fun save(onSuccess: () -> Unit) { onSuccess() }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CardEditViewModel() as T
                }
            }
        }
    }
}

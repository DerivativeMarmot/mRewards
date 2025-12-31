package com.example.rewardsrader.ui.cardlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CardSummaryUi(
    val id: Long,
    val productName: String,
    val issuer: String,
    val status: String
)

data class CardListUiState(
    val isLoading: Boolean = true,
    val cards: List<CardSummaryUi> = emptyList(),
    val error: String? = null,
    val snackbarMessage: String? = null,
    val showUndo: Boolean = false
)

class CardListViewModel(
    private val repository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CardListUiState())
    val state: StateFlow<CardListUiState> = _state.asStateFlow()
    private var lastDeleted: DeletedCardSnapshot? = null

    init {
        loadCards()
    }

    fun loadCards(showLoading: Boolean = true) {
        if (showLoading) {
            _state.value = _state.value.copy(isLoading = true)
        }
        viewModelScope.launch {
            runCatching { repository.getCards() }
                .onSuccess { cards -> _state.value = _state.value.copy(isLoading = false, cards = cards.map(::mapCard)) }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            runCatching {
                val cardWithBenefits = repository.getCard(cardId)?.let { card ->
                    val benefits = repository.getBenefitsForCard(cardId)
                    val benefitIds = benefits.map { it.id }
                    val usage = repository.getUsageForBenefits(benefitIds)
                    val rules = repository.getNotificationRulesForBenefits(benefitIds)
                    val applications = repository.getApplicationsForCard(cardId)
                    DeletedCardSnapshot(card, benefits, applications, usage, rules)
                } ?: return@launch
                lastDeleted = cardWithBenefits
                repository.removeCard(cardId)
                loadCards(showLoading = false)
                _state.value = _state.value.copy(snackbarMessage = "Card deleted", showUndo = true)
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    fun undoDelete() {
        val snapshot = lastDeleted ?: return
        viewModelScope.launch {
            runCatching {
                val cardId = repository.addCard(snapshot.card.copy(id = snapshot.card.id), snapshot.benefits)
                repository.insertApplications(snapshot.applications.map { it.copy(cardId = cardId, id = it.id) })
                repository.insertUsageEntries(snapshot.usageEntries.map { it.copy(id = it.id) })
                repository.insertNotificationRules(snapshot.notificationRules.map { it.copy(id = it.id) })
                lastDeleted = null
                loadCards(showLoading = false)
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
        _state.value = _state.value.copy(snackbarMessage = null, showUndo = false)
    }

    fun snackbarShown() {
        _state.value = _state.value.copy(snackbarMessage = null, showUndo = false)
    }

    private fun mapCard(card: CardEntity): CardSummaryUi =
        CardSummaryUi(
            id = card.id,
            productName = card.productName,
            issuer = card.issuer,
            status = card.status
        )

    companion object {
        fun factory(repository: CardRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CardListViewModel(repository) as T
                }
            }
        }
    }
}

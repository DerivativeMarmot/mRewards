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
    val error: String? = null
)

class CardListViewModel(
    private val repository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CardListUiState())
    val state: StateFlow<CardListUiState> = _state.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards() {
        _state.value = CardListUiState(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getCards() }
                .onSuccess { cards -> _state.value = CardListUiState(isLoading = false, cards = cards.map(::mapCard)) }
                .onFailure { _state.value = CardListUiState(isLoading = false, error = it.message) }
        }
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

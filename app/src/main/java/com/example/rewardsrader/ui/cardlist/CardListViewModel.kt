package com.example.rewardsrader.ui.cardlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.repository.CardRepository
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.remote.FirestoreSyncer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CardSummaryUi(
    val id: String,
    val productName: String,
    val issuer: String,
    val status: String,
    val lastFour: String?,
    val openDate: String?,
    val cardFaceUrl: String?
)

data class CardListUiState(
    val isLoading: Boolean = true,
    val cards: List<CardSummaryUi> = emptyList(),
    val error: String? = null,
    val snackbarMessage: String? = null
)

class CardListViewModel(
    private val repository: CardRepository,
    private val firestoreSyncer: FirestoreSyncer
) : ViewModel() {

    private val defaultProfileId = "default_profile"
    private val _state = MutableStateFlow(CardListUiState())
    val state: StateFlow<CardListUiState> = _state.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards(showLoading: Boolean = true) {
        if (showLoading) {
            _state.value = _state.value.copy(isLoading = true)
        }
        viewModelScope.launch {
            runCatching {
                repository.ensureProfile(defaultProfileId, name = "Default Profile")
                repository.getProfileCardsWithRelations(defaultProfileId)
            }
                .onSuccess { cards -> _state.value = _state.value.copy(isLoading = false, cards = cards.map(::mapCard)) }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            runCatching {
                repository.deleteProfileCard(cardId)
                loadCards(showLoading = false)
                _state.value = _state.value.copy(snackbarMessage = "Card deleted")
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    fun duplicateCard(cardId: String) {
        viewModelScope.launch {
            runCatching {
                repository.duplicateProfileCard(cardId)
                loadCards(showLoading = false)
                _state.value = _state.value.copy(snackbarMessage = "Card duplicated")
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    fun notifyCardAdded(message: String = "Card added") {
        _state.value = _state.value.copy(snackbarMessage = message)
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                withContext(Dispatchers.IO) { firestoreSyncer.syncIssuersAndCards() }
            }
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        snackbarMessage = "Synced ${it.issuersSynced} issuers, ${it.cardsSynced} cards"
                    )
                    loadCards(showLoading = false)
                }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }

    fun snackbarShown() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }

    private fun mapCard(card: ProfileCardWithRelations): CardSummaryUi =
        CardSummaryUi(
            id = card.profileCard.id,
            productName = card.card?.productName ?: card.profileCard.nickname.orEmpty(),
            issuer = card.card?.issuerId ?: "",
            status = card.profileCard.status.name,
            lastFour = card.profileCard.lastFour,
            openDate = card.profileCard.openDateUtc,
            cardFaceUrl = card.cardFace?.remoteUrl
        )

    companion object {
        fun factory(repository: CardRepository, firestoreSyncer: FirestoreSyncer): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CardListViewModel(repository, firestoreSyncer) as T
                }
            }
        }
    }
}

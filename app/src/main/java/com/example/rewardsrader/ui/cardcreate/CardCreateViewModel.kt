package com.example.rewardsrader.ui.cardcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.repository.CardTemplateSource
import com.example.rewardsrader.template.CardTemplateImporterContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardCreateViewModel(
    private val cardTemplates: CardTemplateSource,
    private val importer: CardTemplateImporterContract
) : ViewModel() {

    private val _state = MutableStateFlow(CardCreateState())
    val state: StateFlow<CardCreateState> = _state.asStateFlow()

    private var defaultIssuerId: String? = null
    private var defaultCardId: String? = null

    fun loadTemplates() {
        viewModelScope.launch {
            runCatching {
                cardTemplates.getIssuers() to cardTemplates.getCards()
            }.onSuccess { (issuers, cards) ->
                val issuerOptions = issuers.map { IssuerOption(id = it.id, name = it.name) }
                val cardOptions = cards.map {
                    CardOption(
                        id = it.id,
                        issuerId = it.issuerId,
                        productName = it.productName
                    )
                }
                defaultIssuerId = issuerOptions.firstOrNull()?.id
                defaultCardId = cardOptions.firstOrNull { it.issuerId == defaultIssuerId }?.id
                    ?: cardOptions.firstOrNull()?.id
                _state.value = _state.value.copy(
                    issuers = issuerOptions,
                    selectedIssuerId = defaultIssuerId,
                    cards = cardOptions,
                    selectedCardId = defaultCardId
                )
            }.onFailure {
                _state.value = _state.value.copy(error = it.message ?: "Failed to load card templates")
            }
        }
    }

    fun updateSelectedIssuer(issuerId: String) {
        val nextTemplate = _state.value.cards.firstOrNull { it.issuerId == issuerId }
        _state.value = _state.value.copy(
            selectedIssuerId = issuerId,
            selectedCardId = nextTemplate?.id
        )
    }

    fun updateSelectedTemplate(cardId: String) {
        _state.value = _state.value.copy(selectedCardId = cardId)
    }

    fun updateOpenDate(value: String) {
        _state.value = _state.value.copy(openDateUtc = value)
    }

    fun updateStatementCut(value: String) {
        _state.value = _state.value.copy(statementCutUtc = value)
    }

    fun updateApplicationStatus(value: String) {
        _state.value = _state.value.copy(applicationStatus = value)
    }

    fun updateWelcomeOffer(value: String) {
        _state.value = _state.value.copy(welcomeOfferProgress = value)
    }

    fun save(onSuccess: () -> Unit) {
        val templateId = _state.value.selectedCardId ?: run {
            _state.value = _state.value.copy(error = "Select a card")
            return
        }
        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            when (
                val result = importer.importFromDatabase(
                    cardId = templateId,
                    openDateUtc = _state.value.openDateUtc.ifBlank { null },
                    statementCutUtc = _state.value.statementCutUtc.ifBlank { null },
                    applicationStatus = _state.value.applicationStatus.ifBlank { "pending" },
                    welcomeOfferProgress = _state.value.welcomeOfferProgress.ifBlank { null }
                )
            ) {
                is com.example.rewardsrader.template.ImportResult.Success -> {
                    resetForm()
                    _state.value = _state.value.copy(isSaving = false, error = null, success = true)
                    onSuccess()
                }
                is com.example.rewardsrader.template.ImportResult.Failure -> {
                    _state.value = _state.value.copy(isSaving = false, error = result.reason)
                }
            }
        }
    }

    private fun resetForm() {
        _state.value = _state.value.copy(
            selectedIssuerId = defaultIssuerId,
            selectedCardId = defaultCardId,
            openDateUtc = "",
            statementCutUtc = "",
            applicationStatus = "",
            welcomeOfferProgress = "",
            error = null,
            success = false
        )
    }

    companion object {
        fun factory(repository: CardTemplateSource, importer: CardTemplateImporterContract): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CardCreateViewModel(repository, importer) as T
                }
            }
        }
    }
}

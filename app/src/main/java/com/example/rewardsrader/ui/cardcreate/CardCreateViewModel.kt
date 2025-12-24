package com.example.rewardsrader.ui.cardcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.config.CardConfig
import com.example.rewardsrader.config.CardConfigProvider
import com.example.rewardsrader.config.CardConfigResult
import com.example.rewardsrader.template.CardTemplateImporterContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardCreateViewModel(
    private val configProvider: CardConfigProvider,
    private val importer: CardTemplateImporterContract
) : ViewModel() {

    private val _state = MutableStateFlow(CardCreateState())
    val state: StateFlow<CardCreateState> = _state.asStateFlow()

    private var config: CardConfig? = null

    fun loadTemplates() {
        viewModelScope.launch {
            when (val result = configProvider.load()) {
                is CardConfigResult.Success -> {
                    config = result.config
                    _state.value = _state.value.copy(
                        templates = result.config.cards,
                        selectedTemplateId = result.config.cards.firstOrNull()?.cardId
                    )
                }
                is CardConfigResult.Failure -> {
                    _state.value = _state.value.copy(error = result.errors.joinToString())
                }
            }
        }
    }

    fun updateSelectedTemplate(cardId: Int) {
        _state.value = _state.value.copy(selectedTemplateId = cardId)
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
        val cfg = config ?: run {
            _state.value = _state.value.copy(error = "Config not loaded")
            return
        }
        val templateId = _state.value.selectedTemplateId ?: run {
            _state.value = _state.value.copy(error = "Select a card template")
            return
        }
        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            when (
                val result = importer.importFromConfig(
                    config = cfg,
                    selectedCardId = templateId,
                    openDateUtc = _state.value.openDateUtc.ifBlank { null },
                    statementCutUtc = _state.value.statementCutUtc.ifBlank { null },
                    applicationStatus = _state.value.applicationStatus.ifBlank { "open" },
                    welcomeOfferProgress = _state.value.welcomeOfferProgress.ifBlank { null }
                )
            ) {
                is com.example.rewardsrader.template.ImportResult.Success -> {
                    _state.value = _state.value.copy(isSaving = false, success = true)
                    onSuccess()
                }
                is com.example.rewardsrader.template.ImportResult.Failure -> {
                    _state.value = _state.value.copy(isSaving = false, error = result.reason)
                }
            }
        }
    }

    companion object {
        fun factory(configProvider: CardConfigProvider, importer: CardTemplateImporterContract): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CardCreateViewModel(configProvider, importer) as T
                }
            }
        }
    }
}

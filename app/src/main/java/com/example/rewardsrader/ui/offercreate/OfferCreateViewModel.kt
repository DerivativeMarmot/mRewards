package com.example.rewardsrader.ui.offercreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.OfferEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfferCreateViewModel(
    private val repository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OfferCreateState())
    val state: StateFlow<OfferCreateState> = _state.asStateFlow()

    fun init(cardId: Long, productName: String) {
        _state.value = OfferCreateState(cardId = cardId, productName = productName)
    }

    fun startEdit(offerId: Long, productName: String) {
        viewModelScope.launch {
            runCatching { repository.getOffer(offerId) }
                .onSuccess { offer ->
                    offer ?: return@onSuccess
                    _state.value = OfferCreateState(
                        offerId = offer.id,
                        cardId = offer.cardId,
                        productName = productName,
                        title = offer.title,
                        note = offer.note.orEmpty(),
                        type = offer.type,
                        multiplier = offer.multiplierRate?.toString().orEmpty(),
                        minSpend = offer.minSpendUsd?.toString().orEmpty(),
                        maxCashBack = offer.maxCashBackUsd?.toString().orEmpty(),
                        startDate = offer.startDateUtc.orEmpty(),
                        endDate = offer.endDateUtc.orEmpty(),
                        status = offer.status,
                        isEditing = true
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(error = it.message)
                }
        }
    }

    fun setTitle(value: String) { _state.value = _state.value.copy(title = value) }
    fun setNote(value: String) { _state.value = _state.value.copy(note = value) }
    fun setType(value: String) { _state.value = _state.value.copy(type = value) }
    fun setStatus(value: String) { _state.value = _state.value.copy(status = value) }
    fun setMultiplier(value: String) { _state.value = _state.value.copy(multiplier = value.trimToScale(2)) }
    fun setMinSpend(value: String) { _state.value = _state.value.copy(minSpend = value.trimToScale(2)) }
    fun setMaxCashBack(value: String) { _state.value = _state.value.copy(maxCashBack = value.trimToScale(2)) }
    fun setStartDate(value: String) { _state.value = _state.value.copy(startDate = value) }
    fun setEndDate(value: String) { _state.value = _state.value.copy(endDate = value) }

    fun save(onSuccess: (OfferEntity) -> Unit) {
        val validationError = validate()
        if (validationError != null) {
            _state.value = _state.value.copy(error = validationError)
            return
        }

        val isEditing = _state.value.isEditing && _state.value.offerId != null
        val multiplierRate = _state.value.multiplier.toDoubleOrNull()
        val minSpend = _state.value.minSpend.toDoubleOrNull()
        val maxCash = _state.value.maxCashBack.toDoubleOrNull()
        val offer = OfferEntity(
            id = _state.value.offerId ?: 0L,
            cardId = _state.value.cardId,
            title = _state.value.title.trim(),
            note = _state.value.note.ifBlank { null },
            startDateUtc = _state.value.startDate.ifBlank { null },
            endDateUtc = _state.value.endDate.ifBlank { null },
            type = _state.value.type,
            multiplierRate = if (_state.value.type == "multiplier") multiplierRate else null,
            minSpendUsd = minSpend,
            maxCashBackUsd = maxCash,
            status = _state.value.status
        )

        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching {
                if (isEditing) {
                    repository.updateOffer(offer)
                    offer
                } else {
                    val id = repository.addOffer(offer)
                    offer.copy(id = id)
                }
            }.onSuccess { saved ->
                if (isEditing) {
                    _state.value = _state.value.copy(isSaving = false)
                } else {
                    _state.value = OfferCreateState(
                        cardId = _state.value.cardId,
                        productName = _state.value.productName
                    )
                }
                onSuccess(saved)
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    private fun validate(): String? {
        val title = _state.value.title.trim()
        if (title.isBlank()) return "Title is required"
        if (_state.value.type !in validTypes) return "Select a type"
        if (_state.value.status !in validStatuses) return "Select a status"
        if (_state.value.type == "multiplier" && _state.value.multiplier.isNotBlank() && _state.value.multiplier.toDoubleOrNull() == null) {
            return "Multiplier must be a number"
        }
        if (_state.value.minSpend.isNotBlank() && _state.value.minSpend.toDoubleOrNull() == null) {
            return "Minimum spending must be a number"
        }
        if (_state.value.maxCashBack.isNotBlank() && _state.value.maxCashBack.toDoubleOrNull() == null) {
            return "Maximum cash back must be a number"
        }
        return null
    }

    private fun String.trimToScale(scale: Int): String {
        if (isBlank()) return ""
        val normalized = replace(",", "").trim()
        val parts = normalized.split(".", limit = 2)
        return if (parts.size == 2) {
            val decimals = parts[1].take(scale)
            "${parts[0]}.${decimals}"
        } else {
            normalized
        }
    }

    companion object {
        val validTypes = listOf("credit", "multiplier")
        val validStatuses = listOf("active", "expired", "used")

        fun factory(repository: CardRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return OfferCreateViewModel(repository) as T
                }
            }
        }
    }
}

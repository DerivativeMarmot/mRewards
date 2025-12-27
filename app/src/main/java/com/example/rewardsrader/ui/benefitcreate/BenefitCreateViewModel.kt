package com.example.rewardsrader.ui.benefitcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BenefitCreateViewModel(
    private val repository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BenefitCreateState())
    val state: StateFlow<BenefitCreateState> = _state.asStateFlow()

    fun init(cardId: Long, productName: String, issuer: String) {
        _state.value = _state.value.copy(cardId = cardId, productName = productName, issuer = issuer)
    }

    fun setTitle(value: String) { _state.value = _state.value.copy(title = value) }
    fun setType(type: String) { _state.value = _state.value.copy(type = type) }
    fun setAmount(value: String) { _state.value = _state.value.copy(amount = value) }
    fun setCap(value: String) { _state.value = _state.value.copy(cap = value) }
    fun setCadence(value: String) { _state.value = _state.value.copy(cadence = value) }
    fun setEffectiveDate(value: String) { _state.value = _state.value.copy(effectiveDate = value) }
    fun setExpiryDate(value: String) { _state.value = _state.value.copy(expiryDate = value) }
    fun setNotes(value: String) { _state.value = _state.value.copy(notes = value) }

    fun toggleCategory(category: String) {
        val current = _state.value.categories.toMutableList()
        if (current.contains(category)) current.remove(category) else current.add(category)
        _state.value = _state.value.copy(categories = current)
    }

    fun removeCustomCategory(category: String) {
        val customList = _state.value.customCategories.toMutableList()
        if (customList.remove(category)) {
            val selected = _state.value.categories.toMutableList()
            selected.remove(category)
            _state.value = _state.value.copy(customCategories = customList, categories = selected)
        }
    }

    fun addCustomCategory() {
        val custom = _state.value.customCategory.trim()
        if (custom.isBlank()) return
        val scoped = "${_state.value.issuer}:$custom"
        val customList = _state.value.customCategories.toMutableList()
        if (!customList.contains(scoped)) customList.add(scoped)
        val selected = _state.value.categories.toMutableList()
        if (!selected.contains(scoped)) selected.add(scoped)
        _state.value = _state.value.copy(
            customCategories = customList,
            categories = selected,
            customCategory = ""
        )
    }

    fun setCustomCategory(value: String) {
        _state.value = _state.value.copy(customCategory = value)
    }

    fun save(onSuccess: () -> Unit) {
        val amount = _state.value.amount.toDoubleOrNull()
        val cap = _state.value.cap.toDoubleOrNull()
        val benefit = BenefitEntity(
            cardId = _state.value.cardId,
            type = _state.value.type,
            amountUsd = amount,
            capUsd = if (_state.value.type == "multiplier") cap else null,
            cadence = _state.value.cadence,
            category = _state.value.categories.joinToString(","),
            merchant = null,
            enrollmentRequired = false,
            effectiveDateUtc = _state.value.effectiveDate.ifBlank { "" },
            expiryDateUtc = _state.value.expiryDate.ifBlank { null },
            terms = _state.value.notes.ifBlank { null },
            dataSource = "user",
            notes = _state.value.title.ifBlank { null }
        )
        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching {
                repository.addBenefit(benefit)
            }.onSuccess {
                _state.value = _state.value.copy(
                    title = "",
                    type = "credit",
                    amount = "",
                    cap = "",
                    cadence = "monthly",
                    categories = emptyList(),
                    customCategory = "",
                    effectiveDate = "",
                    expiryDate = "",
                    notes = "",
                    isSaving = false,
                    error = null
                )
                onSuccess()
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    companion object {
        fun factory(repository: CardRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return BenefitCreateViewModel(repository) as T
                }
            }
        }
    }
}

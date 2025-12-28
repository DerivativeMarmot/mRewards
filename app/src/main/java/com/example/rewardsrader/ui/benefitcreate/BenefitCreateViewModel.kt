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
        val existingCustom = _state.value.customCategories
        _state.value = BenefitCreateState(
            cardId = cardId,
            productName = productName,
            issuer = issuer,
            customCategories = existingCustom
        )
    }

    fun startEdit(benefitId: Long, productName: String, issuer: String) {
        viewModelScope.launch {
            val existingCustom = _state.value.customCategories.toMutableList()
            runCatching { repository.getBenefit(benefitId) }
                .onSuccess { benefit ->
                    benefit ?: return@onSuccess
                    val categories = benefit.category
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?: emptyList()
                    existingCustom.addAll(categories)
                    _state.value = BenefitCreateState(
                        benefitId = benefit.id,
                        cardId = benefit.cardId,
                        productName = productName,
                        issuer = issuer,
                        title = benefit.notes.orEmpty(),
                        type = benefit.type,
                        amount = benefit.amountUsd?.toString().orEmpty(),
                        cap = benefit.capUsd?.toString().orEmpty(),
                        cadence = benefit.cadence,
                        categories = categories,
                        customCategories = existingCustom.distinct(),
                        customCategory = "",
                        effectiveDate = benefit.effectiveDateUtc,
                        expiryDate = benefit.expiryDateUtc.orEmpty(),
                        notes = benefit.terms.orEmpty(),
                        dataSource = benefit.dataSource,
                        enrollmentRequired = benefit.enrollmentRequired,
                        merchant = benefit.merchant,
                        isEditing = true
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(error = it.message)
                }
        }
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
        val isEditing = _state.value.isEditing && _state.value.benefitId != null
        val amount = _state.value.amount.toDoubleOrNull()
        val cap = _state.value.cap.toDoubleOrNull()
        val benefit = BenefitEntity(
            id = _state.value.benefitId ?: 0L,
            cardId = _state.value.cardId,
            type = _state.value.type,
            amountUsd = amount,
            capUsd = if (_state.value.type == "multiplier") cap else null,
            cadence = _state.value.cadence,
            category = _state.value.categories.takeIf { it.isNotEmpty() }?.joinToString(","),
            merchant = _state.value.merchant,
            enrollmentRequired = _state.value.enrollmentRequired,
            effectiveDateUtc = _state.value.effectiveDate.ifBlank { "" },
            expiryDateUtc = _state.value.expiryDate.ifBlank { null },
            terms = _state.value.notes.ifBlank { null },
            dataSource = _state.value.dataSource ?: "user",
            notes = _state.value.title.ifBlank { null }
        )
        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching {
                if (isEditing) repository.updateBenefit(benefit) else repository.addBenefit(benefit)
            }.onSuccess {
                if (isEditing) {
                    _state.value = _state.value.copy(isSaving = false, error = null, isEditing = false)
                } else {
                    _state.value = BenefitCreateState(
                        cardId = _state.value.cardId,
                        productName = _state.value.productName,
                        issuer = _state.value.issuer,
                        customCategories = _state.value.customCategories
                    )
                }
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

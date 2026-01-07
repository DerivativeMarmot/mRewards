package com.example.rewardsrader.ui.benefitcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.BenefitCategory
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class BenefitCreateViewModel(
    private val repository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BenefitCreateState())
    val state: StateFlow<BenefitCreateState> = _state.asStateFlow()

    fun init(profileCardId: String, productName: String, issuer: String) {
        val existingCustom = _state.value.customCategories
        _state.value = BenefitCreateState(
            cardId = profileCardId,
            productName = productName,
            issuer = issuer,
            customCategories = existingCustom
        )
    }

    fun startEdit(profileCardId: String, benefitId: String, productName: String, issuer: String) {
        viewModelScope.launch {
            val existingCustom = _state.value.customCategories.toMutableList()
            runCatching { repository.getBenefit(benefitId) }
                .onSuccess { benefit ->
                    benefit ?: return@onSuccess
                    val categories = benefit.category.map { it.name }
                    existingCustom.addAll(categories)
                    _state.value = BenefitCreateState(
                        benefitId = benefit.id,
                        cardId = profileCardId,
                        productName = productName,
                        issuer = issuer,
                        title = benefit.notes.orEmpty(),
                        type = benefit.type.name.lowercase(),
                        amount = benefit.amount?.toString().orEmpty(),
                        cap = benefit.cap?.toString().orEmpty(),
                        cadence = benefit.frequency.name.lowercase(),
                        categories = categories,
                        customCategories = existingCustom.distinct(),
                        customCategory = "",
                        effectiveDate = benefit.startDateUtc,
                        expiryDate = benefit.endDateUtc.orEmpty(),
                        notes = benefit.notes.orEmpty(),
                        transactions = emptyList(),
                        dataSource = null,
                        enrollmentRequired = benefit.enrollmentRequired,
                        merchant = null,
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
    fun setAmount(value: String) { _state.value = _state.value.copy(amount = value.trimToScale(2)) }
    fun setCap(value: String) { _state.value = _state.value.copy(cap = value.trimToScale(2)) }
    fun setTransactionAmount(value: String) { _state.value = _state.value.copy(transactionAmount = value.trimToScale(2)) }
    fun setTransactionDate(value: String) { _state.value = _state.value.copy(transactionDate = value) }
    fun setProgress(value: String) { _state.value = _state.value.copy(progress = value.trimToScale(2)) }
    fun startNewTransaction() {
        _state.value = _state.value.copy(
            transactionAmount = "",
            transactionDate = "",
            editingTransactionIndex = null
        )
    }
    fun startEditTransaction(index: Int) {
        val entry = _state.value.transactions.getOrNull(index) ?: return
        _state.value = _state.value.copy(
            transactionAmount = entry.amount,
            transactionDate = entry.date,
            editingTransactionIndex = index
        )
    }
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

    fun saveTransaction() {
        val amount = _state.value.transactionAmount.trimToScale(2)
        if (amount.isBlank() || _state.value.transactionDate.isBlank()) return
        val updated = _state.value.transactions.toMutableList()
        val editIndex = _state.value.editingTransactionIndex
        val entry = TransactionEntry(amount = amount, date = _state.value.transactionDate)
        if (editIndex != null && editIndex in updated.indices) {
            updated[editIndex] = entry
        } else {
            updated.add(entry)
        }
        _state.value = _state.value.copy(
            transactions = updated,
            transactionAmount = "",
            transactionDate = "",
            editingTransactionIndex = null
        )
    }

    fun deleteTransaction(index: Int) {
        val updated = _state.value.transactions.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _state.value = _state.value.copy(transactions = updated)
        }
    }

    fun save(onSuccess: (BenefitEntity) -> Unit) {
        val isEditing = _state.value.isEditing && _state.value.benefitId != null
        val amount = _state.value.amount.toDoubleOrNull()
        val cap = _state.value.cap.toDoubleOrNull()
        val benefitId = _state.value.benefitId ?: repository.newId()
        val benefit = BenefitEntity(
            id = benefitId,
            type = _state.value.type.toBenefitType(),
            amount = amount,
            cap = if (_state.value.type == "multiplier") cap else cap,
            frequency = _state.value.cadence.toBenefitFrequency(),
            category = _state.value.categories.map { it.toBenefitCategory() },
            enrollmentRequired = _state.value.enrollmentRequired,
            startDateUtc = _state.value.effectiveDate.ifBlank { "" },
            endDateUtc = _state.value.expiryDate.ifBlank { null },
            notes = _state.value.title.ifBlank { _state.value.notes.ifBlank { null } }
        )
        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching {
                if (isEditing) {
                    repository.upsertBenefit(benefit)
                    benefit
                } else {
                    repository.addBenefitForProfileCard(_state.value.cardId, benefit)
                }
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
                onSuccess(it)
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    private fun String.trimToScale(scale: Int): String {
        if (isBlank()) return ""
        val normalized = replace(",", "").trim()
        val parts = normalized.split(".", limit = 2)
        return if (parts.size == 2) {
            val decimals = parts[1].take(scale)
            "${parts[0]}.$decimals"
        } else {
            normalized
        }
    }

    private fun String.toBenefitType(): BenefitType =
        runCatching { BenefitType.valueOf(uppercase(Locale.US)) }.getOrDefault(BenefitType.Credit)

    private fun String.toBenefitFrequency(): BenefitFrequency =
        when (lowercase(Locale.US)) {
            "monthly" -> BenefitFrequency.Monthly
            "quarterly" -> BenefitFrequency.Quarterly
            "semiannually", "semi_annual", "semi-annual", "semi-annually" -> BenefitFrequency.SemiAnnually
            "annually", "annual" -> BenefitFrequency.Annually
            "everyanniversary", "every_anniversary", "anniversary" -> BenefitFrequency.EveryAnniversary
            else -> BenefitFrequency.Monthly
        }

    private fun String.toBenefitCategory(): BenefitCategory {
        val normalized = replace(" ", "")
            .replace("-", "")
            .replace("_", "")
            .substringAfter(":")
            .lowercase(Locale.US)
        return when (normalized) {
            "dining" -> BenefitCategory.Dining
            "onlineshopping" -> BenefitCategory.OnlineShopping
            "grocery", "groceries" -> BenefitCategory.Grocery
            "restaurant" -> BenefitCategory.Restaurant
            "drugstore", "drugstores" -> BenefitCategory.DrugStore
            "travel" -> BenefitCategory.Travel
            "gas" -> BenefitCategory.Gas
            "evcharging", "evcharge", "ev" -> BenefitCategory.EVCharging
            "rideshare", "ride", "rides" -> BenefitCategory.RideShare
            "streaming" -> BenefitCategory.Streaming
            "transit" -> BenefitCategory.Transit
            "utilities", "utility" -> BenefitCategory.Utilities
            else -> BenefitCategory.Others
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

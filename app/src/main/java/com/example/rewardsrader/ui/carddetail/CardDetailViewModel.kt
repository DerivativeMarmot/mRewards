package com.example.rewardsrader.ui.carddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ApplicationUi(
    val status: String,
    val applicationDateUtc: String?,
    val decisionDateUtc: String?,
    val bureau: String?
)

data class BenefitUi(
    val id: Long,
    val title: String?,
    val type: String,
    val amount: String,
    val cadence: String,
    val expiry: String?,
    val notes: String?
)

data class CardDetailUi(
    val id: Long,
    val productName: String,
    val issuer: String,
    val network: String,
    val nickname: String?,
    val lastFour: String?,
    val status: String,
    val annualFee: String,
    val openDate: String?,
    val statementCut: String?,
    val welcomeOfferProgress: String?,
    val notes: String?,
    val applications: List<ApplicationUi>,
    val benefits: List<BenefitUi>
)

data class CardDetailState(
    val isLoading: Boolean = true,
    val detail: CardDetailUi? = null,
    val error: String? = null
)

class CardDetailViewModel(
    private val repository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CardDetailState())
    val state: StateFlow<CardDetailState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()
    private var currentCardId: Long? = null
    private var currentCard: CardEntity? = null

    fun load(cardId: Long) {
        currentCardId = cardId
        _state.value = CardDetailState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                val cards = repository.getCardsWithBenefits().firstOrNull { it.card.id == cardId }
                    ?: throw IllegalArgumentException("Card not found")
                val applications = repository.getApplicationsForCard(cardId)
                cards to applications
            }.onSuccess { (cardWithBenefits, applications) ->
                currentCard = cardWithBenefits.card
                _state.value = CardDetailState(
                    isLoading = false,
                    detail = mapDetail(cardWithBenefits.card, applications, cardWithBenefits.benefits)
                )
            }.onFailure {
                _state.value = CardDetailState(isLoading = false, error = it.message)
            }
        }
    }

    fun deleteBenefit(benefitId: Long) {
        viewModelScope.launch {
            runCatching {
                repository.deleteBenefit(benefitId)
            }.onSuccess {
                removeBenefit(benefitId)
                _events.emit("Benefit removed")
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    fun updateNickname(value: String) = updateCard { it.copy(nickname = value.ifBlank { null }) }

    fun updateAnnualFee(value: String) = updateCard {
        val fee = value.toDoubleOrNull() ?: it.annualFeeUsd
        it.copy(annualFeeUsd = fee)
    }

    fun updateLastFour(value: String) = updateCard { it.copy(lastFour = value.take(4).ifBlank { null }) }

    fun updateOpenDate(value: String) = updateCard { it.copy(openDateUtc = value.ifBlank { null }) }

    fun updateStatementCut(value: String) = updateCard { it.copy(statementCutUtc = value.ifBlank { null }) }

    fun updateStatus(value: String) = updateCard { it.copy(status = value.ifBlank { it.status }) }

    fun updateNotes(value: String) = updateCard { it.copy(notes = value.ifBlank { null }) }

    private fun updateCard(update: (CardEntity) -> CardEntity) {
        val card = currentCard ?: return
        viewModelScope.launch {
            runCatching {
                val updated = update(card)
                repository.updateCard(updated)
                currentCard = updated
                val currentDetail = _state.value.detail
                if (currentDetail != null) {
                    _state.value = _state.value.copy(
                        detail = currentDetail.copy(
                            nickname = updated.nickname,
                            lastFour = updated.lastFour,
                            status = updated.status,
                            annualFee = "$${updated.annualFeeUsd}",
                            openDate = updated.openDateUtc,
                            statementCut = updated.statementCutUtc,
                            welcomeOfferProgress = updated.welcomeOfferProgress,
                            notes = updated.notes
                        )
                    )
                }
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    private fun mapDetail(
        card: CardEntity,
        applications: List<ApplicationEntity>,
        benefits: List<BenefitEntity>
    ): CardDetailUi {
        return CardDetailUi(
            id = card.id,
            productName = card.productName,
            issuer = card.issuer,
            network = card.network,
            nickname = card.nickname,
            lastFour = card.lastFour,
            status = card.status,
            annualFee = "$${card.annualFeeUsd}",
            openDate = card.openDateUtc,
            statementCut = card.statementCutUtc,
            welcomeOfferProgress = card.welcomeOfferProgress,
            notes = card.notes,
            applications = applications.map {
                ApplicationUi(
                    status = it.status,
                    applicationDateUtc = it.applicationDateUtc,
                    decisionDateUtc = it.decisionDateUtc,
                    bureau = it.creditBureau
                )
            },
            benefits = benefits.map { mapBenefit(it) }
        )
    }

    private fun buildAmount(benefit: BenefitEntity): String {
        return when {
            benefit.amountUsd != null && benefit.capUsd != null ->
                "$${benefit.amountUsd} (cap $${benefit.capUsd})"
            benefit.amountUsd != null -> "$${benefit.amountUsd}"
            benefit.capUsd != null -> "Cap $${benefit.capUsd}"
            else -> ""
        }
    }

    private fun mapBenefit(benefit: BenefitEntity): BenefitUi {
        return BenefitUi(
            id = benefit.id,
            title = benefit.notes,
            type = benefit.type,
            amount = buildAmount(benefit),
            cadence = benefit.cadence.replaceFirstChar { it.uppercase() },
            expiry = benefit.expiryDateUtc,
            notes = benefit.terms ?: benefit.notes
        )
    }

    fun upsertBenefit(benefit: BenefitEntity) {
        val detail = _state.value.detail ?: return
        val updatedList = detail.benefits.toMutableList()
        val index = updatedList.indexOfFirst { it.id == benefit.id }
        val mapped = mapBenefit(benefit)
        if (index >= 0) {
            updatedList[index] = mapped
        } else {
            updatedList.add(mapped)
        }
        _state.value = _state.value.copy(detail = detail.copy(benefits = updatedList))
    }

    fun notifyBenefitSaved(isEdit: Boolean) {
        viewModelScope.launch {
            _events.emit(if (isEdit) "Benefit updated" else "Benefit added")
        }
    }

    fun removeBenefit(benefitId: Long) {
        val detail = _state.value.detail ?: return
        val updated = detail.benefits.filterNot { it.id == benefitId }
        _state.value = _state.value.copy(detail = detail.copy(benefits = updated))
    }

    companion object {
        fun factory(repository: CardRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CardDetailViewModel(repository) as T
                }
            }
        }
    }
}

package com.example.rewardsrader.ui.carddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ApplicationUi(
    val status: String,
    val applicationDateUtc: String?,
    val decisionDateUtc: String?,
    val bureau: String?
)

data class BenefitUi(
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
    val status: String,
    val annualFee: String,
    val openDate: String?,
    val statementCut: String?,
    val welcomeOfferProgress: String?,
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

    fun load(cardId: Long) {
        _state.value = CardDetailState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                val cards = repository.getCardsWithBenefits().firstOrNull { it.card.id == cardId }
                    ?: throw IllegalArgumentException("Card not found")
                val applications = repository.getApplicationsForCard(cardId)
                cards to applications
            }.onSuccess { (cardWithBenefits, applications) ->
                _state.value = CardDetailState(
                    isLoading = false,
                    detail = mapDetail(cardWithBenefits.card, applications, cardWithBenefits.benefits)
                )
            }.onFailure {
                _state.value = CardDetailState(isLoading = false, error = it.message)
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
            status = card.status,
            annualFee = "$${card.annualFeeUsd}",
            openDate = card.openDateUtc,
            statementCut = card.statementCutUtc,
            welcomeOfferProgress = card.welcomeOfferProgress,
            applications = applications.map {
                ApplicationUi(
                    status = it.status,
                    applicationDateUtc = it.applicationDateUtc,
                    decisionDateUtc = it.decisionDateUtc,
                    bureau = it.creditBureau
                )
            },
            benefits = benefits.map { benefit ->
                BenefitUi(
                    type = benefit.type,
                    amount = buildAmount(benefit),
                    cadence = benefit.cadence,
                    expiry = benefit.expiryDateUtc,
                    notes = benefit.notes ?: benefit.terms
                )
            }
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

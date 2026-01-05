package com.example.rewardsrader.ui.carddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardStatus
import com.example.rewardsrader.data.local.entity.CardSubDurationUnit
import com.example.rewardsrader.data.local.entity.OfferEntity
import com.example.rewardsrader.data.local.entity.ProfileCardEntity
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI-friendly models
 data class ApplicationUi(
    val status: String,
    val applicationDateUtc: String?,
    val decisionDateUtc: String?,
    val bureau: String?
)

data class BenefitUi(
    val id: String,
    val title: String?,
    val type: String,
    val amount: String,
    val cadence: String,
    val expiry: String?,
    val notes: String?
)

data class OfferUi(
    val id: String,
    val title: String,
    val status: String,
    val window: String,
    val details: String?,
    val note: String?
)

data class CardDetailUi(
    val id: String,
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
    val subSpending: String?,
    val subDuration: String?,
    val subDurationUnit: String?,
    val applications: List<ApplicationUi>,
    val benefits: List<BenefitUi>,
    val offers: List<OfferUi>
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
    private var currentCardId: String? = null
    private var currentCard: ProfileCardEntity? = null
    private var templateAnnualFee: Double = 0.0
    private var templateIssuer: String = ""
    private var templateNetwork: String = ""

    fun load(cardId: String) {
        if (currentCardId == cardId && _state.value.detail != null) return

        currentCardId = cardId
        _state.value = CardDetailState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                repository.getProfileCardWithRelations(cardId)
                    ?: throw IllegalArgumentException("Card not found")
            }.onSuccess { relations ->
                currentCard = relations.profileCard
                templateAnnualFee = relations.templateCard?.annualFee ?: 0.0
                templateIssuer = relations.templateCard?.issuerId.orEmpty()
                templateNetwork = relations.templateCard?.network?.name ?: ""
                _state.value = CardDetailState(
                    isLoading = false,
                    detail = mapDetail(
                        relations,
                        relations.applications,
                        relations.benefits,
                        relations.offers
                    )
                )
            }.onFailure {
                _state.value = CardDetailState(isLoading = false, error = it.message)
            }
        }
    }

    fun deleteBenefit(benefitId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteBenefit(benefitId) }
                .onSuccess {
                    removeBenefit(benefitId)
                    _events.emit("Benefit removed")
                }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteOffer(offerId) }
                .onSuccess {
                    removeOffer(offerId)
                    _events.emit("Offer removed")
                }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun updateNickname(value: String) = updateCard { it.copy(nickname = value.ifBlank { null }) }

    fun updateLastFour(value: String) = updateCard { it.copy(lastFour = value.take(4).ifBlank { null }) }

    fun updateOpenDate(value: String) = updateCard { it.copy(openDateUtc = value.ifBlank { null }) }

    fun updateAnnualFee(value: String) {
        // Annual fee comes from template; no-op placeholder for legacy UI wiring.
    }

    fun updateStatementCut(value: String) = updateCard { it.copy(statementCutUtc = value.ifBlank { null }) }

    fun updateStatus(value: String) = updateCard { it.copy(status = value.toCardStatus()) }

    fun updateNotes(value: String) = updateCard { it.copy(notes = value.ifBlank { null }) }

    fun updateSubSpending(value: String) = updateCard {
        val parsed = value.toDoubleOrNull()
        it.copy(subSpending = parsed)
    }

    fun updateSubDuration(value: String, unit: String) = updateCard {
        val parsed = value.toIntOrNull()
        it.copy(subDuration = parsed, subDurationUnit = unit.toCardSubDurationUnit())
    }

    private fun updateCard(update: (ProfileCardEntity) -> ProfileCardEntity) {
        val card = currentCard ?: return
        viewModelScope.launch {
            runCatching {
                val updated = update(card)
                repository.updateProfileCard(updated)
                currentCard = updated
                val currentDetail = _state.value.detail
                if (currentDetail != null) {
                    _state.value = _state.value.copy(
                        detail = currentDetail.copy(
                            nickname = updated.nickname,
                            lastFour = updated.lastFour,
                            status = updated.status.name,
                            annualFee = "$$templateAnnualFee",
                            openDate = updated.openDateUtc,
                            statementCut = updated.statementCutUtc,
                            welcomeOfferProgress = updated.welcomeOfferProgress,
                            notes = updated.notes,
                            subSpending = updated.subSpending?.toString(),
                            subDuration = updated.subDuration?.toString(),
                            subDurationUnit = updated.subDurationUnit?.name
                        )
                    )
                }
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    private fun mapDetail(
        card: ProfileCardWithRelations,
        applications: List<ApplicationEntity>,
        benefits: List<BenefitEntity>,
        offers: List<OfferEntity>
    ): CardDetailUi {
        return CardDetailUi(
            id = card.profileCard.id,
            productName = card.templateCard?.productName ?: card.profileCard.nickname.orEmpty(),
            issuer = templateIssuer,
            network = templateNetwork,
            nickname = card.profileCard.nickname,
            lastFour = card.profileCard.lastFour,
            status = card.profileCard.status.name,
            annualFee = "${'$'}${card.templateCard?.annualFee ?: 0.0}",
            openDate = card.profileCard.openDateUtc,
            statementCut = card.profileCard.statementCutUtc,
            welcomeOfferProgress = card.profileCard.welcomeOfferProgress,
            notes = card.profileCard.notes,
            subSpending = card.profileCard.subSpending?.toString(),
            subDuration = card.profileCard.subDuration?.toString(),
            subDurationUnit = card.profileCard.subDurationUnit?.name,
            applications = applications.map {
                ApplicationUi(
                    status = it.status,
                    applicationDateUtc = it.applicationDateUtc,
                    decisionDateUtc = it.decisionDateUtc,
                    bureau = it.creditBureau
                )
            },
            benefits = benefits.map { mapBenefit(it) },
            offers = offers.map { mapOffer(it) }
        )
    }

    private fun buildAmount(benefit: BenefitEntity): String {
        return when (benefit.type) {
            BenefitType.Multiplier -> {
                val rate = benefit.amount?.let {
                    if (it % 1.0 == 0.0) "${it.toInt()}%" else "${it}%"
                } ?: ""
                rate.ifBlank { "" }
            }
            else -> {
                when {
                    benefit.amount != null && benefit.cap != null ->
                        "${'$'}${benefit.amount} (cap ${'$'}${benefit.cap})"
                    benefit.amount != null -> "${'$'}${benefit.amount}"
                    benefit.cap != null -> "Cap ${'$'}${benefit.cap}"
                    else -> ""
                }
            }
        }
    }

    private fun mapBenefit(benefit: BenefitEntity): BenefitUi {
        return BenefitUi(
            id = benefit.id,
            title = benefit.notes,
            type = benefit.type.name,
            amount = buildAmount(benefit),
            cadence = benefit.frequency.name,
            expiry = benefit.endDateUtc,
            notes = benefit.notes
        )
    }

    private fun mapOffer(offer: OfferEntity): OfferUi {
        val window = when {
            !offer.startDateUtc.isNullOrBlank() && !offer.endDateUtc.isNullOrBlank() ->
                "${offer.startDateUtc} - ${offer.endDateUtc}"
            !offer.startDateUtc.isNullOrBlank() -> "From ${offer.startDateUtc}"
            !offer.endDateUtc.isNullOrBlank() -> "Until ${offer.endDateUtc}"
            else -> ""
        }
        val detailsParts = mutableListOf<String>()
        offer.multiplierRate?.let { detailsParts.add("${trimAmount(it)}%") }
        offer.minSpend?.let { detailsParts.add("Min MS $${trimAmount(it)}") }
        offer.maxCashBack?.let { detailsParts.add("Max CB $${trimAmount(it)}") }
        val details = detailsParts.joinToString(" \u2013").ifBlank { null }
        return OfferUi(
            id = offer.id,
            title = offer.title,
            status = offer.status.replaceFirstChar { it.uppercase() },
            window = window,
            details = details,
            note = offer.note
        )
    }

    private fun trimAmount(value: Double): String {
        return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
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

    fun removeBenefit(benefitId: String) {
        val detail = _state.value.detail ?: return
        val updated = detail.benefits.filterNot { it.id == benefitId }
        _state.value = _state.value.copy(detail = detail.copy(benefits = updated))
    }

    fun upsertOffer(offer: OfferEntity) {
        val detail = _state.value.detail ?: return
        val updatedList = detail.offers.toMutableList()
        val index = updatedList.indexOfFirst { it.id == offer.id }
        val mapped = mapOffer(offer)
        if (index >= 0) {
            updatedList[index] = mapped
        } else {
            updatedList.add(mapped)
        }
        _state.value = _state.value.copy(detail = detail.copy(offers = updatedList))
    }

    fun notifyOfferSaved(isEdit: Boolean) {
        viewModelScope.launch {
            _events.emit(if (isEdit) "Offer updated" else "Offer added")
        }
    }

    fun removeOffer(offerId: String) {
        val detail = _state.value.detail ?: return
        val updated = detail.offers.filterNot { it.id == offerId }
        _state.value = _state.value.copy(detail = detail.copy(offers = updated))
    }

    private fun String.toCardStatus(): CardStatus =
        runCatching { CardStatus.valueOf(this) }.getOrDefault(CardStatus.Active)

    private fun String.toCardSubDurationUnit(): CardSubDurationUnit? =
        runCatching { CardSubDurationUnit.valueOf(this) }.getOrNull()

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

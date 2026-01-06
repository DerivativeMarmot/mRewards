package com.example.rewardsrader.template

import com.example.rewardsrader.config.BenefitTemplate
import com.example.rewardsrader.config.CardConfig
import com.example.rewardsrader.config.CardTemplate
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.BenefitCategory
import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardBenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardNetwork
import com.example.rewardsrader.data.local.entity.CardSegment
import com.example.rewardsrader.data.local.entity.CardStatus
import com.example.rewardsrader.data.local.entity.PaymentInstrument
import com.example.rewardsrader.data.local.entity.IssuerEntity
import com.example.rewardsrader.data.local.entity.ProfileCardBenefitEntity
import com.example.rewardsrader.data.local.entity.ProfileCardEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import java.util.Locale

sealed class ImportResult {
    data class Success(val profileCardId: String) : ImportResult()
    data class Failure(val reason: String) : ImportResult()
}

interface CardTemplateImporterContract {
    suspend fun importFromConfig(
        config: CardConfig,
        selectedCardId: Int,
        openDateUtc: String?,
        statementCutUtc: String?,
        applicationStatus: String,
        welcomeOfferProgress: String?
    ): ImportResult

    suspend fun importFromDatabase(
        templateCardId: String,
        openDateUtc: String?,
        statementCutUtc: String?,
        applicationStatus: String,
        welcomeOfferProgress: String?
    ): ImportResult
}

class CardTemplateImporter(
    private val repository: CardRepository
) : CardTemplateImporterContract {

    private val defaultProfileId = "default_profile"

    override suspend fun importFromConfig(
        config: CardConfig,
        selectedCardId: Int,
        openDateUtc: String?,
        statementCutUtc: String?,
        applicationStatus: String,
        welcomeOfferProgress: String?
    ): ImportResult {
        val cardTemplate = config.cards.firstOrNull { it.cardId == selectedCardId }
            ?: return ImportResult.Failure("Card template $selectedCardId not found.")
        val benefitTemplates = config.benefits.filter { it.cardId == selectedCardId }
        if (benefitTemplates.isEmpty()) {
            return ImportResult.Failure("No benefits found for card template $selectedCardId.")
        }
        // Ensure issuer/profile
        repository.upsertIssuers(listOf(IssuerEntity(id = cardTemplate.issuer, name = cardTemplate.issuer)))
        val profile = repository.ensureProfile(defaultProfileId, name = "Default Profile")

        val cardEntity = mapCard(cardTemplate)
        val benefitEntities = benefitTemplates.map { mapBenefit(it) }
        repository.upsertCards(listOf(cardEntity))
        repository.upsertBenefits(benefitEntities)
        val cardBenefitLinks = benefitEntities.map {
            CardBenefitEntity(
                id = repository.newId(),
                cardId = cardEntity.id,
                benefitId = it.id
            )
        }
        repository.upsertCardBenefits(cardBenefitLinks)

        val profileCardId = repository.newId()
        val profileCard = mapProfileCard(cardTemplate, profile.id, profileCardId, openDateUtc, statementCutUtc, applicationStatus, welcomeOfferProgress)
        repository.upsertProfileCards(listOf(profileCard))
        val profileBenefitLinks = benefitEntities.map {
            ProfileCardBenefitEntity(
                id = repository.newId(),
                profileCardId = profileCardId,
                benefitId = it.id
            )
        }
        repository.upsertProfileCardBenefits(profileBenefitLinks)

        val application = ApplicationEntity(
            id = repository.newId(),
            profileCardId = profileCardId,
            applicationDateUtc = openDateUtc,
            decisionDateUtc = null,
            status = applicationStatus,
            creditBureau = null,
            reconsiderationNotes = null,
            welcomeOfferTerms = welcomeOfferProgress
        )
        repository.addApplications(listOf(application))
        return ImportResult.Success(profileCardId)
    }

    override suspend fun importFromDatabase(
        templateCardId: String,
        openDateUtc: String?,
        statementCutUtc: String?,
        applicationStatus: String,
        welcomeOfferProgress: String?
    ): ImportResult {
        val cardWithBenefits = repository.getCardWithBenefits(templateCardId)
            ?: return ImportResult.Failure("Card $templateCardId not found.")
        val profile = repository.ensureProfile(defaultProfileId, name = "Default Profile")

        val profileCardId = repository.newId()
        val profileCard = ProfileCardEntity(
            id = profileCardId,
            profileId = profile.id,
            templateCardId = cardWithBenefits.card.id,
            nickname = cardWithBenefits.card.productName,
            annualFee = cardWithBenefits.card.annualFee,
            lastFour = null,
            openDateUtc = openDateUtc,
            closeDateUtc = null,
            statementCutUtc = statementCutUtc,
            welcomeOfferProgress = welcomeOfferProgress,
            status = applicationStatus.toCardStatus(),
            notes = null,
            subSpending = null,
            subDuration = null,
            subDurationUnit = null
        )
        repository.upsertProfileCards(listOf(profileCard))

        if (cardWithBenefits.benefits.isNotEmpty()) {
            val profileBenefitLinks = cardWithBenefits.benefits.map {
                ProfileCardBenefitEntity(
                    id = repository.newId(),
                    profileCardId = profileCardId,
                    benefitId = it.id
                )
            }
            repository.upsertProfileCardBenefits(profileBenefitLinks)
        }

        val application = ApplicationEntity(
            id = repository.newId(),
            profileCardId = profileCardId,
            applicationDateUtc = openDateUtc,
            decisionDateUtc = null,
            status = applicationStatus,
            creditBureau = null,
            reconsiderationNotes = null,
            welcomeOfferTerms = welcomeOfferProgress
        )
        repository.addApplications(listOf(application))

        return ImportResult.Success(profileCardId)
    }

    private fun mapCard(template: CardTemplate): CardEntity =
        CardEntity(
            id = template.cardId.toString(),
            issuerId = template.issuer,
            productName = template.productName,
            network = template.network.toCardNetwork(),
            paymentInstrument = PaymentInstrument.Credit,
            segment = CardSegment.Personal,
            annualFee = template.annualFeeUsd,
            foreignTransactionFee = 0.0
        )

    private fun mapProfileCard(
        template: CardTemplate,
        profileId: String,
        profileCardId: String,
        openDateUtc: String?,
        statementCutUtc: String?,
        applicationStatus: String,
        welcomeOfferProgress: String?
    ): ProfileCardEntity =
        ProfileCardEntity(
            id = profileCardId,
            profileId = profileId,
            templateCardId = template.cardId.toString(),
            nickname = template.productName,
            annualFee = template.annualFeeUsd,
            lastFour = null,
            openDateUtc = openDateUtc,
            closeDateUtc = null,
            statementCutUtc = statementCutUtc,
            welcomeOfferProgress = welcomeOfferProgress,
            status = applicationStatus.toCardStatus(),
            notes = template.notes,
            subSpending = null,
            subDuration = null,
            subDurationUnit = null
        )

    private fun mapBenefit(template: BenefitTemplate): BenefitEntity =
        BenefitEntity(
            id = template.benefitId.toString(),
            type = template.type.toBenefitType(),
            amount = template.amountUsd,
            cap = template.capUsd,
            frequency = template.cadence.toBenefitFrequency(),
            category = template.category!!.toBenefitCategories(),
            enrollmentRequired = template.enrollmentRequired,
            startDateUtc = template.effectiveDate,
            endDateUtc = template.expiryDate,
            notes = template.notes
        )

    private fun String.toCardStatus(): CardStatus = when (lowercase(Locale.US)) {
        "approved", "active" -> CardStatus.Active
        "closed", "denied" -> CardStatus.Closed
        else -> CardStatus.Pending
    }

    private fun String.toBenefitCategories(): List<BenefitCategory> {
        val normalized = lowercase(Locale.US)
        return listOf(
            runCatching { BenefitCategory.valueOf(normalized.replaceFirstChar { it.uppercase() }) }.getOrElse {
                when (normalized) {
                    "drugstore" -> BenefitCategory.DrugStore
                    "online shopping", "online_shopping" -> BenefitCategory.OnlineShopping
                    else -> BenefitCategory.Others
                }
            }
        )
    }

    private fun String.toCardNetwork(): CardNetwork =
        runCatching { CardNetwork.valueOf(this) }.getOrDefault(CardNetwork.Visa)

    private fun com.example.rewardsrader.config.BenefitType.toBenefitType(): BenefitType =
        when (this) {
            com.example.rewardsrader.config.BenefitType.CREDIT -> BenefitType.Credit
            com.example.rewardsrader.config.BenefitType.MULTIPLIER -> BenefitType.Multiplier
            else -> BenefitType.Credit
        }

    private fun com.example.rewardsrader.config.BenefitCadence.toBenefitFrequency(): BenefitFrequency =
        when (this) {
            com.example.rewardsrader.config.BenefitCadence.MONTHLY -> BenefitFrequency.Monthly
            com.example.rewardsrader.config.BenefitCadence.QUARTERLY -> BenefitFrequency.Quarterly
            com.example.rewardsrader.config.BenefitCadence.ANNUALLY -> BenefitFrequency.Annually
            com.example.rewardsrader.config.BenefitCadence.SEMI_ANNUALLY -> BenefitFrequency.Annually
            com.example.rewardsrader.config.BenefitCadence.EVERY_ANNIVERSARY -> BenefitFrequency.EveryAnniversary
            else -> BenefitFrequency.Monthly
        }
}

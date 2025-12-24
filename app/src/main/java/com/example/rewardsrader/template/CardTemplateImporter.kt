package com.example.rewardsrader.template

import com.example.rewardsrader.config.BenefitTemplate
import com.example.rewardsrader.config.CardConfig
import com.example.rewardsrader.config.CardTemplate
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.repository.CardRepository

sealed class ImportResult {
    data class Success(val cardId: Long) : ImportResult()
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
}

class CardTemplateImporter(
    private val repository: CardRepository
) : CardTemplateImporterContract {
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
        val cardEntity = mapCard(cardTemplate, applicationStatus, openDateUtc, statementCutUtc, welcomeOfferProgress)
        val benefitEntities = benefitTemplates.map { mapBenefit(it, cardTemplate.dataSource) }
        val application = ApplicationEntity(
            cardId = 0,
            applicationDateUtc = openDateUtc,
            decisionDateUtc = null,
            status = applicationStatus,
            creditBureau = null,
            reconsiderationNotes = null,
            welcomeOfferTerms = welcomeOfferProgress
        )

        val cardId = repository.addCardWithDetails(cardEntity, benefitEntities, application)
        return ImportResult.Success(cardId)
    }

    private fun mapCard(
        template: CardTemplate,
        applicationStatus: String,
        openDateUtc: String?,
        statementCutUtc: String?,
        welcomeOfferProgress: String?
    ): CardEntity {
        return CardEntity(
            issuer = template.issuer,
            productName = template.productName,
            network = template.network,
            annualFeeUsd = template.annualFeeUsd,
            openDateUtc = openDateUtc,
            status = applicationStatus,
            statementCutUtc = statementCutUtc,
            welcomeOfferProgress = welcomeOfferProgress,
            notes = template.notes
        )
    }

    private fun mapBenefit(template: BenefitTemplate, dataSource: String): BenefitEntity {
        return BenefitEntity(
            cardId = 0,
            type = template.type.name.lowercase(),
            amountUsd = template.amountUsd,
            capUsd = template.capUsd,
            cadence = template.cadence.name.lowercase(),
            category = template.category,
            merchant = template.merchant,
            enrollmentRequired = template.enrollmentRequired,
            effectiveDateUtc = template.effectiveDate,
            expiryDateUtc = template.expiryDate,
            terms = template.terms,
            dataSource = dataSource,
            notes = template.notes
        )
    }
}

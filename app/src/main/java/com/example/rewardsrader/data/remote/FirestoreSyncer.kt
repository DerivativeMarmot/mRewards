package com.example.rewardsrader.data.remote

import com.example.rewardsrader.data.local.entity.BenefitCategory
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardNetwork
import com.example.rewardsrader.data.local.entity.CardSegment
import com.example.rewardsrader.data.local.entity.IssuerEntity
import com.example.rewardsrader.data.local.entity.PaymentInstrument
import com.example.rewardsrader.data.local.entity.TemplateCardBenefitEntity
import com.example.rewardsrader.data.local.entity.TemplateCardEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncer(
    private val firestore: FirebaseFirestore,
    private val repository: CardRepository
    ) {

    suspend fun syncIssuersAndCards(): SyncResult {
        val issuers = firestore.collection("issuers")
            .get()
            .await()
            .documents
            .mapNotNull { it.toIssuerEntity() }
        repository.upsertIssuers(issuers)

        val cards = firestore.collection("cards")
            .get()
            .await()
            .documents
            .mapNotNull { it.toCardEntity() }
        repository.upsertCards(cards)
        val templateCards = firestore.collection("template_cards")
            .get()
            .await()
            .documents
            .mapNotNull { it.toTemplateCardEntity() }
            .ifEmpty { cards.map { TemplateCardEntity(id = it.id, cardId = it.id) } }
        repository.upsertTemplateCards(templateCards)

        val benefits = firestore.collection("benefits")
            .get()
            .await()
            .documents
            .mapNotNull { it.toBenefitEntity() }
        repository.upsertBenefits(benefits)

        val templateCardBenefits = firestore.collection("template_card_benefits")
            .get()
            .await()
            .documents
            .mapNotNull { it.toTemplateCardBenefitEntity() }
        repository.upsertTemplateCardBenefits(templateCardBenefits)

        return SyncResult(
            issuersSynced = issuers.size,
            cardsSynced = cards.size,
            templateCardsSynced = templateCards.size,
            benefitsSynced = benefits.size,
            templateCardBenefitsSynced = templateCardBenefits.size
        )
    }

    private fun DocumentSnapshot.toIssuerEntity(): IssuerEntity? {
        val name = stringField("name") ?: return null
        return IssuerEntity(id = id, name = name)
    }

    private fun DocumentSnapshot.toCardEntity(): CardEntity? {
        val issuerId = stringField("issuerId", "issuer_id") ?: return null
        val productName = stringField("productName", "product_name") ?: return null
        val networkValue = stringField("network")
        val network = networkValue
            ?.let { runCatching { CardNetwork.valueOf(it) }.getOrNull() }
            ?: CardNetwork.Visa
        val instrumentValue = stringField("paymentInstrument", "payment_instrument")
        val paymentInstrument = instrumentValue
            ?.let { runCatching { PaymentInstrument.valueOf(it) }.getOrNull() }
            ?: PaymentInstrument.Credit
        val segmentValue = stringField("segment")
        val segment = segmentValue
            ?.let { runCatching { CardSegment.valueOf(it) }.getOrNull() }
            ?: CardSegment.Personal
        val annualFee = doubleField("annualFee", "annual_fee") ?: 0.0
        val foreignFee = doubleField("foreignTransactionFee", "foreign_transaction_fee")
            ?: doubleField("foreignFeeTransactionFee", "foreign_fee_transaction_fee")
            ?: 0.0
        return CardEntity(
            id = id,
            issuerId = issuerId,
            productName = productName,
            network = network,
            paymentInstrument = paymentInstrument,
            segment = segment,
            annualFee = annualFee,
            foreignTransactionFee = foreignFee
        )
    }

    private fun DocumentSnapshot.toTemplateCardEntity(): TemplateCardEntity? {
        val cardId = stringField("cardId", "card_id") ?: return null
        return TemplateCardEntity(id = id, cardId = cardId)
    }

    private fun DocumentSnapshot.toBenefitEntity(): BenefitEntity? {
        val typeValue = stringField("type")
        val type = typeValue?.let { runCatching { BenefitType.valueOf(it) }.getOrNull() } ?: BenefitType.Credit
        val frequencyValue = stringField("frequency")
        val frequency = frequencyValue?.let { runCatching { BenefitFrequency.valueOf(it) }.getOrNull() }
            ?: BenefitFrequency.Monthly
        val categories = listField("category")?.mapNotNull { raw ->
            val normalized = raw.replace(" ", "").replace("-", "").replace("_", "")
            runCatching { BenefitCategory.valueOf(normalized.replaceFirstChar { it.uppercase() }) }.getOrElse {
                when (normalized.lowercase()) {
                    "drugstore" -> BenefitCategory.DrugStore
                    "onlineshopping" -> BenefitCategory.OnlineShopping
                    "rideshare" -> BenefitCategory.RideShare
                    "supermarket", "supermarkets" -> BenefitCategory.Supermarket
                    "retail", "retailstore" -> BenefitCategory.RetailStore
                    else -> null
                }
            }
        }.orEmpty()
        return BenefitEntity(
            id = id,
            title = stringField("title"),
            type = type,
            amount = doubleField("amount"),
            cap = doubleField("cap"),
            frequency = frequency,
            category = categories.ifEmpty { listOf(BenefitCategory.Others) },
            notes = stringField("notes", "description")
        )
    }

    private fun DocumentSnapshot.toTemplateCardBenefitEntity(): TemplateCardBenefitEntity? {
        val templateCardId = stringField("templateCardId", "template_card_id") ?: return null
        val benefitId = stringField("benefitId", "benefit_id") ?: return null
        return TemplateCardBenefitEntity(
            id = id,
            templateCardId = templateCardId,
            benefitId = benefitId
        )
    }

    private fun DocumentSnapshot.stringField(primary: String, alternate: String? = null): String? {
        val primaryValue = getString(primary)
        if (!primaryValue.isNullOrBlank()) return primaryValue
        if (alternate != null) {
            val altValue = getString(alternate)
            if (!altValue.isNullOrBlank()) return altValue
        }
        return null
    }

    private fun DocumentSnapshot.doubleField(primary: String, alternate: String? = null): Double? {
        val primaryValue = getDouble(primary)
        if (primaryValue != null) return primaryValue
        if (alternate != null) {
            val altValue = getDouble(alternate)
            if (altValue != null) return altValue
        }
        val numberValue = get(primary)
        if (numberValue is Number) return numberValue.toDouble()
        if (alternate != null) {
            val altNumber = get(alternate)
            if (altNumber is Number) return altNumber.toDouble()
        }
        return null
    }

    private fun DocumentSnapshot.listField(field: String): List<String>? {
        val raw = get(field) ?: return null
        return when (raw) {
            is List<*> -> raw.mapNotNull { it?.toString()?.trim()?.takeIf { it.isNotBlank() } }
            is String -> raw.split(",").mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            else -> null
        }
    }

    data class SyncResult(
        val issuersSynced: Int,
        val cardsSynced: Int,
        val templateCardsSynced: Int,
        val benefitsSynced: Int,
        val templateCardBenefitsSynced: Int
    )
}

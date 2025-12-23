package com.example.rewardsrader.config

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

sealed class CardConfigResult {
    data class Success(val config: CardConfig) : CardConfigResult()
    data class Failure(val errors: List<String>) : CardConfigResult()
}

class CardConfigParser(
    private val json: Json = Json { ignoreUnknownKeys = false }
) {
    private val dateFormatters: List<DateTimeFormatter> = listOf(
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm", Locale.US)
    )

    fun parse(jsonString: String): CardConfigResult {
        val errors = mutableListOf<String>()
        val decoded = try {
            json.decodeFromString<CardConfig>(jsonString)
        } catch (e: SerializationException) {
            return CardConfigResult.Failure(listOf("Invalid JSON: ${e.message.orEmpty()}"))
        }

        if (decoded.schemaVersion.isBlank()) {
            errors.add("schema_version is required.")
        }
        if (decoded.dataVersion.isBlank()) {
            errors.add("data_version is required.")
        }
        if (decoded.cards.isEmpty()) {
            errors.add("At least one card is required.")
        }

        val cardIds = mutableSetOf<Int>()
        decoded.cards.forEach { card ->
            if (card.cardId < 0) errors.add("card_id must be non-negative (got ${card.cardId}).")
            if (cardIds.contains(card.cardId)) errors.add("Duplicate card_id ${card.cardId}.")
            cardIds.add(card.cardId)
            if (card.issuer.isBlank()) errors.add("issuer is required for card_id ${card.cardId}.")
            if (card.productName.isBlank()) errors.add("product_name is required for card_id ${card.cardId}.")
            if (card.network.isBlank()) errors.add("network is required for card_id ${card.cardId}.")
            if (card.annualFeeUsd < 0) errors.add("annual_fee_usd must be >= 0 for card_id ${card.cardId}.")
            if (card.dataSource.isBlank()) errors.add("data_source is required for card_id ${card.cardId}.")
            validateDate(card.lastUpdated, "last_updated for card_id ${card.cardId}", errors)
        }

        if (decoded.benefits.isEmpty()) {
            errors.add("At least one benefit is required.")
        }

        val benefitIds = mutableSetOf<Int>()
        decoded.benefits.forEach { benefit ->
            if (benefit.benefitId < 0) errors.add("benefit_id must be non-negative (got ${benefit.benefitId}).")
            if (benefitIds.contains(benefit.benefitId)) errors.add("Duplicate benefit_id ${benefit.benefitId}.")
            benefitIds.add(benefit.benefitId)
            if (!cardIds.contains(benefit.cardId)) {
                errors.add("benefit_id ${benefit.benefitId} references missing card_id ${benefit.cardId}.")
            }
            validateDate(benefit.effectiveDate, "effective_date for benefit_id ${benefit.benefitId}", errors)
            benefit.expiryDate?.let {
                validateDate(it, "expiry_date for benefit_id ${benefit.benefitId}", errors)
            }
        }

        return if (errors.isEmpty()) {
            CardConfigResult.Success(decoded)
        } else {
            CardConfigResult.Failure(errors)
        }
    }

    private fun validateDate(date: String, label: String, errors: MutableList<String>) {
        val parsed = dateFormatters.any { formatter ->
            try {
                LocalDateTime.parse(date, formatter)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }
        if (!parsed) {
            errors.add("$label is invalid (expected MM/dd/yyyy hh:mm in UTC).")
        }
    }
}

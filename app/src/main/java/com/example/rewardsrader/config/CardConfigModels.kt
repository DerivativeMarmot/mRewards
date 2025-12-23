package com.example.rewardsrader.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardConfig(
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("data_version") val dataVersion: String,
    val cards: List<CardTemplate>,
    val benefits: List<BenefitTemplate>
)

@Serializable
data class CardTemplate(
    @SerialName("card_id") val cardId: Int,
    val issuer: String,
    @SerialName("product_name") val productName: String,
    val network: String,
    @SerialName("annual_fee_usd") val annualFeeUsd: Double,
    @SerialName("last_updated") val lastUpdated: String,
    @SerialName("data_source") val dataSource: String,
    val notes: String? = null
)

@Serializable
data class BenefitTemplate(
    @SerialName("benefit_id") val benefitId: Int,
    @SerialName("card_id") val cardId: Int,
    val type: BenefitType,
    @SerialName("amount_usd") val amountUsd: Double? = null,
    @SerialName("cap_usd") val capUsd: Double? = null,
    val cadence: Cadence,
    val category: String? = null,
    val merchant: String? = null,
    @SerialName("enrollment_required") val enrollmentRequired: Boolean,
    @SerialName("effective_date") val effectiveDate: String,
    @SerialName("expiry_date") val expiryDate: String? = null,
    val terms: String? = null,
    val notes: String? = null
)

@Serializable
enum class BenefitType {
    @SerialName("credit") CREDIT,
    @SerialName("multiplier") MULTIPLIER,
    @SerialName("access") ACCESS,
    @SerialName("offer") OFFER
}

@Serializable
enum class Cadence {
    @SerialName("once") ONCE,
    @SerialName("monthly") MONTHLY,
    @SerialName("quarterly") QUARTERLY,
    @SerialName("annual") ANNUAL
}

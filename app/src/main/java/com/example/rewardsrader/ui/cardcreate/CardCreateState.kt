package com.example.rewardsrader.ui.cardcreate

data class IssuerOption(
    val id: String,
    val name: String
)

data class CardSearchItem(
    val id: String,
    val issuerId: String,
    val issuerName: String,
    val productName: String,
    val network: String,
    val segment: String,
    val paymentInstrument: String,
    val annualFee: Double,
    val benefitTypes: Set<String> = emptySet(),
    val benefitCategories: Set<String> = emptySet()
)

data class CardCreateFilters(
    val issuerIds: Set<String> = emptySet(),
    val networks: Set<String> = emptySet(),
    val segments: Set<String> = emptySet(),
    val paymentInstruments: Set<String> = emptySet(),
    val benefitTypes: Set<String> = emptySet(),
    val benefitCategories: Set<String> = emptySet(),
    val annualFeeRange: ClosedFloatingPointRange<Float>? = null,
    val noAnnualFeeOnly: Boolean = false
)

enum class SortMode {
    PRODUCT,
    ISSUER_PRODUCT,
    ANNUAL_FEE_LOW_HIGH,
    ANNUAL_FEE_HIGH_LOW,
    NETWORK
}

data class CardCreateState(
    val isLoading: Boolean = true,
    val query: String = "",
    val sortMode: SortMode = SortMode.PRODUCT,
    val filters: CardCreateFilters = CardCreateFilters(),
    val issuers: List<IssuerOption> = emptyList(),
    val networks: List<String> = emptyList(),
    val segments: List<String> = emptyList(),
    val paymentInstruments: List<String> = emptyList(),
    val benefitCategories: List<String> = emptyList(),
    val benefitTypes: List<String> = listOf("Credit", "Multiplier"),
    val feeRangeBounds: ClosedFloatingPointRange<Float> = 0f..0f,
    val results: List<CardSearchItem> = emptyList(),
    val filteredResults: List<CardSearchItem> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null
)

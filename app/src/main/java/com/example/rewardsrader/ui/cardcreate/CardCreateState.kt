package com.example.rewardsrader.ui.cardcreate

data class IssuerOption(
    val id: String,
    val name: String
)

data class CardOption(
    val id: String,
    val issuerId: String,
    val productName: String
)

data class CardCreateState(
    val issuers: List<IssuerOption> = emptyList(),
    val selectedIssuerId: String? = null,
    val cards: List<CardOption> = emptyList(),
    val selectedCardId: String? = null,
    val openDateUtc: String = "",
    val statementCutUtc: String = "",
    val applicationStatus: String = "",
    val welcomeOfferProgress: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

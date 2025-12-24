package com.example.rewardsrader.ui.cardcreate

data class CardCreateState(
    val templates: List<com.example.rewardsrader.config.CardTemplate> = emptyList(),
    val selectedTemplateId: Int? = null,
    val openDateUtc: String = "",
    val statementCutUtc: String = "",
    val applicationStatus: String = "",
    val welcomeOfferProgress: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

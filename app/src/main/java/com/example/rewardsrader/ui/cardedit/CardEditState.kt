package com.example.rewardsrader.ui.cardedit

data class CardEditState(
    val cardId: String = "",
    val issuer: String = "",
    val productName: String = "",
    val nickname: String = "",
    val annualFee: String = "",
    val lastFour: String = "",
    val openDate: String = "",
    val statementCut: String = "",
    val status: String = "",
    val welcomeOfferProgress: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

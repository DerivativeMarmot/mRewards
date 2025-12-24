package com.example.rewardsrader.ui.cardedit

data class CardEditState(
    val cardId: Long = 0L,
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
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

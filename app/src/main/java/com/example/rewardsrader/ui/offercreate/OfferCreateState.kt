package com.example.rewardsrader.ui.offercreate

data class OfferCreateState(
    val offerId: String? = null,
    val cardId: String = "",
    val productName: String = "",
    val title: String = "",
    val note: String = "",
    val type: String = "credit", // credit | multiplier
    val multiplier: String = "",
    val minSpend: String = "",
    val maxCashBack: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

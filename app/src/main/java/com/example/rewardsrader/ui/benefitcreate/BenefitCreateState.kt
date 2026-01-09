package com.example.rewardsrader.ui.benefitcreate

data class BenefitCreateState(
    val benefitId: String? = null,
    val cardId: String = "",
    val productName: String = "",
    val issuer: String = "",
    val title: String = "",
    val type: String = "credit", // credit | multiplier
    val amount: String = "",
    val cap: String = "",
    val cadence: String = "monthly",
    val categories: List<String> = emptyList(),
    val customCategories: List<String> = emptyList(),
    val customCategory: String = "",
    val effectiveDate: String = "",
    val expiryDate: String = "",
    val notes: String = "",
    val transactionAmount: String = "",
    val transactionDate: String = "",
    val transactions: List<TransactionEntry> = emptyList(),
    val editingTransactionIndex: Int? = null,
    val progress: String = "",
    val dataSource: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

data class TransactionEntry(
    val amount: String,
    val date: String
)

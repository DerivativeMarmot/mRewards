package com.example.rewardsrader.ui.tracker

import com.example.rewardsrader.data.local.entity.TrackerSourceType

data class TrackerDetailUi(
    val id: String,
    val cardName: String,
    val title: String,
    val amount: Double,
    val usedAmount: Double,
    val startDate: String,
    val endDate: String,
    val manualCompleted: Boolean,
    val notes: String?,
    val sourceType: TrackerSourceType
)

data class TrackerTransactionUi(
    val id: String,
    val amount: Double,
    val date: String,
    val notes: String?
)

data class TrackerEditState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val tracker: TrackerDetailUi? = null,
    val offerNotes: String = "",
    val offerCompleted: Boolean = false,
    val transactions: List<TrackerTransactionUi> = emptyList(),
    val entryAmount: String = "",
    val entryDate: String = "",
    val entryNotes: String = "",
    val isSaving: Boolean = false
)

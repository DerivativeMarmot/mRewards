package com.example.rewardsrader.ui.tracker

import com.example.rewardsrader.data.local.entity.TrackerSourceType

enum class TrackerStatus {
    Active,
    Complete,
    Expired
}

data class TrackerItemUi(
    val id: String,
    val profileCardId: String,
    val cardName: String,
    val cardFaceUrl: String?,
    val title: String,
    val amount: Double,
    val usedAmount: Double,
    val timeLeftLabel: String,
    val status: TrackerStatus,
    val sourceType: TrackerSourceType
)

data class TrackerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedFilter: TrackerStatus = TrackerStatus.Active,
    val trackers: List<TrackerItemUi> = emptyList(),
    val activeCount: Int = 0,
    val completeCount: Int = 0,
    val expiredCount: Int = 0
)

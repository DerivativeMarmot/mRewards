package com.example.rewardsrader.ui.cardlist

import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.NotificationRuleEntity
import com.example.rewardsrader.data.local.entity.UsageEntryEntity

data class DeletedCardSnapshot(
    val card: CardEntity,
    val benefits: List<BenefitEntity>,
    val applications: List<ApplicationEntity>,
    val usageEntries: List<UsageEntryEntity>,
    val notificationRules: List<NotificationRuleEntity>
)

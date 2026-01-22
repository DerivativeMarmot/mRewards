package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_schedules",
    indices = [Index(value = ["sourceType", "sourceId"])]
)
data class NotificationScheduleEntity(
    @PrimaryKey val id: String,
    val sourceType: NotificationSourceType,
    val sourceId: String,
    val triggerAtMillis: Long,
    val daysBefore: Int,
    val enabled: Boolean
)

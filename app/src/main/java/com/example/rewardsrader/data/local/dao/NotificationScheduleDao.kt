package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.NotificationScheduleEntity
import com.example.rewardsrader.data.local.entity.NotificationSourceType

@Dao
interface NotificationScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: NotificationScheduleEntity)

    @Query("SELECT * FROM notification_schedules WHERE sourceType = :sourceType AND sourceId = :sourceId")
    suspend fun getForSource(
        sourceType: NotificationSourceType,
        sourceId: String
    ): List<NotificationScheduleEntity>

    @Query("SELECT * FROM notification_schedules WHERE sourceType = :sourceType AND enabled = 1")
    suspend fun getEnabledForSourceType(sourceType: NotificationSourceType): List<NotificationScheduleEntity>

    @Query("DELETE FROM notification_schedules WHERE id = :scheduleId")
    suspend fun deleteById(scheduleId: String)

    @Query("DELETE FROM notification_schedules WHERE sourceType = :sourceType AND sourceId = :sourceId")
    suspend fun deleteForSource(sourceType: NotificationSourceType, sourceId: String)
}

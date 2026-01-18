package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rewardsrader.data.local.entity.TrackerEntity

@Dao
interface TrackerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(trackers: List<TrackerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tracker: TrackerEntity)

    @Update
    suspend fun update(tracker: TrackerEntity)

    @Query("SELECT * FROM trackers WHERE id = :trackerId LIMIT 1")
    suspend fun getById(trackerId: String): TrackerEntity?

    @Query("SELECT * FROM trackers WHERE profileCardId IN (:profileCardIds)")
    suspend fun getForProfileCards(profileCardIds: List<String>): List<TrackerEntity>
}

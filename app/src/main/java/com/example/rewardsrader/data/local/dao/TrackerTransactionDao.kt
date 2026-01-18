package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.TrackerTransactionEntity

@Dao
interface TrackerTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TrackerTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TrackerTransactionEntity>)

    @Query("SELECT * FROM tracker_transactions WHERE trackerId = :trackerId ORDER BY dateUtc")
    suspend fun getForTracker(trackerId: String): List<TrackerTransactionEntity>

    @Query("SELECT * FROM tracker_transactions WHERE trackerId IN (:trackerIds)")
    suspend fun getForTrackers(trackerIds: List<String>): List<TrackerTransactionEntity>

    @Query("DELETE FROM tracker_transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: String)
}

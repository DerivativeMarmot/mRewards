package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rewardsrader.data.local.entity.OfferEntity

@Dao
interface OfferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offer: OfferEntity): Long

    @Update
    suspend fun update(offer: OfferEntity)

    @Query("SELECT * FROM offers WHERE cardId = :cardId ORDER BY startDateUtc IS NULL, startDateUtc")
    suspend fun getForCard(cardId: Long): List<OfferEntity>

    @Query("SELECT * FROM offers WHERE id = :offerId LIMIT 1")
    suspend fun getById(offerId: Long): OfferEntity?

    @Query("DELETE FROM offers WHERE id = :offerId")
    suspend fun deleteById(offerId: Long)
}

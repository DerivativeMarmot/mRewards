package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.CardFaceEntity

@Dao
interface CardFaceDao {
    @Upsert
    suspend fun insert(face: CardFaceEntity)

    @Upsert
    suspend fun insertAll(faces: List<CardFaceEntity>)

    @Query("SELECT * FROM card_faces WHERE cardId = :cardId")
    suspend fun getForCard(cardId: String): List<CardFaceEntity>

    @Query(
        """
        SELECT * FROM card_faces
        WHERE cardId = :cardId
        ORDER BY CASE WHEN isDefault THEN 0 ELSE 1 END
        LIMIT 1
        """
    )
    suspend fun getPreferredForCard(cardId: String): CardFaceEntity?
}

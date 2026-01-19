package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.rewardsrader.data.local.entity.CardEntity

@Dao
interface CardDao {
    @Upsert
    suspend fun insert(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :cardId")
    suspend fun deleteById(cardId: String)

    @Query("SELECT * FROM cards")
    suspend fun getAll(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :cardId LIMIT 1")
    suspend fun getById(cardId: String): CardEntity?

    @Update
    suspend fun update(card: CardEntity)
}

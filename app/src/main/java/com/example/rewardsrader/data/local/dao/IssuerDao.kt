package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.IssuerEntity

@Dao
interface IssuerDao {
    @Upsert
    suspend fun insert(issuer: IssuerEntity)

    @Upsert
    suspend fun insertAll(issuers: List<IssuerEntity>)

    @Query("SELECT * FROM issuers")
    suspend fun getAll(): List<IssuerEntity>

    @Query("SELECT * FROM issuers WHERE id = :issuerId LIMIT 1")
    suspend fun getById(issuerId: String): IssuerEntity?
}

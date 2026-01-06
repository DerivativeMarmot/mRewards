package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE benefitId = :benefitId")
    suspend fun getForBenefit(benefitId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE profileCardBenefitId = :profileCardBenefitId")
    suspend fun getForProfileCardBenefit(profileCardBenefitId: String): List<TransactionEntity>
}

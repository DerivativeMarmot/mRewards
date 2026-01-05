package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.ProfileCardBenefitEntity

@Dao
interface ProfileCardBenefitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: ProfileCardBenefitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<ProfileCardBenefitEntity>)

    @Query("SELECT * FROM profile_card_benefits WHERE profileCardId = :profileCardId")
    suspend fun getForProfileCard(profileCardId: String): List<ProfileCardBenefitEntity>
}

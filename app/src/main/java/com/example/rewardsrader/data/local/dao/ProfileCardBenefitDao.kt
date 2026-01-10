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

    @Query("SELECT * FROM profile_card_benefits WHERE profileCardId = :profileCardId AND benefitId = :benefitId LIMIT 1")
    suspend fun getForProfileCardAndBenefit(profileCardId: String, benefitId: String): ProfileCardBenefitEntity?

    @Query("UPDATE profile_card_benefits SET startDateUtc = :startDateUtc, endDateUtc = :endDateUtc WHERE profileCardId = :profileCardId AND benefitId = :benefitId")
    suspend fun updateDates(profileCardId: String, benefitId: String, startDateUtc: String?, endDateUtc: String?)

    @Query("DELETE FROM profile_card_benefits WHERE id = :linkId")
    suspend fun deleteById(linkId: String)
}

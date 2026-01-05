package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rewardsrader.data.local.entity.ProfileCardEntity

@Dao
interface ProfileCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profileCard: ProfileCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profileCards: List<ProfileCardEntity>)

    @Update
    suspend fun update(profileCard: ProfileCardEntity)

    @Query("SELECT * FROM profile_cards WHERE profileId = :profileId")
    suspend fun getForProfile(profileId: String): List<ProfileCardEntity>

    @Query("SELECT * FROM profile_cards WHERE id = :profileCardId LIMIT 1")
    suspend fun getById(profileCardId: String): ProfileCardEntity?
}

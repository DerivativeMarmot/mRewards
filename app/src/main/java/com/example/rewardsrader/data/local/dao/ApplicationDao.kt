package com.example.rewardsrader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rewardsrader.data.local.entity.ApplicationEntity

@Dao
interface ApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: ApplicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(applications: List<ApplicationEntity>)

    @Query("SELECT * FROM applications WHERE profileCardId = :profileCardId")
    suspend fun getForProfileCard(profileCardId: String): List<ApplicationEntity>
}

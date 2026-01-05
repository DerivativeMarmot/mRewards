package com.example.rewardsrader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issuers")
data class IssuerEntity(
    @PrimaryKey val id: String,
    val name: String
)

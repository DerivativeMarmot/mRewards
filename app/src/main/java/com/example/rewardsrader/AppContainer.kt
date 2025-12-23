package com.example.rewardsrader

import android.content.Context
import androidx.room.Room
import com.example.rewardsrader.data.local.AppDatabase
import com.example.rewardsrader.data.local.repository.CardRepository

class AppContainer(context: Context) {
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "rewardsrader.db"
    ).build()

    val cardRepository: CardRepository = CardRepository(
        db.cardDao(),
        db.benefitDao(),
        db.applicationDao()
    )
}

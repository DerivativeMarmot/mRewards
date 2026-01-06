package com.example.rewardsrader

import android.content.Context
import androidx.room.Room
import com.example.rewardsrader.config.CardConfigLoader
import com.example.rewardsrader.config.CardConfigProvider
import com.example.rewardsrader.config.DefaultCardConfigProvider
import com.example.rewardsrader.data.local.AppDatabase
import com.example.rewardsrader.data.local.repository.CardRepository
import com.example.rewardsrader.data.remote.FirestoreSyncer
import com.example.rewardsrader.template.CardTemplateImporter
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "rewardsrader.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    val cardRepository: CardRepository = CardRepository(
        issuerDao = db.issuerDao(),
        cardDao = db.cardDao(),
        cardFaceDao = db.cardFaceDao(),
        cardBenefitDao = db.cardBenefitDao(),
        profileDao = db.profileDao(),
        profileCardDao = db.profileCardDao(),
        profileCardBenefitDao = db.profileCardBenefitDao(),
        benefitDao = db.benefitDao(),
        transactionDao = db.transactionDao(),
        notificationRuleDao = db.notificationRuleDao(),
        offerDao = db.offerDao(),
        applicationDao = db.applicationDao()
    )

    private val cardConfigLoader = CardConfigLoader(context)
    val cardConfigProvider: CardConfigProvider = DefaultCardConfigProvider(cardConfigLoader)
    val cardTemplateImporter = CardTemplateImporter(cardRepository)
    val firestoreSyncer = FirestoreSyncer(FirebaseFirestore.getInstance(), cardRepository)
}

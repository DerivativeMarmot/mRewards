package com.example.rewardsrader.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration 1->2 added statementCutUtc and welcomeOfferProgress to cards.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE cards ADD COLUMN statementCutUtc TEXT")
        database.execSQL("ALTER TABLE cards ADD COLUMN welcomeOfferProgress TEXT")
    }
}

// Migration 2->3 adds nickname and lastFour to cards.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE cards ADD COLUMN nickname TEXT")
        database.execSQL("ALTER TABLE cards ADD COLUMN lastFour TEXT")
    }
}

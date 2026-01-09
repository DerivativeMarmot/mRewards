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

// Migration 3->4 adds transactionsJson to benefits.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE benefits ADD COLUMN transactionsJson TEXT")
    }
}

// Migration 4->5 adds SUB fields to cards.
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE cards ADD COLUMN subSpendingUsd REAL")
        database.execSQL("ALTER TABLE cards ADD COLUMN subDuration INTEGER")
        database.execSQL("ALTER TABLE cards ADD COLUMN subDurationUnit TEXT")
    }
}

// Migration 5->6 adds offers table.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS offers (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                cardId INTEGER NOT NULL,
                title TEXT NOT NULL,
                note TEXT,
                startDateUtc TEXT,
                endDateUtc TEXT,
                type TEXT NOT NULL,
                minSpendUsd REAL,
                maxCashBackUsd REAL,
                status TEXT NOT NULL DEFAULT 'active',
                FOREIGN KEY(cardId) REFERENCES cards(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_offers_cardId ON offers(cardId)")
    }
}

// Migration 6->7 adds multiplierRate to offers.
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE offers ADD COLUMN multiplierRate REAL")
    }
}

// Migration 8->9 adds paymentInstrument and segment to cards.
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE cards ADD COLUMN paymentInstrument TEXT NOT NULL DEFAULT 'Credit'")
        database.execSQL("ALTER TABLE cards ADD COLUMN segment TEXT NOT NULL DEFAULT 'Personal'")
    }
}

// Migration 9->10 renames foreign transaction fee column by adding a new column and backfilling.
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE cards ADD COLUMN foreignTransactionFee REAL NOT NULL DEFAULT 0.0")
        database.execSQL("UPDATE cards SET foreignTransactionFee = COALESCE(foreignFeeTransactionFee, 0.0)")
    }
}

// Migration 10->12 recreates benefits without the removed categoriesRaw column.
val MIGRATION_10_12 = object : Migration(10, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS benefits_new (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                amount REAL,
                cap REAL,
                frequency TEXT NOT NULL,
                category TEXT NOT NULL,
                enrollmentRequired INTEGER NOT NULL,
                startDateUtc TEXT NOT NULL,
                endDateUtc TEXT,
                notes TEXT
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO benefits_new (id, type, amount, cap, frequency, category, enrollmentRequired, startDateUtc, endDateUtc, notes)
            SELECT id, type, amount, cap, frequency, category, enrollmentRequired, startDateUtc, endDateUtc, notes
            FROM benefits
            """.trimIndent()
        )
        database.execSQL("DROP TABLE benefits")
        database.execSQL("ALTER TABLE benefits_new RENAME TO benefits")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS uniq_template_card_benefitId ON template_card_benefits(benefitId)")
    }
}

// Migration 12->13 adds SUB fields to profile_cards (previously only on template cards).
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE profile_cards ADD COLUMN subSpending REAL")
        database.execSQL("ALTER TABLE profile_cards ADD COLUMN subDuration INTEGER")
        database.execSQL("ALTER TABLE profile_cards ADD COLUMN subDurationUnit TEXT")
    }
}

// Migration 13->14 renames templateCardId to cardId on profile_cards and adds template_cards table.
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS profile_cards_new (
                id TEXT NOT NULL PRIMARY KEY,
                profileId TEXT NOT NULL,
                cardId TEXT,
                nickname TEXT,
                annualFee REAL NOT NULL,
                lastFour TEXT,
                openDateUtc TEXT,
                closeDateUtc TEXT,
                statementCutUtc TEXT,
                welcomeOfferProgress TEXT,
                status TEXT NOT NULL,
                notes TEXT,
                subSpending REAL,
                subDuration INTEGER,
                subDurationUnit TEXT,
                FOREIGN KEY(profileId) REFERENCES profiles(id) ON DELETE CASCADE ON UPDATE NO ACTION,
                FOREIGN KEY(cardId) REFERENCES cards(id) ON DELETE SET NULL ON UPDATE NO ACTION
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO profile_cards_new (
                id,
                profileId,
                cardId,
                nickname,
                annualFee,
                lastFour,
                openDateUtc,
                closeDateUtc,
                statementCutUtc,
                welcomeOfferProgress,
                status,
                notes,
                subSpending,
                subDuration,
                subDurationUnit
            )
            SELECT
                id,
                profileId,
                templateCardId,
                nickname,
                annualFee,
                lastFour,
                openDateUtc,
                closeDateUtc,
                statementCutUtc,
                welcomeOfferProgress,
                status,
                notes,
                subSpending,
                subDuration,
                subDurationUnit
            FROM profile_cards
            """.trimIndent()
        )
        database.execSQL("DROP TABLE profile_cards")
        database.execSQL("ALTER TABLE profile_cards_new RENAME TO profile_cards")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_profile_cards_profileId ON profile_cards(profileId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_profile_cards_cardId ON profile_cards(cardId)")

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS template_cards (
                id TEXT NOT NULL PRIMARY KEY,
                cardId TEXT NOT NULL,
                FOREIGN KEY(cardId) REFERENCES cards(id) ON DELETE CASCADE ON UPDATE NO ACTION
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_template_cards_cardId ON template_cards(cardId)")
    }
}

// Migration 14->15 replaces card_benefits with template_card_benefits.
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS template_card_benefits (
                id TEXT NOT NULL PRIMARY KEY,
                templateCardId TEXT NOT NULL,
                benefitId TEXT NOT NULL,
                FOREIGN KEY(templateCardId) REFERENCES template_cards(id) ON DELETE CASCADE ON UPDATE NO ACTION,
                FOREIGN KEY(benefitId) REFERENCES benefits(id) ON DELETE CASCADE ON UPDATE NO ACTION
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_template_card_benefits_templateCardId ON template_card_benefits(templateCardId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_template_card_benefits_benefitId ON template_card_benefits(benefitId)")
        database.execSQL(
            """
            INSERT INTO template_card_benefits (id, templateCardId, benefitId)
            SELECT id, cardId, benefitId FROM card_benefits
            """.trimIndent()
        )
        database.execSQL("DROP TABLE IF EXISTS card_benefits")
    }
}

// Migration 15->16 adds title to benefits, removes enrollment/dates, and moves dates to profile_card_benefits.
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE profile_card_benefits ADD COLUMN startDateUtc TEXT")
        database.execSQL("ALTER TABLE profile_card_benefits ADD COLUMN endDateUtc TEXT")
        database.execSQL(
            """
            UPDATE profile_card_benefits
            SET startDateUtc = (
                SELECT startDateUtc FROM benefits WHERE benefits.id = profile_card_benefits.benefitId
            ),
                endDateUtc = (
                SELECT endDateUtc FROM benefits WHERE benefits.id = profile_card_benefits.benefitId
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS benefits_new (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT,
                type TEXT NOT NULL,
                amount REAL,
                cap REAL,
                frequency TEXT NOT NULL,
                category TEXT NOT NULL,
                notes TEXT
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO benefits_new (id, title, type, amount, cap, frequency, category, notes)
            SELECT id, NULL, type, amount, cap, frequency, category, notes FROM benefits
            """.trimIndent()
        )
        database.execSQL("DROP TABLE benefits")
        database.execSQL("ALTER TABLE benefits_new RENAME TO benefits")
    }
}

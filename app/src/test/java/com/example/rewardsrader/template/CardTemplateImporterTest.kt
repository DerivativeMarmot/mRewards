package com.example.rewardsrader.template

import androidx.room.Room
import com.example.rewardsrader.config.CardConfigParser
import com.example.rewardsrader.config.CardConfigResult
import com.example.rewardsrader.data.local.AppDatabase
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CardTemplateImporterTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CardRepository
    private lateinit var importer: CardTemplateImporter
    private val parser = CardConfigParser()

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CardRepository(db.cardDao(), db.benefitDao(), db.applicationDao())
        importer = CardTemplateImporter(repository)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun importFromConfig_createsCardBenefitsAndApplication() = runTest {
        val config = (parser.parse(sampleJson) as CardConfigResult.Success).config
        val result = importer.importFromConfig(
            config = config,
            selectedCardId = 1,
            openDateUtc = "01/05/2025 09:00",
            statementCutUtc = "01/15/2025 09:00",
            applicationStatus = "approved",
            welcomeOfferProgress = "75%"
        )

        assertTrue(result is ImportResult.Success)
        val cardId = (result as ImportResult.Success).cardId

        val cards = repository.getCardsWithBenefits()
        assertEquals(1, cards.size)
        assertEquals(cardId, cards.first().card.id)
        assertEquals("approved", cards.first().card.status)
        assertEquals("01/15/2025 09:00", cards.first().card.statementCutUtc)
        assertEquals(2, cards.first().benefits.size)

        val application = db.applicationDao().getForCard(cardId)
        assertEquals(1, application.size)
        assertEquals("approved", application.first().status)
        assertEquals("75%", application.first().welcomeOfferTerms)
    }

    private val sampleJson = """
        {
          "schema_version": "1.0",
          "data_version": "2025.12.23",
          "cards": [
            {
              "card_id": 1,
              "issuer": "Example Bank",
              "product_name": "Example Cash Preferred",
              "network": "Visa",
              "annual_fee_usd": 95,
              "last_updated": "12/23/2025 10:00",
              "data_source": "RewardsRader Maintainers",
              "notes": "US-only benefits; requires enrollment for partner offers."
            }
          ],
          "benefits": [
            {
              "benefit_id": 101,
              "card_id": 1,
              "type": "credit",
              "amount_usd": 10,
              "cap_usd": 10,
              "cadence": "monthly",
              "category": "dining",
              "merchant": "Restaurant partners",
              "enrollment_required": true,
              "effective_date": "01/01/2025 09:00",
              "expiry_date": "12/31/2025 11:59",
              "terms": "Enroll each calendar year; credit resets monthly.",
              "notes": "Credit applies per statement month."
            },
            {
              "benefit_id": 102,
              "card_id": 1,
              "type": "multiplier",
              "amount_usd": null,
              "cap_usd": 6000,
              "cadence": "annual",
              "category": "groceries",
              "merchant": "US supermarkets",
              "enrollment_required": false,
              "effective_date": "01/01/2025 09:00",
              "expiry_date": "12/31/2025 11:59",
              "terms": "6x points on up to $6k annual supermarket spend; excludes superstores.",
              "notes": null
            }
          ]
        }
    """.trimIndent()
}

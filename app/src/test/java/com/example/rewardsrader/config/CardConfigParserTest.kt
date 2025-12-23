package com.example.rewardsrader.config

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardConfigParserTest {

    private val parser = CardConfigParser()

    @Test
    fun `parses valid config`() {
        val result = parser.parse(sampleJson)
        assertTrue(result is CardConfigResult.Success)
        val success = result as CardConfigResult.Success
        assertEquals("1.0", success.config.schemaVersion)
        assertEquals(1, success.config.cards.size)
        assertEquals(2, success.config.benefits.size)
    }

    @Test
    fun `fails on invalid date format`() {
        val brokenJson = sampleJson.replace("12/23/2025 10:00", "2025-12-23T10:00")
        val result = parser.parse(brokenJson)
        assertTrue(result is CardConfigResult.Failure)
        val failure = result as CardConfigResult.Failure
        assertTrue(failure.errors.any { it.contains("last_updated") })
    }

    @Test
    fun `fails on missing card reference`() {
        val parsed = json.decodeFromString(CardConfig.serializer(), sampleJson)
        val broken = parsed.copy(
            benefits = parsed.benefits.mapIndexed { index, benefit ->
                if (index == 0) {
                    benefit.copy(cardId = 999)
                } else benefit
            }
        )
        val brokenJson = json.encodeToString(CardConfig.serializer(), broken)
        val result = parser.parse(brokenJson)
        assertTrue(result is CardConfigResult.Failure)
        val failure = result as CardConfigResult.Failure
        assertTrue(failure.errors.any { it.contains("references missing card_id") })
    }

    private val json = Json { prettyPrint = false }
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

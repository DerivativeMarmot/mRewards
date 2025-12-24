package com.example.rewardsrader.data

import androidx.room.Room
import com.example.rewardsrader.data.local.AppDatabase
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
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
class CardRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CardRepository

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CardRepository(db.cardDao(), db.benefitDao(), db.applicationDao())
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun addListRemoveCardWithBenefits() = runTest {
        val card = CardEntity(
            issuer = "Example Bank",
            nickname = "Daily Driver",
            productName = "Example Cash Preferred",
            network = "Visa",
            annualFeeUsd = 95.0,
            lastFour = "1234",
            openDateUtc = "01/01/2025 09:00",
            statementCutUtc = "01/15/2025 09:00",
            welcomeOfferProgress = "50%",
            status = "open"
        )
        val benefits = listOf(
            BenefitEntity(
                cardId = 0,
                type = "credit",
                amountUsd = 10.0,
                capUsd = 10.0,
                cadence = "monthly",
                category = "dining",
                merchant = "Restaurant partners",
                enrollmentRequired = true,
                effectiveDateUtc = "01/01/2025 09:00",
                expiryDateUtc = "12/31/2025 11:59",
                terms = "Enroll each calendar year; credit resets monthly.",
                dataSource = "Bundle"
            ),
            BenefitEntity(
                cardId = 0,
                type = "multiplier",
                amountUsd = null,
                capUsd = 6000.0,
                cadence = "annual",
                category = "groceries",
                merchant = "US supermarkets",
                enrollmentRequired = false,
                effectiveDateUtc = "01/01/2025 09:00",
                expiryDateUtc = "12/31/2025 11:59",
                terms = "6x points on up to $6k annual supermarket spend; excludes superstores.",
                dataSource = "Bundle"
            )
        )

        val cardId = repository.addCard(card, benefits)

        val cards = repository.getCards()
        assertEquals(1, cards.size)
        assertEquals(cardId, cards.first().id)

        val withBenefits = repository.getCardsWithBenefits()
        assertEquals(1, withBenefits.size)
        assertEquals(2, withBenefits.first().benefits.size)
        assertEquals("credit", withBenefits.first().benefits.first().type)

        val updatedCard = cards.first().copy(
            productName = "Updated Product",
            annualFeeUsd = 120.0,
            nickname = "Updated Nick",
            lastFour = "9999",
            welcomeOfferProgress = "done"
        )
        repository.updateCard(updatedCard)
        val fetched = repository.getCard(cardId)
        assertEquals("Updated Product", fetched?.productName)
        assertEquals(120.0, fetched?.annualFeeUsd ?: 0.0, 0.0)
        assertEquals("Updated Nick", fetched?.nickname)
        assertEquals("9999", fetched?.lastFour)
        assertEquals("done", fetched?.welcomeOfferProgress)

        repository.removeCard(cardId)
        val afterDelete = repository.getCardsWithBenefits()
        assertTrue(afterDelete.isEmpty())
    }
}

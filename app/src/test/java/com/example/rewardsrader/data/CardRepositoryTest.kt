package com.example.rewardsrader.data

import androidx.room.Room
import com.example.rewardsrader.data.local.AppDatabase
import com.example.rewardsrader.data.local.entity.BenefitCategory
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardNetwork
import com.example.rewardsrader.data.local.entity.CardStatus
import com.example.rewardsrader.data.local.entity.IssuerEntity
import com.example.rewardsrader.data.local.entity.OfferEntity
import com.example.rewardsrader.data.local.entity.ProfileCardEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import android.os.Build
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class CardRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CardRepository

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CardRepository(
            db.issuerDao(),
            db.cardDao(),
            db.cardFaceDao(),
            db.profileDao(),
            db.profileCardDao(),
            db.profileCardBenefitDao(),
            db.benefitDao(),
            db.transactionDao(),
            db.trackerDao(),
            db.trackerTransactionDao(),
            db.notificationRuleDao(),
            db.offerDao(),
            db.applicationDao(),
            db.templateCardDao(),
            db.templateCardBenefitDao()
        )
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertAndQueryProfileCardsWithBenefitsAndOffers() = runTest {
        val profile = repository.ensureProfile("profile_1", "Test User")
        repository.upsertIssuers(listOf(IssuerEntity(id = "issuer_1", name = "Issuer One")))
        repository.upsertCards(
            listOf(
                CardEntity(
                    id = "card_1",
                    issuerId = "issuer_1",
                    productName = "Example Cash Preferred",
                    network = CardNetwork.Visa,
                    annualFee = 95.0,
                    foreignTransactionFee = 0.0
                )
            )
        )
        val benefits = listOf(
            BenefitEntity(
                id = "benefit_1",
                title = "Dining credit",
                type = BenefitType.Credit,
                amount = 10.0,
                cap = 10.0,
                frequency = BenefitFrequency.Monthly,
                category = listOf(BenefitCategory.Dining),
                notes = "Dining credit"
            ),
            BenefitEntity(
                id = "benefit_2",
                title = "Supermarket multiplier",
                type = BenefitType.Multiplier,
                amount = 6.0,
                cap = 6000.0,
                frequency = BenefitFrequency.Annually,
                category = listOf(BenefitCategory.Grocery),
                notes = "Supermarket multiplier"
            )
        )
        repository.upsertBenefits(benefits)
        val profileCardId = repository.newId()
        repository.upsertProfileCards(
            listOf(
                ProfileCardEntity(
                    id = profileCardId,
                    profileId = profile.id,
                    cardId = "card_1",
                    nickname = "Daily Driver",
                    annualFee = 95.0,
                    lastFour = "1234",
                    openDateUtc = "01/01/2025",
                    statementCutUtc = "01/15/2025",
                    welcomeOfferProgress = "50%",
                    status = CardStatus.Active,
                    notes = "Test note",
                    subSpending = 1000.0,
                    subDuration = 3,
                    subDurationUnit = com.example.rewardsrader.data.local.entity.CardSubDurationUnit.Month
                )
            )
        )
        benefits.forEach {
            repository.addBenefitForProfileCard(
                profileCardId = profileCardId,
                benefit = it,
                startDateUtc = "01/01/2025 09:00",
                endDateUtc = "12/31/2025 11:59"
            )
        }

        val relations = repository.getProfileCardWithRelations(profileCardId)
        assertNotNull(relations)
        assertEquals(2, relations?.benefits?.size ?: 0)
        val profileCards = repository.getProfileCardsWithRelations(profile.id)
        assertEquals(1, profileCards.size)

        val updatedCard = relations!!.profileCard.copy(
            nickname = "Updated Nick",
            lastFour = "9999",
            status = CardStatus.Closed,
            welcomeOfferProgress = "done"
        )
        repository.updateProfileCard(updatedCard)
        val fetched = repository.getProfileCardWithRelations(profileCardId)
        assertEquals("Updated Nick", fetched?.profileCard?.nickname)
        assertEquals("9999", fetched?.profileCard?.lastFour)
        assertEquals(CardStatus.Closed, fetched?.profileCard?.status)
        assertEquals("done", fetched?.profileCard?.welcomeOfferProgress)

        val offerId = repository.newId()
        repository.addOffer(
            OfferEntity(
                id = offerId,
                profileCardId = profileCardId,
                title = "Groceries Back",
                note = "Stack with grocery portal",
                startDateUtc = "01/01/2025",
                endDateUtc = "03/01/2025",
                type = "credit",
                multiplierRate = null,
                minSpend = 500.0,
                maxCashBack = 50.0,
                status = "active"
            )
        )
        val offers = repository.getOffersForProfileCard(profileCardId)
        assertEquals(1, offers.size)
        assertEquals(offerId, offers.first().id)

        val updatedOffer = offers.first().copy(status = "used", title = "Updated")
        repository.updateOffer(updatedOffer)
        val fetchedOffer = repository.getOffer(offerId)
        assertEquals("used", fetchedOffer?.status)
        assertEquals("Updated", fetchedOffer?.title)

        repository.deleteOffer(offerId)
        assertTrue(repository.getOffersForProfileCard(profileCardId).isEmpty())

        repository.deleteProfileCard(profileCardId)
        val afterDelete = repository.getProfileCardsWithRelations(profile.id)
        assertTrue(afterDelete.isEmpty())
    }
}

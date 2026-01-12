package com.example.rewardsrader.ui.cardcreate

import com.example.rewardsrader.data.local.entity.BenefitCategory
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardNetwork
import com.example.rewardsrader.data.local.entity.CardSegment
import com.example.rewardsrader.data.local.entity.IssuerEntity
import com.example.rewardsrader.data.local.entity.PaymentInstrument
import com.example.rewardsrader.data.local.entity.TemplateCardEntity
import com.example.rewardsrader.data.local.entity.TemplateCardWithBenefits
import com.example.rewardsrader.data.local.repository.CardTemplateSource
import com.example.rewardsrader.template.CardTemplateImporterContract
import com.example.rewardsrader.template.ImportResult
import com.example.rewardsrader.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardCreateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadAndCreateSuccess() = runTest(UnconfinedTestDispatcher()) {
        val card = CardEntity(
            id = "card_1",
            issuerId = "example_bank",
            productName = "Example Cash Preferred",
            network = CardNetwork.Visa,
            paymentInstrument = PaymentInstrument.Credit,
            segment = CardSegment.Personal,
            annualFee = 95.0,
            foreignTransactionFee = 0.0
        )
        val templateSource = object : CardTemplateSource {
            override suspend fun getIssuers(): List<IssuerEntity> =
                listOf(IssuerEntity(id = "example_bank", name = "Example Bank"))

            override suspend fun getCards(): List<CardEntity> = listOf(card)

            override suspend fun getTemplateCardsWithBenefits(): List<TemplateCardWithBenefits> =
                listOf(
                    TemplateCardWithBenefits(
                        templateCard = TemplateCardEntity(id = "card_1", cardId = "card_1"),
                        card = card,
                        benefits = listOf(
                            BenefitEntity(
                                id = "benefit_1",
                                title = "Dining",
                                type = BenefitType.Credit,
                                amount = 10.0,
                                cap = null,
                                frequency = BenefitFrequency.Monthly,
                                category = listOf(BenefitCategory.Dining),
                                notes = null
                            )
                        )
                    )
                )
        }
        val importer = object : CardTemplateImporterContract {
            override suspend fun importFromConfig(
                config: com.example.rewardsrader.config.CardConfig,
                selectedCardId: Int,
                openDateUtc: String?,
                statementCutUtc: String?,
                applicationStatus: String,
                welcomeOfferProgress: String?
            ): ImportResult = ImportResult.Success("profile_1")

            override suspend fun importFromDatabase(
                cardId: String,
                openDateUtc: String?,
                statementCutUtc: String?,
                applicationStatus: String,
                welcomeOfferProgress: String?
            ): ImportResult = ImportResult.Success("profile_1")
        }

        val vm = CardCreateViewModel(templateSource, importer)
        vm.loadTemplates()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.filteredResults.size)
        vm.updateQuery("Preferred")
        assertEquals(1, vm.state.value.filteredResults.size)

        val event = async { vm.events.first() }
        vm.createCard("card_1")
        advanceUntilIdle()

        assertTrue(event.await() is CardCreateEvent.Created)
    }
}

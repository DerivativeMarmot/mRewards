package com.example.rewardsrader.ui.cardcreate

import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardNetwork
import com.example.rewardsrader.data.local.entity.IssuerEntity
import com.example.rewardsrader.data.local.repository.CardTemplateSource
import com.example.rewardsrader.template.CardTemplateImporterContract
import com.example.rewardsrader.template.ImportResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.example.rewardsrader.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class CardCreateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadAndSaveSuccess() = runTest {
        val templateSource = object : CardTemplateSource {
            override suspend fun getIssuers(): List<IssuerEntity> =
                listOf(IssuerEntity(id = "example_bank", name = "Example Bank"))

            override suspend fun getCards(): List<CardEntity> =
                listOf(
                    CardEntity(
                        id = "card_1",
                        issuerId = "example_bank",
                        productName = "Example Cash Preferred",
                        network = CardNetwork.Visa,
                        annualFee = 95.0,
                        foreignFeeTransactionFee = 0.0
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
                templateCardId: String,
                openDateUtc: String?,
                statementCutUtc: String?,
                applicationStatus: String,
                welcomeOfferProgress: String?
            ): ImportResult = ImportResult.Success("profile_1")
        }

        val vm = CardCreateViewModel(templateSource, importer)
        vm.loadTemplates()
        assertEquals(1, vm.state.value.cards.size)
        assertEquals("example_bank", vm.state.value.selectedIssuerId)
        assertEquals("card_1", vm.state.value.selectedCardId)
        vm.updateSelectedTemplate("card_1")
        vm.updateOpenDate("01/01/2025")
        vm.updateApplicationStatus("approved")
        vm.save { }

        assertEquals(null, vm.state.value.error)
    }
}

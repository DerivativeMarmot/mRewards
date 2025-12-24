package com.example.rewardsrader.ui.cardcreate

import com.example.rewardsrader.config.CardConfig
import com.example.rewardsrader.config.CardConfigProvider
import com.example.rewardsrader.config.CardConfigResult
import com.example.rewardsrader.config.CardTemplate
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
        val config = CardConfig(
            schemaVersion = "1.0",
            dataVersion = "2025.12.23",
            cards = listOf(
                CardTemplate(
                    cardId = 1,
                    issuer = "Example Bank",
                    productName = "Example Cash Preferred",
                    network = "Visa",
                    annualFeeUsd = 95.0,
                    lastUpdated = "12/23/2025 10:00",
                    dataSource = "Test",
                    notes = null
                )
            ),
            benefits = emptyList()
        )
        val provider = object : CardConfigProvider {
            override suspend fun load(): CardConfigResult = CardConfigResult.Success(config)
        }
        val importer = object : CardTemplateImporterContract {
            override suspend fun importFromConfig(
                config: CardConfig,
                selectedCardId: Int,
                openDateUtc: String?,
                statementCutUtc: String?,
                applicationStatus: String,
                welcomeOfferProgress: String?
            ): ImportResult = ImportResult.Success(1L)
        }

        val vm = CardCreateViewModel(provider, importer)
        vm.loadTemplates()
        assertEquals(1, vm.state.value.templates.size)
        vm.updateSelectedTemplate(1)
        vm.updateOpenDate("01/01/2025 09:00")
        vm.updateApplicationStatus("approved")
        vm.save { }

        assertTrue(vm.state.value.success)
        assertEquals(null, vm.state.value.error)
    }
}

package com.example.rewardsrader.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rewardsrader.ui.carddetail.CardDetailScreen
import com.example.rewardsrader.ui.carddetail.CardDetailState
import com.example.rewardsrader.ui.carddetail.CardDetailUi
import com.example.rewardsrader.ui.carddetail.BenefitUi
import com.example.rewardsrader.ui.carddetail.ApplicationUi
import com.example.rewardsrader.ui.cardlist.CardListScreen
import com.example.rewardsrader.ui.cardlist.CardListUiState
import com.example.rewardsrader.ui.cardlist.CardSummaryUi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cardList_showsCards() {
        val state = MutableStateFlow(
            CardListUiState(
                isLoading = false,
                cards = listOf(
                    CardSummaryUi(
                        id = "1",
                        productName = "Example Cash Preferred",
                        issuer = "Example Bank",
                        status = "approved",
                        lastFour = "7861",
                        openDate = "01/01/2025",
                        cardFaceUrl = null
                    )
                )
            )
        )
        composeRule.setContent {
            CardListScreen(
                stateFlow = state,
                onSelectCard = {},
                onAddCard = {},
                onDeleteCard = {},
                onDuplicateCard = {},
                onResume = {},
                onSnackbarShown = {},
                onSync = {}
            )
        }

        composeRule.onNodeWithText("Example Cash Preferred ...7861").assertIsDisplayed()
        composeRule.onNodeWithText("Status: approved", substring = true).assertIsDisplayed()
    }

    @Test
    fun cardDetail_showsBenefitsAndApplication() {
        val detailState = MutableStateFlow(
            CardDetailState(
                isLoading = false,
                detail = CardDetailUi(
                    id = "1",
                    cardId = "template_1",
                    productName = "Example Cash Preferred",
                    issuer = "Example Bank",
                    network = "Visa",
                    cardFaceUrl = null,
                    nickname = null,
                    lastFour = null,
                    status = "approved",
                    annualFee = "$95.0",
                    openDate = "01/05/2025",
                    statementCut = "01/15/2025",
                    welcomeOfferProgress = "75%",
                    notes = "",
                    subSpending = null,
                    subDuration = null,
                    subDurationUnit = "months",
                    applications = listOf(
                        ApplicationUi(
                            status = "approved",
                            applicationDateUtc = "01/02/2025 09:00",
                            decisionDateUtc = "01/03/2025 09:00",
                            bureau = "Experian"
                        )
                    ),
                    benefits = listOf(
                        BenefitUi(
                            id = "benefit_1",
                            title = "Dining credit",
                            type = "credit",
                            amount = "$10.0 (cap $10.0)",
                            cadence = "monthly",
                            expiry = null,
                            notes = "Enroll each calendar year; credit resets monthly."
                        )
                    ),
                    offers = emptyList()
                ),
                error = null
            )
        )

        composeRule.setContent {
            CardDetailScreen(
                stateFlow = detailState,
                events = kotlinx.coroutines.flow.MutableSharedFlow(),
                onBack = {},
                onAddBenefit = { _, _ -> },
                onEditBenefit = {},
                onDeleteBenefit = {},
                onAddOffer = { _, _ -> },
                onEditOffer = {},
                onDeleteOffer = {},
                onUpdateNickname = {},
                onUpdateAnnualFee = {},
                onUpdateLastFour = {},
                onUpdateOpenDate = {},
                onUpdateStatementCut = {},
                onUpdateStatus = {},
                onUpdateNotes = {},
                onUpdateSubSpending = {},
                onUpdateSubDuration = { _, _ -> },
                onSelectCardFace = {},
                onDeleteCard = {}
            )
        }

        composeRule.onNodeWithText("Benefits").performClick()

        composeRule.onNodeWithText("Example Cash Preferred").assertIsDisplayed()
        composeRule.onNodeWithText("Benefits").assertIsDisplayed()
        composeRule.onNodeWithText("Dining credit", substring = true).assertIsDisplayed()
    }
}

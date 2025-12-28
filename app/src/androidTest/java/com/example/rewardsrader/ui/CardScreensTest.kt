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
                    CardSummaryUi(1, "Example Cash Preferred", "Example Bank", "approved")
                )
            )
        )
        composeRule.setContent {
            CardListScreen(
                stateFlow = state,
                onSelectCard = {},
                onAddCard = {},
                onDeleteCard = {},
                onUndoDelete = {},
                onSnackbarShown = {}
            )
        }

        composeRule.onNodeWithText("Example Cash Preferred").assertIsDisplayed()
        composeRule.onNodeWithText("Example Bank").assertIsDisplayed()
        composeRule.onNodeWithText("Status: approved").assertIsDisplayed()
    }

    @Test
    fun cardDetail_showsBenefitsAndApplication() {
        val detailState = MutableStateFlow(
            CardDetailState(
                isLoading = false,
                detail = CardDetailUi(
                    id = 1,
                    productName = "Example Cash Preferred",
                    issuer = "Example Bank",
                    network = "Visa",
                    status = "approved",
                    annualFee = "$95.0",
                    openDate = "01/05/2025 09:00",
                    statementCut = "01/15/2025 09:00",
                    welcomeOfferProgress = "75%",
                    notes = "",
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
                            id = 1L,
                            title = "Dining credit",
                            type = "credit",
                            amount = "$10.0 (cap $10.0)",
                            cadence = "monthly",
                            expiry = "12/31/2025 11:59",
                            notes = "Enroll each calendar year; credit resets monthly."
                        )
                    )
                ),
                error = null
            )
        )

        composeRule.setContent {
            CardDetailScreen(
                stateFlow = detailState,
                onBack = {},
                onAddBenefit = { _, _ -> },
                onEditBenefit = {},
                onDeleteBenefit = {},
                onUpdateNickname = {},
                onUpdateAnnualFee = {},
                onUpdateLastFour = {},
                onUpdateOpenDate = {},
                onUpdateStatementCut = {},
                onUpdateStatus = {},
                onUpdateNotes = {}
            )
        }

        composeRule.onNodeWithText("Benefits").performClick()

        composeRule.onNodeWithText("Example Cash Preferred").assertIsDisplayed()
        composeRule.onNodeWithText("Benefits").assertIsDisplayed()
        composeRule.onNodeWithText("credit").assertIsDisplayed()
        composeRule.onNodeWithText("Expiry: 12/31/2025 11:59").assertIsDisplayed()
    }
}

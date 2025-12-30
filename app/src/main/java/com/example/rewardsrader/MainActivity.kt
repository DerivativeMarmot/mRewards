package com.example.rewardsrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.rewardsrader.ui.carddetail.CardDetailScreen
import com.example.rewardsrader.ui.carddetail.CardDetailViewModel
import com.example.rewardsrader.ui.benefitcreate.BenefitCreateScreen
import com.example.rewardsrader.ui.benefitcreate.BenefitCreateViewModel
import com.example.rewardsrader.ui.cardcreate.CardCreateScreen
import com.example.rewardsrader.ui.cardcreate.CardCreateViewModel
import com.example.rewardsrader.ui.cardlist.CardListScreen
import com.example.rewardsrader.ui.cardlist.CardListViewModel
import com.example.rewardsrader.ui.theme.RewardsRaderTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)
        val cardListViewModel: CardListViewModel by viewModels {
            CardListViewModel.factory(appContainer.cardRepository)
        }
        val cardDetailViewModel: CardDetailViewModel by viewModels {
            CardDetailViewModel.factory(appContainer.cardRepository)
        }
        val benefitCreateViewModel: BenefitCreateViewModel by viewModels {
            BenefitCreateViewModel.factory(appContainer.cardRepository)
        }
        val cardCreateViewModel: CardCreateViewModel by viewModels {
            CardCreateViewModel.factory(appContainer.cardConfigProvider, appContainer.cardTemplateImporter)
        }
        setContent {
            RewardsRaderTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.List) }
                BackHandler(enabled = screen != Screen.List) {
                    screen = when (screen) {
                        Screen.List -> Screen.List
                        is Screen.Detail -> Screen.List
                        Screen.Create -> Screen.List
                        is Screen.Benefit -> {
                            val detailId = (screen as Screen.Benefit).cardId
                            Screen.Detail(detailId, initialTab = 2)
                        }
                    }
                }

                when (val currentScreen = screen) {
                    is Screen.List -> CardListScreen(
                        stateFlow = cardListViewModel.state,
                        onSelectCard = { id ->
                            cardDetailViewModel.load(id)
                            screen = Screen.Detail(id)
                        },
                        onAddCard = {
                            screen = Screen.Create
                        },
                        onDeleteCard = { id -> cardListViewModel.deleteCard(id) },
                        onUndoDelete = { cardListViewModel.undoDelete() },
                        onSnackbarShown = { cardListViewModel.snackbarShown() }
                    )
                    is Screen.Detail -> {
                        CardDetailScreen(
                            stateFlow = cardDetailViewModel.state,
                            events = cardDetailViewModel.events,
                            initialTab = currentScreen.initialTab,
                            onBack = { screen = Screen.List },
                            onAddBenefit = { id, productName ->
                                val issuer = cardDetailViewModel.state.value.detail?.issuer ?: ""
                                benefitCreateViewModel.init(id, productName, issuer)
                                screen = Screen.Benefit(id, BenefitMode.Add)
                            },
                            onEditBenefit = { benefitId ->
                                val detail = cardDetailViewModel.state.value.detail ?: return@CardDetailScreen
                                benefitCreateViewModel.startEdit(benefitId, detail.productName, detail.issuer)
                                screen = Screen.Benefit(detail.id, BenefitMode.Edit(benefitId))
                            },
                            onDeleteBenefit = { benefitId ->
                                cardDetailViewModel.deleteBenefit(benefitId)
                            },
                            onUpdateNickname = { cardDetailViewModel.updateNickname(it) },
                            onUpdateAnnualFee = { cardDetailViewModel.updateAnnualFee(it) },
                            onUpdateLastFour = { cardDetailViewModel.updateLastFour(it) },
                            onUpdateOpenDate = { cardDetailViewModel.updateOpenDate(it) },
                            onUpdateStatementCut = { cardDetailViewModel.updateStatementCut(it) },
                            onUpdateStatus = { cardDetailViewModel.updateStatus(it) },
                            onUpdateNotes = { cardDetailViewModel.updateNotes(it) },
                            onUpdateSubSpending = { cardDetailViewModel.updateSubSpending(it) },
                            onUpdateSubDuration = { value, unit -> cardDetailViewModel.updateSubDuration(value, unit) }
                        )
                    }
                    Screen.Create -> CardCreateScreen(
                        stateFlow = cardCreateViewModel.state,
                        onLoad = { cardCreateViewModel.loadTemplates() },
                        onIssuerSelected = { cardCreateViewModel.updateSelectedIssuer(it) },
                        onTemplateSelected = { cardCreateViewModel.updateSelectedTemplate(it) },
                        onOpenDateChange = { cardCreateViewModel.updateOpenDate(it) },
                        onStatementCutChange = { cardCreateViewModel.updateStatementCut(it) },
                        onApplicationStatusChange = { cardCreateViewModel.updateApplicationStatus(it) },
                        onWelcomeOfferChange = { cardCreateViewModel.updateWelcomeOffer(it) },
                        onSave = {
                            cardCreateViewModel.save {
                                cardListViewModel.loadCards()
                                screen = Screen.List
                            }
                        },
                        onBack = { screen = Screen.List }
                    )
                    is Screen.Benefit -> {
                        val mode = currentScreen
                        BenefitCreateScreen(
                            stateFlow = benefitCreateViewModel.state,
                            onBack = {
                                screen = Screen.Detail(mode.cardId, initialTab = 2)
                            },
                            onSave = {
                                benefitCreateViewModel.save { savedBenefit ->
                                    cardDetailViewModel.upsertBenefit(savedBenefit)
                                    cardDetailViewModel.notifyBenefitSaved(mode.mode is BenefitMode.Edit)
                                    screen = Screen.Detail(mode.cardId, initialTab = 2)
                                }
                            },
                            onTypeChange = { benefitCreateViewModel.setType(it) },
                            onAmountChange = { benefitCreateViewModel.setAmount(it) },
                            onCapChange = { benefitCreateViewModel.setCap(it) },
                            onCadenceChange = { benefitCreateViewModel.setCadence(it) },
                            onEffectiveDateChange = { benefitCreateViewModel.setEffectiveDate(it) },
                            onExpiryDateChange = { benefitCreateViewModel.setExpiryDate(it) },
                            onTitleChange = { benefitCreateViewModel.setTitle(it) },
                            onNotesChange = { benefitCreateViewModel.setNotes(it) },
                            onToggleCategory = { benefitCreateViewModel.toggleCategory(it) },
                            onCustomCategoryChange = { benefitCreateViewModel.setCustomCategory(it) },
                            onAddCustomCategory = { benefitCreateViewModel.addCustomCategory() },
                            onRemoveCustomCategory = { benefitCreateViewModel.removeCustomCategory(it) },
                            onTransactionAmountChange = { benefitCreateViewModel.setTransactionAmount(it) },
                            onTransactionDateChange = { benefitCreateViewModel.setTransactionDate(it) },
                            onAddTransaction = { benefitCreateViewModel.saveTransaction() },
                            onStartTransaction = { benefitCreateViewModel.startNewTransaction() },
                            onEditTransaction = { benefitCreateViewModel.startEditTransaction(it) },
                            onDeleteTransaction = { benefitCreateViewModel.deleteTransaction(it) },
                            onProgressChange = { benefitCreateViewModel.setProgress(it) }
                        )
                    }
                }
            }
        }
    }
}

private sealed class Screen {
    data object List : Screen()
    data class Detail(val id: Long, val initialTab: Int = 0) : Screen()
    data object Create : Screen()
    data class Benefit(val cardId: Long, val mode: BenefitMode) : Screen()
}

private sealed class BenefitMode {
    data object Add : BenefitMode()
    data class Edit(val benefitId: Long) : BenefitMode()
}

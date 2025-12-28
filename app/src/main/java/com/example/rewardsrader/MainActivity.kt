package com.example.rewardsrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
        @OptIn(ExperimentalMaterial3Api::class)
        setContent {
            RewardsRaderTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.List) }
                var benefitSheetMode by remember { mutableStateOf<BenefitSheetMode?>(null) }
                val addBenefitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val sheetScope = rememberCoroutineScope()

                val closeBenefitSheet: () -> Unit = {
                    sheetScope.launch {
                        addBenefitSheetState.hide()
                        benefitSheetMode = null
                    }
                }

                BackHandler(enabled = benefitSheetMode != null || screen != Screen.List) {
                    if (benefitSheetMode != null) {
                        closeBenefitSheet()
                    } else {
                        screen = Screen.List
                    }
                }

                benefitSheetMode?.let { mode ->
                    ModalBottomSheet(
                        onDismissRequest = { closeBenefitSheet() },
                        sheetState = addBenefitSheetState
                    ) {
                        val cardId = when (mode) {
                            is BenefitSheetMode.Add -> mode.cardId
                            is BenefitSheetMode.Edit -> mode.cardId
                        }
                        val productName = when (mode) {
                            is BenefitSheetMode.Add -> mode.productName
                            is BenefitSheetMode.Edit -> mode.productName
                        }
                        val issuer = when (mode) {
                            is BenefitSheetMode.Add -> mode.issuer
                            is BenefitSheetMode.Edit -> mode.issuer
                        }
                        BenefitCreateScreen(
                            stateFlow = benefitCreateViewModel.state,
                            onInit = {
                                when (mode) {
                                    is BenefitSheetMode.Add -> benefitCreateViewModel.init(cardId, productName, issuer)
                                    is BenefitSheetMode.Edit -> benefitCreateViewModel.startEdit(mode.benefitId, productName, issuer)
                                }
                            },
                            onBack = { closeBenefitSheet() },
                            onSave = {
                                benefitCreateViewModel.save { savedBenefit ->
                                    cardDetailViewModel.upsertBenefit(savedBenefit)
                                    cardDetailViewModel.notifyBenefitSaved(mode is BenefitSheetMode.Edit)
                                    closeBenefitSheet()
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
                            onRemoveCustomCategory = { benefitCreateViewModel.removeCustomCategory(it) }
                        )
                    }
                }

                when (screen) {
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
                            onBack = { screen = Screen.List },
                            onAddBenefit = { id, productName ->
                                val issuer = cardDetailViewModel.state.value.detail?.issuer ?: ""
                                benefitSheetMode = BenefitSheetMode.Add(id, productName, issuer)
                            },
                            onEditBenefit = { benefitId ->
                                val detail = cardDetailViewModel.state.value.detail ?: return@CardDetailScreen
                                benefitSheetMode = BenefitSheetMode.Edit(detail.id, benefitId, detail.productName, detail.issuer)
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
                            onUpdateNotes = { cardDetailViewModel.updateNotes(it) }
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
                }
            }
        }
    }
}

private sealed class Screen {
    data object List : Screen()
    data class Detail(val id: Long) : Screen()
    data object Create : Screen()
}

private sealed class BenefitSheetMode {
    data class Add(val cardId: Long, val productName: String, val issuer: String) : BenefitSheetMode()
    data class Edit(val cardId: Long, val benefitId: Long, val productName: String, val issuer: String) : BenefitSheetMode()
}

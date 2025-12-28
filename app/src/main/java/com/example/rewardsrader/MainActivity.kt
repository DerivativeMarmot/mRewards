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
                var showAddBenefitSheet by remember { mutableStateOf(false) }
                var addBenefitCardId by remember { mutableStateOf<Long?>(null) }
                var addBenefitProductName by remember { mutableStateOf("") }
                var addBenefitIssuer by remember { mutableStateOf("") }
                val addBenefitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val sheetScope = rememberCoroutineScope()

                val closeAddBenefitSheet: () -> Unit = {
                    sheetScope.launch {
                        addBenefitSheetState.hide()
                        showAddBenefitSheet = false
                        addBenefitCardId = null
                        addBenefitProductName = ""
                        addBenefitIssuer = ""
                    }
                }

                BackHandler(enabled = showAddBenefitSheet || screen != Screen.List) {
                    if (showAddBenefitSheet) {
                        closeAddBenefitSheet()
                    } else {
                        screen = Screen.List
                    }
                }

                if (showAddBenefitSheet && addBenefitCardId != null) {
                    ModalBottomSheet(
                        onDismissRequest = { closeAddBenefitSheet() },
                        sheetState = addBenefitSheetState
                    ) {
                        val cardId = addBenefitCardId ?: return@ModalBottomSheet
                        BenefitCreateScreen(
                            stateFlow = benefitCreateViewModel.state,
                            onInit = { benefitCreateViewModel.init(cardId, addBenefitProductName, addBenefitIssuer) },
                            onBack = { closeAddBenefitSheet() },
                            onSave = {
                                benefitCreateViewModel.save {
                                    cardDetailViewModel.load(cardId)
                                    closeAddBenefitSheet()
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
                            onBack = { screen = Screen.List },
                            onEdit = {},
                            onAddBenefit = { id, productName ->
                                val issuer = cardDetailViewModel.state.value.detail?.issuer ?: ""
                                addBenefitCardId = id
                                addBenefitProductName = productName
                                addBenefitIssuer = issuer
                                showAddBenefitSheet = true
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

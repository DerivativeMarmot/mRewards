package com.example.rewardsrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
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
                        is Screen.Detail -> Screen.List
                        Screen.Create -> Screen.List
                        is Screen.AddBenefit -> {
                            val id = (screen as Screen.AddBenefit).cardId
                            Screen.Detail(id)
                        }
                        Screen.List -> Screen.List
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
                                benefitCreateViewModel.init(id, productName, issuer)
                                screen = Screen.AddBenefit(id, productName, issuer)
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
                    is Screen.AddBenefit -> {
                        val addId = (screen as Screen.AddBenefit).cardId
                        val productName = (screen as Screen.AddBenefit).productName
                        val issuer = (screen as Screen.AddBenefit).issuer
                        BenefitCreateScreen(
                            stateFlow = benefitCreateViewModel.state,
                            onInit = { benefitCreateViewModel.init(addId, productName, issuer) },
                            onBack = { screen = Screen.Detail(addId) },
                            onSave = {
                                benefitCreateViewModel.save {
                                    cardDetailViewModel.load(addId)
                                    screen = Screen.Detail(addId)
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
            }
        }
    }
}

private sealed class Screen {
    data object List : Screen()
    data class Detail(val id: Long) : Screen()
    data object Create : Screen()
    data class AddBenefit(val cardId: Long, val productName: String, val issuer: String) : Screen()
}

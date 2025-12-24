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
import com.example.rewardsrader.ui.cardedit.CardEditScreen
import com.example.rewardsrader.ui.cardedit.CardEditViewModel
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
        val cardEditViewModel: com.example.rewardsrader.ui.cardedit.CardEditViewModel by viewModels {
            com.example.rewardsrader.ui.cardedit.CardEditViewModel.factory(appContainer.cardRepository)
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
                        is Screen.Edit -> {
                            val id = (screen as Screen.Edit).id
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
                            onEdit = { id ->
                                cardEditViewModel.load(id)
                                screen = Screen.Edit(id)
                            }
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
                    is Screen.Edit -> {
                        val editId = (screen as Screen.Edit).id
                        CardEditScreen(
                            stateFlow = cardEditViewModel.state,
                            onLoad = { id -> cardEditViewModel.load(id) },
                            cardId = editId,
                            onSave = {
                                cardEditViewModel.save {
                                    cardDetailViewModel.load(editId)
                                    screen = Screen.Detail(editId)
                                }
                            },
                            onBack = { screen = Screen.Detail(editId) },
                            onNicknameChange = { value -> cardEditViewModel.updateNickname(value) },
                            onAnnualFeeChange = { value -> cardEditViewModel.updateAnnualFee(value) },
                            onLastFourChange = { value -> cardEditViewModel.updateLastFour(value) },
                            onOpenDateChange = { value -> cardEditViewModel.updateOpenDate(value) },
                            onStatementDateChange = { value -> cardEditViewModel.updateStatementCut(value) },
                            onStatusChange = { value -> cardEditViewModel.updateStatus(value) },
                            onWelcomeOfferChange = { value -> cardEditViewModel.updateWelcomeOffer(value) },
                            onNotesChange = { value -> cardEditViewModel.updateNotes(value) }
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
    data class Edit(val id: Long) : Screen()
}

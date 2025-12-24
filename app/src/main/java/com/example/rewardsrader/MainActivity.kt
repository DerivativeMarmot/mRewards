package com.example.rewardsrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.rewardsrader.ui.carddetail.CardDetailScreen
import com.example.rewardsrader.ui.carddetail.CardDetailViewModel
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
        val cardCreateViewModel: CardCreateViewModel by viewModels {
            CardCreateViewModel.factory(appContainer.cardConfigProvider, appContainer.cardTemplateImporter)
        }
        setContent {
            RewardsRaderTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.List) }

                when (screen) {
                    is Screen.List -> CardListScreen(
                        stateFlow = cardListViewModel.state,
                        onSelectCard = { id ->
                            cardDetailViewModel.load(id)
                            screen = Screen.Detail(id)
                        },
                        onAddCard = {
                            screen = Screen.Create
                        }
                    )
                    is Screen.Detail -> CardDetailScreen(
                        stateFlow = cardDetailViewModel.state,
                        onBack = { screen = Screen.List }
                    )
                    Screen.Create -> CardCreateScreen(
                        stateFlow = cardCreateViewModel.state,
                        onLoad = { cardCreateViewModel.loadTemplates() },
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

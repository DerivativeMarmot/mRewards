package com.example.rewardsrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.rewardsrader.ui.carddetail.CardDetailScreen
import com.example.rewardsrader.ui.carddetail.CardDetailViewModel
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
        setContent {
            RewardsRaderTheme {
                var selectedCardId by remember { mutableStateOf<Long?>(null) }

                if (selectedCardId == null) {
                    CardListScreen(
                        stateFlow = cardListViewModel.state,
                        onSelectCard = { id ->
                            selectedCardId = id
                            cardDetailViewModel.load(id)
                        }
                    )
                } else {
                    CardDetailScreen(
                        stateFlow = cardDetailViewModel.state,
                        onBack = { selectedCardId = null }
                    )
                }
            }
        }
    }
}

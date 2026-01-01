package com.example.rewardsrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.compose.PredictiveBackHandler
import android.app.Activity
import androidx.activity.BackEventCompat
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rewardsrader.ui.carddetail.CardDetailScreen
import com.example.rewardsrader.ui.carddetail.CardDetailViewModel
import com.example.rewardsrader.ui.benefitcreate.BenefitCreateScreen
import com.example.rewardsrader.ui.benefitcreate.BenefitCreateViewModel
import com.example.rewardsrader.ui.cardcreate.CardCreateScreen
import com.example.rewardsrader.ui.cardcreate.CardCreateViewModel
import com.example.rewardsrader.ui.cardlist.CardListScreen
import com.example.rewardsrader.ui.cardlist.CardListViewModel
import com.example.rewardsrader.ui.theme.RewardsRaderTheme
import kotlinx.coroutines.CancellationException

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RewardsRaderTheme {
                NavigationApp()
            }
        }
    }


    @Composable
    fun NavigationApp(navController: NavHostController = rememberNavController()) {

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


        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "list",
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it })
            },
            exitTransition = {
                slideOutHorizontally (targetOffsetX = { -it })
            },
            popEnterTransition = {
                scaleIn(initialScale = 1.2f)
            },
            popExitTransition = {
                scaleOut(targetScale = 0.8f) + fadeOut()
            }
        )
        {
            composable("list") {
                CardListScreen(
                    stateFlow = cardListViewModel.state,
                    onSelectCard = { id ->
                        navController.navigate("detail/$id")
                    },
                    onAddCard = {
                        navController.navigate("create")
                    },
                    onDeleteCard = { id -> cardListViewModel.deleteCard(id) },
                    onUndoDelete = { cardListViewModel.undoDelete() },
                    onSnackbarShown = { cardListViewModel.snackbarShown() }
                )
            }

            composable(
                "detail/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getLong("cardId") ?: return@composable
                cardDetailViewModel.load(cardId)

                CardDetailScreen(
                    stateFlow = cardDetailViewModel.state,
                    events = cardDetailViewModel.events,
                    initialTab = 0,
                    onBack = { navController.popBackStack() },
                    onAddBenefit = { id, productName ->
                        val issuer = cardDetailViewModel.state.value.detail?.issuer ?: ""
                        benefitCreateViewModel.init(id, productName, issuer)
                        navController.navigate("benefit/$id/add")
                    },
                    onEditBenefit = { benefitId ->
                        val detail = cardDetailViewModel.state.value.detail ?: return@CardDetailScreen
                        benefitCreateViewModel.startEdit(benefitId, detail.productName, detail.issuer)
                        navController.navigate("benefit/${detail.id}/edit/$benefitId")
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

            composable("create") {
                CardCreateScreen(
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
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                "benefit/{cardId}/add",
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) { backStackEntry ->

                BenefitCreateScreen(
                    stateFlow = benefitCreateViewModel.state,
                    onBack = { navController.popBackStack() },
                    onSave = {
                        benefitCreateViewModel.save { savedBenefit ->
                            cardDetailViewModel.upsertBenefit(savedBenefit)
                            cardDetailViewModel.notifyBenefitSaved(false)
                            navController.popBackStack()
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

            composable(
                "benefit/{cardId}/edit/{benefitId}",
                arguments = listOf(
                    navArgument("cardId") { type = NavType.LongType },
                    navArgument("benefitId") { type = NavType.LongType }
                )
            ) { backStackEntry ->

                BenefitCreateScreen(
                    stateFlow = benefitCreateViewModel.state,
                    onBack = { navController.popBackStack() },
                    onSave = {
                        benefitCreateViewModel.save { savedBenefit ->
                            cardDetailViewModel.upsertBenefit(savedBenefit)
                            cardDetailViewModel.notifyBenefitSaved(true)
                            navController.popBackStack()
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

package com.example.rewardsrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rewardsrader.ui.benefitcreate.BenefitCreateScreen
import com.example.rewardsrader.ui.benefitcreate.BenefitCreateViewModel
import com.example.rewardsrader.ui.cardcreate.CardCreateScreen
import com.example.rewardsrader.ui.cardcreate.CardCreateViewModel
import com.example.rewardsrader.ui.carddetail.CardDetailScreen
import com.example.rewardsrader.ui.carddetail.CardDetailViewModel
import com.example.rewardsrader.ui.cardlist.CardListScreen
import com.example.rewardsrader.ui.cardlist.CardListViewModel
import com.example.rewardsrader.ui.offercreate.OfferCreateScreen
import com.example.rewardsrader.ui.offercreate.OfferCreateViewModel
import com.example.rewardsrader.ui.theme.RewardsRaderTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        val offerCreateViewModel: OfferCreateViewModel by viewModels {
            OfferCreateViewModel.factory(appContainer.cardRepository)
        }
        val cardCreateViewModel: CardCreateViewModel by viewModels {
            CardCreateViewModel.factory(appContainer.cardConfigProvider, appContainer.cardTemplateImporter)
        }

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
        ) {
            composable("list") {
                CardListScreen(
                    stateFlow = cardListViewModel.state,
                    onSelectCard = { id -> navController.navigate("detail/$id") },
                    onAddCard = { navController.navigate("create") },
                    onDeleteCard = { id -> cardListViewModel.deleteCard(id) },
                    onSnackbarShown = { cardListViewModel.snackbarShown() }
                )
            }

            composable(
                "detail/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
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
                    onEditBenefit = { profileCardId, benefitId ->
                        val detail = cardDetailViewModel.state.value.detail
                        if (detail != null) {
                            benefitCreateViewModel.startEdit(profileCardId, benefitId, detail.productName, detail.issuer)
                        }
                        navController.navigate("benefit/$profileCardId/edit/$benefitId")
                    },
                    onDeleteBenefit = { benefitId -> cardDetailViewModel.deleteBenefit(benefitId) },
                    onAddOffer = { id, productName ->
                        offerCreateViewModel.init(id, productName)
                        navController.navigate("offer/$id/add")
                    },
                    onEditOffer = { offerId ->
                        val detail = cardDetailViewModel.state.value.detail ?: return@CardDetailScreen
                        offerCreateViewModel.startEdit(offerId, detail.productName)
                        navController.navigate("offer/${detail.id}/edit/$offerId")
                    },
                    onDeleteOffer = { offerId -> cardDetailViewModel.deleteOffer(offerId) },
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
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
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
                    navArgument("cardId") { type = NavType.StringType },
                    navArgument("benefitId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                val benefitId = backStackEntry.arguments?.getString("benefitId") ?: return@composable
                val detail = cardDetailViewModel.state.value.detail
                if (detail != null) {
                    benefitCreateViewModel.startEdit(cardId, benefitId, detail.productName, detail.issuer)
                } else {
                    benefitCreateViewModel.init(cardId, "", "")
                }
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

            composable(
                "offer/{cardId}/add",
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) {
                OfferCreateScreen(
                    stateFlow = offerCreateViewModel.state,
                    onBack = { navController.popBackStack() },
                    onSave = {
                        offerCreateViewModel.save { saved ->
                            cardDetailViewModel.upsertOffer(saved)
                            cardDetailViewModel.notifyOfferSaved(false)
                            navController.popBackStack()
                        }
                    },
                    onTitleChange = { offerCreateViewModel.setTitle(it) },
                    onNoteChange = { offerCreateViewModel.setNote(it) },
                    onTypeChange = { offerCreateViewModel.setType(it) },
                    onMultiplierChange = { offerCreateViewModel.setMultiplier(it) },
                    onStatusChange = { offerCreateViewModel.setStatus(it) },
                    onMinSpendChange = { offerCreateViewModel.setMinSpend(it) },
                    onMaxCashBackChange = { offerCreateViewModel.setMaxCashBack(it) },
                    onStartDateChange = { offerCreateViewModel.setStartDate(it) },
                    onEndDateChange = { offerCreateViewModel.setEndDate(it) }
                )
            }

            composable(
                "offer/{cardId}/edit/{offerId}",
                arguments = listOf(
                    navArgument("cardId") { type = NavType.StringType },
                    navArgument("offerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId") ?: return@composable
                val detail = cardDetailViewModel.state.value.detail
                if (detail != null) {
                    offerCreateViewModel.startEdit(offerId, detail.productName)
                }
                OfferCreateScreen(
                    stateFlow = offerCreateViewModel.state,
                    onBack = { navController.popBackStack() },
                    onSave = {
                        offerCreateViewModel.save { saved ->
                            cardDetailViewModel.upsertOffer(saved)
                            cardDetailViewModel.notifyOfferSaved(true)
                            navController.popBackStack()
                        }
                    },
                    onTitleChange = { offerCreateViewModel.setTitle(it) },
                    onNoteChange = { offerCreateViewModel.setNote(it) },
                    onTypeChange = { offerCreateViewModel.setType(it) },
                    onMultiplierChange = { offerCreateViewModel.setMultiplier(it) },
                    onStatusChange = { offerCreateViewModel.setStatus(it) },
                    onMinSpendChange = { offerCreateViewModel.setMinSpend(it) },
                    onMaxCashBackChange = { offerCreateViewModel.setMaxCashBack(it) },
                    onStartDateChange = { offerCreateViewModel.setStartDate(it) },
                    onEndDateChange = { offerCreateViewModel.setEndDate(it) }
                )
            }
        }
    }
}

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.example.rewardsrader.ui.tracker.TrackerEditScreen
import com.example.rewardsrader.ui.tracker.TrackerEditViewModel
import com.example.rewardsrader.ui.tracker.TrackerScreen
import com.example.rewardsrader.ui.tracker.TrackerViewModel

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
            CardListViewModel.factory(appContainer.cardRepository, appContainer.firestoreSyncer)
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
            CardCreateViewModel.factory(
                appContainer.cardRepository,
                appContainer.cardTemplateImporter,
                com.example.rewardsrader.ui.cardcreate.CardSyncer {
                    appContainer.firestoreSyncer.syncIssuersAndCards()
                }
            )
        }
        val trackerViewModel: TrackerViewModel by viewModels {
            TrackerViewModel.factory(appContainer.cardRepository)
        }
        val trackerEditViewModel: TrackerEditViewModel by viewModels {
            TrackerEditViewModel.factory(appContainer.cardRepository)
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val showBottomBar = currentRoute == "list" || currentRoute == "tracker"
        val snackbarHostState = remember { SnackbarHostState() }
        val isSnackbarVisible = snackbarHostState.currentSnackbarData != null
        val cardListState by cardListViewModel.state.collectAsState()

        LaunchedEffect(currentRoute, cardListState.snackbarMessage) {
            val message = cardListState.snackbarMessage
            if (currentRoute == "list" && !message.isNullOrBlank()) {
                snackbarHostState.showSnackbar(message = message)
                cardListViewModel.snackbarShown()
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "list",
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
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
                        onDuplicateCard = { id -> cardListViewModel.duplicateCard(id) },
                        onResume = { cardListViewModel.loadCards(showLoading = false) },
                        isSnackbarVisible = isSnackbarVisible
                    )
                }

                composable("tracker") {
                    TrackerScreen(
                        stateFlow = trackerViewModel.state,
                        onLoad = { trackerViewModel.loadTrackers() },
                        onResume = { trackerViewModel.loadTrackers(showLoading = false) },
                        onSelectTracker = { id -> navController.navigate("tracker/$id") },
                        onFilterChange = { trackerViewModel.setFilter(it) }
                    )
                }

                composable(
                    "tracker/{trackerId}",
                    arguments = listOf(navArgument("trackerId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getString("trackerId") ?: return@composable
                    LaunchedEffect(trackerId) {
                        trackerEditViewModel.load(trackerId)
                    }
                    TrackerEditScreen(
                        stateFlow = trackerEditViewModel.state,
                        onBack = { navController.popBackStack() },
                        onEntryAmountChange = { trackerEditViewModel.setEntryAmount(it) },
                        onEntryDateChange = { trackerEditViewModel.setEntryDate(it) },
                        onEntryNotesChange = { trackerEditViewModel.setEntryNotes(it) },
                        onAddTransaction = { trackerEditViewModel.addTransaction() },
                        onDeleteTransaction = { trackerEditViewModel.deleteTransaction(it) },
                        onToggleOfferComplete = { trackerEditViewModel.setOfferCompleted(it) },
                        onOfferNotesChange = { trackerEditViewModel.setOfferNotes(it) },
                        onSaveOffer = {
                            trackerEditViewModel.saveOfferTracker {
                                navController.popBackStack()
                            }
                        }
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
                        onUpdateSubDuration = { value, unit -> cardDetailViewModel.updateSubDuration(value, unit) },
                        onSelectCardFace = { cardDetailViewModel.selectCardFace(it) },
                        onDeleteCard = {
                            cardDetailViewModel.deleteCard {
                                cardListViewModel.loadCards(showLoading = false)
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable("create") {
                    CardCreateScreen(
                        stateFlow = cardCreateViewModel.state,
                        events = cardCreateViewModel.events,
                        onLoad = { cardCreateViewModel.loadTemplates() },
                        onSync = { cardCreateViewModel.syncFromCloud() },
                        onQueryChange = { cardCreateViewModel.updateQuery(it) },
                        onSelectSort = { cardCreateViewModel.setSortMode(it) },
                        onToggleIssuer = { cardCreateViewModel.toggleIssuerFilter(it) },
                        onToggleNetwork = { cardCreateViewModel.toggleNetworkFilter(it) },
                        onToggleSegment = { cardCreateViewModel.toggleSegmentFilter(it) },
                        onToggleInstrument = { cardCreateViewModel.togglePaymentInstrument(it) },
                        onToggleBenefitType = { cardCreateViewModel.toggleBenefitType(it) },
                        onToggleBenefitCategory = { cardCreateViewModel.toggleBenefitCategory(it) },
                        onUpdateFeeRange = { cardCreateViewModel.updateFeeRange(it) },
                        onToggleNoFee = { cardCreateViewModel.toggleNoAnnualFeeOnly() },
                        onResetFilters = { cardCreateViewModel.resetFilters() },
                        onSelectCard = { cardCreateViewModel.createCard(it) },
                        onBack = { navController.popBackStack() },
                        onCreated = {
                            cardListViewModel.loadCards(showLoading = false)
                            cardListViewModel.notifyCardAdded()
                            navController.popBackStack()
                        }
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
                        onRemoveCustomCategory = { benefitCreateViewModel.removeCustomCategory(it) }
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
                        onRemoveCustomCategory = { benefitCreateViewModel.removeCustomCategory(it) }
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
                        onMinSpendChange = { offerCreateViewModel.setMinSpend(it) },
                        onMaxCashBackChange = { offerCreateViewModel.setMaxCashBack(it) },
                        onStartDateChange = { offerCreateViewModel.setStartDate(it) },
                        onEndDateChange = { offerCreateViewModel.setEndDate(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "list",
            onClick = { onNavigate("list") },
            icon = { Icon(Icons.Default.List, contentDescription = "Cards") },
            label = { Text("Cards") }
        )
        NavigationBarItem(
            selected = currentRoute == "tracker",
            onClick = { onNavigate("tracker") },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Tracker") },
            label = { Text("Tracker") }
        )
    }
}

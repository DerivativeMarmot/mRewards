package com.example.rewardsrader.ui.cardlist

import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    stateFlow: StateFlow<CardListUiState>,
    onSelectCard: (String) -> Unit,
    onAddCard: () -> Unit,
    onDeleteCard: (String) -> Unit,
    onSnackbarShown: () -> Unit,
    onSync: () -> Unit
) {
    val state by stateFlow.collectAsState()
    val error = state.error
    val snackbarHostState = remember { SnackbarHostState() }
    val isSnackbarVisible = snackbarHostState.currentSnackbarData != null
    val fabBottomPadding by animateDpAsState(
        targetValue = if (isSnackbarVisible) 80.dp else 16.dp,
        label = "cardListFabBottomPadding"
    )
    val listBottomPadding: Dp = if (isSnackbarVisible) 144.dp else 96.dp

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = message)
        onSnackbarShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cards") },
                actions = {
                    IconButton(onClick = onSync) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync cards")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> LoadingMessage(modifier = Modifier.fillMaxSize())
                error != null -> ErrorMessage(error, Modifier.fillMaxSize())
                else -> CardListContent(
                    cards = state.cards,
                    onSelectCard = onSelectCard,
                    onDeleteCard = onDeleteCard,
                    listBottomPadding = listBottomPadding,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AddCardFab(
                onAddCard = onAddCard,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = fabBottomPadding)
            )
        }
    }
}

@Composable
private fun CardListContent(
    cards: List<CardSummaryUi>,
    onSelectCard: (String) -> Unit,
    onDeleteCard: (String) -> Unit,
    listBottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    if (cards.isEmpty()) {
        EmptyMessage(modifier = modifier)
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = listBottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards) { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onSelectCard(card.id) }
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(card.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(card.issuer, style = MaterialTheme.typography.bodyMedium)
                                Text("Status: ${card.status}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onDeleteCard(card.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete card")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Loading...")
    }
}

@Composable
private fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error: $message", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun EmptyMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No cards yet. Add one from a template.")
    }
}

@Composable
private fun AddCardFab(
    onAddCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onAddCard,
        modifier = modifier,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Add card") }
    )
}

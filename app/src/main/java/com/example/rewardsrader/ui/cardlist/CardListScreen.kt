package com.example.rewardsrader.ui.cardlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    stateFlow: StateFlow<CardListUiState>,
    onSelectCard: (Long) -> Unit,
    onAddCard: () -> Unit,
    onDeleteCard: (Long) -> Unit,
    onUndoDelete: () -> Unit,
    onSnackbarShown: () -> Unit
    ) {
    val state by stateFlow.collectAsState()
    val error = state.error
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage, state.showUndo) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = if (state.showUndo) "Undo" else null
        )
        if (result == SnackbarResult.ActionPerformed && state.showUndo) {
            onUndoDelete()
        }
        onSnackbarShown()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cards") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> LoadingMessage(modifier = Modifier.padding(padding))
            error != null -> ErrorMessage(error, Modifier.padding(padding))
            state.cards.isEmpty() -> EmptyMessage(modifier = Modifier.padding(padding))
            else -> CardListContent(
                cards = state.cards,
                onSelectCard = onSelectCard,
                onDeleteCard = onDeleteCard,
                modifier = Modifier.padding(padding)
            )
        }
        AddCardButton(onAddCard = onAddCard, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun CardListContent(
    cards: List<CardSummaryUi>,
    onSelectCard: (Long) -> Unit,
    onDeleteCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
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

@Composable
private fun LoadingMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Loading…")
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
private fun AddCardButton(onAddCard: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            onClick = onAddCard,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("Add card")
        }
    }
}

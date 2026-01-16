package com.example.rewardsrader.ui.cardlist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    stateFlow: StateFlow<CardListUiState>,
    onSelectCard: (String) -> Unit,
    onAddCard: () -> Unit,
    onDeleteCard: (String) -> Unit,
    onResume: () -> Unit,
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
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, onResume) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    CardListItem(
                        card = card,
                        onSelectCard = onSelectCard,
                        onDeleteCard = onDeleteCard
                    )
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
private fun CardListItem(
    card: CardSummaryUi,
    onSelectCard: (String) -> Unit,
    onDeleteCard: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectCard(card.id) }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val fullHeight = maxWidth / 1.6f
            val visibleHeight = fullHeight * 0.33f
            Column(modifier = Modifier.fillMaxWidth()) {
                CardFaceStrip(
                    cardFaceUrl = card.cardFaceUrl,
                    visibleHeight = visibleHeight,
                    fullHeight = fullHeight
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildCardTitle(card.productName, card.lastFour),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onDeleteCard(card.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete card")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status: ${card.status}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = buildApprovalDurationLabel(card.openDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardFaceStrip(
    cardFaceUrl: String?,
    visibleHeight: Dp,
    fullHeight: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(visibleHeight)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .clipToBounds()
    ) {
        if (!cardFaceUrl.isNullOrBlank()) {
            AsyncImage(
                model = cardFaceUrl,
                contentDescription = "Card face",
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fullHeight)
            )
        } else {
            Text(
                text = "No card face",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun buildCardTitle(productName: String, lastFour: String?): String {
    val trimmedLastFour = lastFour?.trim().orEmpty()
    return if (trimmedLastFour.isBlank()) {
        productName
    } else {
        "${productName} (...${trimmedLastFour})"
    }
}

private fun buildApprovalDurationLabel(openDate: String?): String {
    if (openDate.isNullOrBlank()) return "Approval date unknown"
    val startDate = runCatching {
        LocalDate.parse(openDate, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
    }.getOrNull() ?: return "Approval date unknown"
    val today = LocalDate.now()
    val period = if (startDate.isAfter(today)) Period.ZERO else Period.between(startDate, today)
    return "${formatUnit(period.years, "y")} ${formatUnit(period.months, "mo")} ${formatUnit(period.days, "d")}"
}

private fun formatUnit(value: Int, unit: String): String {
    if (value == 0) return ""
    return "${value}${unit}"
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

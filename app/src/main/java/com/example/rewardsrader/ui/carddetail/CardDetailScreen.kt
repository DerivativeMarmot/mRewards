package com.example.rewardsrader.ui.carddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    stateFlow: StateFlow<CardDetailState>,
    onBack: () -> Unit
) {
    val state by stateFlow.collectAsState()
    val detail = state.detail
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.productName ?: "Card Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> DetailMessage("Loading…", Modifier.padding(padding))
            state.error != null -> DetailMessage("Error: ${state.error}", Modifier.padding(padding))
            detail == null -> DetailMessage("No details available.", Modifier.padding(padding))
            else -> DetailContent(
                detail = detail,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun DetailContent(detail: CardDetailUi, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(detail.productName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("${detail.issuer} • ${detail.network}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${detail.status}", style = MaterialTheme.typography.bodySmall)
            Text("Annual fee: ${detail.annualFee}", style = MaterialTheme.typography.bodySmall)
            detail.openDate?.let { Text("Open date: $it", style = MaterialTheme.typography.bodySmall) }
            detail.statementCut?.let { Text("Statement cut: $it", style = MaterialTheme.typography.bodySmall) }
            detail.welcomeOfferProgress?.let { Text("Welcome offer: $it", style = MaterialTheme.typography.bodySmall) }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Application timeline", style = MaterialTheme.typography.titleMedium)
        }
        items(detail.applications) { app ->
            Card(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Status: ${app.status}", fontWeight = FontWeight.SemiBold)
                    app.applicationDateUtc?.let { Text("Applied: $it") }
                    app.decisionDateUtc?.let { Text("Decision: $it") }
                    app.bureau?.let { Text("Bureau: $it") }
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Benefits", style = MaterialTheme.typography.titleMedium)
        }
        items(detail.benefits) { benefit ->
            Card(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(benefit.type, fontWeight = FontWeight.SemiBold)
                    if (benefit.amount.isNotBlank()) Text(benefit.amount)
                    Text("Cadence: ${benefit.cadence}")
                    benefit.expiry?.let { Text("Expiry: $it") }
                    benefit.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
private fun DetailMessage(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message)
    }
}

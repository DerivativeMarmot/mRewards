package com.example.rewardsrader.ui.carddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    stateFlow: StateFlow<CardDetailState>,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onAddBenefit: (Long, String) -> Unit,
    onDeleteBenefit: (Long) -> Unit
) {
    val state by stateFlow.collectAsState()
    val detail = state.detail
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    detail?.let { d ->
                        IconButton(onClick = { onEdit(d.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
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
                onAddBenefit = { onAddBenefit(detail.id, detail.productName) },
                onDeleteBenefit = onDeleteBenefit,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun BenefitsHeader(onAddBenefit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Benefits", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onAddBenefit) {
            Icon(Icons.Default.Add, contentDescription = "Add benefit")
        }
    }
}

@Composable
private fun DetailContent(
    detail: CardDetailUi,
    onAddBenefit: () -> Unit,
    onDeleteBenefit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Card Info", "Signup Bonus", "Benefits", "Offers")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                    Text(detail.productName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(detail.issuer, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                item { CardInfoTab(detail) }
            }
            1 -> {
                item { SignupBonusTab() }
            }
            2 -> {
                item {
                    BenefitsHeader(onAddBenefit = onAddBenefit)
                }
                items(detail.benefits, key = { it.id }) { benefit ->
                    BenefitCard(
                        benefit = benefit,
                        onDelete = { onDeleteBenefit(benefit.id) }
                    )
                }
            }
            3 -> {
                item { OffersTab() }
            }
        }
    }
}

@Composable
private fun BenefitCard(benefit: BenefitUi, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                benefit.title?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                Text(benefit.type, fontWeight = FontWeight.SemiBold)
                if (benefit.amount.isNotBlank()) Text(benefit.amount)
                Text("Cadence: ${benefit.cadence}")
                benefit.expiry?.let { Text("Expiry: $it") }
                benefit.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete benefit",
                    tint = MaterialTheme.colorScheme.error
                )
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
@Composable
private fun CardInfoTab(detail: CardDetailUi) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${detail.issuer} • ${detail.network}", style = MaterialTheme.typography.bodyMedium)
        detail.nickname?.let { Text("Nickname: $it", style = MaterialTheme.typography.bodySmall) }
        detail.lastFour?.let { Text("Last 4: $it", style = MaterialTheme.typography.bodySmall) }
        Text("Status: ${detail.status}", style = MaterialTheme.typography.bodySmall)
        Text("Annual fee: ${detail.annualFee}", style = MaterialTheme.typography.bodySmall)
        detail.openDate?.let { Text("Open date: $it", style = MaterialTheme.typography.bodySmall) }
        detail.statementCut?.let { Text("Statement cut: $it", style = MaterialTheme.typography.bodySmall) }
        detail.welcomeOfferProgress?.let { Text("Welcome offer: $it", style = MaterialTheme.typography.bodySmall) }

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Application timeline", style = MaterialTheme.typography.titleMedium)
        detail.applications.forEach { app ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Status: ${app.status}", fontWeight = FontWeight.SemiBold)
                    app.applicationDateUtc?.let { Text("Applied: $it") }
                    app.decisionDateUtc?.let { Text("Decision: $it") }
                    app.bureau?.let { Text("Bureau: $it") }
                }
            }
        }
    }
}

@Composable
private fun SignupBonusTab() {
    DetailMessage("No signup bonus tracker yet.")
}

@Composable
private fun OffersTab() {
    DetailMessage("No offers yet.")
}

@Composable
private fun BenefitsTab(
    detail: CardDetailUi,
    onAddBenefit: () -> Unit,
    onDeleteBenefit: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            BenefitsHeader(onAddBenefit = onAddBenefit)
        }
        items(detail.benefits, key = { it.id }) { benefit ->
            BenefitCard(
                benefit = benefit,
                onDelete = { onDeleteBenefit(benefit.id) }
            )
        }
    }
}

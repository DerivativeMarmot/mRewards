package com.example.rewardsrader.ui.cardcreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCreateScreen(
    stateFlow: StateFlow<CardCreateState>,
    onLoad: () -> Unit,
    onIssuerSelected: (String) -> Unit,
    onCardSelected: (String) -> Unit,
    onOpenDateChange: (String) -> Unit,
    onStatementCutChange: (String) -> Unit,
    onApplicationStatusChange: (String) -> Unit,
    onWelcomeOfferChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val state by stateFlow.collectAsState()
    LaunchedEffect(Unit) { onLoad() }
    var showIssuerDialog by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showOpenDatePicker by remember { mutableStateOf(false) }
    var showStatementDatePicker by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val selectedIssuerName = state.issuers.firstOrNull { it.id == state.selectedIssuerId }?.name
        ?: "Select issuer"
    val selectedProductName = state.cards.firstOrNull { it.id == state.selectedCardId }?.productName
        ?: "Select product"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Card") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectionRow(
                label = "Issuer",
                value = selectedIssuerName,
                onClick = { showIssuerDialog = true }
            )

            SelectionRow(
                label = "Product",
                value = selectedProductName,
                onClick = { showProductDialog = true }
            )

            SelectionRow(
                label = "Open date (MM/dd/yyyy)",
                value = state.openDateUtc.ifBlank { "Select date" },
                onClick = { showOpenDatePicker = true }
            )
            SelectionRow(
                label = "Statement cut (MM/dd/yyyy)",
                value = state.statementCutUtc.ifBlank { "Select date" },
                onClick = { showStatementDatePicker = true }
            )
            SelectionRow(
                label = "Application status",
                value = state.applicationStatus.ifBlank { "Select status" },
                onClick = { showStatusDialog = true }
            )
            OutlinedTextField(
                value = state.welcomeOfferProgress,
                onValueChange = onWelcomeOfferChange,
                label = { Text("Welcome offer progress") },
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let { Text("Error: $it") }
            if (state.success) Text("Saved!")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onBack, enabled = !state.isSaving) {
                    Text("Cancel")
                }
                Button(onClick = onSave, enabled = !state.isSaving) {
                    Text("Save")
                }
            }
        }
    }

    if (showIssuerDialog) {
        SimpleSelectionDialog(
            title = "Select issuer",
            options = state.issuers.map { it.name },
            selected = selectedIssuerName,
            onSelect = { name ->
                state.issuers.firstOrNull { it.name == name }?.id?.let(onIssuerSelected)
                showIssuerDialog = false
            },
            onDismiss = { showIssuerDialog = false }
        )
    }

    if (showProductDialog) {
        val filteredCards = state.cards.filter { card ->
            state.selectedIssuerId?.let { card.issuerId == it } ?: true
        }
        SimpleSelectionDialog(
            title = "Select product",
            options = filteredCards.map { it.productName },
            selected = filteredCards.firstOrNull { it.id == state.selectedCardId }?.productName,
            onSelect = { productName ->
                filteredCards.firstOrNull { it.productName == productName }?.id?.let(onCardSelected)
                showProductDialog = false
            },
            onDismiss = { showProductDialog = false }
        )
    }

    if (showOpenDatePicker) {
        DatePickerDialog(
            onDismiss = { showOpenDatePicker = false },
            onDateSelected = { formatted ->
                onOpenDateChange(formatted)
                showOpenDatePicker = false
            },
            context = context
        )
    }

    if (showStatementDatePicker) {
        DatePickerDialog(
            onDismiss = { showStatementDatePicker = false },
            onDateSelected = { formatted ->
                onStatementCutChange(formatted)
                showStatementDatePicker = false
            },
            context = context
        )
    }

    if (showStatusDialog) {
        SimpleSelectionDialog(
            title = "Application status",
            options = listOf("pending", "approved", "denied"),
            selected = state.applicationStatus,
            onSelect = {
                onApplicationStatusChange(it)
                showStatusDialog = false
            },
            onDismiss = { showStatusDialog = false }
        )
    }
}

@Composable
private fun SelectionRow(label: String, value: String, onClick: () -> Unit) {
    Column {
        Text(label)
        Spacer(Modifier.height(4.dp))
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(value)
        }
    }
}

@Composable
private fun SimpleSelectionDialog(
    title: String,
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(options) { option ->
                    val label = if (option == selected) "$option (selected)" else option
                    Button(onClick = { onSelect(option) }, modifier = Modifier.fillMaxWidth()) {
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    context: android.content.Context
) {
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            onDateSelected(formatter.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setOnDismissListener { onDismiss() }
        show()
    }
}

package com.example.rewardsrader.ui.cardedit

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
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
fun CardEditScreen(
    stateFlow: StateFlow<CardEditState>,
    onLoad: (Long) -> Unit,
    cardId: Long,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onAnnualFeeChange: (String) -> Unit,
    onLastFourChange: (String) -> Unit,
    onOpenDateChange: (String) -> Unit,
    onStatementDateChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onWelcomeOfferChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    val state by stateFlow.collectAsState()
    val context = LocalContext.current
    var showOpenDatePicker by remember { mutableStateOf(false) }
    var showStatementDatePicker by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    if (state.isLoading && state.cardId != cardId) {
        onLoad(cardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSave, enabled = !state.isSaving) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        if (state.error != null) {
            Text(
                text = "Error: ${state.error}",
                modifier = Modifier.padding(padding).padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Issuer: ${state.issuer}")
                Text("Product: ${state.productName}")

                OutlinedTextField(
                    value = state.nickname,
                    onValueChange = onNicknameChange,
                    label = { Text("Nickname") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.annualFee,
                    onValueChange = onAnnualFeeChange,
                    label = { Text("Annual fee (USD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.lastFour,
                    onValueChange = onLastFourChange,
                    label = { Text("Last 4 digits") },
                    modifier = Modifier.fillMaxWidth()
                )

                SelectionRow(
                    label = "Open date (MM/dd/yyyy)",
                    value = state.openDate.ifBlank { "Select date" },
                    onClick = { showOpenDatePicker = true }
                )
                SelectionRow(
                    label = "Statement cut (MM/dd/yyyy)",
                    value = state.statementCut.ifBlank { "Select date" },
                    onClick = { showStatementDatePicker = true }
                )
                SelectionRow(
                    label = "Status",
                    value = state.status.ifBlank { "Select status" },
                    onClick = { showStatusDialog = true }
                )

                OutlinedTextField(
                    value = state.welcomeOfferProgress,
                    onValueChange = onWelcomeOfferChange,
                    label = { Text("Welcome offer progress") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showOpenDatePicker) {
        DatePickerDialog(
            onDismiss = { showOpenDatePicker = false },
            onDateSelected = {
                onOpenDateChange(it)
                showOpenDatePicker = false
            },
            context = context
        )
    }

    if (showStatementDatePicker) {
        DatePickerDialog(
            onDismiss = { showStatementDatePicker = false },
            onDateSelected = {
                onStatementDateChange(it)
                showStatementDatePicker = false
            },
            context = context
        )
    }

    if (showStatusDialog) {
        SimpleSelectionDialog(
            title = "Status",
            options = listOf("pending", "approved", "denied", "open", "closed"),
            selected = state.status,
            onSelect = {
                onStatusChange(it)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val label = if (option == selected) "$option (selected)" else option
                    Button(onClick = { onSelect(option) }, modifier = Modifier.fillMaxWidth()) {
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { Button(onClick = onDismiss) { Text("Close") } }
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
    DatePickerDialog(
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

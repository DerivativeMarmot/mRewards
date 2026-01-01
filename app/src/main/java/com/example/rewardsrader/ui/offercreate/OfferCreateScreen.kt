package com.example.rewardsrader.ui.offercreate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferCreateScreen(
    stateFlow: StateFlow<OfferCreateState>,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onMultiplierChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onMinSpendChange: (String) -> Unit,
    onMaxCashBackChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    val state by stateFlow.collectAsState()
    var showTypeDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    LaunchedEffect(showStartPicker, state.startDate) {
        if (showStartPicker) {
            startDatePickerState.selectedDateMillis = state.startDate.toMillis()
        }
    }
    LaunchedEffect(showEndPicker, state.endDate) {
        if (showEndPicker) {
            endDatePickerState.selectedDateMillis = state.endDate.toMillis()
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Offer" else "Add Offer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onSave, enabled = !state.isSaving) {
                        Text(if (state.isSaving) "Saving..." else "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.productName.isNotBlank()) {
                Text(
                    text = "Product: ${state.productName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth()
            )

            InlineSelectionRow(
                label = "Type",
                value = state.type.replaceFirstChar { it.uppercase() },
                onClick = { showTypeDialog = true }
            )

            if (state.type == "multiplier") {
                OutlinedTextField(
                    value = state.multiplier,
                    onValueChange = onMultiplierChange,
                    label = { Text("Multiplier") },
                    trailingIcon = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            InlineSelectionRow(
                label = "Status",
                value = state.status.replaceFirstChar { it.uppercase() },
                onClick = { showStatusDialog = true }
            )

            OutlinedTextField(
                value = state.minSpend,
                onValueChange = onMinSpendChange,
                label = { Text("Minimum spending") },
                leadingIcon = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.maxCashBack,
                onValueChange = onMaxCashBackChange,
                label = { Text("Maximum cash back") },
                leadingIcon = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            InlineSelectionRow(
                label = "Start date",
                value = state.startDate.ifBlank { "Select date" },
                onClick = { showStartPicker = true }
            )
            InlineSelectionRow(
                label = "End date",
                value = state.endDate.ifBlank { "Select date" },
                onClick = { showEndPicker = true }
            )

            if (!state.error.isNullOrBlank()) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showTypeDialog) {
        RadioDialog(
            title = "Select type",
            options = listOf("credit" to "Credit", "multiplier" to "Multiplier"),
            selected = state.type,
            onSelect = {
                onTypeChange(it)
                showTypeDialog = false
            },
            onDismiss = { showTypeDialog = false }
        )
    }

    if (showStatusDialog) {
        RadioDialog(
            title = "Select status",
            options = listOf("active" to "Active", "expired" to "Expired", "used" to "Used"),
            selected = state.status,
            onSelect = {
                onStatusChange(it)
                showStatusDialog = false
            },
            onDismiss = { showStatusDialog = false }
        )
    }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = startDatePickerState.selectedDateMillis.toDateString()
                    if (formatted != null) onStartDateChange(formatted)
                    showStartPicker = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = endDatePickerState.selectedDateMillis.toDateString()
                    if (formatted != null) onEndDateChange(formatted)
                    showEndPicker = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}

@Composable
private fun InlineSelectionRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun RadioDialog(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == key,
                            onClick = { onSelect(key) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private val offerDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

private fun String.toMillis(): Long? {
    if (isBlank()) return null
    return runCatching {
        LocalDate.parse(this, offerDateFormatter)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun Long?.toDateString(): String? {
    this ?: return null
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    return offerDateFormatter.format(date)
}

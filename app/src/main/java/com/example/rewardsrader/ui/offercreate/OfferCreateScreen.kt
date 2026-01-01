package com.example.rewardsrader.ui.offercreate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 48.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.productName.isNotBlank()) {
                Text(
                    textAlign = TextAlign.Center,
                    text = state.productName,
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

            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))

            Box {
                OutlinedTextField(
                    value = state.type.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Select type") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showTypeDialog = true }
                )
            }

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

            Box {
                OutlinedTextField(
                    value = state.status.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Select status") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showStatusDialog = true }
                )
            }

            OutlinedTextField(
                value = state.minSpend,
                onValueChange = onMinSpendChange,
                label = { Text("Minimum spending") },
                leadingIcon = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            val recommendedSpend = remember(state.type, state.multiplier, state.maxCashBack) {
                if (state.type != "multiplier") return@remember null
                val ratePercent = state.multiplier.toDoubleOrNull()
                val maxCash = state.maxCashBack.toDoubleOrNull()
                if (ratePercent == null || ratePercent == 0.0 || maxCash == null) return@remember null
                maxCash / (ratePercent / 100)
            }
            recommendedSpend?.let { spend ->
                Text(
                    text = "Recommended spending: $${formatAmount(spend)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedTextField(
                value = state.maxCashBack,
                onValueChange = onMaxCashBackChange,
                label = { Text("Maximum cash back") },
                leadingIcon = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))

            Box {
                OutlinedTextField(
                    value = state.startDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start date") },
                    placeholder = { Text("Select date") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Pick start date") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showStartPicker = true }
                )
            }
            Box {
                OutlinedTextField(
                    value = state.endDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End date") },
                    placeholder = { Text("Select date") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Pick end date") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showEndPicker = true }
                )
            }

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
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun Long?.toDateString(): String? {
    this ?: return null
    val date = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
    return offerDateFormatter.format(date)
}

private fun formatAmount(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value)
    }
}

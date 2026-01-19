package com.example.rewardsrader.ui.benefitcreate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.StateFlow

private val commonCategories = listOf(
    "Dining", "Grocery", "Online Shopping", "Travel", "Gas", "Drugstore", "Streaming", "Transit", "Utilities", "RideShare"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BenefitCreateScreen(
    stateFlow: StateFlow<BenefitCreateState>,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onTitleChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCapChange: (String) -> Unit,
    onCadenceChange: (String) -> Unit,
    onEffectiveDateChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onCustomCategoryChange: (String) -> Unit,
    onAddCustomCategory: () -> Unit,
    onRemoveCustomCategory: (String) -> Unit
) {
    val state by stateFlow.collectAsState()
    var showEffectivePicker by remember { mutableStateOf(false) }
    var showExpiryPicker by remember { mutableStateOf(false) }
    var showTypeDialog by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    val effectiveDatePickerState = rememberDatePickerState()
    val expiryDatePickerState = rememberDatePickerState()

    LaunchedEffect(showEffectivePicker, state.effectiveDate) {
        if (showEffectivePicker) {
            val millis = state.effectiveDate.toMillis()
            if (millis != null) {
                effectiveDatePickerState.selectedDateMillis = millis
            }
        }
    }
    LaunchedEffect(showExpiryPicker, state.expiryDate) {
        if (showExpiryPicker) {
            val millis = state.expiryDate.toMillis()
             if (millis != null) {
                expiryDatePickerState.selectedDateMillis = millis
            }
        }
    }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Benefit" else "Add Benefit") },
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
                .imePadding()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(horizontal = 48.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChange,
                label = { Text("Notes/terms") },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))

            InlineSelectionRow(
                label = "Type",
                value = state.type,
                onClick = { showTypeDialog = true }
            )

            if (state.type == "credit") {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount") },
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = onAmountChange,
                    label = { Text("Rate") },
                    trailingIcon = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.cap,
                    onValueChange = onCapChange,
                    label = { Text("Cap") },
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))

            InlineSelectionRow(
                label = "Effective date",
                value = state.effectiveDate.ifBlank { "Select date" },
                onClick = { showEffectivePicker = true }
            )
            InlineSelectionRow(
                label = "Expiration date",
                value = state.expiryDate.ifBlank { "Select date" },
                onClick = { showExpiryPicker = true }
            )
            InlineSelectionRow(
                label = "Frequency",
                value = state.cadence.toDisplayLabel(),
                onClick = { showFrequencyDialog = true }
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Categories")
                // Categories fixed to enum values; no edit dialog.
            }
            val allCategories = com.example.rewardsrader.data.local.entity.BenefitCategory.values().map {
                val label = it.name.replace(Regex("([A-Z])"), " $1").trim()
                CategoryItem(label = label, key = it.name)
            }
            FlowCategoryChips(
                categories = allCategories,
                selected = state.categories,
                onToggle = onToggleCategory
            )

            state.error?.let { Text("Error: $it") }
        }
    }

    if (showTypeDialog) {
        RadioSelectionDialog(
            title = "Select type",
            options = listOf("credit", "multiplier"),
            selected = state.type,
            onSelect = {
                onTypeChange(it)
                showTypeDialog = false
            },
            onDismiss = { showTypeDialog = false }
        )
    }

    if (showFrequencyDialog) {
        RadioSelectionDialog(
            title = "Select frequency",
            options = listOf("once", "monthly", "quarterly", "semiannually", "annually", "everytransaction"),
            selected = state.cadence,
            onSelect = {
                onCadenceChange(it)
                showFrequencyDialog = false
            },
            onDismiss = { showFrequencyDialog = false }
        )
    }

    // Custom category dialog removed (enum-only categories).

    if (showEffectivePicker) {
        DatePickerDialog(
            onDismissRequest = { showEffectivePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = effectiveDatePickerState.selectedDateMillis.toDateString()
                    formatted?.let { onEffectiveDateChange(it) }
                    showEffectivePicker = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEffectivePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = effectiveDatePickerState)
        }
    }
    if (showExpiryPicker) {
        DatePickerDialog(
            onDismissRequest = { showExpiryPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = expiryDatePickerState.selectedDateMillis.toDateString()
                    formatted?.let { onExpiryDateChange(it) }
                    showExpiryPicker = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showExpiryPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = expiryDatePickerState)
        }
    }
    // Transaction creation/editing is handled in the tracker feature.
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
                    val display = option.toDisplayLabel()
                    val label = if (option == selected) "$display (selected)" else display
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
private fun RadioSelectionDialog(
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) }
                        )
                        Text(option.toDisplayLabel())
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
private fun AddCategoryDialog(
    current: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
    customCategories: List<String>,
    issuer: String,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Unused placeholder retained for API compatibility; categories are fixed to enum values.
    onDismiss()
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun FlowCategoryChips(
    categories: List<CategoryItem>,
    selected: List<String>,
    onToggle: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selected.contains(category.key)
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(category.key) },
                label = { Text(category.label) }
            )
        }
    }
}

private val benefitDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

private fun String.toDisplayLabel(): String = when (lowercase()) {
    "everytransaction", "every_transaction" -> "Every transaction"
    "everyanniversary", "every_anniversary" -> "Every anniversary"
    "semiannually", "semi_annual", "semi-annual", "semi-annually" -> "Semi-annually"
    "annually", "annual" -> "Annually"
    "quarterly" -> "Quarterly"
    "monthly" -> "Monthly"
    "once" -> "Once"
    "credit" -> "Credit"
    "multiplier" -> "Multiplier"
    else -> replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun String.toMillis(): Long? {
    if (isBlank()) return null
    return runCatching {
        LocalDate.parse(this, benefitDateFormatter)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun Long?.toDateString(): String? {
    this ?: return null
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    return benefitDateFormatter.format(date)
}

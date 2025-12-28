package com.example.rewardsrader.ui.benefitcreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.RadioButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val commonCategories = listOf(
    "Dining", "Grocery", "Online Shopping", "Travel", "Gas", "Drugstore", "Streaming", "Transit", "Utilities"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BenefitCreateScreen(
    stateFlow: StateFlow<BenefitCreateState>,
    onInit: () -> Unit,
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
    var showCategoryDialog by remember { mutableStateOf(false) }
    val effectiveDatePickerState = rememberDatePickerState()
    val expiryDatePickerState = rememberDatePickerState()

    LaunchedEffect(onInit) { onInit() }
    LaunchedEffect(showEffectivePicker, state.effectiveDate) {
        if (showEffectivePicker) {
            val millis = state.effectiveDate.toMillis()
            effectiveDatePickerState.setSelection(millis)
        }
    }
    LaunchedEffect(showExpiryPicker, state.expiryDate) {
        if (showExpiryPicker) {
            val millis = state.expiryDate.toMillis()
            expiryDatePickerState.setSelection(millis)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            Text(
                text = if (state.isEditing) "Edit Benefit" else "Add Benefit",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onSave, enabled = !state.isSaving) {
                Text(if (state.isSaving) "Saving..." else "Save")
            }
        }
        Divider(modifier = Modifier.fillMaxWidth())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Divider(modifier = Modifier.fillMaxWidth())

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

            Divider(modifier = Modifier.fillMaxWidth())

            InlineSelectionRow(
                label = "Frequency",
                value = state.cadence,
                onClick = { showFrequencyDialog = true }
            )
            Divider(modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Categories")
                IconButton(onClick = { showCategoryDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit categories")
                }
            }
            val issuerPrefix = "${state.issuer}:"
            val scopedCommon = commonCategories.map { label -> CategoryItem(label = label, key = "$issuerPrefix$label") }
            val scopedCustom = state.customCategories.filter { it.startsWith(issuerPrefix) }
                .map { scoped ->
                    CategoryItem(label = scoped.removePrefix(issuerPrefix), key = scoped)
                }
            val allCategories = (scopedCommon + scopedCustom).distinctBy { it.key }
            FlowCategoryChips(
                categories = allCategories,
                selected = state.categories,
                onToggle = onToggleCategory
            )
            Divider(modifier = Modifier.fillMaxWidth())

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
            Divider(modifier = Modifier.fillMaxWidth())

            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChange,
                label = { Text("Notes/terms") },
                modifier = Modifier.fillMaxWidth()
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
            options = listOf("once", "monthly", "quarterly", "annually"),
            selected = state.cadence,
            onSelect = {
                onCadenceChange(it)
                showFrequencyDialog = false
            },
            onDismiss = { showFrequencyDialog = false }
        )
    }

    if (showCategoryDialog) {
        AddCategoryDialog(
            current = state.customCategory,
            onValueChange = onCustomCategoryChange,
            onAdd = {
                onAddCustomCategory()
                showCategoryDialog = false
            },
            onRemove = onRemoveCustomCategory,
            customCategories = state.customCategories,
            issuer = state.issuer,
            onDismiss = { showCategoryDialog = false }
        )
    }

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
                        Text(option)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit categories") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = onValueChange,
                    label = { Text("Category name") },
                    modifier = Modifier.fillMaxWidth()
                )
                val prefix = if (issuer.isNotBlank()) "$issuer:" else ""
                val scoped = customCategories.filter { prefix.isBlank() || it.startsWith(prefix) }
                if (scoped.isNotEmpty()) {
                    Text("Custom categories", style = MaterialTheme.typography.titleSmall)
                    scoped.forEach { scopedCat ->
                        val label = scopedCat.removePrefix(prefix)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label)
                            IconButton(onClick = { onRemove(scopedCat) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove category")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onAdd, enabled = current.isNotBlank()) { Text("Add") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
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

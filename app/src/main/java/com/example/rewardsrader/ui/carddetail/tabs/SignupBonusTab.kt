package com.example.rewardsrader.ui.carddetail.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.rewardsrader.ui.carddetail.components.InfoRow
import com.example.rewardsrader.ui.carddetail.components.StaticInfoRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupBonusTab(
    spending: String?,
    duration: String?,
    durationUnit: String,
    openDate: String?,
    onUpdateSpending: (String) -> Unit,
    onUpdateDuration: (String, String) -> Unit
) {
    var spendingValue by rememberSaveable(spending) { mutableStateOf(spending.orEmpty()) }
    var durationValue by rememberSaveable(duration) { mutableStateOf(duration.orEmpty()) }
    var durationUnitSelection by rememberSaveable(durationUnit) {
        mutableStateOf(if (durationUnit == DurationUnit.DAYS.label) DurationUnit.DAYS else DurationUnit.MONTHS)
    }
    var editingField by remember { mutableStateOf<BonusField?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        InfoRow(
            label = "Spending",
            value = if (spendingValue.isNotBlank()) "$$spendingValue" else "Tap to add",
            onClick = { editingField = BonusField.Spending }
        )
        androidx.compose.material3.Divider()
        InfoRow(
            label = "Duration",
            value = durationValue.ifBlank { "Tap to add" }.let { if (durationValue.isNotBlank()) "$durationValue ${durationUnitSelection.label}" else it },
            onClick = { editingField = BonusField.Duration }
        )
        androidx.compose.material3.Divider()
        val dueDate = remember(openDate, durationValue, durationUnitSelection) {
            calculateDueDate(openDate, durationValue, durationUnitSelection)
        }
        StaticInfoRow(
            label = "Due date",
            value = dueDate ?: "Add open date and duration"
        )
    }

    editingField?.let { field ->
        var draft by remember { mutableStateOf(if (field == BonusField.Spending) spendingValue else durationValue) }
        val focusRequester = remember { FocusRequester() }
        var unitSelection by remember { mutableStateOf(durationUnitSelection) }
        AlertDialog(
            onDismissRequest = { editingField = null },
            title = { Text(if (field == BonusField.Spending) "Edit spending" else "Edit duration") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (field == BonusField.Duration) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = draft,
                                onValueChange = { draft = it },
                                label = { Text("Value") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                            )
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = unitSelection.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Unit") },
                                    trailingIcon = {
                                        IconButton(onClick = { expanded = !expanded }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select unit")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DurationUnit.values().forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit.label) },
                                            onClick = {
                                                unitSelection = unit
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            label = { Text("Value") },
                            leadingIcon = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (field == BonusField.Spending) {
                        spendingValue = draft
                        onUpdateSpending(draft)
                    } else {
                        durationValue = draft
                        durationUnitSelection = unitSelection
                        onUpdateDuration(draft, unitSelection.label)
                    }
                    editingField = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) { Text("Cancel") }
            }
        )
        LaunchedEffect(field) { focusRequester.requestFocus() }
    }
}

private enum class BonusField { Spending, Duration }
private enum class DurationUnit(val label: String) { MONTHS("months"), DAYS("days") }

private fun calculateDueDate(openDate: String?, duration: String, unit: DurationUnit): String? {
    if (openDate.isNullOrBlank() || duration.isBlank()) return null
    val startDate = runCatching {
        LocalDate.parse(openDate, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
    }.getOrNull() ?: return null
    val amount = duration.toLongOrNull() ?: return null
    val due = when (unit) {
        DurationUnit.MONTHS -> startDate.plusMonths(amount)
        DurationUnit.DAYS -> startDate.plusDays(amount)
    }
    return "%02d/%02d/%d".format(due.monthValue, due.dayOfMonth, due.year)
}

package com.example.rewardsrader.ui.carddetail.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun EditFieldDialog(
    title: String,
    initial: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    valueFilter: (String) -> String = { it },
    prefixText: String? = null,
    autoFocus: Boolean = false,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text = remember { mutableStateOf(TextFieldValue(initial)) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text.value,
                onValueChange = {
                    val filtered = valueFilter(it.text)
                    text.value = TextFieldValue(filtered, selection = androidx.compose.ui.text.TextRange(filtered.length))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = keyboardOptions,
                prefix = prefixText?.let { { Text(it) } }
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text.value.text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StatusDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("Pending", "Active", "Closed")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select status") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option.equals(current, ignoreCase = true),
                            onClick = { onSelect(option) }
                        )
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun StatementCutDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedDay = extractStatementCutDay(current)
    val options = (1..31).toList()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select statement / closing date") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                items(options) { day ->
                    val label = formatStatementCut(day)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(label) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDay == day,
                            onClick = { onSelect(label) }
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {},
//        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun extractStatementCutDay(value: String): Int? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    val parts = trimmed.split("/")
    if (parts.size == 3) {
        val day = parts[1].toIntOrNull()
        if (day != null && day in 1..31) return day
    }
    val match = Regex("\\d{1,2}").find(trimmed)?.value
    val day = match?.toIntOrNull()
    return day?.takeIf { it in 1..31 }
}

private fun formatStatementCut(day: Int): String = "${day}${ordinalSuffix(day)} of the month"

private fun ordinalSuffix(day: Int): String {
    val mod100 = day % 100
    return if (mod100 in 11..13) {
        "th"
    } else {
        when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}

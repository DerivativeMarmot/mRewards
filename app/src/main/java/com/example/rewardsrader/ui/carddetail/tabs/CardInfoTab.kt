package com.example.rewardsrader.ui.carddetail.tabs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.rewardsrader.ui.carddetail.CardDetailUi
import com.example.rewardsrader.ui.carddetail.components.CardField
import com.example.rewardsrader.ui.carddetail.components.InfoRow
import com.example.rewardsrader.ui.carddetail.components.NotesRow

@Composable
fun CardInfoTab(
    detail: CardDetailUi,
    onStartEdit: (CardField, String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onOpenDateClick: () -> Unit,
    onStatementDateClick: () -> Unit
) {
    var showNotesDialog by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf(detail.notes.orEmpty()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        InfoRow(
            label = "Nickname",
            value = detail.nickname.orEmpty(),
            onClick = { onStartEdit(CardField.Nickname, detail.nickname.orEmpty()) }
        )
        HorizontalDivider()
        InfoRow(
            label = "Last 4-6 digits",
            value = detail.lastFour.orEmpty(),
            onClick = { onStartEdit(CardField.LastFour, detail.lastFour.orEmpty()) }
        )
        HorizontalDivider()
        InfoRow(
            label = "Status",
            value = detail.status.replaceFirstChar { it.uppercase() },
            onClick = { onStartEdit(CardField.Status, detail.status) }
        )
        HorizontalDivider()
        InfoRow(
            label = "Annual fee",
            value = detail.annualFee,
            onClick = { onStartEdit(CardField.AnnualFee, detail.annualFee.removePrefix("$")) }
        )
        HorizontalDivider()
        InfoRow(
            label = "Open date",
            value = detail.openDate.orEmpty(),
            onClick = onOpenDateClick
        )
        HorizontalDivider()
        InfoRow(
            label = "Statement / Closing date",
            value = detail.statementCut.orEmpty(),
            onClick = onStatementDateClick
        )
        HorizontalDivider()
        InfoRow(
            label = "Notes",
            value = detail.notes.orEmpty().ifBlank { "" },
            onClick = {
                notesDraft = detail.notes.orEmpty()
                showNotesDialog = true
            },
            alignLabelTop = true,
            valueTextAlign = androidx.compose.ui.text.style.TextAlign.Start
        )
    }

    if (showNotesDialog) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Edit notes") },
            text = {
                OutlinedTextField(
                    value = notesDraft,
                    onValueChange = { notesDraft = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateNotes(notesDraft)
                    showNotesDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) { Text("Cancel") }
            }
        )
    }
}

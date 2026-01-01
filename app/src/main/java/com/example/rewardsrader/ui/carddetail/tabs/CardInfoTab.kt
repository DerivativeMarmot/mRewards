package com.example.rewardsrader.ui.carddetail.tabs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        InfoRow(
            label = "Nickname",
            value = detail.nickname.orEmpty(),
            onClick = { onStartEdit(CardField.Nickname, detail.nickname.orEmpty()) }
        )
        Divider()
        InfoRow(
            label = "Last 4 digits",
            value = detail.lastFour.orEmpty(),
            onClick = { onStartEdit(CardField.LastFour, detail.lastFour.orEmpty()) }
        )
        Divider()
        InfoRow(
            label = "Status",
            value = detail.status.replaceFirstChar { it.uppercase() },
            onClick = { onStartEdit(CardField.Status, detail.status) }
        )
        Divider()
        InfoRow(
            label = "Annual fee",
            value = detail.annualFee,
            onClick = { onStartEdit(CardField.AnnualFee, detail.annualFee.removePrefix("$")) }
        )
        Divider()
        InfoRow(
            label = "Open date",
            value = detail.openDate.orEmpty(),
            onClick = onOpenDateClick
        )
        Divider()
        InfoRow(
            label = "Statement cut",
            value = detail.statementCut.orEmpty(),
            onClick = onStatementDateClick
        )
        Divider()
        InfoRow(
            label = "Notes",
            value = detail.notes.orEmpty().ifBlank { "Tap to add" },
            onClick = {
                notesDraft = detail.notes.orEmpty()
                showNotesDialog = true
            }
        )
    }

    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Edit notes") },
            text = {
                OutlinedTextField(
                    value = notesDraft,
                    onValueChange = { notesDraft = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
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

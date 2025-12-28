package com.example.rewardsrader.ui.carddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    stateFlow: StateFlow<CardDetailState>,
    events: kotlinx.coroutines.flow.SharedFlow<String>,
    onBack: () -> Unit,
    onAddBenefit: (Long, String) -> Unit,
    onEditBenefit: (Long) -> Unit,
    onDeleteBenefit: (Long) -> Unit,
    onUpdateNickname: (String) -> Unit,
    onUpdateAnnualFee: (String) -> Unit,
    onUpdateLastFour: (String) -> Unit,
    onUpdateOpenDate: (String) -> Unit,
    onUpdateStatementCut: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateNotes: (String) -> Unit
) {
    val state by stateFlow.collectAsState()
    val detail = state.detail
    var showOpenDatePicker by remember { mutableStateOf(false) }
    var showStatementDatePicker by remember { mutableStateOf(false) }
    val openDatePickerState = androidx.compose.material3.rememberDatePickerState()
    val statementDatePickerState = androidx.compose.material3.rememberDatePickerState()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.runtime.LaunchedEffect(events) {
            events.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
        when {
            state.isLoading -> DetailMessage("Loading…", Modifier.padding(padding))
            state.error != null -> DetailMessage("Error: ${state.error}", Modifier.padding(padding))
            detail == null -> DetailMessage("No details available.", Modifier.padding(padding))
            else -> DetailContent(
                detail = detail,
                onAddBenefit = { onAddBenefit(detail.id, detail.productName) },
                onEditBenefit = onEditBenefit,
                onDeleteBenefit = onDeleteBenefit,
                onUpdateNickname = onUpdateNickname,
                onUpdateAnnualFee = onUpdateAnnualFee,
                onUpdateLastFour = onUpdateLastFour,
                onUpdateOpenDate = onUpdateOpenDate,
                onUpdateStatementCut = onUpdateStatementCut,
                onUpdateStatus = onUpdateStatus,
                onUpdateNotes = onUpdateNotes,
                onOpenDateClick = { showOpenDatePicker = true },
                onStatementDateClick = { showStatementDatePicker = true },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showOpenDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showOpenDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    openDatePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val formatted = "%02d/%02d/%d".format(date.monthValue, date.dayOfMonth, date.year)
                        onUpdateOpenDate(formatted)
                    }
                    showOpenDatePicker = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showOpenDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = openDatePickerState)
        }
    }

    if (showStatementDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStatementDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    statementDatePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val formatted = "%02d/%02d/%d".format(date.monthValue, date.dayOfMonth, date.year)
                        onUpdateStatementCut(formatted)
                    }
                    showStatementDatePicker = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showStatementDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = statementDatePickerState)
        }
    }
}

@Composable
private fun DetailContent(
    detail: CardDetailUi,
    onAddBenefit: () -> Unit,
    onEditBenefit: (Long) -> Unit,
    onDeleteBenefit: (Long) -> Unit,
    onUpdateNickname: (String) -> Unit,
    onUpdateAnnualFee: (String) -> Unit,
    onUpdateLastFour: (String) -> Unit,
    onUpdateOpenDate: (String) -> Unit,
    onUpdateStatementCut: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onOpenDateClick: () -> Unit,
    onStatementDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem("Info", Icons.Default.Info),
        TabItem("SUB", Icons.Default.CardGiftcard),
        TabItem("Benefits", Icons.Default.Verified),
        TabItem("Offers", Icons.Default.LocalOffer)
    )
    var editingField by remember { mutableStateOf<CardField?>(null) }
    var editingValue by remember { mutableStateOf("") }
    val showFab = selectedTab == 2

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = if (showFab) 96.dp else 16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                        Text(detail.productName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(detail.issuer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(tab.icon, contentDescription = tab.label)
                                    Text(tab.label)
                                }
                            }
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    item {
                        CardInfoTab(
                            detail = detail,
                            onStartEdit = { field, value ->
                                editingField = field
                                editingValue = value
                            },
                            onUpdateNotes = onUpdateNotes,
                            onOpenDateClick = onOpenDateClick,
                            onStatementDateClick = onStatementDateClick
                        )
                    }
                }
                1 -> {
                    item { SignupBonusTab() }
                }
                2 -> {
                    items(detail.benefits, key = { it.id }) { benefit ->
                        BenefitCard(
                            benefit = benefit,
                            onEdit = { onEditBenefit(benefit.id) },
                            onDelete = { onDeleteBenefit(benefit.id) }
                        )
                    }
                }
                3 -> {
                    item { OffersTab() }
                }
            }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = onAddBenefit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add benefit")
            }
        }
    }

    when (editingField) {
        CardField.Nickname -> EditFieldDialog(
            title = "Edit nickname",
            initial = editingValue,
            onSave = {
                onUpdateNickname(it)
                editingField = null
            },
            onDismiss = { editingField = null }
        )
        CardField.AnnualFee -> EditFieldDialog(
            title = "Edit annual fee",
            initial = editingValue,
            onSave = {
                onUpdateAnnualFee(it)
                editingField = null
            },
            onDismiss = { editingField = null }
        )
        CardField.LastFour -> EditFieldDialog(
            title = "Edit last 4 digits",
            initial = editingValue,
            onSave = {
                onUpdateLastFour(it)
                editingField = null
            },
            onDismiss = { editingField = null }
        )
        CardField.Status -> StatusDialog(
            current = editingValue,
            onSelect = {
                onUpdateStatus(it)
                editingField = null
            },
            onDismiss = { editingField = null }
        )
        null -> {}
    }
}

@Composable
private fun InfoRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value.ifBlank { "Tap to add" }, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun NotesRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = label)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text("Enter notes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun EditFieldDialog(
    title: String,
    initial: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue(initial)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text.text) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatusDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("Pending", "Approved", "Denied", "Open", "Closed")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select status") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.lowercase()) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = option.equals(current, ignoreCase = true),
                            onClick = { onSelect(option.lowercase()) }
                        )
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class CardField {
    Nickname,
    AnnualFee,
    LastFour,
    Status
}

private data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun BenefitCard(benefit: BenefitUi, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val primaryLine = listOf(benefit.amount, benefit.title.orEmpty())
                    .filter { it.isNotBlank() }
                    .joinToString(" – ")
                if (primaryLine.isNotBlank()) {
                    Text(primaryLine, fontWeight = FontWeight.SemiBold)
                }
                // Only show amount/rate and title
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete benefit",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DetailMessage(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message)
    }
}
@Composable
private fun CardInfoTab(
    detail: CardDetailUi,
    onStartEdit: (CardField, String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onOpenDateClick: () -> Unit,
    onStatementDateClick: () -> Unit
) {
    var showNotesDialog by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf(detail.notes.orEmpty()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

@Composable
private fun SignupBonusTab() {
    DetailMessage("No signup bonus tracker yet.")
}

@Composable
private fun OffersTab() {
    DetailMessage("No offers yet.")
}

@Composable
private fun BenefitsTab(
    detail: CardDetailUi,
    onAddBenefit: () -> Unit,
    onEditBenefit: (Long) -> Unit,
    onDeleteBenefit: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) { }
}

package com.example.rewardsrader.ui.tracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerEditScreen(
    stateFlow: StateFlow<TrackerEditState>,
    onBack: () -> Unit,
    onEntryAmountChange: (String) -> Unit,
    onEntryDateChange: (String) -> Unit,
    onEntryNotesChange: (String) -> Unit,
    onAddTransaction: () -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onToggleOfferComplete: (Boolean) -> Unit,
    onOfferNotesChange: (String) -> Unit,
    onSaveOfferNotes: (String) -> Unit,
    onAddReminder: (Int) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onReminderPermissionDenied: () -> Unit
) {
    val state by stateFlow.collectAsState()
    val tracker = state.tracker
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTransactionSheet by remember { mutableStateOf(false) }
    var showReminderDaysDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf("") }
    var pendingReminderDays by remember { mutableStateOf<Int?>(null) }
    val datePickerState = rememberDatePickerState()
    val transactionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val alarmManager = remember {
        context.getSystemService(AlarmManager::class.java)
    }
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val reminderDays = pendingReminderDays
        pendingReminderDays = null
        if (granted && reminderDays != null) {
            onAddReminder(reminderDays)
            requestExactAlarmPermissionIfNeeded(alarmManager, exactAlarmLauncher)
        } else if (!granted) {
            onReminderPermissionDenied()
        }
    }

    LaunchedEffect(state.entryDate) {
        datePickerState.selectedDateMillis = state.entryDate.toMillisOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracker Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Loading tracker...")
                }
            }
            state.error != null && tracker == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error ?: "Something went wrong",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            tracker != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(start = 24.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TrackerSummaryCard(tracker = tracker)
                    }
                    item {
                        HorizontalDivider()
                    }
                    item {
                        ReminderListCard(
                            reminders = state.reminders,
                            isUpdating = state.isReminderUpdating,
                            onAdd = { showReminderDaysDialog = true },
                            onDelete = onDeleteReminder
                        )
                    }
                    item {
                        HorizontalDivider()
                    }
                    if (tracker.sourceType == TrackerSourceType.Offer) {
                        item {
                            NotesRow(
                                notes = state.offerNotes,
                                onClick = {
                                    notesDraft = state.offerNotes
                                    showNotesDialog = true
                                }
                            )
                        }
                        if (state.error != null) {
                            item {
                                Text(
                                    text = state.error ?: "Something went wrong",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        item {
                            OfferCompleteRow(
                                isCompleted = state.offerCompleted,
                                onToggleComplete = onToggleOfferComplete
                            )
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Transactions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                IconButton(onClick = { showTransactionSheet = true }) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Add transaction")
                                }
                            }
                        }
                        if (state.transactions.isEmpty()) {
                            item {
                                Text("No transactions yet.")
                            }
                        } else {
                            items(state.transactions, key = { it.id }) { entry ->
                                TransactionItem(entry = entry, onDelete = onDeleteTransaction)
                            }
                        }
                        if (state.error != null) {
                            item {
                                Text(
                                    text = state.error ?: "Something went wrong",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTransactionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTransactionSheet = false },
            sheetState = transactionSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = state.entryAmount,
                    onValueChange = onEntryAmountChange,
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date")
                    TextButton(onClick = {
                        focusManager.clearFocus()
                        showDatePicker = true
                    }) {
                        Text(state.entryDate.ifBlank { "Select date" })
                    }
                }
                OutlinedTextField(
                    value = state.entryNotes,
                    onValueChange = onEntryNotesChange,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val dateValid = parseTrackerDate(state.entryDate) != null
                        val amountValid = state.entryAmount.toDoubleOrNull()?.let { it > 0.0 } == true
                        if (dateValid && amountValid) {
                            onAddTransaction()
                            showTransactionSheet = false
                        } else {
                            onAddTransaction()
                        }
                    },
                    enabled = !state.isSaving
                ) {
                    Text("Add")
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onEntryDateChange(millis.toDateString())
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showReminderDaysDialog) {
        ReminderDaysDialog(
            disabledDays = state.reminders.map { it.daysBefore }.toSet(),
            onSelect = {
                val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                if (needsPermission) {
                    pendingReminderDays = it
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onAddReminder(it)
                    requestExactAlarmPermissionIfNeeded(alarmManager, exactAlarmLauncher)
                }
                showReminderDaysDialog = false
            },
            onDismiss = { showReminderDaysDialog = false }
        )
    }

    if (showNotesDialog) {
        NotesDialog(
            notes = notesDraft,
            onNotesChange = { notesDraft = it },
            onSave = {
                onSaveOfferNotes(notesDraft)
                showNotesDialog = false
            },
            onDismiss = { showNotesDialog = false }
        )
    }
}

@Composable
private fun TrackerSummaryCard(tracker: TrackerDetailUi) {
    val endDate = parseTrackerDate(tracker.endDate) ?: LocalDate.now()
    val timeLeft = formatTimeLeftLabel(endDate)
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (tracker.title.isNotBlank()) {
            Text(
                text = tracker.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = tracker.cardName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal
        )
        Box(modifier = Modifier.padding(top = 10.dp))
        Text(
            text = "${formatTrackerAmount(tracker.usedAmount)} used / ${formatTrackerAmount(tracker.amount)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Ends ${tracker.endDate} ($timeLeft)",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun OfferCompleteRow(
    isCompleted: Boolean,
    onToggleComplete: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        val label = if (isCompleted) "Mark offer active" else "Mark offer complete"
        Button(
            onClick = { onToggleComplete(!isCompleted) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(label,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReminderListCard(
    reminders: List<TrackerReminderUi>,
    isUpdating: Boolean,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column() {
            Row(){
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Reminders"
                )
            }

        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reminders.forEach { reminder ->
                    ReminderRow(reminder = reminder, onDelete = onDelete)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isUpdating, onClick = onAdd)
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = "Add reminder",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: TrackerReminderUi,
    onDelete: (String) -> Unit
) {
    Row(
//        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(reminder.daysBefore.toReminderLabel(), style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = { onDelete(reminder.id) }) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete reminder")
        }
    }
}

@Composable
private fun NotesRow(
    notes: String,
    onClick: () -> Unit
) {
    val trimmed = notes.trim()
    val hasNotes = trimmed.isNotBlank()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Notes,
            contentDescription = "Notes"
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = trimmed.takeIf { hasNotes } ?: "Add notes",
            style = MaterialTheme.typography.bodyLarge,
            color = if (hasNotes) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

    }
}

@Composable
private fun TransactionItem(
    entry: TrackerTransactionUi,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formatTrackerAmount(entry.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { onDelete(entry.id) }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete transaction")
            }
        }
        entry.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Text(text = notes, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NotesDialog(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notes") },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
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
private fun ReminderDaysDialog(
    disabledDays: Set<Int>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableStateOf<Int?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder timing") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..7).forEach { day ->
                    val isDisabled = disabledDays.contains(day)
                    val textColor = if (isDisabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedDays == day,
                                enabled = !isDisabled,
                                role = Role.RadioButton,
                                onClick = {
                                    selectedDays = day
                                    onSelect(day)
                                }
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDays == day,
                            onClick = null,
                            enabled = !isDisabled
                        )
                        Text(
                            text = day.toReminderLabel(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

private fun String.toMillisOrNull(): Long? {
    val date = parseTrackerDate(this) ?: return null
    return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun requestExactAlarmPermissionIfNeeded(
    alarmManager: AlarmManager?,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    if (alarmManager == null) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    if (alarmManager.canScheduleExactAlarms()) return
    launcher.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
}

private fun Int.toReminderLabel(): String =
    if (this == 1) "1 day before" else "$this days before"

private fun Long.toDateString(): String {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    return formatTrackerDate(date)
}

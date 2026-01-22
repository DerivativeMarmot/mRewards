package com.example.rewardsrader.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
    onSaveOffer: () -> Unit,
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TrackerSummaryCard(tracker = tracker)
                    }
                    item {
                        ReminderListCard(
                            reminders = state.reminders,
                            isUpdating = state.isReminderUpdating,
                            onAdd = { showReminderDaysDialog = true },
                            onDelete = onDeleteReminder
                        )
                    }
                    if (tracker.sourceType == TrackerSourceType.Offer) {
                        item {
                            OfferCompleteRow(
                                checked = state.offerCompleted,
                                onCheckedChange = onToggleOfferComplete
                            )
                        }
                        item {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = state.offerNotes,
                                onValueChange = onOfferNotesChange,
                                label = { Text("Offer notes") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                        item {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onSaveOffer()
                                },
                                enabled = !state.isSaving
                            ) {
                                Text("Save")
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
                                    Icon(Icons.Default.Add, contentDescription = "Add transaction")
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
}

@Composable
private fun TrackerSummaryCard(tracker: TrackerDetailUi) {
    val endDate = parseTrackerDate(tracker.endDate) ?: LocalDate.now()
    val timeLeft = formatTimeLeftLabel(endDate)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = tracker.cardName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (tracker.title.isNotBlank()) {
                Text(text = tracker.title, style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amount ${formatTrackerAmount(tracker.amount)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Used ${formatTrackerAmount(tracker.usedAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Ends ${tracker.endDate} ($timeLeft)",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun OfferCompleteRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Mark offer complete", style = MaterialTheme.typography.bodyMedium)
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onAdd, enabled = !isUpdating) {
                    Icon(Icons.Default.Add, contentDescription = "Add reminder")
                }
            }
            if (reminders.isEmpty()) {
                Text("No reminders yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                reminders.forEach { reminder ->
                    ReminderRow(reminder = reminder, onDelete = onDelete)
                }
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(reminder.daysBefore.toReminderLabel(), style = MaterialTheme.typography.bodyMedium)
            Text(reminder.fireDateLabel, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { onDelete(reminder.id) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete reminder")
        }
    }
}

@Composable
private fun TransactionItem(
    entry: TrackerTransactionUi,
    onDelete: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                    Icon(Icons.Default.Delete, contentDescription = "Delete transaction")
                }
            }
            entry.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Text(text = notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ReminderDaysDialog(
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableStateOf(1) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder timing") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..7).forEach { day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedDays == day,
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
                            onClick = {
                                selectedDays = day
                                onSelect(day)
                            }
                        )
                        Text(
                            text = day.toReminderLabel(),
                            style = MaterialTheme.typography.bodyMedium,
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

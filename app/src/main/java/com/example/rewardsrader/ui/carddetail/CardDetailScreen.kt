package com.example.rewardsrader.ui.carddetail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    stateFlow: StateFlow<CardDetailState>,
    events: kotlinx.coroutines.flow.SharedFlow<String>,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onAddBenefit: (Long, String) -> Unit,
    onEditBenefit: (Long) -> Unit,
    onDeleteBenefit: (Long) -> Unit,
    onAddOffer: (Long, String) -> Unit,
    onEditOffer: (Long) -> Unit,
    onDeleteOffer: (Long) -> Unit,
    onUpdateNickname: (String) -> Unit,
    onUpdateAnnualFee: (String) -> Unit,
    onUpdateLastFour: (String) -> Unit,
    onUpdateOpenDate: (String) -> Unit,
    onUpdateStatementCut: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateSubSpending: (String) -> Unit,
    onUpdateSubDuration: (String, String) -> Unit
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
                title = {Text("Card Detail")},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                initialTab = initialTab,
                snackbarHostState = snackbarHostState,
                onAddBenefit = { onAddBenefit(detail.id, detail.productName) },
                onEditBenefit = onEditBenefit,
                onDeleteBenefit = onDeleteBenefit,
                onAddOffer = { onAddOffer(detail.id, detail.productName) },
                onEditOffer = onEditOffer,
                onDeleteOffer = onDeleteOffer,
                onUpdateNickname = onUpdateNickname,
                onUpdateAnnualFee = onUpdateAnnualFee,
                onUpdateLastFour = onUpdateLastFour,
                onUpdateOpenDate = onUpdateOpenDate,
                onUpdateStatementCut = onUpdateStatementCut,
                onUpdateStatus = onUpdateStatus,
                onUpdateNotes = onUpdateNotes,
                onUpdateSubSpending = onUpdateSubSpending,
                onUpdateSubDuration = onUpdateSubDuration,
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
    initialTab: Int,
    snackbarHostState: SnackbarHostState,
    onAddBenefit: () -> Unit,
    onEditBenefit: (Long) -> Unit,
    onDeleteBenefit: (Long) -> Unit,
    onAddOffer: () -> Unit,
    onEditOffer: (Long) -> Unit,
    onDeleteOffer: (Long) -> Unit,
    onUpdateNickname: (String) -> Unit,
    onUpdateAnnualFee: (String) -> Unit,
    onUpdateLastFour: (String) -> Unit,
    onUpdateOpenDate: (String) -> Unit,
    onUpdateStatementCut: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateSubSpending: (String) -> Unit,
    onUpdateSubDuration: (String, String) -> Unit,
    onOpenDateClick: () -> Unit,
    onStatementDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = initialTab.coerceIn(0, 3),
        pageCount = { 4 }
    )
    val coroutineScope = rememberCoroutineScope()
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    val tabs = listOf(
        TabItem("Info", Icons.Default.Info),
        TabItem("SUB", Icons.Default.CardGiftcard),
        TabItem("Benefits", Icons.Default.Verified),
        TabItem("Offers", Icons.Default.LocalOffer)
    )
    var editingField by remember { mutableStateOf<CardField?>(null) }
    var editingValue by remember { mutableStateOf("") }
    val fabAction: (() -> Unit)? = when (currentPage) {
        2 -> onAddBenefit
        3 -> onAddOffer
        else -> null
    }
    val fabDescription = when (currentPage) {
        2 -> "Add benefit"
        3 -> "Add offer"
        else -> ""
    }
    val showFab = fabAction != null
    val isSnackbarVisible = snackbarHostState.currentSnackbarData != null
    val fabBottomPadding by animateDpAsState(
        targetValue = if (isSnackbarVisible) 80.dp else 16.dp,
        label = "fabBottomPadding"
    )
    val listBottomPadding = if (showFab) {
        96.dp + if (isSnackbarVisible) 48.dp else 0.dp
    } else {
        16.dp
    }

    val scrollState = rememberScrollState()
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = listBottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

            SecondaryTabRow(selectedTabIndex = currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(tab.icon, contentDescription = tab.label)
                                Text(tab.label)
                            }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top)
            { page ->
                when (page) {
                    0 -> {
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
                    1 -> {
                        SignupBonusTab(
                            spending = detail.subSpending,
                            duration = detail.subDuration,
                            durationUnit = detail.subDurationUnit ?: "months",
                            openDate = detail.openDate,
                            onUpdateSpending = onUpdateSubSpending,
                            onUpdateDuration = onUpdateSubDuration
                        )
                    }
                    2 -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),) {
                            detail.benefits.forEach { benefit ->
                                BenefitCard(
                                    benefit = benefit,
                                    onEdit = { onEditBenefit(benefit.id) },
                                    onDelete = { onDeleteBenefit(benefit.id) }
                                )
                            }
                        }
                    }
                    3 -> {
                        if (detail.offers.isEmpty()) {
                            DetailMessage("No offers yet.")
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),) {
                                detail.offers.forEach { offer ->
                                    OfferCard(
                                        offer = offer,
                                        onEdit = { onEditOffer(offer.id) },
                                        onDelete = { onDeleteOffer(offer.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = { fabAction?.invoke() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = fabBottomPadding)
            ) {
                Icon(Icons.Default.Add, contentDescription = fabDescription.ifBlank { "Add" })
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
private fun OfferCard(offer: OfferUi, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(offer.title, fontWeight = FontWeight.SemiBold)
                if (offer.window.isNotBlank()) {
                    Text(offer.window, style = MaterialTheme.typography.bodyMedium)
                }
                offer.details?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    offer.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete offer",
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupBonusTab(
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
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoRow(
            label = "Spending",
            value = if (spendingValue.isNotBlank()) "$$spendingValue" else "Tap to add",
            onClick = { editingField = BonusField.Spending }
        )
        Divider()
        InfoRow(
            label = "Duration",
            value = durationValue.ifBlank { "Tap to add" }.let { if (durationValue.isNotBlank()) "$durationValue ${durationUnitSelection.label}" else it },
            onClick = { editingField = BonusField.Duration }
        )
        Divider()
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
                            verticalAlignment = Alignment.CenterVertically
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
        LaunchedEffect(field) {
            focusRequester.requestFocus()
        }
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

@Composable
private fun BenefitsTab(
    detail: CardDetailUi,
    onAddBenefit: () -> Unit,
    onEditBenefit: (Long) -> Unit,
    onDeleteBenefit: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) { }
}

@Composable
private fun StaticInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

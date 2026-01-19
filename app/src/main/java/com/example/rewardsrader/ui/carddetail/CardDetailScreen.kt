package com.example.rewardsrader.ui.carddetail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rewardsrader.ui.carddetail.CardFaceUi
import com.example.rewardsrader.ui.carddetail.components.BenefitCard
import com.example.rewardsrader.ui.carddetail.components.CardField
import com.example.rewardsrader.ui.carddetail.components.EditFieldDialog
import com.example.rewardsrader.ui.carddetail.components.OfferCard
import com.example.rewardsrader.ui.carddetail.components.StatusDialog
import com.example.rewardsrader.ui.carddetail.tabs.CardInfoTab
import com.example.rewardsrader.ui.carddetail.tabs.SignupBonusTab
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    stateFlow: StateFlow<CardDetailState>,
    events: kotlinx.coroutines.flow.SharedFlow<String>,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onAddBenefit: (String, String) -> Unit,
    onEditBenefit: (String, String) -> Unit,
    onDeleteBenefit: (String) -> Unit,
    onAddOffer: (String, String) -> Unit,
    onEditOffer: (String) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onUpdateNickname: (String) -> Unit,
    onUpdateAnnualFee: (String) -> Unit,
    onUpdateLastFour: (String) -> Unit,
    onUpdateOpenDate: (String) -> Unit,
    onUpdateStatementCut: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateSubSpending: (String) -> Unit,
    onUpdateSubDuration: (String, String) -> Unit,
    onSelectCardFace: (String) -> Unit
) {
    val state by stateFlow.collectAsState()
    val detail = state.detail
    var showOpenDatePicker by remember { mutableStateOf(false) }
    var showStatementDatePicker by remember { mutableStateOf(false) }
    var showFacePicker by remember { mutableStateOf(false) }
    val openDatePickerState = androidx.compose.material3.rememberDatePickerState()
    val statementDatePickerState = androidx.compose.material3.rememberDatePickerState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Card Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More actions")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Switch card face") },
                            onClick = {
                                showMenu = false
                                showFacePicker = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        LaunchedEffect(events) {
            events.collect { message -> snackbarHostState.showSnackbar(message) }
        }
        when {
            state.isLoading -> DetailMessage("Loading...", Modifier.padding(padding))
            state.error != null -> DetailMessage("Error: ${'$'}{state.error}", Modifier.padding(padding))
            detail == null -> DetailMessage("No details available.", Modifier.padding(padding))
            else -> DetailContent(
                detail = detail,
                initialTab = initialTab,
                snackbarHostState = snackbarHostState,
                onAddBenefit = { onAddBenefit(detail.id, detail.productName) },
                onEditBenefit = { benefitId -> onEditBenefit(detail.id, benefitId) },
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
                cardFaces = state.cardFaces,
                showFacePicker = showFacePicker,
                onDismissFacePicker = { showFacePicker = false },
                onSelectCardFace = { onSelectCardFace(it) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    detail: CardDetailUi,
    initialTab: Int,
    snackbarHostState: SnackbarHostState,
    onAddBenefit: () -> Unit,
    onEditBenefit: (String) -> Unit,
    onDeleteBenefit: (String) -> Unit,
    onAddOffer: () -> Unit,
    onEditOffer: (String) -> Unit,
    onDeleteOffer: (String) -> Unit,
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
    cardFaces: List<CardFaceUi>,
    showFacePicker: Boolean,
    onDismissFacePicker: () -> Unit,
    onSelectCardFace: (String) -> Unit,
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

    Box(modifier = modifier.fillMaxSize()) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = listBottomPadding
            )
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!detail.cardFaceUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = detail.cardFaceUrl,
                                contentDescription = "Card face",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 200.dp)
                                    .aspectRatio(1.6f, matchHeightConstraintsFirst = false)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 200.dp)
                                    .aspectRatio(1.6f, matchHeightConstraintsFirst = false)
                                    .clip(RoundedCornerShape(12.dp))
                                    .padding(4.dp)
                            ) {
                                Text(
                                    "No card face",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            item {
                TabRow(selectedTabIndex = currentPage) {
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
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SignupBonusTab(
                                    spending = detail.subSpending,
                                    duration = detail.subDuration,
                                    durationUnit = detail.subDurationUnit ?: "months",
                                    openDate = detail.openDate,
                                    onUpdateSpending = onUpdateSubSpending,
                                    onUpdateDuration = onUpdateSubDuration
                                )
                            }
                        }
                        2 -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                            ) {
                                detail.benefits.forEachIndexed { index, benefit ->
                                    val shape = listItemShape(index, detail.benefits.size)
                                    BenefitCard(
                                        benefit = benefit,
                                        shape = shape,
                                        onEdit = { onEditBenefit(benefit.id) },
                                        onDelete = { onDeleteBenefit(benefit.id) }
                                    )
                                }
                            }
                        }
                        3 -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                            ) {
                                if (detail.offers.isEmpty()) {
                                    DetailMessage("No offers yet.")
                                } else {
                                    detail.offers.forEachIndexed { index, offer ->
                                        val shape = listItemShape(index, detail.offers.size)
                                        OfferCard(
                                            offer = offer,
                                            shape = shape,
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

        if (showFacePicker) {
            val maxSheetHeight = (LocalConfiguration.current.screenHeightDp / 2).dp
            ModalBottomSheet(
                onDismissRequest = onDismissFacePicker,
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select card face", style = MaterialTheme.typography.titleMedium)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxSheetHeight)
                    ) {
                        items(cardFaces) { face ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            onSelectCardFace(face.id)
                                            sheetState.hide()
                                            onDismissFacePicker()
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AsyncImage(
                                        model = face.remoteUrl,
                                        contentDescription = "Card face",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    val subtitle = when {
                                        face.isSelected -> "Current selection"
                                        face.isDefault -> "Default"
                                        else -> null
                                    }
                                    subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }
                }
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
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
private fun DetailMessage(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message)
    }
}

private fun listItemShape(index: Int, size: Int): RoundedCornerShape {
    val radius = 12.dp
    if (size <= 1) {
        return RoundedCornerShape(radius)
    }
    return when (index) {
        0 -> RoundedCornerShape(topStart = radius, topEnd = radius)
        size - 1 -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
}

private data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

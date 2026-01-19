package com.example.rewardsrader.ui.cardcreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCreateScreen(
    stateFlow: StateFlow<CardCreateState>,
    events: SharedFlow<CardCreateEvent>,
    onLoad: () -> Unit,
    onSync: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectSort: (SortMode) -> Unit,
    onToggleIssuer: (String) -> Unit,
    onToggleNetwork: (String) -> Unit,
    onToggleSegment: (String) -> Unit,
    onToggleInstrument: (String) -> Unit,
    onToggleBenefitType: (String) -> Unit,
    onToggleBenefitCategory: (String) -> Unit,
    onUpdateFeeRange: (ClosedFloatingPointRange<Float>) -> Unit,
    onToggleNoFee: () -> Unit,
    onResetFilters: () -> Unit,
    onSelectCard: (String) -> Unit,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val state by stateFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewItem by remember { mutableStateOf<CardSearchItem?>(null) }
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) { onLoad() }
    LaunchedEffect(state.query) {
        listState.scrollToItem(0)
    }
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                CardCreateEvent.Created -> onCreated()
                is CardCreateEvent.Error -> snackbarHostState.showSnackbar(event.message)
                CardCreateEvent.SyncSuccess -> snackbarHostState.showSnackbar("Synced card templates")
                is CardCreateEvent.SyncError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSync, enabled = !state.isSyncing) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync cards")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search cards") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* live search */ })
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = false,
                    onClick = { showSortSheet = true },
                    label = { Text("Sort") },
                    leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null) }
                )
                FilterChip(
                    selected = activeFilterCount(state) > 0,
                    onClick = { showFilterSheet = true },
                    label = {
                        val count = activeFilterCount(state)
                        Text(if (count > 0) "Filters ($count)" else "Filters")
                    },
                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.filteredResults.size} results",
                    fontWeight = FontWeight.SemiBold
                )
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }

            when {
                state.isLoading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Something went wrong",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.filteredResults.isEmpty() -> {
                    Text("No cards match your search yet. Adjust sort or filters.")
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        items(state.filteredResults, key = { it.id }) { item ->
                            CardResultRow(
                                item = item,
                                onOpen = {
                                    coroutineScope.launch {
                                        previewItem = item
                                        previewSheetState.show()
                                    }
                                },
                                onAdd = { onSelectCard(item.id) },
                                enabled = !state.isSaving
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sortSheetState
        ) {
            SortSheet(
                selected = state.sortMode,
                onSelect = { mode ->
                    coroutineScope.launch {
                        onSelectSort(mode)
                        listState.animateScrollToItem(0)
                        sortSheetState.hide()
                        showSortSheet = false
                    }
                }
            )
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.85f)) {
                FilterSheet(
                    state = state,
                    onToggleIssuer = onToggleIssuer,
                    onToggleNetwork = onToggleNetwork,
                    onToggleSegment = onToggleSegment,
                    onToggleInstrument = onToggleInstrument,
                    onToggleBenefitType = onToggleBenefitType,
                    onToggleBenefitCategory = onToggleBenefitCategory,
                    onUpdateFeeRange = onUpdateFeeRange,
                    onToggleNoFee = onToggleNoFee,
                    onReset = {
                        onResetFilters()
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                            filterSheetState.hide()
                            showFilterSheet = false
                        }
                    },
                    onApply = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                            filterSheetState.hide()
                            showFilterSheet = false
                        }
                    }
                )
            }
        }
    }

    previewItem?.let { selected ->
        LaunchedEffect(selected) {
            previewSheetState.show()
        }
        ModalBottomSheet(
            onDismissRequest = { previewItem = null },
            sheetState = previewSheetState
        ) {
            CardPreviewSheet(
                item = selected,
                onAdd = {
                    coroutineScope.launch {
                        onSelectCard(selected.id)
                        previewSheetState.hide()
                        previewItem = null
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        previewSheetState.hide()
                        previewItem = null
                    }
                }
            )
        }
    }
}

private fun activeFilterCount(state: CardCreateState): Int {
    val filters = state.filters
    var count = 0
    if (filters.issuerIds.isNotEmpty()) count++
    if (filters.networks.isNotEmpty()) count++
    if (filters.segments.isNotEmpty()) count++
    if (filters.paymentInstruments.isNotEmpty()) count++
    if (filters.benefitTypes.isNotEmpty()) count++
    if (filters.benefitCategories.isNotEmpty()) count++
    val boundsChanged = filters.annualFeeRange != null && filters.annualFeeRange != state.feeRangeBounds
    if (boundsChanged) count++
    if (filters.noAnnualFeeOnly) count++
    return count
}

package com.example.rewardsrader.ui.cardcreate

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rewardsrader.R
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCreateScreen(
    stateFlow: StateFlow<CardCreateState>,
    events: SharedFlow<CardCreateEvent>,
    onLoad: () -> Unit,
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

    LaunchedEffect(Unit) { onLoad() }
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                CardCreateEvent.Created -> onCreated()
                is CardCreateEvent.Error -> snackbarHostState.showSnackbar(event.message)
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
                                onClick = { onSelectCard(item.id) },
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
}

@Composable
private fun SortSheet(
    selected: SortMode,
    onSelect: (SortMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Sort by", fontWeight = FontWeight.SemiBold)
        SortMode.values().forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(mode) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadioButton(selected = selected == mode, onClick = { onSelect(mode) })
                Text(mode.label())
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FilterSheet(
    state: CardCreateState,
    onToggleIssuer: (String) -> Unit,
    onToggleNetwork: (String) -> Unit,
    onToggleSegment: (String) -> Unit,
    onToggleInstrument: (String) -> Unit,
    onToggleBenefitType: (String) -> Unit,
    onToggleBenefitCategory: (String) -> Unit,
    onUpdateFeeRange: (ClosedFloatingPointRange<Float>) -> Unit,
    onToggleNoFee: () -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxWidth()) {
        HeaderWithActions(onReset = onReset, onApply = onApply)
        Box(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterSection(title = "Issuer") {
                    FlowChips(
                        options = state.issuers.map { it.id to it.name },
                        selected = state.filters.issuerIds,
                        onToggle = onToggleIssuer,
                        iconFor = { issuerIconRes(it) }
                    )
                }
                if (state.networks.isNotEmpty()) {
                    FilterSection(title = "Network") {
                        FlowChips(
                            options = state.networks.map { it to it },
                            selected = state.filters.networks,
                            onToggle = onToggleNetwork
                        )
                    }
                }
                if (state.segments.isNotEmpty()) {
                    FilterSection(title = "Segment") {
                        FlowChips(
                            options = state.segments.map { it to it },
                            selected = state.filters.segments,
                            onToggle = onToggleSegment
                        )
                    }
                }
                if (state.paymentInstruments.isNotEmpty()) {
                    FilterSection(title = "Payment instrument") {
                        FlowChips(
                            options = state.paymentInstruments.map { it to it },
                            selected = state.filters.paymentInstruments,
                            onToggle = onToggleInstrument
                        )
                    }
                }
                FilterSection(title = "Annual fee") {
                    val range = state.filters.annualFeeRange ?: state.feeRangeBounds
                    val hasRange = state.feeRangeBounds.endInclusive > state.feeRangeBounds.start
                    if (hasRange) {
                        RangeSlider(
                            value = range,
                            onValueChange = onUpdateFeeRange,
                            valueRange = state.feeRangeBounds,
                            steps = 4
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "$${range.start.toInt()}")
                            Text(text = "$${range.endInclusive.toInt()}")
                        }
                        Spacer(Modifier.height(8.dp))
                    } else {
                        Text(text = "Annual fee: $${range.start.toInt()}")
                    }
                    FilterChip(
                        selected = state.filters.noAnnualFeeOnly,
                        onClick = onToggleNoFee,
                        label = { Text("No annual fee only") }
                    )
                }
                if (state.benefitTypes.isNotEmpty()) {
                    FilterSection(title = "Benefit type") {
                        FlowChips(
                            options = state.benefitTypes.map { it to it },
                            selected = state.filters.benefitTypes,
                            onToggle = onToggleBenefitType
                        )
                    }
                }
                if (state.benefitCategories.isNotEmpty()) {
                    FilterSection(title = "Benefit categories") {
                        FlowChips(
                            options = state.benefitCategories.map { it to it },
                            selected = state.filters.benefitCategories,
                            onToggle = onToggleBenefitCategory
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            ScrollIndicator(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun HeaderWithActions(
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Filters", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onReset) { Text("Reset") }
            TextButton(onClick = onApply) { Text("Apply") }
        }
    }
    HorizontalDivider()
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Medium)
        content()
    }
}

@Composable
private fun FlowChips(
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    iconFor: (String) -> Int? = { null }
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (id, label) ->
            val iconRes = iconFor(id)
            FilterChip(
                selected = selected.contains(id),
                onClick = { onToggle(id) },
                label = { Text(label) },
                leadingIcon = iconRes?.let { res ->
                    {
                        Icon(
                            painter = painterResource(res),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
//                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}

@Composable
private fun CardResultRow(
    item: CardSearchItem,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(),
        onClick = onClick,
        enabled = enabled
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = item.productName,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically

            ) {
                CardFacePreview(
                    url = item.cardFaceUrl,
                    label = item.productName
                )
                Text(
                    text = if (item.annualFee > 0) "$${floor(item.annualFee).toInt()}" else "No annual fee",
                    fontWeight = FontWeight.Medium
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxLines = 1,
            ) {
                item.benefitCategories.forEach { category ->
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(category) },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CardFacePreview(url: String?, label: String) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .width(160.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.CenterStart
    ) {
        if (url.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

private fun SortMode.label(): String = when (this) {
    SortMode.PRODUCT -> "Product (A-Z)"
    SortMode.ISSUER_PRODUCT -> "Issuer, then product"
    SortMode.ANNUAL_FEE_LOW_HIGH -> "Annual fee (low to high)"
    SortMode.ANNUAL_FEE_HIGH_LOW -> "Annual fee (high to low)"
    SortMode.NETWORK -> "Network"
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

private fun issuerIconRes(issuerId: String): Int? {
    val key = issuerId.lowercase().replace(" ", "_")
    return when (key) {
        "chase" -> R.drawable.ic_issuer_chase
        "citi" -> R.drawable.ic_issuer_citi
        "amex" -> R.drawable.ic_issuer_amex
        "capitalone" -> R.drawable.ic_issuer_capital_one
        "boa" -> R.drawable.ic_issuer_bofa
        "discover" -> R.drawable.ic_issuer_discover
        "barclays" -> R.drawable.ic_issuer_barclays
        "hsbc" -> R.drawable.ic_issuer_hsbc
        "usbank" -> R.drawable.ic_issuer_us_bank
        "wellsfargo" -> R.drawable.ic_issuer_wells_fargo
        else -> null
    }
}

@Composable
private fun ScrollIndicator(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
    Canvas(
        modifier = modifier
            .width(4.dp)
    ) {
        val trackWidth = size.width
        val trackHeight = size.height
        if (trackHeight <= 0f) return@Canvas
        drawRoundRect(
            color = trackColor,
            size = Size(trackWidth, trackHeight),
            cornerRadius = CornerRadius(trackWidth, trackWidth)
        )
        val max = scrollState.maxValue.toFloat()
        if (max <= 0f) {
            drawRoundRect(
                color = thumbColor,
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(trackWidth, trackWidth)
            )
            return@Canvas
        }
        val viewport = trackHeight
        val contentHeight = viewport + max
        val minThumb = 24.dp.toPx()
        val thumbHeight = (viewport * viewport / contentHeight).coerceIn(minThumb, viewport)
        val thumbOffset = (scrollState.value / max) * (viewport - thumbHeight)
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(0f, thumbOffset),
            size = Size(trackWidth, thumbHeight),
            cornerRadius = CornerRadius(trackWidth, trackWidth)
        )
    }
}


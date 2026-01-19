package com.example.rewardsrader.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    stateFlow: StateFlow<TrackerUiState>,
    onLoad: () -> Unit,
    onResume: () -> Unit,
    onSelectTracker: (String) -> Unit,
    onFilterChange: (TrackerStatus) -> Unit
) {
    val state by stateFlow.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { onLoad() }
    DisposableEffect(lifecycleOwner, onResume) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val filtered = state.trackers.filter { it.status == state.selectedFilter }
    val grouped = linkedMapOf<String, TrackerGroupUi>().also { map ->
        filtered.forEach { tracker ->
            val existing = map[tracker.profileCardId]
            val updated = if (existing == null) {
                TrackerGroupUi(
                    profileCardId = tracker.profileCardId,
                    cardName = tracker.cardName,
                    cardFaceUrl = tracker.cardFaceUrl,
                    trackers = mutableListOf(tracker)
                )
            } else {
                existing.trackers.add(tracker)
                existing
            }
            map[tracker.profileCardId] = updated
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tracker") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.selectedFilter == TrackerStatus.Active,
                    onClick = { onFilterChange(TrackerStatus.Active) },
                    label = { Text("Active (${state.activeCount})") }
                )
                FilterChip(
                    selected = state.selectedFilter == TrackerStatus.Complete,
                    onClick = { onFilterChange(TrackerStatus.Complete) },
                    label = { Text("Complete (${state.completeCount})") }
                )
                FilterChip(
                    selected = state.selectedFilter == TrackerStatus.Expired,
                    onClick = { onFilterChange(TrackerStatus.Expired) },
                    label = { Text("Expired (${state.expiredCount})") }
                )
            }

            when {
                state.isLoading -> {
                    Text("Loading trackers...")
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Something went wrong",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                filtered.isEmpty() -> {
                    Text("No trackers in this view yet.")
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        items(grouped.values.toList(), key = { it.profileCardId }) { group ->
                            TrackerCardGroup(
                                cardName = group.cardName,
                                cardFaceUrl = group.cardFaceUrl,
                                trackers = group.trackers,
                                onSelect = onSelectTracker
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackerCardGroup(
    cardName: String,
    cardFaceUrl: String?,
    trackers: List<TrackerItemUi>,
    onSelect: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val fullHeight = maxWidth / 1.6f
                val visibleHeight = fullHeight * 0.33f
                CardFaceStrip(
                    cardFaceUrl = cardFaceUrl,
                    visibleHeight = visibleHeight,
                    fullHeight = fullHeight
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = cardName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val sections = listOf(
                    TrackerSectionUi(
                        type = TrackerSourceType.Sub,
                        label = null,
                        trackers = trackers.filter { it.sourceType == TrackerSourceType.Sub }
                    ),
                    TrackerSectionUi(
                        type = TrackerSourceType.Offer,
                        label = "Offers",
                        trackers = trackers.filter { it.sourceType == TrackerSourceType.Offer }
                    ),
                    TrackerSectionUi(
                        type = TrackerSourceType.Benefit,
                        label = "Benefits",
                        trackers = trackers.filter { it.sourceType == TrackerSourceType.Benefit }
                    )
                ).filter { it.trackers.isNotEmpty() }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    sections.forEach { section ->
                        section.label?.let { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            section.trackers.forEachIndexed { index, tracker ->
                                val shape = when {
                                    section.trackers.size == 1 -> RoundedCornerShape(12.dp)
                                    index == 0 -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                    index == section.trackers.lastIndex ->
                                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                    else -> RoundedCornerShape(0.dp)
                                }
                                TrackerListItem(
                                    tracker = tracker,
                                    onSelect = onSelect,
                                    shape = shape
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackerListItem(
    tracker: TrackerItemUi,
    onSelect: (String) -> Unit,
    shape: RoundedCornerShape
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = shape,
        onClick = { onSelect(tracker.id) }
    ) {
        val showTitle = tracker.title.isNotBlank()
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(if (showTitle) 6.dp else 0.dp)
        ) {
            if (showTitle) {
                Text(
                    text = tracker.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatTrackerAmount(tracker.usedAmount)} used / ${formatTrackerAmount(tracker.amount)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = tracker.timeLeftLabel,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CardFaceStrip(
    cardFaceUrl: String?,
    visibleHeight: androidx.compose.ui.unit.Dp,
    fullHeight: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(visibleHeight)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .clipToBounds()
    ) {
        if (!cardFaceUrl.isNullOrBlank()) {
            AsyncImage(
                model = cardFaceUrl,
                contentDescription = "Card face",
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fullHeight)
            )
        } else {
            Text(
                text = "No card face",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private data class TrackerGroupUi(
    val profileCardId: String,
    val cardName: String,
    val cardFaceUrl: String?,
    val trackers: MutableList<TrackerItemUi>
)

private data class TrackerSectionUi(
    val type: TrackerSourceType,
    val label: String?,
    val trackers: List<TrackerItemUi>
)

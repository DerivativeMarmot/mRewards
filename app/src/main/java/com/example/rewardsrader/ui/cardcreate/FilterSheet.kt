package com.example.rewardsrader.ui.cardcreate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rewardsrader.R

@Composable
fun FilterSheet(
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
                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
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
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp),
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
            )
        }
    }
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
fun ScrollIndicator(
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

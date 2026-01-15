package com.example.rewardsrader.ui.cardcreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.BenefitType
import kotlin.math.floor

@Composable
fun CardPreviewSheet(
    item: CardSearchItem,
    onAdd: () -> Unit,
    onClose: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add card")
            }
        }
        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp)
                    .aspectRatio(1.6f, matchHeightConstraintsFirst = false)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.cardFaceUrl.isNullOrBlank()) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    AsyncImage(
                        model = item.cardFaceUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Text(
                text = if (item.annualFee > 0) "$${floor(item.annualFee).toInt()} annual fee" else "No annual fee",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Benefits",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.benefits.forEach { benefit ->
                    Text(
                        text = "- ${formatBenefit(benefit)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun formatBenefit(benefit: BenefitEntity): String {
    val title = benefit.title?.takeIf { it.isNotBlank() }
    return when (benefit.type) {
        BenefitType.Credit -> {
            val amountText = benefit.amount?.let { "$${trimAmount(it)}" } ?: ""
            val cadence = benefit.frequency.name.lowercase().replaceFirstChar { it.uppercase() }
            listOfNotNull(amountText, title, cadence).filter { it.isNotBlank() }.joinToString(" ")
        }
        BenefitType.Multiplier -> {
            val rate = benefit.amount?.let { trimAmount(it) } ?: ""
            val rateText = if (rate.isNotBlank()) "${rate}x" else ""
            listOfNotNull(rateText, title).filter { it.isNotBlank() }.joinToString(" ")
        }
    }
}

private fun trimAmount(value: Double): String {
    val formatted = String.format("%.2f", value)
    return formatted.trimEnd('0').trimEnd('.')
}

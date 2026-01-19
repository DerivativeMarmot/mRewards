package com.example.rewardsrader.ui.carddetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rewardsrader.ui.carddetail.BenefitUi
import com.example.rewardsrader.ui.carddetail.OfferUi

@Composable
fun BenefitCard(
    benefit: BenefitUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    shape: Shape
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val primaryLine = listOf(benefit.amount, benefit.title.orEmpty())
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                if (primaryLine.isNotBlank()) {
                    Text(primaryLine, fontWeight = FontWeight.SemiBold)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete benefit", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun OfferCard(
    offer: OfferUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    shape: Shape
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(offer.title, fontWeight = FontWeight.SemiBold)
                if (offer.window.isNotBlank()) {
                    Text(offer.window, style = MaterialTheme.typography.bodyMedium)
                }
                offer.details?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                Text(offer.status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete offer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

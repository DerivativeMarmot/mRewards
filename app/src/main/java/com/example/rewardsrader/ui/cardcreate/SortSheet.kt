package com.example.rewardsrader.ui.cardcreate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SortSheet(
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

fun SortMode.label(): String = when (this) {
    SortMode.PRODUCT -> "Product (A-Z)"
    SortMode.ISSUER_PRODUCT -> "Issuer, then product"
    SortMode.ANNUAL_FEE_LOW_HIGH -> "Annual fee (low to high)"
    SortMode.ANNUAL_FEE_HIGH_LOW -> "Annual fee (high to low)"
    SortMode.NETWORK -> "Network"
}

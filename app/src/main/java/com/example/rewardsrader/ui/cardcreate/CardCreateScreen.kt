package com.example.rewardsrader.ui.cardcreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rewardsrader.config.CardTemplate
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCreateScreen(
    stateFlow: StateFlow<CardCreateState>,
    onLoad: () -> Unit,
    onTemplateSelected: (Int) -> Unit,
    onOpenDateChange: (String) -> Unit,
    onStatementCutChange: (String) -> Unit,
    onApplicationStatusChange: (String) -> Unit,
    onWelcomeOfferChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val state by stateFlow.collectAsState()
    LaunchedEffect(Unit) { onLoad() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Card") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TemplateSelector(
                templates = state.templates,
                selected = state.selectedTemplateId,
                onSelect = onTemplateSelected
            )

            OutlinedTextField(
                value = state.openDateUtc,
                onValueChange = onOpenDateChange,
                label = { Text("Open date (MM/dd/yyyy hh:mm UTC)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.statementCutUtc,
                onValueChange = onStatementCutChange,
                label = { Text("Statement cut (MM/dd/yyyy hh:mm UTC)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.applicationStatus,
                onValueChange = onApplicationStatusChange,
                label = { Text("Application status") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.welcomeOfferProgress,
                onValueChange = onWelcomeOfferChange,
                label = { Text("Welcome offer progress") },
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let { Text("Error: $it") }
            if (state.success) Text("Saved!")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onBack, enabled = !state.isSaving) {
                    Text("Cancel")
                }
                Button(onClick = onSave, enabled = !state.isSaving) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun TemplateSelector(
    templates: List<CardTemplate>,
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    Column {
        Text("Select template")
        Spacer(Modifier.height(8.dp))
        templates.forEach { template ->
            val isSelected = template.cardId == selected
            Button(
                onClick = { onSelect(template.cardId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSelected
            ) {
                Text("${template.productName} (${template.issuer})")
            }
        }
    }
}

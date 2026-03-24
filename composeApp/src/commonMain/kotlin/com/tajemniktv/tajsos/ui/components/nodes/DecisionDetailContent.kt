/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.DecisionOptionEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme
import kotlinx.coroutines.launch

@Composable
fun DecisionDetailContent(
    viewModel: MainViewModel,
    node: NodeEntity,
    onNavigateToProject: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val options by viewModel.getOptionsForDecision(node.id).collectAsState(initial = emptyList())

    var showAddOptionDialog by remember { mutableStateOf(false) }
    var showDecideDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        SectionTitle("DECISION STATUS")
        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            val statuses = listOf("pending", "parked", "expired")
            statuses.forEach { status ->
                FilterChip(
                    selected = node.decisionStatus == status,
                    onClick = {
                        viewModel.updateNode(node.copy(decisionStatus = status))
                    },
                    label = { Text(status.uppercase()) }
                )
            }
        }

        SectionTitle("DECISION CATEGORY")
        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            val categories = listOf("tiny", "major")
            categories.forEach { category ->
                FilterChip(
                    selected = node.decisionCategory == category,
                    onClick = {
                        viewModel.updateNode(node.copy(decisionCategory = category))
                    },
                    label = { Text(category.uppercase()) }
                )
            }
        }

        DecisionField(
            label = "WHAT INFO IS MISSING?",
            value = node.decisionInfoMissing ?: "",
            onValueChange = { viewModel.updateNode(node.copy(decisionInfoMissing = it)) }
        )

        DecisionField(
            label = "WHAT MAKES THIS DIFFICULT?",
            value = node.decisionDifficultBecause ?: "",
            onValueChange = { viewModel.updateNode(node.copy(decisionDifficultBecause = it)) }
        )

        DecisionField(
            label = "WHAT WOULD MAKE THIS EASIER?",
            value = node.decisionEasierIf ?: "",
            onValueChange = { viewModel.updateNode(node.copy(decisionEasierIf = it)) }
        )

        HorizontalDivider(
            color = TactileTheme.Border,
            modifier = Modifier.padding(vertical = TactileTheme.SpacingSm)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("OPTIONS")
            IconButton(onClick = { showAddOptionDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = TactileTheme.Primary)
            }
        }

        if (options.isEmpty()) {
            Text(
                "NO OPTIONS ADDED YET",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.padding(vertical = TactileTheme.SpacingSm)
            )
        } else {
            options.forEach { option ->
                OptionCard(
                    option = option,
                    onUpdate = { viewModel.updateDecisionOption(it) },
                    onDelete = { viewModel.deleteDecisionOption(it) }
                )
            }
        }

        HorizontalDivider(
            color = TactileTheme.Border,
            modifier = Modifier.padding(vertical = TactileTheme.SpacingSm)
        )

        if (node.decisionStatus == "decided") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = TactileTheme.Success
                )
                Spacer(modifier = Modifier.width(TactileTheme.SpacingSm))
                Column {
                    Text(
                        "OUTCOME",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Success
                    )
                    Text(
                        node.decisionOutcome ?: "NO OUTCOME RECORDED",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TactileTheme.Text
                    )
                }
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

            Button(
                onClick = { viewModel.convertDecisionToProject(node.id) },
                colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Accent),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("CONVERT TO ACTION PLAN (PROJECT)")
            }

            OutlinedButton(
                onClick = { viewModel.convertDecisionToTask(node.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, TactileTheme.Border)
            ) {
                Text("CONVERT TO FOLLOW-UP TASK", color = TactileTheme.Text)
            }
        } else {
            Button(
                onClick = { showDecideDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Primary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("FINALIZE DECISION")
            }
        }
    }

    if (showAddOptionDialog) {
        var optionTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddOptionDialog = false },
            title = { Text("ADD OPTION") },
            text = {
                OutlinedTextField(
                    value = optionTitle,
                    onValueChange = { optionTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (optionTitle.isNotEmpty()) {
                        viewModel.addDecisionOption(node.id, optionTitle)
                        showAddOptionDialog = false
                    }
                }) { Text("ADD") }
            },
            dismissButton = {
                TextButton(onClick = { showAddOptionDialog = false }) { Text("CANCEL") }
            },
            containerColor = TactileTheme.Background,
            titleContentColor = TactileTheme.Text,
            textContentColor = TactileTheme.Text
        )
    }

    if (showDecideDialog) {
        var outcome by remember { mutableStateOf("") }
        var selectedOptionId by remember { mutableStateOf<Long?>(null) }

        AlertDialog(
            onDismissRequest = { showDecideDialog = false },
            title = { Text("FINALIZE DECISION") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    Text("Selected Option:", style = MaterialTheme.typography.labelSmall)
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                                .clickable { selectedOptionId = option.id }
                        ) {
                            RadioButton(
                                selected = selectedOptionId == option.id,
                                onClick = { selectedOptionId = option.id })
                            Text(option.title)
                        }
                    }
                    OutlinedTextField(
                        value = outcome,
                        onValueChange = { outcome = it },
                        label = { Text("Outcome / Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (outcome.isNotEmpty()) {
                        viewModel.decideOn(node.id, outcome, selectedOptionId)
                        showDecideDialog = false
                    }
                }) { Text("DECIDE") }
            },
            dismissButton = {
                TextButton(onClick = { showDecideDialog = false }) { Text("CANCEL") }
            },
            containerColor = TactileTheme.Background,
            titleContentColor = TactileTheme.Text,
            textContentColor = TactileTheme.Text
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = TactileTheme.Muted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun DecisionField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        SectionTitle(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(vertical = TactileTheme.SpacingSm),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TactileTheme.Text),
            cursorBrush = SolidColor(TactileTheme.Primary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            "Tap to add...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TactileTheme.Muted
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun OptionCard(
    option: DecisionOptionEntity,
    onUpdate: (DecisionOptionEntity) -> Unit,
    onDelete: (DecisionOptionEntity) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Background,
        border = BorderStroke(1.dp, TactileTheme.Border),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingSm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    option.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onDelete(option) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = TactileTheme.Muted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (option.description != null) {
                Text(
                    option.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted
                )
            }
        }
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.InboxEntryEntity
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.inbox_add
import tajsos.composeapp.generated.resources.inbox_empty
import tajsos.composeapp.generated.resources.inbox_placeholder
import tajsos.composeapp.generated.resources.inbox_process
import tajsos.composeapp.generated.resources.inbox_quick_capture
import tajsos.composeapp.generated.resources.inbox_recent_entries
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_task

object InboxDashboardBlockRegistry {
    private val renderers: Map<String, InboxDashboardBlockRenderer> =
        mapOf("inbox_main" to ::renderInboxMainBlock)

    fun resolve(id: String): InboxDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderInboxMainBlock(context: InboxDashboardContext) {
    InboxMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InboxMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    var itemInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("inbox") }
    val inboxEntries by viewModel.inboxEntries.collectAsState()
    val nodes by viewModel.inboxNodes.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            stringResource(Res.string.inbox_quick_capture),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedType == "inbox",
                onClick = { selectedType = "inbox" },
                label = { Text("CAPTURE") },
            )
            FilterChip(
                selected = selectedType == "task",
                onClick = { selectedType = "task" },
                label = { Text(stringResource(Res.string.type_task)) },
            )
            FilterChip(
                selected = selectedType == "note",
                onClick = { selectedType = "note" },
                label = { Text(stringResource(Res.string.type_note)) },
            )
            FilterChip(
                selected = selectedType == "record",
                onClick = { selectedType = "record" },
                label = { Text("RECORD") },
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = itemInput,
            onValueChange = { itemInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(Res.string.inbox_placeholder)) },
            trailingIcon = {
                FilledIconButton(
                    onClick = {
                        if (selectedType == "inbox") {
                            viewModel.captureInboxEntry(itemInput)
                        } else {
                            viewModel.addNode(itemInput, type = selectedType)
                        }
                        itemInput = ""
                    },
                    enabled = itemInput.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(Res.string.inbox_add),
                        tint =
                            if (itemInput.isNotBlank()) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.38f,
                                )
                            },
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        if (itemInput.isNotBlank()) {
                            if (selectedType == "inbox") {
                                viewModel.captureInboxEntry(itemInput)
                            } else {
                                viewModel.addNode(itemInput, type = selectedType)
                            }
                            itemInput = ""
                        }
                    },
                ),
            shape = RoundedCornerShape(16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
        if (inboxEntries.isNotEmpty()) {
            Text(
                "RAW CAPTURE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                inboxEntries.forEach { entry ->
                    InboxCaptureCard(entry = entry, viewModel = viewModel)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            stringResource(Res.string.inbox_recent_entries),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (nodes.isEmpty() && inboxEntries.isEmpty()) {
            EmptyState(message = stringResource(Res.string.inbox_empty))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                nodes.forEach { nodeWithPin ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NodeCard(
                            modifier = Modifier.weight(1f),
                            nodeWithPin = nodeWithPin,
                            onToggleDone = { status: String ->
                                viewModel.updateNodeStatus(
                                    nodeWithPin.node,
                                    status,
                                )
                            },
                            onTogglePin = { isPinned: Boolean ->
                                viewModel.togglePin(
                                    nodeWithPin.node,
                                    isPinned,
                                )
                            },
                            onClick = { onEditNode(nodeWithPin.node.id) },
                            onLongClick = { onEditNode(nodeWithPin.node.id) },
                            onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                        )

                        IconButton(onClick = { viewModel.markAsProcessed(nodeWithPin.node.id) }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(Res.string.inbox_process),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxCaptureCard(
    entry: InboxEntryEntity,
    viewModel: MainViewModel,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = entry.rawText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = false,
                    onClick = { viewModel.triageInboxEntry(entry.id, ItemKind.TASK) },
                    label = { Text("TASK") },
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.triageInboxEntry(entry.id, ItemKind.NOTE) },
                    label = { Text("NOTE") },
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.triageInboxEntry(entry.id, ItemKind.RECORD) },
                    label = { Text("RECORD") },
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.triageInboxEntry(entry.id, ItemKind.PROJECT) },
                    label = { Text("PROJECT") },
                )
                IconButton(onClick = { viewModel.dismissInboxEntry(entry) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss capture",
                    )
                }
            }
        }
    }
}

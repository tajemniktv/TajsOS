/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.nodes.NodeCard
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    var itemInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("task") }
    val nodes by viewModel.inboxNodes.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            stringResource(Res.string.inbox_quick_capture),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                selected = selectedType == "idea",
                onClick = { selectedType = "idea" },
                label = { Text(stringResource(Res.string.type_idea)) },
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = itemInput,
            onValueChange = { itemInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(Res.string.inbox_placeholder)) },
            trailingIcon = {
                if (itemInput.isNotBlank()) {
                    IconButton(onClick = {
                        viewModel.addNode(itemInput, type = selectedType)
                        itemInput = ""
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(Res.string.inbox_add),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(onDone = {
                    if (itemInput.isNotBlank()) {
                        viewModel.addNode(itemInput, type = selectedType)
                        itemInput = ""
                    }
                }),
            shape = RoundedCornerShape(16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(Res.string.inbox_recent_entries),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (nodes.isEmpty()) {
            EmptyState(message = stringResource(Res.string.inbox_empty))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(nodes, key = { it.node.id }) { nodeWithPin ->
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

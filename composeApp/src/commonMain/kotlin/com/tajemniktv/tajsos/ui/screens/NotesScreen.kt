/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.EmptyState
import com.tajemniktv.tajsos.ui.components.NodeCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@Composable
fun NotesScreen(
    viewModel: MainViewModel,
    onNoteClick: (Long) -> Unit,
) {
    val activeNodes by viewModel.activeNodes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredNodes =
        remember(activeNodes, searchQuery) {
            if (searchQuery.isBlank()) {
                activeNodes
            } else {
                activeNodes.filter {
                    it.node.title.contains(searchQuery, ignoreCase = true) ||
                        it.node.content.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    val pinnedKnowledge =
        filteredNodes.filter { it.node.isPinned && (it.node.type == "note" || it.node.type == "idea" || it.node.type == "resource") }
    val unpinnedIdeas = filteredNodes.filter { !it.node.isPinned && it.node.type == "idea" }
    val unpinnedNotes = filteredNodes.filter { !it.node.isPinned && it.node.type == "note" }
    val unpinnedResources = filteredNodes.filter { !it.node.isPinned && it.node.type == "resource" }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                stringResource(Res.string.notes_title),
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.notes_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (pinnedKnowledge.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.notes_pinned_knowledge),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(pinnedKnowledge, key = { "pinned_${it.node.id}" }) { nodeWithPin ->
                NodeCard(
                    nodeWithPin = nodeWithPin,
                    onClick = { onNoteClick(nodeWithPin.node.id) },
                    onToggleDone = { status ->
                        viewModel.updateNodeStatus(
                            nodeWithPin.node,
                            status,
                        )
                    },
                    onTogglePin = { isPinned -> viewModel.togglePin(nodeWithPin.node, isPinned) },
                    onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (unpinnedIdeas.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.notes_ideas),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(unpinnedIdeas, key = { it.node.id }) { nodeWithPin ->
                NodeCard(
                    nodeWithPin = nodeWithPin,
                    onClick = { onNoteClick(nodeWithPin.node.id) },
                    onToggleDone = { status ->
                        viewModel.updateNodeStatus(
                            nodeWithPin.node,
                            status,
                        )
                    },
                    onTogglePin = { isPinned -> viewModel.togglePin(nodeWithPin.node, isPinned) },
                    onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (unpinnedNotes.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.notes_notes),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(unpinnedNotes, key = { it.node.id }) { nodeWithPin ->
                NodeCard(
                    nodeWithPin = nodeWithPin,
                    onClick = { onNoteClick(nodeWithPin.node.id) },
                    onToggleDone = { status ->
                        viewModel.updateNodeStatus(
                            nodeWithPin.node,
                            status,
                        )
                    },
                    onTogglePin = { isPinned -> viewModel.togglePin(nodeWithPin.node, isPinned) },
                    onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (unpinnedResources.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.notes_resources),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(unpinnedResources, key = { it.node.id }) { nodeWithPin ->
                NodeCard(
                    nodeWithPin = nodeWithPin,
                    onClick = { onNoteClick(nodeWithPin.node.id) },
                    onToggleDone = { status ->
                        viewModel.updateNodeStatus(
                            nodeWithPin.node,
                            status,
                        )
                    },
                    onTogglePin = { isPinned -> viewModel.togglePin(nodeWithPin.node, isPinned) },
                    onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                )
            }
        }

        if (pinnedKnowledge.isEmpty() && unpinnedIdeas.isEmpty() && unpinnedNotes.isEmpty() && unpinnedResources.isEmpty()) {
            item {
                EmptyState(
                    message = if (searchQuery.isEmpty()) stringResource(Res.string.notes_empty) else stringResource(
                        Res.string.notes_no_results
                    )
                )
            }
        }
    }
}

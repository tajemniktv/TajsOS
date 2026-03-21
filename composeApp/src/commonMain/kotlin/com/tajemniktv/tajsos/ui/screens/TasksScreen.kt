/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.EmptyState
import com.tajemniktv.tajsos.ui.components.NodeCard

@Composable
fun TasksScreen(viewModel: MainViewModel, onEditNode: (Long) -> Unit) {
    val nodes by viewModel.allNodes.collectAsState()
    val tasks = nodes.filter { it.node.type == "task" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("TASKS", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(24.dp))

        if (tasks.isEmpty()) {
            EmptyState(message = "NO TASKS FOUND")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tasks, key = { it.node.id }) { nodeWithPin ->
                    NodeCard(
                        nodeWithPin = nodeWithPin,
                        onToggleDone = { status ->
                            viewModel.updateNodeStatus(
                                nodeWithPin.node,
                                status
                            )
                        },
                        onTogglePin = { isPinned ->
                            viewModel.togglePin(
                                nodeWithPin.node,
                                isPinned
                            )
                        },
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        onLongClick = { onEditNode(nodeWithPin.node.id) },
                        onArchive = { viewModel.archiveNode(nodeWithPin.node) }
                    )
                }
            }
        }
    }
}

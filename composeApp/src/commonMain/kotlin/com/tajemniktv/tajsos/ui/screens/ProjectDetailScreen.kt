/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.NodeCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    viewModel: MainViewModel,
    projectId: Long,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit
) {
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, projectId) { nodes.find { it.node.id == projectId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Project not found", modifier = Modifier.padding(TactileTheme.SpacingMd))
        }
        return
    }

    val project = nodeWithPin.node

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PROJECT // ${project.status.uppercase()}",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.archiveNode(project)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Archive")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TactileTheme.SpacingMd)
        ) {
            Text(
                text = project.title.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = TactileTheme.Text
            )
            if (project.content.isNotEmpty()) {
                Text(
                    text = project.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Muted
                )
            }
            Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

            Text(
                text = "TASKS & NOTES",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

            val nodesWithPinForProject by viewModel.getNodesForProject(projectId)
                .collectAsState(initial = emptyList())

            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(nodesWithPinForProject) { item ->
                    NodeCard(
                        nodeWithPin = item,
                        onToggleDone = { status ->
                            viewModel.updateNodeStatus(
                                item.node,
                                status
                            )
                        },
                        onTogglePin = { isPinned -> viewModel.togglePin(item.node, isPinned) },
                        onLongClick = { onEditNode(item.node.id) },
                        onArchive = { viewModel.archiveNode(item.node) }
                    )
                }
            }
        }
    }
}

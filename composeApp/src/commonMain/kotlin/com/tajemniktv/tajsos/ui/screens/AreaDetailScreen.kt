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
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.NodeCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDetailScreen(
    viewModel: MainViewModel, 
    areaId: Long, 
    onNavigateToProject: (Long) -> Unit,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit
) {
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, areaId) { nodes.find { it.node.id == areaId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Area not found", modifier = Modifier.padding(TactileTheme.SpacingMd))
        }
        return
    }

    val area = nodeWithPin.node
    val projects by viewModel.getProjectsForArea(areaId).collectAsState(initial = emptyList())
    val nodesWithPinInArea by viewModel.getNodesForArea(areaId)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AREA // ${area.status.uppercase()}",
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
                        viewModel.archiveNode(area)
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
                text = area.title.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = TactileTheme.Text
            )
            Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

            if (projects.isNotEmpty()) {
                Text(
                    text = "PROJECTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
                ) {
                    items(projects) { project ->
                        val projectNodes = nodes.filter { it.node.projectId == project.id }
                        val total = projectNodes.size
                        val completed = projectNodes.count { it.node.status == "done" }
                        val progress = if (total > 0) completed.toFloat() / total else 0f

                        ProjectItem(
                            project,
                            progress,
                            total
                        ) { onNavigateToProject(project.id) }
                    }
                }
                Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
            }

            Text(
                text = "DIRECT ITEMS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(nodesWithPinInArea) { item ->
                    NodeCard(
                        nodeWithPin = item,
                        onToggleDone = { status ->
                            viewModel.updateNodeStatus(
                                item.node,
                                status
                            )
                        },
                        onTogglePin = { isPinned -> viewModel.togglePin(item.node, isPinned) },
                        onClick = { onEditNode(item.node.id) },
                        onLongClick = { onEditNode(item.node.id) },
                        onArchive = { viewModel.archiveNode(item.node) }
                    )
                }
            }
        }
    }
}

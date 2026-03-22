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
import com.tajemniktv.tajsos.ui.components.ProjectItem
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 *
 */
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
            Text(
                stringResource(Res.string.area_detail_not_found),
                modifier = Modifier.padding(TactileTheme.SpacingMd)
            )
        }
        return
    }

    val area = nodeWithPin.node

    LaunchedEffect(areaId) {
        viewModel.setLastActiveContext(null, areaId)
    }

    val projects by viewModel.getProjectsForArea(areaId).collectAsState(initial = emptyList())
    val nodesWithPinInArea by viewModel.getNodesForArea(areaId)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${stringResource(Res.string.type_area).uppercase()} // ${area.status.uppercase()}",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.detail_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.archiveNode(area)
                        onBack()
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.detail_archive)
                        )
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

            val activeProjects =
                projects.filter { it.status == "active" || it.projectStatus == "active" }

            val areaTasks = nodesWithPinInArea.filter { it.node.type == "task" }
            val staleTime =
                kotlin.time.Clock.System.now().toEpochMilliseconds() - (14 * 24 * 60 * 60 * 1000L)
            val neglectedTasks =
                areaTasks.count { it.node.status == "active" && it.node.updatedAt < staleTime }

            if (neglectedTasks > 0) {
                Surface(
                    color = TactileTheme.Error.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.area_detail_neglected_tasks, neglectedTasks),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

            if (activeProjects.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.area_detail_active_projects),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                activeProjects.forEach { project ->
                    val projectNodes = nodes.filter { it.node.projectId == project.id }
                    val total = projectNodes.size
                    val completed = projectNodes.count { it.node.status == "done" }
                    val progress = if (total > 0) completed.toFloat() / total else 0f

                    ProjectItem(
                        project = project,
                        progress = progress,
                        totalItems = total,
                        onLongClick = { onEditNode(project.id) }
                    ) { onNavigateToProject(project.id) }
                    Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                }
                Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
            }

            Text(
                text = stringResource(Res.string.area_detail_recent_activity),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(nodesWithPinInArea.sortedByDescending { it.node.updatedAt }
                    .take(10)) { item ->
                    NodeCard(
                        nodeWithPin = item,
                        onToggleDone = { status -> viewModel.updateNodeStatus(item.node, status) },
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

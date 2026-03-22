/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.NodeCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

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
            Text(
                stringResource(Res.string.project_detail_not_found),
                modifier = Modifier.padding(TactileTheme.SpacingMd)
            )
        }
        return
    }

    val project = nodeWithPin.node
    val nodesWithPinForProject by viewModel.getNodesForProject(projectId)
        .collectAsState(initial = emptyList())

    val total = nodesWithPinForProject.size
    val completed = nodesWithPinForProject.count { it.node.status == "done" }
    val progress = if (total > 0) completed.toFloat() / total else 0f

    val staleTime =
        kotlin.time.Clock.System.now().toEpochMilliseconds() - (14 * 24 * 60 * 60 * 1000L)
    val isNeglected =
        nodesWithPinForProject.none { it.node.updatedAt >= staleTime } && project.status == "active"

    LaunchedEffect(projectId) {
        viewModel.setLastActiveContext(projectId, project.areaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${stringResource(Res.string.type_project).uppercase()} // ${project.projectStatus?.uppercase() ?: project.status.uppercase()}",
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
                        onEditNode(projectId)
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.project_detail_edit)
                        )
                    }
                    IconButton(onClick = {
                        viewModel.updateNode(project.copy(isFrozen = !project.isFrozen))
                    }) {
                        Icon(
                            if (project.isFrozen) Icons.Default.AcUnit else Icons.Default.WbSunny,
                            contentDescription = stringResource(Res.string.project_detail_freeze),
                            tint = if (project.isFrozen) TactileTheme.Accent else TactileTheme.Primary
                        )
                    }
                    IconButton(onClick = {
                        viewModel.archiveNode(project)
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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = project.title.uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    color = if (project.isFrozen) TactileTheme.Muted else TactileTheme.Text,
                    modifier = Modifier.weight(1f)
                )
                if (isNeglected) {
                    Text(
                        stringResource(Res.string.project_detail_neglected),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            if (project.isFrozen) {
                Surface(
                    color = TactileTheme.Accent.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.project_detail_frozen_msg),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Accent,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Text(
                text = stringResource(
                    Res.string.project_detail_why,
                    project.projectWhy ?: stringResource(Res.string.detail_not_set)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (project.content.isNotEmpty()) {
                Text(
                    text = project.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Muted
                )
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (project.isFrozen) TactileTheme.Muted else TactileTheme.Primary,
                trackColor = TactileTheme.Border,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Text(
                stringResource(
                    Res.string.project_detail_progress,
                    (progress * 100).toInt(),
                    completed,
                    total
                ),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(TactileTheme.SpacingLg))

            Text(
                text = stringResource(Res.string.project_detail_next_actions),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            val nextActions =
                nodesWithPinForProject.filter { it.node.status == "active" && it.node.type == "task" }
            if (nextActions.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_no_tasks),
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted
                )
            } else {
                nextActions.take(3).forEach { item ->
                    NodeCard(
                        nodeWithPin = item,
                        onToggleDone = { status -> viewModel.updateNodeStatus(item.node, status) },
                        onTogglePin = { isPinned -> viewModel.togglePin(item.node, isPinned) },
                        onClick = { onEditNode(item.node.id) },
                        onLongClick = { onEditNode(item.node.id) },
                        onArchive = { viewModel.archiveNode(item.node) }
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                }
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingLg))

            val linkedNotes =
                nodesWithPinForProject.filter { it.node.type == "note" || it.node.type == "idea" }
            if (linkedNotes.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.project_detail_linked_notes),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                linkedNotes.forEach { item ->
                    NodeCard(
                        nodeWithPin = item,
                        onToggleDone = { status -> viewModel.updateNodeStatus(item.node, status) },
                        onTogglePin = { isPinned -> viewModel.togglePin(item.node, isPinned) },
                        onClick = { onEditNode(item.node.id) },
                        onLongClick = { onEditNode(item.node.id) },
                        onArchive = { viewModel.archiveNode(item.node) }
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                }
                Spacer(modifier = Modifier.height(TactileTheme.SpacingLg))
            }

            val linkedResources = nodesWithPinForProject.filter { it.node.type == "resource" }
            if (linkedResources.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.project_detail_resources),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                linkedResources.forEach { item ->
                    NodeCard(
                        nodeWithPin = item,
                        onToggleDone = { status -> viewModel.updateNodeStatus(item.node, status) },
                        onTogglePin = { isPinned -> viewModel.togglePin(item.node, isPinned) },
                        onClick = { onEditNode(item.node.id) },
                        onLongClick = { onEditNode(item.node.id) },
                        onArchive = { viewModel.archiveNode(item.node) }
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                }
                Spacer(modifier = Modifier.height(TactileTheme.SpacingLg))
            }

            Text(
                text = stringResource(Res.string.project_detail_timeline),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
            val logs by viewModel.getLogsForNode(projectId).collectAsState(initial = emptyList())
            logs.take(10).forEach { log ->
                ProjectTimelineItem(log)
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingLg))

            Text(
                text = stringResource(Res.string.project_detail_inbox_all),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

            nodesWithPinForProject.forEach { item ->
                NodeCard(
                    nodeWithPin = item,
                    onToggleDone = { status -> viewModel.updateNodeStatus(item.node, status) },
                    onTogglePin = { isPinned -> viewModel.togglePin(item.node, isPinned) },
                    onClick = { onEditNode(item.node.id) },
                    onLongClick = { onEditNode(item.node.id) },
                    onArchive = { viewModel.archiveNode(item.node) }
                )
                Spacer(Modifier.height(TactileTheme.SpacingSm))
            }
        }
    }
}

@Composable
fun ProjectTimelineItem(log: EventLogEntity) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(8.dp).background(TactileTheme.Muted, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = log.eventType.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold
            )
            val date = kotlin.time.Instant.fromEpochMilliseconds(log.timestamp)
                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            Text(
                text = "${date.hour}:${date.minute} // ${date.day}/${date.month.number}",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted
            )
        }
    }
}

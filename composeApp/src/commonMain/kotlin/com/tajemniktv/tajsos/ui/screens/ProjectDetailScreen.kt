/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import androidx.compose.ui.unit.sp

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
    val nodesWithPinForProject by viewModel.getNodesForProject(projectId).collectAsState(initial = emptyList())
    var showStatusDialog by remember { mutableStateOf(false) }
    val logs by viewModel.getLogsForNode(projectId).collectAsState(initial = emptyList())

    val (healthLabel, healthColor) = calculateHealthStatus(project, nodesWithPinForProject)

    LaunchedEffect(projectId) {
        viewModel.setLastActiveContext(projectId, project.areaId)
    }

    Scaffold(
        topBar = {
            ProjectTopBar(
                project = project,
                onBack = onBack,
                onEditNode = { onEditNode(projectId) },
                onToggleFreeze = { viewModel.updateNode(project.copy(isFrozen = !project.isFrozen)) },
                onArchive = {
                    viewModel.archiveNode(project)
                    onBack()
                },
                onShowStatusDialog = { showStatusDialog = true }
            )
        }
    ) { padding ->
        ProjectDetailContent(
            padding = padding,
            project = project,
            nodesWithPinForProject = nodesWithPinForProject,
            logs = logs,
            healthLabel = healthLabel,
            healthColor = healthColor,
            onEditNode = onEditNode,
            onShowStatusDialog = { showStatusDialog = true }
        )
    }

    if (showStatusDialog) {
        ProjectStatusDialog(
            project = project,
            onDismiss = { showStatusDialog = false },
            onUpdateStatus = { status ->
                viewModel.updateNodeStatus(project, status)
                showStatusDialog = false
            },
            onToggleFreeze = { isFrozen ->
                viewModel.updateNode(project.copy(isFrozen = isFrozen))
                showStatusDialog = false
            }
        )
    }
}

@Composable
private fun calculateHealthStatus(project: NodeEntity, nodes: List<NodeWithPin>): Pair<String, androidx.compose.ui.graphics.Color> {
    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val staleTime = now - (14 * 24 * 60 * 60 * 1000L)

    val hasCriticalOverdue = nodes.any {
        val dueAt = it.node.dueAt
        it.node.status == "active" && it.node.isHardDeadline && dueAt != null && dueAt < now
    }
    val isNeglected = nodes.none { it.node.updatedAt >= staleTime } && project.status == "active" && !project.isFrozen

    return when {
        project.isFrozen -> stringResource(Res.string.project_health_frozen) to TactileTheme.Accent
        project.status == "on_hold" -> stringResource(Res.string.project_health_on_hold) to TactileTheme.Muted
        hasCriticalOverdue -> stringResource(Res.string.project_health_critical) to TactileTheme.Error
        isNeglected -> stringResource(Res.string.project_health_neglected) to TactileTheme.Error
        else -> stringResource(Res.string.project_health_healthy) to TactileTheme.Success
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectTopBar(
    project: NodeEntity,
    onBack: () -> Unit,
    onEditNode: () -> Unit,
    onToggleFreeze: () -> Unit,
    onArchive: () -> Unit,
    onShowStatusDialog: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "NEURAL_INTERFACE",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.detail_back),
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onShowStatusDialog) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onEditNode) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onToggleFreeze) {
                Icon(
                    if (project.isFrozen) Icons.Default.AcUnit else Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = if (project.isFrozen) TactileTheme.Accent else TactileTheme.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onArchive) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TactileTheme.Background,
            titleContentColor = TactileTheme.Primary,
            navigationIconContentColor = TactileTheme.Text,
            actionIconContentColor = TactileTheme.Text
        )
    )
}

@Composable
private fun ProjectDetailContent(
    padding: PaddingValues,
    project: NodeEntity,
    nodesWithPinForProject: List<NodeWithPin>,
    logs: List<EventLogEntity>,
    healthLabel: String,
    healthColor: androidx.compose.ui.graphics.Color,
    onEditNode: (Long) -> Unit,
    onShowStatusDialog: () -> Unit
) {
    val total = nodesWithPinForProject.size
    val completed = nodesWithPinForProject.count { it.node.status == "done" }
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(TactileTheme.Background)
            .verticalScroll(rememberScrollState())
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)
    ) {
        DetailHeader(
            category = "CURRENT WORKSPACE",
            title = project.title
        )

        ProjectActionButtons()

        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            DetailSectionHeader(
                title = stringResource(Res.string.detail_organization),
                icon = Icons.Default.BarChart
            )
            StatusCard(
                status = healthLabel,
                color = healthColor,
                onClick = onShowStatusDialog
            )
        }

        InfoCard(
            title = "PROGRESS",
            value = "${(progress * 100).toInt()}% COMPLETE",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            color = if (project.isFrozen) TactileTheme.Muted else TactileTheme.Primary
        )

        ProjectPurposeSection(project)

        ProjectNextActionsSection(nodesWithPinForProject, onEditNode)

        ProjectTimelineSection(logs)

        Spacer(Modifier.height(TactileTheme.SpacingXl))
    }
}

@Composable
private fun ProjectActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
    ) {
        ActionButton(
            text = "ADD LINK",
            onClick = { /* Add link logic */ },
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "LINK NODE",
            onClick = { /* Link node logic */ },
            containerColor = TactileTheme.Primary,
            contentColor = TactileTheme.Background,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProjectPurposeSection(project: NodeEntity) {
    if (project.projectWhy != null || project.content.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TactileTheme.Surface,
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
            border = BorderStroke(1.dp, TactileTheme.Border)
        ) {
            Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                Text(
                    text = "PURPOSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = project.projectWhy ?: project.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Text
                )
            }
        }
    }
}

@Composable
private fun ProjectNextActionsSection(
    nodesWithPinForProject: List<NodeWithPin>,
    onEditNode: (Long) -> Unit
) {
    val nextActions = nodesWithPinForProject.filter { it.node.status == "active" && it.node.type == "task" }
    if (nextActions.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            DetailSectionHeader(title = "NEXT ACTIONS", icon = Icons.Default.PlayArrow)
            nextActions.take(5).forEach { item ->
                LinkedNodeItem(
                    title = item.node.title,
                    subtitle = item.node.nextSmallestStep ?: "Active Task",
                    icon = Icons.Default.CheckCircle,
                    onClick = { onEditNode(item.node.id) }
                )
            }
        }
    }
}

@Composable
private fun ProjectTimelineSection(logs: List<EventLogEntity>) {
    if (logs.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            DetailSectionHeader(title = "TIMELINE", icon = Icons.Default.History)
            logs.take(5).forEach { log ->
                ProjectTimelineItem(log)
            }
        }
    }
}

@Composable
private fun ProjectStatusDialog(
    project: NodeEntity,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onToggleFreeze: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.project_set_status)) },
        text = {
            Column {
                val statuses = listOf("active", "on_hold", "someday")
                statuses.forEach { s ->
                    ListItem(
                        headlineContent = { Text(s.uppercase()) },
                        modifier = Modifier.clickable { onUpdateStatus(s) }
                    )
                }
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.project_detail_freeze).uppercase()) },
                    supportingContent = { Text("Stop all progress indicators") },
                    trailingContent = {
                        Switch(
                            checked = project.isFrozen,
                            onCheckedChange = onToggleFreeze
                        )
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.projects_dialog_cancel))
            }
        }
    )
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

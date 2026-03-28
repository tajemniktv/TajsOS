/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.ActionButton
import com.tajemniktv.tajsos.ui.components.cards.InfoCard
import com.tajemniktv.tajsos.ui.components.cards.LinkedNodeItem
import com.tajemniktv.tajsos.ui.components.cards.StatusCard
import com.tajemniktv.tajsos.ui.components.common.DetailHeader
import com.tajemniktv.tajsos.ui.components.common.DetailSectionHeader
import com.tajemniktv.tajsos.ui.components.common.SelectorDialog
import com.tajemniktv.tajsos.ui.components.layout.LocalHeaderActions
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Renders the project detail screen for the specified project.
 *
 * Shows project header, health/status, progress, purpose, next actions, and timeline; provides header actions for changing status, editing, freezing/unfreezing, and archiving the project.
 *
 * @param projectId The id of the project to display.
 * @param onEditNode Callback invoked with a node id when the UI requests navigation to edit that node.
 * @param onBack Callback invoked when the screen should navigate back (for example after archiving the project).
 */
@Composable
fun ProjectDetailScreen(
    viewModel: MainViewModel,
    projectId: Long,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, projectId) { nodes.find { it.node.id == projectId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(Res.string.project_detail_not_found),
                modifier = Modifier.padding(TactileTheme.SpacingMd),
            )
        }
        return
    }

    val project = nodeWithPin.node
    val nodesWithPinForProject by viewModel
        .getNodesForProject(projectId)
        .collectAsState(initial = emptyList())

    var showStatusDialog by remember { mutableStateOf(false) }

    val total = nodesWithPinForProject.size
    val completed = nodesWithPinForProject.count { it.node.status == "done" }
    val progress = if (total > 0) completed.toFloat() / total else 0f

    val now =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds()
    val staleTime = now - (14 * 24 * 60 * 60 * 1000L)

    val hasCriticalOverdue =
        nodesWithPinForProject.any {
            val dueAt = it.node.dueAt
            it.node.status == "active" && it.node.isHardDeadline && dueAt != null && dueAt < now
        }
    val isNeglected =
        nodesWithPinForProject.none { it.node.updatedAt >= staleTime } && project.status == "active" && !project.isFrozen

    val (healthLabel, healthColor) =
        when
            {
                project.isFrozen -> stringResource(Res.string.project_health_frozen) to TactileTheme.Accent
                project.status == "on_hold" -> stringResource(Res.string.project_health_on_hold) to TactileTheme.Muted
                hasCriticalOverdue -> stringResource(Res.string.project_health_critical) to TactileTheme.Error
                isNeglected -> stringResource(Res.string.project_health_neglected) to TactileTheme.Error
                else -> stringResource(Res.string.project_health_healthy) to TactileTheme.Success
            }

    LaunchedEffect(projectId) {
        viewModel.setLastActiveContext(projectId, project.areaId)
    }

    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { showStatusDialog = true }) {
            Icon(
                Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(onClick = { onEditNode(projectId) }) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(
            onClick = {
                viewModel.updateNode(project.copy(isFrozen = !project.isFrozen))
            },
        ) {
            Icon(
                if (project.isFrozen) Icons.Default.AcUnit else Icons.Default.WbSunny,
                contentDescription = null,
                tint = if (project.isFrozen) TactileTheme.Accent else TactileTheme.Primary,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(
            onClick = {
                viewModel.archiveNode(project)
                onBack()
            },
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }

    CompositionLocalProvider(LocalHeaderActions provides actions) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(TactileTheme.Background),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(TactileTheme.Background)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = TactileTheme.SpacingMd)
                        .padding(bottom = TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
            ) {
                // Header
                DetailHeader(
                    title = project.title,
                    subtitle = "CURRENT WORKSPACE",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                ) {
                    ActionButton(
                        text = "ADD LINK",
                        onClick = { /* Add link logic */ },
                        modifier = Modifier.weight(1f),
                    )
                    ActionButton(
                        text = "LINK NODE",
                        onClick = { /* Link node logic */ },
                        containerColor = TactileTheme.Primary,
                        contentColor = TactileTheme.Background,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Health / Status
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    DetailSectionHeader(
                        title = stringResource(Res.string.detail_organization),
                        icon = Icons.Default.BarChart,
                    )
                    StatusCard(
                        status = healthLabel,
                        color = healthColor,
                        onClick = { showStatusDialog = true },
                    )
                }

                // Progress Card
                InfoCard(
                    title = "PROGRESS",
                    value = "${(progress * 100).toInt()}% COMPLETE",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = if (project.isFrozen) TactileTheme.Muted else TactileTheme.Primary,
                )

                // Why Section
                if (project.projectWhy != null || project.content.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        border = BorderStroke(1.dp, TactileTheme.Border),
                    ) {
                        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                            Text(
                                text = "PURPOSE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = project.projectWhy ?: project.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                            )
                        }
                    }
                }

                // Next Actions
                val nextActions =
                    nodesWithPinForProject.filter { it.node.status == "active" && it.node.type == "task" }
                if (nextActions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                        DetailSectionHeader(title = "NEXT ACTIONS", icon = Icons.Default.PlayArrow)
                        nextActions.take(5).forEach { item ->
                            LinkedNodeItem(
                                title = item.node.title,
                                subtitle = item.node.nextSmallestStep ?: "Active Task",
                                icon = Icons.Default.CheckCircle,
                                onClick = { onEditNode(item.node.id) },
                            )
                        }
                    }
                }

                // Timeline
                val logs by viewModel
                    .getLogsForNode(projectId)
                    .collectAsState(initial = emptyList())
                if (logs.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                        DetailSectionHeader(title = "TIMELINE", icon = Icons.Default.History)
                        logs.take(5).forEach { log ->
                            ProjectTimelineItem(log)
                        }
                    }
                }

                Spacer(Modifier.height(TactileTheme.SpacingXl))
            }
        }
    }

    if (showStatusDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showStatusDialog = false },
            title = "SET STATUS",
            options = listOf("active", "on_hold", "someday"),
            selectedOption = project.status,
            onSelect = { status ->
                viewModel.updateNodeStatus(project, status)
                showStatusDialog = false
            },
            optionName = { status -> status },
            optionIcon = { status ->
                when (status)
                {
                    "active" -> Icons.Default.PlayArrow
                    "on_hold" -> Icons.Default.Pause
                    "someday" -> Icons.Default.CalendarToday
                    else -> Icons.Default.Info
                }
            },
            optionSubtext = { status -> "PROJ_STATE_${status.uppercase()}" },
        )
    }
}

/**
 * Renders a single timeline row for an event log with a colored marker, the event label,
 * and a formatted timestamp.
 *
 * @param log The event log to display; its `eventType` is shown as the label and its
 * `timestamp` is shown as `hour:minute // day/month`.
 */
@Composable
fun ProjectTimelineItem(log: EventLogEntity) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(8.dp).background(TactileTheme.Muted, CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = log.eventType.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            val date =
                kotlin.time.Instant
                    .fromEpochMilliseconds(log.timestamp)
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            Text(
                text = "${date.hour}:${date.minute} // ${date.day}/${date.month.number}",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
            )
        }
    }
}

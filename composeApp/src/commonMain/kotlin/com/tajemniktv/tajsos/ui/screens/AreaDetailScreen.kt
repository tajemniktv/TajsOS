/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import com.tajemniktv.tajsos.data.ProjectState
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.projectStateOrNull
import com.tajemniktv.tajsos.data.taskStateOrNull
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.InfoCard
import com.tajemniktv.tajsos.ui.components.cards.LinkedNodeItem
import com.tajemniktv.tajsos.ui.components.cards.StatusCard
import com.tajemniktv.tajsos.ui.components.common.DetailHeader
import com.tajemniktv.tajsos.ui.components.common.DetailSectionHeader
import com.tajemniktv.tajsos.ui.components.layout.LocalHeaderActions
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.area_detail_not_found

/**
 * Renders the detail screen for a workspace area, showing header, status, progress, optional core principle note,
 * active projects, and recent activity, and provides a header action to archive the area.
 *
 * If the area identified by [areaId] is not found, a full-screen "not found" message is displayed.
 *
 * @param viewModel The screen's ViewModel providing nodes, projects, and actions.
 * @param areaId The identifier of the area to display.
 * @param onNavigateToProject Callback invoked with a project id when the user selects a project.
 * @param onEditNode Callback invoked with a node id when the user requests to edit a node.
 * @param onBack Callback invoked to navigate back (used after archive action).
 */
@Composable
fun AreaDetailScreen(
    viewModel: MainViewModel,
    areaId: Long,
    onNavigateToProject: (Long) -> Unit,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, areaId) { nodes.find { it.node.id == areaId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(Res.string.area_detail_not_found),
                modifier = Modifier.padding(TactileTheme.SpacingMd),
            )
        }
        return
    }

    val area = nodeWithPin.node
    val areaSnapshot by viewModel.areaHealthSnapshot.collectAsState()
    val areaMetrics = areaSnapshot.areas.firstOrNull { it.areaId == area.id }

    LaunchedEffect(areaId) {
        viewModel.setLastActiveContext(null, areaId)
    }

    val projects by viewModel.getProjectsForArea(areaId).collectAsState(initial = emptyList())
    val nodesWithPinInArea by viewModel
        .getNodesForArea(areaId)
        .collectAsState(initial = emptyList())

    val foundationalNote =
        nodesWithPinInArea.find {
            it.node.isNoteItem() &&
                it.tags.any { tag ->
                    tag.name.equals(
                        "foundational",
                        ignoreCase = true,
                    )
                }
        }

    val actions: @Composable RowScope.() -> Unit = {
        IconButton(
            onClick = {
                viewModel.archiveNode(area)
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
                    title = area.title,
                    subtitle = "RESPONSIBILITY AREA",
                )

                // Health / Status
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    DetailSectionHeader(
                        title = "SYSTEM STATE",
                        icon = Icons.Default.BarChart,
                    )
                    StatusCard(
                        status = (areaMetrics?.status ?: "stable"),
                        color =
                            when (areaMetrics?.status)
                            {
                                "on_fire" -> TactileTheme.Error
                                "overloaded" -> TactileTheme.Accent
                                "neglected" -> TactileTheme.Muted
                                "active" -> TactileTheme.Primary
                                else -> TactileTheme.Success
                            },
                    )
                    if (areaMetrics != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                        ) {
                            InfoCard(
                                title = "OPEN LOOPS",
                                value = areaMetrics.openLoops.toString(),
                                icon = Icons.Default.AllInclusive,
                                color = TactileTheme.Accent,
                                modifier = Modifier.weight(1f),
                            )
                            InfoCard(
                                title = "DEADLINES",
                                value = "${areaMetrics.overdueDeadlines}/${areaMetrics.deadlines}",
                                icon = Icons.Default.EventBusy,
                                color = if (areaMetrics.overdueDeadlines > 0) TactileTheme.Error else TactileTheme.Primary,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        InfoCard(
                            title = "STRESS LOAD",
                            value = "${areaMetrics.stressLoad}%",
                            icon = Icons.Default.Speed,
                            color = if (areaMetrics.stressLoad >= 70) TactileTheme.Error else TactileTheme.Primary,
                        )
                        Text(
                            "Recent activity: ${areaMetrics.recentActivity} • Neglected: ${areaMetrics.neglectedDays} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted,
                        )
                    }
                }

                // Area Progress
                val areaNodesTotal = nodes.filter { it.node.areaId == areaId }
                if (areaNodesTotal.isNotEmpty()) {
                    val totalCount = areaNodesTotal.size
                    val completedCount = areaNodesTotal.count { it.node.status == "done" }
                    val areaProgress = completedCount.toFloat() / totalCount

                    InfoCard(
                        title = "AREA COMPLETION",
                        value = "${(areaProgress * 100).toInt()}% COMPLETE",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = TactileTheme.Primary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    ) {
                        InfoCard(
                            title = "PROJECTS",
                            value = projects.size.toString(),
                            icon = Icons.Default.Folder,
                            color = TactileTheme.Primary,
                            modifier = Modifier.weight(1f),
                        )
                        InfoCard(
                            title = "ACTIVE TASKS",
                            value = nodesWithPinInArea.count {
                                it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE
                            }.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = TactileTheme.Accent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Foundational Principle
                if (foundationalNote != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                        DetailSectionHeader(
                            title = "CORE PRINCIPLE",
                            icon = Icons.Default.AutoAwesome,
                        )
                        Surface(
                            onClick = { onEditNode(foundationalNote.node.id) },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(TactileTheme.RadiusLg),
                            border = BorderStroke(1.dp, TactileTheme.Primary.copy(alpha = 0.3f)),
                        ) {
                            Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
                                Text(
                                    text = foundationalNote.node.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TactileTheme.Text,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = foundationalNote.node.content.take(150) + "...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TactileTheme.Muted,
                                )
                            }
                        }
                    }
                }

                // Active Projects
                val activeProjects =
                    projects.filter { it.projectStateOrNull() in setOf(ProjectState.ACTIVE, ProjectState.ON_HOLD) }
                if (activeProjects.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                        DetailSectionHeader(
                            title = "CURRENT PROJECTS",
                            icon = Icons.Default.AccountTree,
                        )
                        activeProjects.forEach { project ->
                            LinkedNodeItem(
                                title = project.title,
                                subtitle =
                                    when (project.projectStateOrNull()) {
                                        ProjectState.ON_HOLD -> "On hold project"
                                        else -> "Active project"
                                    },
                                icon = Icons.Default.Folder,
                                onClick = { onNavigateToProject(project.id) },
                            )
                        }
                    }
                }

                // Recent Activity
                if (nodesWithPinInArea.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                        DetailSectionHeader(title = "RECENT ACTIVITY", icon = Icons.Default.History)
                        nodesWithPinInArea
                            .sortedByDescending { it.node.updatedAt }
                            .take(5)
                            .forEach { item ->
                                LinkedNodeItem(
                                    title = item.node.title,
                                    subtitle =
                                        when {
                                            item.node.isTaskItem() -> "TASK"
                                            item.node.type == "record" -> "RECORD"
                                            item.node.isNoteItem() -> "NOTE"
                                            item.node.projectStateOrNull() != null -> "PROJECT"
                                            else -> item.node.type.uppercase()
                                        },
                                    icon =
                                        when {
                                            item.node.isTaskItem() -> Icons.Default.CheckCircle
                                            item.node.type == "record" -> Icons.Default.History
                                            item.node.isNoteItem() -> Icons.Default.Description
                                            item.node.projectStateOrNull() != null -> Icons.Default.Folder
                                            else -> Icons.Default.Description
                                        },
                                    onClick = { onEditNode(item.node.id) },
                                )
                            }
                    }
                }

                Spacer(Modifier.height(TactileTheme.SpacingXl))
            }
        }
    }
}

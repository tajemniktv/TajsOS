/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.AttachmentEntity
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.ProjectState
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isRecordItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.projectStateOrNull
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.data.toNodeStatus
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.LinkedNodeItem
import com.tajemniktv.tajsos.ui.components.common.DetailHeader
import com.tajemniktv.tajsos.ui.components.common.SelectorDialog
import com.tajemniktv.tajsos.ui.components.layout.LocalHeaderActions
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.detail_unassign
import tajsos.composeapp.generated.resources.project_detail_actions
import tajsos.composeapp.generated.resources.project_detail_assets
import tajsos.composeapp.generated.resources.project_detail_blockers
import tajsos.composeapp.generated.resources.project_detail_contextual_panel
import tajsos.composeapp.generated.resources.project_detail_empty_assets
import tajsos.composeapp.generated.resources.project_detail_empty_blockers
import tajsos.composeapp.generated.resources.project_detail_empty_notes
import tajsos.composeapp.generated.resources.project_detail_empty_records
import tajsos.composeapp.generated.resources.project_detail_empty_timeline
import tajsos.composeapp.generated.resources.project_detail_empty_work
import tajsos.composeapp.generated.resources.project_detail_goal_fallback
import tajsos.composeapp.generated.resources.project_detail_health
import tajsos.composeapp.generated.resources.project_detail_linked_notes
import tajsos.composeapp.generated.resources.project_detail_linked_records
import tajsos.composeapp.generated.resources.project_detail_linked_tasks
import tajsos.composeapp.generated.resources.project_detail_mission_control
import tajsos.composeapp.generated.resources.project_detail_next_actions
import tajsos.composeapp.generated.resources.project_detail_not_found
import tajsos.composeapp.generated.resources.project_detail_outcome
import tajsos.composeapp.generated.resources.project_detail_overview
import tajsos.composeapp.generated.resources.project_detail_progress
import tajsos.composeapp.generated.resources.project_detail_progress_value
import tajsos.composeapp.generated.resources.project_detail_relations
import tajsos.composeapp.generated.resources.project_detail_review
import tajsos.composeapp.generated.resources.project_detail_schedule
import tajsos.composeapp.generated.resources.project_detail_set_status
import tajsos.composeapp.generated.resources.project_detail_status
import tajsos.composeapp.generated.resources.project_detail_target_date
import tajsos.composeapp.generated.resources.project_detail_timeline
import tajsos.composeapp.generated.resources.project_detail_work
import tajsos.composeapp.generated.resources.project_health_critical
import tajsos.composeapp.generated.resources.project_health_frozen
import tajsos.composeapp.generated.resources.project_health_healthy
import tajsos.composeapp.generated.resources.project_health_neglected
import tajsos.composeapp.generated.resources.project_health_on_hold
import tajsos.composeapp.generated.resources.project_set_status
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_record
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Project mission-control detail screen scoped to one project outcome.
 *
 * Desktop renders a three-pane workspace (main + contextual right panel) while compact
 * screens fall back to a single-column composition with the same tabbed sections.
 */
@Composable
fun ProjectDetailScreen(
    viewModel: MainViewModel,
    projectId: Long,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit,
    isDesktop: Boolean = false,
) {
    val nodes by viewModel.allNodes.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val nodeWithPin = remember(nodes, projectId) { nodes.find { it.node.id == projectId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.project_detail_not_found),
                color = TactileTheme.Muted,
                modifier = Modifier.padding(TactileTheme.SpacingMd),
            )
        }
        return
    }

    val project = nodeWithPin.node
    val nodesWithPinForProject by viewModel
        .getNodesForProject(projectId)
        .collectAsState(initial = emptyList())
    val relations by viewModel.getRelationsForNode(projectId).collectAsState(initial = emptyList())
    val attachments by viewModel
        .getAttachmentsForNode(projectId)
        .collectAsState(initial = emptyList())
    val logs by viewModel.getLogsForNode(projectId).collectAsState(initial = emptyList())
    val tags by viewModel.getTagsForNode(projectId).collectAsState(initial = emptyList())

    val areaName = allAreas.find { it.id == project.areaId }?.title

    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable(projectId) { mutableStateOf(ProjectDetailTab.Overview) }

    val projectItems = remember(nodesWithPinForProject) { nodesWithPinForProject.map { it.node } }
    val projectTasks = remember(projectItems) { projectItems.filter { it.isTaskItem() } }
    val linkedNotes = remember(projectItems) { projectItems.filter { it.isNoteItem() } }
    val linkedRecords = remember(projectItems) { projectItems.filter { it.isRecordItem() } }

    val now = Clock.System.now().toEpochMilliseconds()
    val staleTime = now - (14 * 24 * 60 * 60 * 1000L)

    val completedTasks = projectTasks.filter { it.taskStateOrNull() == TaskState.DONE }
    val activeTasks = projectTasks.filter { it.taskStateOrNull() == TaskState.ACTIVE }
    val blockedTasks =
        projectTasks.filter {
            it.taskStateOrNull() == TaskState.BLOCKED ||
                (
                    it.taskStateOrNull() == TaskState.ACTIVE && it.isHardDeadline && (
                        it.dueAt
                            ?: Long.MAX_VALUE
                    ) < now
                )
        }
    val nextActions = activeTasks.filterNot { task -> blockedTasks.any { it.id == task.id } }
    val upcomingMilestones =
        projectTasks
            .filter { it.dueAt != null }
            .sortedBy { it.dueAt }
            .take(5)

    val hasCriticalOverdue =
        blockedTasks.any { it.isHardDeadline && (it.dueAt ?: Long.MAX_VALUE) < now }
    val isNeglected =
        projectItems.none { it.updatedAt >= staleTime } &&
            project.projectStateOrNull() == ProjectState.ACTIVE &&
            !project.isFrozen

    val (healthLabel, healthColor) =
        when
            {
                project.isFrozen -> {
                    stringResource(Res.string.project_health_frozen) to TactileTheme.Accent
                }

                project.projectStateOrNull() == ProjectState.ON_HOLD -> {
                    stringResource(Res.string.project_health_on_hold) to TactileTheme.Muted
                }

                hasCriticalOverdue -> {
                    stringResource(Res.string.project_health_critical) to TactileTheme.Error
                }

                isNeglected -> {
                    stringResource(Res.string.project_health_neglected) to
                        TactileTheme.Error
                }

                else -> {
                    stringResource(Res.string.project_health_healthy) to
                        TactileTheme.Success
                }
            }

    val progress =
        if (projectTasks.isNotEmpty()) {
            completedTasks.size.toFloat() / projectTasks.size.toFloat()
        } else {
            0f
        }

    val outcomeText =
        project.projectWhy
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: project.content.trim().takeIf(String::isNotEmpty)
            ?: stringResource(Res.string.project_detail_goal_fallback)

    val targetDate = project.dueAt ?: upcomingMilestones.firstOrNull()?.dueAt

    val nodesById = remember(nodes) { nodes.associateBy { it.node.id } }
    val relatedNodeIds =
        remember(relations, projectId) {
            relations.mapNotNull { relation ->
                when (projectId)
                {
                    relation.fromNodeId -> relation.toNodeId
                    relation.toNodeId -> relation.fromNodeId
                    else -> null
                }
            }
        }

    LaunchedEffect(projectId) {
        viewModel.setLastActiveContext(projectId, project.areaId)
    }

    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { showStatusDialog = true }) {
            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { onEditNode(projectId) }) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { viewModel.updateNode(project.copy(isFrozen = !project.isFrozen)) }) {
            Icon(
                if (project.isFrozen) Icons.Default.WbSunny else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (project.isFrozen) TactileTheme.Primary else TactileTheme.Muted,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(
            onClick = {
                viewModel.archiveNode(project)
                onBack()
            },
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }

    CompositionLocalProvider(LocalHeaderActions provides actions) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().background(TactileTheme.Background),
        ) {
            val useWideLayout = isDesktop && maxWidth >= 1180.dp
            if (useWideLayout) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProjectMainPane(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        project = project,
                        areaName = areaName,
                        healthLabel = healthLabel,
                        healthColor = healthColor,
                        progress = progress,
                        completedTasks = completedTasks.size,
                        totalTasks = projectTasks.size,
                        outcomeText = outcomeText,
                        targetDate = targetDate,
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it },
                        nextActions = nextActions,
                        blockedTasks = blockedTasks,
                        linkedNotes = linkedNotes,
                        linkedRecords = linkedRecords,
                        linkedTasks = projectTasks,
                        timeline = logs,
                        attachments = attachments,
                        milestones = upcomingMilestones,
                        onEditNode = onEditNode,
                        onStatusClick = { showStatusDialog = true },
                    )

                    ProjectSidebar(
                        modifier = Modifier.width(320.dp).fillMaxHeight(),
                        areaName = areaName,
                        healthLabel = healthLabel,
                        healthColor = healthColor,
                        targetDate = targetDate,
                        tags = tags.map { it.name },
                        relatedNodeIds = relatedNodeIds,
                        nodesById = nodesById,
                        attachments = attachments.map { it.title ?: it.uriOrPath },
                        onEditNode = onEditNode,
                    )
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProjectMainPane(
                        modifier = Modifier.fillMaxWidth(),
                        project = project,
                        areaName = areaName,
                        healthLabel = healthLabel,
                        healthColor = healthColor,
                        progress = progress,
                        completedTasks = completedTasks.size,
                        totalTasks = projectTasks.size,
                        outcomeText = outcomeText,
                        targetDate = targetDate,
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it },
                        nextActions = nextActions,
                        blockedTasks = blockedTasks,
                        linkedNotes = linkedNotes,
                        linkedRecords = linkedRecords,
                        linkedTasks = projectTasks,
                        timeline = logs,
                        attachments = attachments,
                        milestones = upcomingMilestones,
                        onEditNode = onEditNode,
                        onStatusClick = { showStatusDialog = true },
                    )
                    ProjectSidebar(
                        modifier = Modifier.fillMaxWidth(),
                        areaName = areaName,
                        healthLabel = healthLabel,
                        healthColor = healthColor,
                        targetDate = targetDate,
                        tags = tags.map { it.name },
                        relatedNodeIds = relatedNodeIds,
                        nodesById = nodesById,
                        attachments = attachments.map { it.title ?: it.uriOrPath },
                        onEditNode = onEditNode,
                    )
                }
            }
        }
    }

    if (showStatusDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showStatusDialog = false },
            title = stringResource(Res.string.project_set_status),
            options = listOf("active", "on_hold", "someday"),
            selectedOption = project.projectStateOrNull()?.toNodeStatus() ?: project.status,
            onSelect = { status ->
                viewModel.updateNodeStatus(project, status)
                showStatusDialog = false
            },
            optionName = { it },
            optionIcon = { status ->
                when (status)
                {
                    "active" -> Icons.Default.PlayArrow
                    "on_hold" -> Icons.Default.Schedule
                    "someday" -> Icons.Default.CalendarToday
                    else -> Icons.Default.Adjust
                }
            },
            optionSubtext = { status -> "PROJECT_${status.uppercase()}" },
        )
    }
}

private enum class ProjectDetailTab(
    val labelRes: StringResource,
    val icon: ImageVector,
) {
    Overview(Res.string.project_detail_overview, Icons.Default.Insights),
    Work(Res.string.project_detail_work, Icons.Default.CheckCircle),
    Notes(Res.string.project_detail_linked_notes, Icons.Default.Description),
    Timeline(Res.string.project_detail_timeline, Icons.Default.History),
    Assets(Res.string.project_detail_assets, Icons.Default.AttachFile),
    Review(Res.string.project_detail_review, Icons.Default.EventAvailable),
}

@Composable
private fun ProjectMainPane(
    modifier: Modifier = Modifier,
    project: NodeEntity,
    areaName: String?,
    healthLabel: String,
    healthColor: Color,
    progress: Float,
    completedTasks: Int,
    totalTasks: Int,
    outcomeText: String,
    targetDate: Long?,
    selectedTab: ProjectDetailTab,
    onSelectTab: (ProjectDetailTab) -> Unit,
    nextActions: List<NodeEntity>,
    blockedTasks: List<NodeEntity>,
    linkedNotes: List<NodeEntity>,
    linkedRecords: List<NodeEntity>,
    linkedTasks: List<NodeEntity>,
    timeline: List<EventLogEntity>,
    attachments: List<AttachmentEntity>,
    milestones: List<NodeEntity>,
    onEditNode: (Long) -> Unit,
    onStatusClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetailHeader(
                title = project.title,
                subtitle = stringResource(Res.string.project_detail_mission_control),
            )

            ProjectHero(
                project = project,
                areaName = areaName,
                healthLabel = healthLabel,
                healthColor = healthColor,
                progress = progress,
                completedTasks = completedTasks,
                totalTasks = totalTasks,
                outcomeText = outcomeText,
                targetDate = targetDate,
                onStatusClick = onStatusClick,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProjectDetailTab.entries.forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        label = { Text(stringResource(tab.labelRes)) },
                        leadingIcon = { Icon(tab.icon, contentDescription = null) },
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.SurfaceLow,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (selectedTab)
                    {
                        ProjectDetailTab.Overview -> {
                            ProjectSectionTitle(
                                Res.string.project_detail_next_actions,
                                Icons.Default.PlayArrow,
                            )
                            if (nextActions.isEmpty()) {
                                ProjectEmptyState(Res.string.project_detail_empty_work)
                            } else {
                                nextActions.take(6).forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.nextSmallestStep
                                                ?: stringResource(Res.string.project_detail_actions),
                                        icon = Icons.Default.CheckCircle,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }

                            ProjectSectionTitle(
                                Res.string.project_detail_blockers,
                                Icons.Default.Block,
                            )
                            if (blockedTasks.isEmpty()) {
                                ProjectEmptyState(Res.string.project_detail_empty_blockers)
                            } else {
                                blockedTasks.take(4).forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.nextSmallestStep
                                                ?: stringResource(Res.string.project_detail_work),
                                        icon = Icons.Default.Block,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }

                            if (milestones.isNotEmpty()) {
                                ProjectSectionTitle(
                                    Res.string.project_detail_schedule,
                                    Icons.Default.CalendarToday,
                                )
                                milestones.forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle = formatTimestamp(it.dueAt),
                                        icon = Icons.Default.Schedule,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }
                        }

                        ProjectDetailTab.Work -> {
                            ProjectSectionTitle(
                                Res.string.project_detail_linked_tasks,
                                Icons.Default.CheckCircle,
                            )
                            if (linkedTasks.isEmpty()) {
                                ProjectEmptyState(Res.string.project_detail_empty_work)
                            } else {
                                linkedTasks.forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.taskStateOrNull()?.name
                                                ?: stringResource(Res.string.project_detail_status),
                                        icon = Icons.Default.CheckCircle,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }
                        }

                        ProjectDetailTab.Notes -> {
                            ProjectSectionTitle(
                                Res.string.project_detail_linked_notes,
                                Icons.Default.Description,
                            )
                            if (linkedNotes.isEmpty()) {
                                ProjectEmptyState(Res.string.project_detail_empty_notes)
                            } else {
                                linkedNotes.forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.noteType
                                                ?: stringResource(Res.string.type_note),
                                        icon = Icons.Default.Description,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }

                            ProjectSectionTitle(
                                Res.string.project_detail_linked_records,
                                Icons.Default.History,
                            )
                            if (linkedRecords.isEmpty()) {
                                ProjectEmptyState(Res.string.project_detail_empty_records)
                            } else {
                                linkedRecords.forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle = stringResource(Res.string.type_record),
                                        icon = Icons.Default.History,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }
                        }

                        ProjectDetailTab.Timeline -> {
                            ProjectSectionTitle(
                                Res.string.project_detail_timeline,
                                Icons.Default.History,
                            )
                            if (timeline.isEmpty()) {
                                ProjectEmptyState(Res.string.project_detail_empty_timeline)
                            } else {
                                timeline
                                    .sortedByDescending { it.timestamp }
                                    .take(12)
                                    .forEach { log ->
                                        ProjectTimelineItem(log)
                                    }
                            }
                        }

                        ProjectDetailTab.Assets -> {
                            ProjectSectionTitle(
                                Res.string.project_detail_assets,
                                Icons.Default.AttachFile,
                            )
                            if (attachments.isEmpty()) {
                                ProjectEmptyState(Res.string.project_detail_empty_assets)
                            } else {
                                attachments.forEach { attachment ->
                                    LinkedNodeItem(
                                        title = attachment.title ?: attachment.uriOrPath,
                                        subtitle =
                                            attachment.mimeType
                                                ?: stringResource(Res.string.project_detail_assets),
                                        icon = Icons.Default.AttachFile,
                                        onClick = {},
                                    )
                                }
                            }
                        }

                        ProjectDetailTab.Review -> {
                            ProjectSectionTitle(
                                Res.string.project_detail_review,
                                Icons.Default.EventAvailable,
                            )
                            MissionMetric(
                                label = stringResource(Res.string.project_detail_progress),
                                value =
                                    stringResource(
                                        Res.string.project_detail_progress_value,
                                        (progress * 100f).roundToInt(),
                                        completedTasks,
                                        totalTasks,
                                    ),
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                            )
                            MissionMetric(
                                label = stringResource(Res.string.project_detail_health),
                                value = healthLabel,
                                icon = Icons.Default.BarChart,
                                tone = healthColor,
                            )
                            MissionMetric(
                                label = stringResource(Res.string.project_detail_target_date),
                                value = formatTimestamp(targetDate),
                                icon = Icons.Default.CalendarToday,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectHero(
    project: NodeEntity,
    areaName: String?,
    healthLabel: String,
    healthColor: Color,
    progress: Float,
    completedTasks: Int,
    totalTasks: Int,
    outcomeText: String,
    targetDate: Long?,
    onStatusClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.SurfaceHighest,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(Res.string.project_detail_outcome),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
            Text(
                text = outcomeText,
                style = MaterialTheme.typography.bodyLarge,
                color = TactileTheme.Text,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = onStatusClick,
                        label = {
                            Text(
                                "${
                                    stringResource(
                                        Res.string.project_detail_status,
                                    )
                                }: ${project.projectStateOrNull()?.name ?: project.status}",
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${stringResource(Res.string.project_detail_health)}: $healthLabel") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = healthColor,
                            )
                        },
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "${(progress * 100f).roundToInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TactileTheme.Text,
                    )
                    Text(
                        text =
                            stringResource(
                                Res.string.project_detail_progress_value,
                                (progress * 100f).roundToInt(),
                                completedTasks,
                                totalTasks,
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = if (project.isFrozen) TactileTheme.Muted else TactileTheme.Primary,
                trackColor = TactileTheme.SurfaceLow,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(onClick = onStatusClick) {
                    Icon(Icons.Default.Tune, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.project_detail_set_status))
                }
                FilledTonalButton(onClick = {}) {
                    Icon(Icons.Default.FolderOpen, null)
                    Spacer(Modifier.width(6.dp))
                    Text(areaName ?: stringResource(Res.string.detail_unassign))
                }
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "${stringResource(Res.string.project_detail_target_date)}: ${
                                formatTimestamp(
                                    targetDate,
                                )
                            }",
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                )
            }
        }
    }
}

@Composable
private fun ProjectSidebar(
    modifier: Modifier = Modifier,
    areaName: String?,
    healthLabel: String,
    healthColor: Color,
    targetDate: Long?,
    tags: List<String>,
    relatedNodeIds: List<Long>,
    nodesById: Map<Long, NodeWithPin>,
    attachments: List<String>,
    onEditNode: (Long) -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SidebarCard(
            title = stringResource(Res.string.project_detail_contextual_panel),
            icon = Icons.Default.Insights,
        ) {
            SidebarValueRow(
                label = stringResource(Res.string.project_detail_health),
                value = healthLabel,
                valueColor = healthColor,
            )
            SidebarValueRow(
                label = stringResource(Res.string.type_area),
                value = areaName ?: stringResource(Res.string.detail_unassign),
            )
            SidebarValueRow(
                label = stringResource(Res.string.project_detail_target_date),
                value = formatTimestamp(targetDate),
            )
        }

        SidebarCard(
            title = stringResource(Res.string.project_detail_relations),
            icon = Icons.Default.Hub,
        ) {
            if (relatedNodeIds.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_work),
                    color = TactileTheme.Muted,
                )
            } else {
                relatedNodeIds.take(8).forEach { id ->
                    val node = nodesById[id]?.node ?: return@forEach
                    Text(
                        text = "• ${node.title}",
                        color = TactileTheme.Text,
                        modifier = Modifier.fillMaxWidth().clickable { onEditNode(node.id) },
                    )
                }
            }
        }

        SidebarCard(
            title = stringResource(Res.string.project_detail_schedule),
            icon = Icons.Default.Schedule,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = TactileTheme.Primary,
                )
                Text(formatTimestamp(targetDate), color = TactileTheme.Text)
            }
        }

        SidebarCard(
            title = stringResource(Res.string.project_detail_assets),
            icon = Icons.Default.Archive,
        ) {
            if (attachments.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_assets),
                    color = TactileTheme.Muted,
                )
            } else {
                attachments.take(6).forEach {
                    Text("• $it", color = TactileTheme.Text)
                }
            }
        }

        SidebarCard(
            title = stringResource(Res.string.project_detail_actions),
            icon = Icons.Default.Label,
        ) {
            if (tags.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_timeline),
                    color = TactileTheme.Muted,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.take(4).forEach { tag ->
                        AssistChip(onClick = {}, label = { Text(tag) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icon, contentDescription = null, tint = TactileTheme.Primary)
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            content()
        }
    }
}

@Composable
private fun SidebarValueRow(
    label: String,
    value: String,
    valueColor: Color = TactileTheme.Text,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

@Composable
private fun ProjectSectionTitle(
    title: StringResource,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TactileTheme.Primary)
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TactileTheme.Text,
        )
    }
    HorizontalDivider(color = TactileTheme.GhostBorder)
}

@Composable
private fun ProjectEmptyState(text: StringResource) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Text(
            text = stringResource(text),
            color = TactileTheme.Muted,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun MissionMetric(
    label: String,
    value: String,
    icon: ImageVector,
    tone: Color = TactileTheme.Primary,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = tone)
                Column {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TactileTheme.Text,
                    )
                }
            }
        }
    }
}

/**
 * Renders one timeline event entry for the project activity feed.
 */
@Composable
fun ProjectTimelineItem(log: EventLogEntity) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(8.dp)
                    .height(8.dp)
                    .background(TactileTheme.Muted, RoundedCornerShape(99.dp)),
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
                Instant
                    .fromEpochMilliseconds(log.timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            Text(
                text = "${date.hour.toString().padStart(2, '0')}:${
                    date.minute.toString().padStart(2, '0')
                } // ${date.day}/${date.month.number}",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) {
        return "-"
    }
    val date =
        Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${
        date.day.toString().padStart(2, '0')
    }"
}

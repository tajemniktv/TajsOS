/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.ProjectState
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isRecordItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.projectStateOrNull
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.data.toNodeStatus
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.SelectorDialog
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderModel
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.components.screen.SplitScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.screenBreadcrumbs
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.no_specific_outcome_defined
import tajsos.composeapp.generated.resources.project_detail_not_found
import tajsos.composeapp.generated.resources.project_health_critical
import tajsos.composeapp.generated.resources.project_health_frozen
import tajsos.composeapp.generated.resources.project_health_healthy
import tajsos.composeapp.generated.resources.project_health_neglected
import tajsos.composeapp.generated.resources.project_health_on_hold
import tajsos.composeapp.generated.resources.project_set_status
import tajsos.composeapp.generated.resources.screen_project

/**
 * Project mission-control detail route that collects state and coordinates interactions.
 *
 * @param viewModel Source of project state.
 * @param projectId ID of the project to display.
 * @param onEditNode Callback to edit a node.
 * @param onBack Callback to go back.
 * @param isDesktop Whether the current environment is a desktop layout.
 * @param onNavigate Navigation callback.
 */
@Composable
fun ProjectDetailRoute(
    viewModel: MainViewModel,
    projectId: Long,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit,
    isDesktop: Boolean = false,
    onNavigate: (String) -> Unit,
) {
    val nodes by viewModel.allNodes.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val nodeWithPin = remember(nodes, projectId) { nodes.find { it.node.id == projectId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.project_detail_not_found),
                color = TajsOSTheme.Muted,
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
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

    // Periodic time state to keep time-sensitive computations fresh
    var now by remember {
        mutableLongStateOf(
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds(),
        )
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(60_000L) // Update every minute
            now =
                kotlin.time.Clock.System
                    .now()
                    .toEpochMilliseconds()
        }
    }

    val staleTime = now - (14 * 24 * 60 * 60 * 1000L)

    val completedTasks =
        remember(projectTasks) { projectTasks.filter { it.taskStateOrNull() == TaskState.DONE } }
    val activeTasks =
        remember(projectTasks) { projectTasks.filter { it.taskStateOrNull() == TaskState.ACTIVE } }
    val blockedTasks =
        remember(projectTasks, now) {
            projectTasks.filter {
                it.taskStateOrNull() == TaskState.BLOCKED ||
                    (
                        it.taskStateOrNull() == TaskState.ACTIVE &&
                            it.isHardDeadline &&
                            (
                                it.dueAt
                                    ?: Long.MAX_VALUE
                            ) < now
                    )
            }
        }
    val blockedTaskIds = remember(blockedTasks) { blockedTasks.map { it.id }.toSet() }
    val nextActions =
        remember(activeTasks, blockedTaskIds) {
            activeTasks.filterNot { task -> task.id in blockedTaskIds }
        }
    val upcomingMilestones =
        remember(projectTasks) {
            projectTasks
                .filter { it.dueAt != null }
                .sortedBy { it.dueAt }
                .take(5)
        }

    val hasCriticalOverdue =
        blockedTasks.any { it.isHardDeadline && (it.dueAt ?: Long.MAX_VALUE) < now }
    val isNeglected =
        projectItems.none { it.updatedAt >= staleTime } &&
            project.projectStateOrNull() == ProjectState.ACTIVE &&
            !project.isFrozen

    val (healthLabel, healthColor) =
        when {
            project.isFrozen -> {
                stringResource(Res.string.project_health_frozen) to TajsOSTheme.Accent
            }

            project.projectStateOrNull() == ProjectState.ON_HOLD -> {
                stringResource(Res.string.project_health_on_hold) to TajsOSTheme.Muted
            }

            hasCriticalOverdue -> {
                stringResource(Res.string.project_health_critical) to TajsOSTheme.Error
            }

            isNeglected -> {
                stringResource(Res.string.project_health_neglected) to
                    TajsOSTheme.Error
            }

            else -> {
                stringResource(Res.string.project_health_healthy) to
                    TajsOSTheme.Success
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
            ?: stringResource(Res.string.no_specific_outcome_defined)

    val targetDate = project.dueAt ?: upcomingMilestones.firstOrNull()?.dueAt

    val nodesById = remember(nodes) { nodes.associateBy { it.node.id } }
    val relatedNodeIds =
        remember(relations, projectId) {
            relations.mapNotNull { relation ->
                when (projectId) {
                    relation.fromNodeId -> relation.toNodeId
                    relation.toNodeId -> relation.fromNodeId
                    else -> null
                }
            }
        }

    LaunchedEffect(projectId) {
        viewModel.setLastActiveContext(projectId, project.areaId)
    }

    val context =
        ProjectDetailContext(
            viewModel = viewModel,
            project = project,
            areaName = areaName,
            healthLabel = healthLabel,
            healthColor = healthColor,
            progress = progress,
            completedTasksCount = completedTasks.size,
            totalTasksCount = projectTasks.size,
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
            tags = tags.map { it.name },
            relatedNodeIds = relatedNodeIds,
            nodesById = nodesById,
            attachmentNames = attachments.map { it.title ?: it.uriOrPath },
            onEditNode = onEditNode,
            onStatusClick = { showStatusDialog = true },
        )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(TajsOSTheme.Background),
    ) {
        val surface =
            if (isDesktop && maxWidth >= 1180.dp) {
                ProjectDetailSurface.DESKTOP
            } else {
                ProjectDetailSurface.MOBILE
            }

        val plan = remember(surface) { buildProjectDetailPlan(surface) }

        ProjectDetailScreen(
            context = context,
            plan = plan,
            surface = surface,
            onBack = onBack,
            onNavigate = onNavigate,
            showStatusDialog = showStatusDialog,
            onDismissStatusDialog = { showStatusDialog = false },
        )
    }
}

/**
 * Stateless project detail screen content.
 *
 * @param context Project detail context.
 * @param plan Project detail plan.
 * @param surface Current UI surface mode.
 * @param onBack Callback to go back.
 * @param onNavigate Navigation callback.
 * @param showStatusDialog Whether to show the status dialog.
 * @param onDismissStatusDialog Callback to dismiss the status dialog.
 */
@Composable
fun ProjectDetailScreen(
    context: ProjectDetailContext,
    plan: ProjectDetailPlan,
    surface: ProjectDetailSurface,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    showStatusDialog: Boolean,
    onDismissStatusDialog: () -> Unit,
) {
    val project = context.project
    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { context.onStatusClick() }) {
            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { context.onEditNode(project.id) }) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { context.viewModel.updateNode(project.copy(isFrozen = !project.isFrozen)) }) {
            Icon(
                if (project.isFrozen) Icons.Default.WbSunny else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (project.isFrozen) TajsOSTheme.Primary else TajsOSTheme.Muted,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(
            onClick = {
                context.viewModel.archiveNode(project)
                onBack()
            },
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }

    SplitScreenScaffold(
        isSplitLayout = surface == ProjectDetailSurface.DESKTOP,
        screen = Screen.ProjectDetail,
        onNavigate = onNavigate,
        screenHeader =
            ScreenHeaderModel(
                breadcrumbs = screenBreadcrumbs(Screen.ProjectDetail),
                title = project.title,
                subtitle = stringResource(Res.string.screen_project),
                actions = actions,
            ),
        backgroundColor = TajsOSTheme.Background,
        scrollBehavior = ScreenScrollBehavior.PaneScroll,
        header = {
            if (surface == ProjectDetailSurface.DESKTOP) {
                ProjectDetailBlockRegistry.resolve("project_header")?.invoke(context)
                ProjectDetailBlockRegistry.resolve("project_hero")?.invoke(context)
            }
        },
        primary = {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalArrangement =
                    androidx.compose.foundation.layout.Arrangement
                        .spacedBy(12.dp),
            ) {
                if (surface == ProjectDetailSurface.DESKTOP) {
                    ProjectDetailBlockRegistry.resolve("project_tabs")?.invoke(context)
                    ProjectDetailBlockRegistry.resolve("project_content")?.invoke(context)
                } else {
                    plan.primary.forEach { block ->
                        ProjectDetailBlockRegistry.resolve(block.id)?.invoke(context)
                    }
                }
            }
        },
        secondary =
            if (surface == ProjectDetailSurface.DESKTOP) {
                {
                    Column(
                        modifier = Modifier.fillMaxWidth().width(320.dp).fillMaxHeight(),
                        verticalArrangement =
                            androidx.compose.foundation.layout.Arrangement
                                .spacedBy(12.dp),
                    ) {
                        plan.secondary.forEach { block ->
                            ProjectDetailBlockRegistry.resolve(block.id)?.invoke(context)
                        }
                    }
                }
            } else {
                null
            },
    )

    if (showStatusDialog) {
        SelectorDialog(
            show = true,
            onDismiss = onDismissStatusDialog,
            title = stringResource(Res.string.project_set_status),
            options = listOf("active", "on_hold", "someday"),
            selectedOption = project.projectStateOrNull()?.toNodeStatus() ?: project.status,
            onSelect = { status ->
                context.viewModel.updateNodeStatus(project, status)
                onDismissStatusDialog()
            },
            optionName = { it },
            optionIcon = { status ->
                when (status) {
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

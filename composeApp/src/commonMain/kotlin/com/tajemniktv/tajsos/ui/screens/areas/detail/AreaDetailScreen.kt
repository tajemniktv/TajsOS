/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderController
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderModel
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.components.screen.SplitScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.screenBreadcrumbs
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.area_detail_not_found
import tajsos.composeapp.generated.resources.area_detail_responsibility_fallback
import tajsos.composeapp.generated.resources.detail_unassign
import kotlin.time.Clock

/**
 * Area detail route that collects system state and coordinates interactions.
 *
 * @param viewModel Source of area state.
 * @param areaId ID of the area to display.
 * @param onNavigateToProject Project navigation callback.
 * @param onEditNode Node edit callback.
 * @param onBack Callback to go back.
 * @param isDesktop Whether the current environment is a desktop layout.
 * @param onNavigate Navigation callback.
 */
@Composable
fun AreaDetailRoute(
    viewModel: MainViewModel,
    areaId: Long,
    onNavigateToProject: (Long) -> Unit,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit,
    isDesktop: Boolean = false,
    onNavigate: (String) -> Unit,
) {
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, areaId) { nodes.find { it.node.id == areaId } }
    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.area_detail_not_found), color = TajsOSTheme.Muted)
        }
        return
    }

    val area = nodeWithPin.node
    val areaSnapshot by viewModel.areaHealthSnapshot.collectAsState()
    val metrics = areaSnapshot.areas.firstOrNull { it.areaId == area.id }
    val projects by viewModel.getProjectsForArea(areaId).collectAsState(initial = emptyList())
    val items by viewModel.getNodesForArea(areaId).collectAsState(initial = emptyList())
    val relations by viewModel.getRelationsForNode(areaId).collectAsState(initial = emptyList())
    val attachments by viewModel.getAttachmentsForNode(areaId).collectAsState(initial = emptyList())
    val tags by viewModel.getTagsForNode(areaId).collectAsState(initial = emptyList())
    val logs by viewModel.getLogsForNode(areaId).collectAsState(initial = emptyList())
    var tab by rememberSaveable(areaId) { mutableStateOf(AreaTab.Overview) }

    // Periodic time state to keep time-sensitive computations fresh
    var currentTimeMs by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(60_000L) // Update every minute
            currentTimeMs = Clock.System.now().toEpochMilliseconds()
        }
    }

    /**
     * Extracted mapping to reduce redundant allocations per node type.
     */
    val nodesList = remember(items) { items.map { it.node } }
    val tasks = remember(nodesList) { nodesList.filter { it.isTaskItem() && it.taskStateOrNull() != TaskState.ARCHIVED } }
    val notes = remember(nodesList) { nodesList.filter { it.isNoteItem() } }
    val records = remember(nodesList) { nodesList.filter { it.isRecordItem() } }
    val activeProjects =
        remember(projects) {
            projects.filter {
                val state = it.projectStateOrNull()
                state == ProjectState.ACTIVE || state == ProjectState.ON_HOLD
            }
        }
    val openResponsibilities =
        remember(tasks) { tasks.filter { it.projectId == null && it.taskStateOrNull() != TaskState.DONE } }
    val pressure =
        remember(tasks, currentTimeMs) {
            tasks.filter {
                it.taskStateOrNull() == TaskState.BLOCKED ||
                    (
                        it.isHardDeadline &&
                            (it.dueAt ?: Long.MAX_VALUE) < currentTimeMs
                    )
            }
        }
    val recentItems = remember(items) { items.sortedByDescending { it.node.updatedAt }.take(10) }
    val healthLabel =
        metrics?.status?.replace("_", " ") ?: stringResource(Res.string.detail_unassign)
    val healthColor =
        when (metrics?.status)
        {
            "on_fire" -> TajsOSTheme.Error
            "overloaded" -> TajsOSTheme.Accent
            "neglected" -> TajsOSTheme.Muted
            "active" -> TajsOSTheme.Primary
            else -> TajsOSTheme.Success
        }
    val load = (metrics?.stressLoad ?: 0).coerceIn(0, 100)
    val cadence = if ((metrics?.neglectedDays ?: 0) >= 10) "Biweekly" else "Weekly"
    val statement =
        area.content
            .trim()
            .ifBlank { stringResource(Res.string.area_detail_responsibility_fallback) }
    val relationIds =
        relations.mapNotNull {
            if (it.fromNodeId ==
                areaId
            ) {
                it.toNodeId
            } else if (it.toNodeId == areaId) {
                it.fromNodeId
            } else {
                null
            }
        }
    val nodesById = nodes.associateBy { it.node.id }

    LaunchedEffect(areaId) { viewModel.setLastActiveContext(null, areaId) }

    val context =
        AreaDetailContext(
            viewModel = viewModel,
            area = area,
            areaName = area.title,
            statement = statement,
            health = healthLabel,
            healthColor = healthColor,
            load = load,
            cadence = cadence,
            tab = tab,
            onTab = { tab = it },
            activeProjects = activeProjects,
            openResponsibilities = openResponsibilities,
            pressure = pressure,
            tasks = tasks,
            notes = notes,
            records = records,
            recentItems = recentItems.map { it.node },
            logs = logs,
            relationIds = relationIds,
            nodesById = nodesById,
            attachmentNames = attachments.map { it.title ?: it.uriOrPath },
            tags = tags.map { it.name },
            onNavigateToProject = onNavigateToProject,
            onEditNode = onEditNode,
            onBack = onBack,
        )

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(TajsOSTheme.Background)) {
        val surface =
            if (isDesktop && maxWidth >= 1180.dp) {
                AreaDetailSurface.DESKTOP
            } else {
                AreaDetailSurface.MOBILE
            }

        val plan = remember(surface) { buildAreaDetailPlan(surface) }

        AreaDetailScreen(
            context = context,
            plan = plan,
            surface = surface,
            healthLabel = healthLabel,
            onBack = onBack,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless area detail screen content.
 *
 * @param context Area detail context.
 * @param plan Area detail plan.
 * @param surface Current UI surface mode.
 * @param healthLabel Formatted health status label.
 * @param onBack Callback to go back.
 * @param onNavigate Navigation callback.
 */
@Composable
fun AreaDetailScreen(
    context: AreaDetailContext,
    plan: AreaDetailPlan,
    surface: AreaDetailSurface,
    healthLabel: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val area = context.area
    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = {
            context.viewModel.archiveNode(area)
            onBack()
        }) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }

    SplitScreenScaffold(
        isSplitLayout = surface == AreaDetailSurface.DESKTOP,
        screen = Screen.AreaDetail,
        onNavigate = onNavigate,
        screenHeader =
            ScreenHeaderModel(
                breadcrumbs = screenBreadcrumbs(Screen.AreaDetail),
                title = area.title,
                subtitle = healthLabel,
                actions = actions,
            ),
        backgroundColor = TajsOSTheme.Background,
        scrollBehavior = ScreenScrollBehavior.PaneScroll,
        primary = {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            ) {
                val blocks =
                    if (surface == AreaDetailSurface.DESKTOP) {
                        plan.primary.filterNot { it.id == "area_sidebar" }
                    } else {
                        plan.primary
                    }
                blocks.forEach { block ->
                    AreaDetailBlocks.resolve(block.id)?.invoke(context)
                }
            }
        },
        secondary =
            if (surface == AreaDetailSurface.DESKTOP) {
                {
                    Column(
                        modifier = Modifier.fillMaxWidth().width(320.dp).fillMaxHeight(),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                    ) {
                        plan.secondary.forEach { block ->
                            AreaDetailBlocks.resolve(block.id)?.invoke(context)
                        }
                    }
                }
            } else {
                null
            },
    )
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.ProjectState
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isRecordItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.projectStateOrNull
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.layout.LocalHeaderActions
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.area_detail_not_found
import tajsos.composeapp.generated.resources.area_detail_responsibility_fallback
import tajsos.composeapp.generated.resources.detail_unassign
import kotlin.time.Clock

@Composable
fun AreaDetailScreen(
    viewModel: MainViewModel,
    areaId: Long,
    onNavigateToProject: (Long) -> Unit,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit,
    isDesktop: Boolean = false,
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

    val tasks = items.map { it.node }.filter { it.isTaskItem() && it.status != "archived" }
    val notes = items.map { it.node }.filter { it.isNoteItem() }
    val records = items.map { it.node }.filter { it.isRecordItem() }
    val activeProjects =
        projects.filter {
            it.projectStateOrNull() in
                setOf(
                    ProjectState.ACTIVE,
                    ProjectState.ON_HOLD,
                )
        }
    val openResponsibilities =
        tasks.filter { it.projectId == null && it.taskStateOrNull() != TaskState.DONE }
    val pressure =
        tasks.filter {
            it.taskStateOrNull() == TaskState.BLOCKED ||
                (
                    it.isHardDeadline &&
                        (it.dueAt ?: Long.MAX_VALUE) <
                        Clock.System
                            .now()
                            .toEpochMilliseconds()
                )
        }
    val recentItems = items.sortedByDescending { it.node.updatedAt }.take(10)
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

    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = {
            viewModel.archiveNode(area)
            onBack()
        }) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }

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

    CompositionLocalProvider(LocalHeaderActions provides actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(TajsOSTheme.Background)) {
            val surface =
                if (isDesktop && maxWidth >= 1180.dp) {
                    AreaDetailSurface.DESKTOP
                } else {
                    AreaDetailSurface.MOBILE
                }

            val plan = remember(surface) { buildAreaDetailPlan(surface) }

            if (surface == AreaDetailSurface.DESKTOP) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        plan.primary.filterNot { it.id == "area_sidebar" }.forEach { block ->
                            AreaDetailBlocks.resolve(block.id)?.invoke(context)
                        }
                    }
                    Column(
                        modifier = Modifier.width(320.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        plan.secondary.forEach { block ->
                            AreaDetailBlocks.resolve(block.id)?.invoke(context)
                        }
                    }
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
                    plan.primary.forEach { block ->
                        AreaDetailBlocks.resolve(block.id)?.invoke(context)
                    }
                }
            }
        }
    }
}

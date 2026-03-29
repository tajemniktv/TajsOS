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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
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
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.ProjectState
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isRecordItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.projectStateOrNull
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.LinkedNodeItem
import com.tajemniktv.tajsos.ui.components.common.DetailHeader
import com.tajemniktv.tajsos.ui.components.layout.LocalHeaderActions
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.area_detail_cadence
import tajsos.composeapp.generated.resources.area_detail_context
import tajsos.composeapp.generated.resources.area_detail_health
import tajsos.composeapp.generated.resources.area_detail_mission_control
import tajsos.composeapp.generated.resources.area_detail_not_found
import tajsos.composeapp.generated.resources.area_detail_open_responsibilities
import tajsos.composeapp.generated.resources.area_detail_pressure
import tajsos.composeapp.generated.resources.area_detail_responsibility_fallback
import tajsos.composeapp.generated.resources.area_detail_review_signals
import tajsos.composeapp.generated.resources.area_detail_subtitle
import tajsos.composeapp.generated.resources.area_detail_tab_notes
import tajsos.composeapp.generated.resources.area_detail_tab_overview
import tajsos.composeapp.generated.resources.area_detail_tab_projects
import tajsos.composeapp.generated.resources.area_detail_tab_review
import tajsos.composeapp.generated.resources.area_detail_tab_timeline
import tajsos.composeapp.generated.resources.area_detail_tab_work
import tajsos.composeapp.generated.resources.area_detail_timeline_empty
import tajsos.composeapp.generated.resources.detail_unassign
import tajsos.composeapp.generated.resources.project_detail_assets
import tajsos.composeapp.generated.resources.project_detail_empty_assets
import tajsos.composeapp.generated.resources.project_detail_empty_notes
import tajsos.composeapp.generated.resources.project_detail_empty_projects
import tajsos.composeapp.generated.resources.project_detail_empty_work
import tajsos.composeapp.generated.resources.project_detail_linked_notes
import tajsos.composeapp.generated.resources.project_detail_status
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_record
import kotlin.time.Clock
import kotlin.time.Instant

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
            Text(stringResource(Res.string.area_detail_not_found), color = TactileTheme.Muted)
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
                    it.isHardDeadline && (it.dueAt ?: Long.MAX_VALUE) <
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
            "on_fire" -> TactileTheme.Error
            "overloaded" -> TactileTheme.Accent
            "neglected" -> TactileTheme.Muted
            "active" -> TactileTheme.Primary
            else -> TactileTheme.Success
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

    CompositionLocalProvider(LocalHeaderActions provides actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(TactileTheme.Background)) {
            val wide = isDesktop && maxWidth >= 1180.dp
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AreaMain(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
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
                        onNavigateToProject = onNavigateToProject,
                        onEditNode = onEditNode,
                    )
                    AreaContextSidebar(
                        modifier = Modifier.width(320.dp).fillMaxHeight(),
                        health = healthLabel,
                        healthColor = healthColor,
                        cadence = cadence,
                        relationIds = relationIds,
                        nodesById = nodesById,
                        attachmentNames = attachments.map { it.title ?: it.uriOrPath },
                        tags = tags.map { it.name },
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
                    AreaMain(
                        modifier = Modifier.fillMaxWidth(),
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
                        onNavigateToProject = onNavigateToProject,
                        onEditNode = onEditNode,
                    )
                    AreaContextSidebar(
                        modifier = Modifier.fillMaxWidth(),
                        health = healthLabel,
                        healthColor = healthColor,
                        cadence = cadence,
                        relationIds = relationIds,
                        nodesById = nodesById,
                        attachmentNames = attachments.map { it.title ?: it.uriOrPath },
                        tags = tags.map { it.name },
                    )
                }
            }
        }
    }
}

private enum class AreaTab(
    val label: StringResource,
    val icon: ImageVector,
) {
    Overview(Res.string.area_detail_tab_overview, Icons.Default.Insights),
    Projects(Res.string.area_detail_tab_projects, Icons.Default.Folder),
    Work(Res.string.area_detail_tab_work, Icons.Default.CheckCircle),
    Notes(Res.string.area_detail_tab_notes, Icons.Default.Description),
    Timeline(Res.string.area_detail_tab_timeline, Icons.Default.History),
    Review(Res.string.area_detail_tab_review, Icons.Default.Reviews),
}

@Composable
private fun AreaMain(
    modifier: Modifier = Modifier,
    areaName: String,
    statement: String,
    health: String,
    healthColor: Color,
    load: Int,
    cadence: String,
    tab: AreaTab,
    onTab: (AreaTab) -> Unit,
    activeProjects: List<com.tajemniktv.tajsos.data.NodeEntity>,
    openResponsibilities: List<com.tajemniktv.tajsos.data.NodeEntity>,
    pressure: List<com.tajemniktv.tajsos.data.NodeEntity>,
    tasks: List<com.tajemniktv.tajsos.data.NodeEntity>,
    notes: List<com.tajemniktv.tajsos.data.NodeEntity>,
    records: List<com.tajemniktv.tajsos.data.NodeEntity>,
    recentItems: List<com.tajemniktv.tajsos.data.NodeEntity>,
    logs: List<com.tajemniktv.tajsos.data.EventLogEntity>,
    onNavigateToProject: (Long) -> Unit,
    onEditNode: (Long) -> Unit,
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
                title = areaName,
                subtitle = stringResource(Res.string.area_detail_subtitle),
            )
            Surface(
                color = TactileTheme.SurfaceHighest,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        stringResource(Res.string.area_detail_mission_control),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        statement,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TactileTheme.Text,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {
                        }, label = {
                            Text("${stringResource(Res.string.area_detail_health)}: $health")
                        }, leadingIcon = { Icon(Icons.Default.BarChart, null, tint = healthColor) })
                        AssistChip(onClick = {
                        }, label = {
                            Text(
                                "${stringResource(Res.string.area_detail_cadence)}: $cadence",
                            )
                        }, leadingIcon = { Icon(Icons.Default.Schedule, null) })
                        AssistChip(
                            onClick = {},
                            label = { Text("Load: $load%") },
                            leadingIcon = { Icon(Icons.Default.Speed, null) },
                        )
                    }
                    LinearProgressIndicator(
                        progress = { load / 100f },
                        modifier = Modifier.fillMaxWidth().height(7.dp),
                        color =
                            if (load >=
                                70
                            ) {
                                TactileTheme.Error
                            } else {
                                TactileTheme.Primary
                            },
                        trackColor = TactileTheme.SurfaceLow,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = {}) { Text(stringResource(Res.string.area_detail_tab_work)) }
                        FilledTonalButton(onClick = {}) { Text(stringResource(Res.string.area_detail_tab_notes)) }
                        FilledTonalButton(onClick = {}) { Text(stringResource(Res.string.area_detail_tab_projects)) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AreaTab.entries.forEach { t ->
                    FilterChip(
                        selected = tab == t,
                        onClick = { onTab(t) },
                        label = { Text(stringResource(t.label)) },
                        leadingIcon = { Icon(t.icon, null) },
                    )
                }
            }
            Surface(
                color = TactileTheme.SurfaceLow,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    when (tab)
                    {
                        AreaTab.Overview -> {
                            SectionHeader(
                                Res.string.area_detail_open_responsibilities,
                                Icons.Default.PlayArrow,
                            )
                            if (openResponsibilities.isEmpty()) {
                                EmptyLabel(Res.string.project_detail_empty_work)
                            } else {
                                openResponsibilities.take(8).forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.nextSmallestStep
                                                ?: stringResource(Res.string.area_detail_tab_work),
                                        icon = Icons.Default.CheckCircle,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }
                            SectionHeader(Res.string.area_detail_pressure, Icons.Default.EventBusy)
                            if (pressure.isEmpty()) {
                                EmptyLabel(Res.string.project_detail_empty_work)
                            } else {
                                pressure.take(6).forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.nextSmallestStep
                                                ?: stringResource(Res.string.area_detail_review_signals),
                                        icon = Icons.Default.EventBusy,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }
                        }

                        AreaTab.Projects -> {
                            SectionHeader(Res.string.area_detail_tab_projects, Icons.Default.Folder)
                            if (activeProjects.isEmpty()) {
                                EmptyLabel(Res.string.project_detail_empty_projects)
                            } else {
                                activeProjects.forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.projectStateOrNull()?.name
                                                ?: stringResource(Res.string.project_detail_status),
                                        icon = Icons.Default.Folder,
                                        onClick = { onNavigateToProject(it.id) },
                                    )
                                }
                            }
                        }

                        AreaTab.Work -> {
                            SectionHeader(
                                Res.string.area_detail_tab_work,
                                Icons.Default.CheckCircle,
                            )
                            if (tasks.isEmpty()) {
                                EmptyLabel(Res.string.project_detail_empty_work)
                            } else {
                                tasks.forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.taskStateOrNull()?.name
                                                ?: stringResource(Res.string.area_detail_tab_work),
                                        icon = Icons.Default.CheckCircle,
                                        onClick = { onEditNode(it.id) },
                                    )
                                }
                            }
                        }

                        AreaTab.Notes -> {
                            SectionHeader(
                                Res.string.project_detail_linked_notes,
                                Icons.Default.Description,
                            )
                            if (notes.isEmpty()) {
                                EmptyLabel(Res.string.project_detail_empty_notes)
                            } else {
                                notes.forEach {
                                    LinkedNodeItem(
                                        title = it.title,
                                        subtitle =
                                            it.noteType ?: stringResource(
                                                Res.string.type_note,
                                            ),
                                        icon = Icons.Default.Description,
                                        onClick = {
                                            onEditNode(it.id)
                                        },
                                    )
                                }
                            }
                            SectionHeader(Res.string.type_record, Icons.Default.History)
                            records.forEach {
                                LinkedNodeItem(
                                    title = it.title,
                                    subtitle =
                                        stringResource(
                                            Res.string.type_record,
                                        ),
                                    icon = Icons.Default.History,
                                    onClick = {
                                        onEditNode(it.id)
                                    },
                                )
                            }
                        }

                        AreaTab.Timeline -> {
                            SectionHeader(
                                Res.string.area_detail_tab_timeline,
                                Icons.Default.History,
                            )
                            if (logs.isEmpty() && recentItems.isEmpty()) EmptyLabel(Res.string.area_detail_timeline_empty)
                            logs.sortedByDescending { it.timestamp }.take(6).forEach {
                                LinkedNodeItem(
                                    title =
                                        it.eventType.replace(
                                            "_",
                                            " ",
                                        ),
                                    subtitle = formatTimestamp(it.timestamp),
                                    icon = Icons.Default.History,
                                    onClick = {
                                        val targetId = it.nodeId ?: return@LinkedNodeItem
                                        onEditNode(targetId)
                                    },
                                )
                            }
                            recentItems.take(6).forEach {
                                LinkedNodeItem(
                                    title = it.title,
                                    subtitle =
                                        formatTimestamp(
                                            it.updatedAt,
                                        ),
                                    icon = if (it.isTaskItem()) Icons.Default.CheckCircle else Icons.Default.Description,
                                    onClick = {
                                        onEditNode(it.id)
                                    },
                                )
                            }
                        }

                        AreaTab.Review -> {
                            SectionHeader(Res.string.area_detail_tab_review, Icons.Default.Reviews)
                            MetricRow(
                                stringResource(Res.string.area_detail_health),
                                health,
                                Icons.Default.BarChart,
                                healthColor,
                            )
                            MetricRow(
                                "Load",
                                "$load%",
                                Icons.Default.Speed,
                                if (load >= 70) TactileTheme.Error else TactileTheme.Primary,
                            )
                            MetricRow(
                                stringResource(Res.string.area_detail_cadence),
                                cadence,
                                Icons.AutoMirrored.Filled.TrendingUp,
                                TactileTheme.Primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaContextSidebar(
    modifier: Modifier = Modifier,
    health: String,
    healthColor: Color,
    cadence: String,
    relationIds: List<Long>,
    nodesById: Map<Long, NodeWithPin>,
    attachmentNames: List<String>,
    tags: List<String>,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SidebarBlock(
            title = stringResource(Res.string.area_detail_context),
            icon = Icons.Default.Insights,
        ) {
            MetricRow(
                stringResource(Res.string.area_detail_health),
                health,
                Icons.Default.BarChart,
                healthColor,
            )
            MetricRow(
                stringResource(Res.string.area_detail_cadence),
                cadence,
                Icons.Default.Schedule,
            )
        }
        SidebarBlock(
            title = stringResource(Res.string.area_detail_tab_timeline),
            icon = Icons.Default.CalendarToday,
        ) {
            MetricRow(
                "Now",
                formatTimestamp(Clock.System.now().toEpochMilliseconds()),
                Icons.Default.AccessTime,
            )
        }
        SidebarBlock(
            title = stringResource(Res.string.area_detail_review_signals),
            icon = Icons.Default.Hub,
        ) {
            if (relationIds.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_work),
                    color = TactileTheme.Muted,
                )
            }
            relationIds.take(8).forEach { id ->
                val node = nodesById[id]?.node ?: return@forEach
                Text(
                    "• ${node.title}",
                    color = TactileTheme.Text,
                    modifier = Modifier.fillMaxWidth().clickable { },
                )
            }
        }
        SidebarBlock(
            title = stringResource(Res.string.project_detail_assets),
            icon = Icons.Default.AttachFile,
        ) {
            if (attachmentNames.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_assets),
                    color = TactileTheme.Muted,
                )
            }
            attachmentNames.take(6).forEach { Text("• $it", color = TactileTheme.Text) }
        }
        SidebarBlock(
            title = stringResource(Res.string.area_detail_review_signals),
            icon = Icons.Default.Description,
        ) {
            if (tags.isEmpty()) {
                Text(
                    stringResource(Res.string.detail_unassign),
                    color = TactileTheme.Muted,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.take(4).forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
        }
    }
}

@Composable
private fun SidebarBlock(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Surface(color = TactileTheme.Surface, shape = RoundedCornerShape(TactileTheme.RadiusMd)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, null, tint = TactileTheme.Primary)
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
private fun MetricRow(
    label: String,
    value: String,
    icon: ImageVector,
    tone: Color = TactileTheme.Text,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tone)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TactileTheme.Text)
        }
    }
}

@Composable
private fun SectionHeader(
    title: StringResource,
    icon: ImageVector,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = TactileTheme.Primary)
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
    HorizontalDivider(color = TactileTheme.GhostBorder)
}

@Composable
private fun EmptyLabel(text: StringResource) {
    Surface(color = TactileTheme.Surface, shape = RoundedCornerShape(TactileTheme.RadiusMd)) {
        Text(stringResource(text), color = TactileTheme.Muted, modifier = Modifier.padding(12.dp))
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "-"
    val dt =
        Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.year}-${dt.monthNumber.toString().padStart(2, '0')}-${
        dt.day.toString().padStart(2, '0')
    }"
}

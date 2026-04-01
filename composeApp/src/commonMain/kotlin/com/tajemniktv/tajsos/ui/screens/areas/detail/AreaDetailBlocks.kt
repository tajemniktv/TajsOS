/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.projectStateOrNull
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.components.cards.LinkedNodeItem
import com.tajemniktv.tajsos.ui.components.common.DetailHeader
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.area_detail_cadence
import tajsos.composeapp.generated.resources.area_detail_context
import tajsos.composeapp.generated.resources.area_detail_health
import tajsos.composeapp.generated.resources.area_detail_mission_control
import tajsos.composeapp.generated.resources.area_detail_open_responsibilities
import tajsos.composeapp.generated.resources.area_detail_pressure
import tajsos.composeapp.generated.resources.area_detail_review_signals
import tajsos.composeapp.generated.resources.area_detail_subtitle
import tajsos.composeapp.generated.resources.area_detail_tab_notes
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

object AreaDetailBlocks {
    private val renderers: Map<String, AreaDetailBlockRenderer> =
        mapOf(
            "area_header" to ::renderAreaHeader,
            "area_hero" to ::renderAreaHero,
            "area_tabs" to ::renderAreaTabs,
            "area_content" to ::renderAreaContent,
            "area_sidebar" to ::renderAreaSidebar,
        )

    fun resolve(id: String): AreaDetailBlockRenderer? = renderers[id]
}

@Composable
private fun renderAreaHeader(context: AreaDetailContext) {
    DetailHeader(
        title = context.areaName,
        subtitle = stringResource(Res.string.area_detail_subtitle),
    )
}

@Composable
private fun renderAreaHero(context: AreaDetailContext) {
    Surface(
        color = TajsOSTheme.SurfaceHighest,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(Res.string.area_detail_mission_control),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                context.statement,
                style = MaterialTheme.typography.bodyLarge,
                color = TajsOSTheme.Text,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {
                }, label = {
                    Text("${stringResource(Res.string.area_detail_health)}: ${context.health}")
                }, leadingIcon = { Icon(Icons.Default.BarChart, null, tint = context.healthColor) })
                AssistChip(onClick = {
                }, label = {
                    Text(
                        "${stringResource(Res.string.area_detail_cadence)}: ${context.cadence}",
                    )
                }, leadingIcon = { Icon(Icons.Default.Schedule, null) })
                AssistChip(
                    onClick = {},
                    label = { Text("Load: ${context.load}%") },
                    leadingIcon = { Icon(Icons.Default.Speed, null) },
                )
            }
            LinearProgressIndicator(
                progress = { context.load / 100f },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color =
                    if (context.load >= 70) {
                        TajsOSTheme.Error
                    } else {
                        TajsOSTheme.Primary
                    },
                trackColor = TajsOSTheme.SurfaceLow,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = {}) { Text(stringResource(Res.string.area_detail_tab_work)) }
                FilledTonalButton(onClick = {}) { Text(stringResource(Res.string.area_detail_tab_notes)) }
                FilledTonalButton(onClick = {}) { Text(stringResource(Res.string.area_detail_tab_projects)) }
            }
        }
    }
}

@Composable
private fun renderAreaTabs(context: AreaDetailContext) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AreaTab.entries.forEach { t ->
            FilterChip(
                selected = context.tab == t,
                onClick = { context.onTab(t) },
                label = { Text(stringResource(t.label)) },
                leadingIcon = { Icon(t.icon, null) },
            )
        }
    }
}

@Composable
private fun renderAreaContent(context: AreaDetailContext) {
    Surface(
        color = TajsOSTheme.SurfaceLow,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (context.tab)
            {
                AreaTab.Overview -> {
                    SectionHeader(
                        Res.string.area_detail_open_responsibilities,
                        Icons.Default.PlayArrow,
                    )
                    if (context.openResponsibilities.isEmpty()) {
                        EmptyLabel(Res.string.project_detail_empty_work)
                    } else {
                        context.openResponsibilities.take(8).forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.nextSmallestStep
                                        ?: stringResource(Res.string.area_detail_tab_work),
                                icon = Icons.Default.CheckCircle,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }
                    SectionHeader(Res.string.area_detail_pressure, Icons.Default.EventBusy)
                    if (context.pressure.isEmpty()) {
                        EmptyLabel(Res.string.project_detail_empty_work)
                    } else {
                        context.pressure.take(6).forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.nextSmallestStep
                                        ?: stringResource(Res.string.area_detail_review_signals),
                                icon = Icons.Default.EventBusy,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }
                }

                AreaTab.Projects -> {
                    SectionHeader(Res.string.area_detail_tab_projects, Icons.Default.Folder)
                    if (context.activeProjects.isEmpty()) {
                        EmptyLabel(Res.string.project_detail_empty_projects)
                    } else {
                        context.activeProjects.forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.projectStateOrNull()?.name
                                        ?: stringResource(Res.string.project_detail_status),
                                icon = Icons.Default.Folder,
                                onClick = { context.onNavigateToProject(it.id) },
                            )
                        }
                    }
                }

                AreaTab.Work -> {
                    SectionHeader(
                        Res.string.area_detail_tab_work,
                        Icons.Default.CheckCircle,
                    )
                    if (context.tasks.isEmpty()) {
                        EmptyLabel(Res.string.project_detail_empty_work)
                    } else {
                        context.tasks.forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.taskStateOrNull()?.name
                                        ?: stringResource(Res.string.area_detail_tab_work),
                                icon = Icons.Default.CheckCircle,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }
                }

                AreaTab.Notes -> {
                    SectionHeader(
                        Res.string.project_detail_linked_notes,
                        Icons.Default.Description,
                    )
                    if (context.notes.isEmpty()) {
                        EmptyLabel(Res.string.project_detail_empty_notes)
                    } else {
                        context.notes.forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.noteType ?: stringResource(
                                        Res.string.type_note,
                                    ),
                                icon = Icons.Default.Description,
                                onClick = {
                                    context.onEditNode(it.id)
                                },
                            )
                        }
                    }
                    SectionHeader(Res.string.type_record, Icons.Default.History)
                    context.records.forEach {
                        LinkedNodeItem(
                            title = it.title,
                            subtitle =
                                stringResource(
                                    Res.string.type_record,
                                ),
                            icon = Icons.Default.History,
                            onClick = {
                                context.onEditNode(it.id)
                            },
                        )
                    }
                }

                AreaTab.Timeline -> {
                    SectionHeader(
                        Res.string.area_detail_tab_timeline,
                        Icons.Default.History,
                    )
                    if (context.logs.isEmpty() && context.recentItems.isEmpty()) EmptyLabel(Res.string.area_detail_timeline_empty)
                    context.logs.sortedByDescending { it.timestamp }.take(6).forEach {
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
                                context.onEditNode(targetId)
                            },
                        )
                    }
                    context.recentItems.take(6).forEach {
                        LinkedNodeItem(
                            title = it.title,
                            subtitle =
                                formatTimestamp(
                                    it.updatedAt,
                                ),
                            icon = if (it.isTaskItem()) Icons.Default.CheckCircle else Icons.Default.Description,
                            onClick = {
                                context.onEditNode(it.id)
                            },
                        )
                    }
                }

                AreaTab.Review -> {
                    SectionHeader(Res.string.area_detail_tab_review, Icons.Default.Reviews)
                    MetricRow(
                        stringResource(Res.string.area_detail_health),
                        context.health,
                        Icons.Default.BarChart,
                        context.healthColor,
                    )
                    MetricRow(
                        "Load",
                        "${context.load}%",
                        Icons.Default.Speed,
                        if (context.load >= 70) TajsOSTheme.Error else TajsOSTheme.Primary,
                    )
                    MetricRow(
                        stringResource(Res.string.area_detail_cadence),
                        context.cadence,
                        Icons.AutoMirrored.Filled.TrendingUp,
                        TajsOSTheme.Primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun renderAreaSidebar(context: AreaDetailContext) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SidebarBlock(
            title = stringResource(Res.string.area_detail_context),
            icon = Icons.Default.Insights,
        ) {
            MetricRow(
                stringResource(Res.string.area_detail_health),
                context.health,
                Icons.Default.BarChart,
                context.healthColor,
            )
            MetricRow(
                stringResource(Res.string.area_detail_cadence),
                context.cadence,
                Icons.Default.Schedule,
            )
        }
        SidebarBlock(
            title = stringResource(Res.string.area_detail_tab_timeline),
            icon = Icons.Default.CalendarToday,
        ) {
            MetricRow(
                "Now",
                formatTimestamp(
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds(),
                ),
                Icons.Default.AccessTime,
            )
        }
        SidebarBlock(
            title = stringResource(Res.string.area_detail_review_signals),
            icon = Icons.Default.Hub,
        ) {
            if (context.relationIds.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_work),
                    color = TajsOSTheme.Muted,
                )
            }
            context.relationIds.take(8).forEach { id ->
                val node = context.nodesById[id]?.node ?: return@forEach
                Text(
                    "• ${node.title}",
                    color = TajsOSTheme.Text,
                    modifier = Modifier.fillMaxWidth().clickable { },
                )
            }
        }
        SidebarBlock(
            title = stringResource(Res.string.project_detail_assets),
            icon = Icons.Default.AttachFile,
        ) {
            if (context.attachmentNames.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_assets),
                    color = TajsOSTheme.Muted,
                )
            }
            context.attachmentNames.take(6).forEach { Text("• $it", color = TajsOSTheme.Text) }
        }
        SidebarBlock(
            title = stringResource(Res.string.area_detail_review_signals),
            icon = Icons.Default.Description,
        ) {
            if (context.tags.isEmpty()) {
                Text(
                    stringResource(Res.string.detail_unassign),
                    color = TajsOSTheme.Muted,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                context.tags.take(4).forEach { AssistChip(onClick = {}, label = { Text(it) }) }
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
    Surface(color = TajsOSTheme.Surface, shape = RoundedCornerShape(TajsOSTheme.RadiusMd)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, null, tint = TajsOSTheme.Primary)
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
    tone: Color = TajsOSTheme.Text,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tone)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TajsOSTheme.Muted)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TajsOSTheme.Text)
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
        Icon(icon, null, tint = TajsOSTheme.Primary)
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
    HorizontalDivider(color = TajsOSTheme.GhostBorder)
}

@Composable
private fun EmptyLabel(text: StringResource) {
    Surface(color = TajsOSTheme.Surface, shape = RoundedCornerShape(TajsOSTheme.RadiusMd)) {
        Text(stringResource(text), color = TajsOSTheme.Muted, modifier = Modifier.padding(12.dp))
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "-"
    val dt =
        kotlin.time.Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.year}-${dt.month.number.toString().padStart(2, '0')}-${
        dt.day.toString().padStart(2, '0')
    }"
}

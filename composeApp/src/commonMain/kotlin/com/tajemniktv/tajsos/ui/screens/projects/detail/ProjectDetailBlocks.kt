/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.EventLogEntity
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
import tajsos.composeapp.generated.resources.project_detail_health
import tajsos.composeapp.generated.resources.project_detail_linked_notes
import tajsos.composeapp.generated.resources.project_detail_linked_records
import tajsos.composeapp.generated.resources.project_detail_linked_tasks
import tajsos.composeapp.generated.resources.project_detail_mission_control
import tajsos.composeapp.generated.resources.project_detail_next_actions
import tajsos.composeapp.generated.resources.project_detail_outcome
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
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_record
import kotlin.math.roundToInt

object ProjectDetailBlockRegistry {
    private val renderers: Map<String, ProjectDetailBlockRenderer> =
        mapOf(
            "project_header" to ::renderProjectHeader,
            "project_hero" to ::renderProjectHero,
            "project_tabs" to ::renderProjectTabs,
            "project_content" to ::renderProjectContent,
            "project_sidebar" to ::renderProjectSidebar,
        )

    fun resolve(id: String): ProjectDetailBlockRenderer? = renderers[id]
}

@Composable
private fun renderProjectHeader(context: ProjectDetailContext) {
    DetailHeader(
        title = context.project.title,
        subtitle = stringResource(Res.string.project_detail_mission_control),
    )
}

@Composable
private fun renderProjectHero(context: ProjectDetailContext) {
    val project = context.project
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.SurfaceHighest,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(Res.string.project_detail_outcome),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
            Text(
                text = context.outcomeText,
                style = MaterialTheme.typography.bodyLarge,
                color = TajsOSTheme.Text,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = context.onStatusClick,
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
                        label = { Text("${stringResource(Res.string.project_detail_health)}: ${context.healthLabel}") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = context.healthColor,
                            )
                        },
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "${(context.progress * 100f).roundToInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TajsOSTheme.Text,
                    )
                    Text(
                        text =
                            stringResource(
                                Res.string.project_detail_progress_value,
                                (context.progress * 100f).roundToInt(),
                                context.completedTasksCount,
                                context.totalTasksCount,
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }

            LinearProgressIndicator(
                progress = { context.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = if (project.isFrozen) TajsOSTheme.Muted else TajsOSTheme.Primary,
                trackColor = TajsOSTheme.SurfaceLow,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(onClick = context.onStatusClick) {
                    Icon(Icons.Default.Tune, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.project_detail_set_status))
                }
                FilledTonalButton(onClick = {}) {
                    Icon(Icons.Default.FolderOpen, null)
                    Spacer(Modifier.width(6.dp))
                    Text(context.areaName ?: stringResource(Res.string.detail_unassign))
                }
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "${stringResource(Res.string.project_detail_target_date)}: ${
                                formatTimestamp(
                                    context.targetDate,
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
private fun renderProjectTabs(context: ProjectDetailContext) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProjectDetailTab.entries.forEach { tab ->
            FilterChip(
                selected = context.selectedTab == tab,
                onClick = { context.onSelectTab(tab) },
                label = { Text(stringResource(tab.labelRes)) },
                leadingIcon = { Icon(tab.icon, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun renderProjectContent(context: ProjectDetailContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.SurfaceLow,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (context.selectedTab)
            {
                ProjectDetailTab.Overview -> {
                    ProjectSectionTitle(
                        Res.string.project_detail_next_actions,
                        Icons.Default.PlayArrow,
                    )
                    if (context.nextActions.isEmpty()) {
                        ProjectEmptyState(Res.string.project_detail_empty_work)
                    } else {
                        context.nextActions.take(6).forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.nextSmallestStep
                                        ?: stringResource(Res.string.project_detail_actions),
                                icon = Icons.Default.CheckCircle,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }

                    ProjectSectionTitle(
                        Res.string.project_detail_blockers,
                        Icons.Default.Block,
                    )
                    if (context.blockedTasks.isEmpty()) {
                        ProjectEmptyState(Res.string.project_detail_empty_blockers)
                    } else {
                        context.blockedTasks.take(4).forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.nextSmallestStep
                                        ?: stringResource(Res.string.project_detail_work),
                                icon = Icons.Default.Block,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }

                    if (context.milestones.isNotEmpty()) {
                        ProjectSectionTitle(
                            Res.string.project_detail_schedule,
                            Icons.Default.CalendarToday,
                        )
                        context.milestones.forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle = formatTimestamp(it.dueAt),
                                icon = Icons.Default.Schedule,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }
                }

                ProjectDetailTab.Work -> {
                    ProjectSectionTitle(
                        Res.string.project_detail_linked_tasks,
                        Icons.Default.CheckCircle,
                    )
                    if (context.linkedTasks.isEmpty()) {
                        ProjectEmptyState(Res.string.project_detail_empty_work)
                    } else {
                        context.linkedTasks.forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.taskStateOrNull()?.name
                                        ?: stringResource(Res.string.project_detail_status),
                                icon = Icons.Default.CheckCircle,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }
                }

                ProjectDetailTab.Notes -> {
                    ProjectSectionTitle(
                        Res.string.project_detail_linked_notes,
                        Icons.Default.Description,
                    )
                    if (context.linkedNotes.isEmpty()) {
                        ProjectEmptyState(Res.string.project_detail_empty_notes)
                    } else {
                        context.linkedNotes.forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle =
                                    it.noteType
                                        ?: stringResource(Res.string.type_note),
                                icon = Icons.Default.Description,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }

                    ProjectSectionTitle(
                        Res.string.project_detail_linked_records,
                        Icons.Default.History,
                    )
                    if (context.linkedRecords.isEmpty()) {
                        ProjectEmptyState(Res.string.project_detail_empty_records)
                    } else {
                        context.linkedRecords.forEach {
                            LinkedNodeItem(
                                title = it.title,
                                subtitle = stringResource(Res.string.type_record),
                                icon = Icons.Default.History,
                                onClick = { context.onEditNode(it.id) },
                            )
                        }
                    }
                }

                ProjectDetailTab.Timeline -> {
                    ProjectSectionTitle(
                        Res.string.project_detail_timeline,
                        Icons.Default.History,
                    )
                    if (context.timeline.isEmpty()) {
                        ProjectEmptyState(Res.string.project_detail_empty_timeline)
                    } else {
                        context.timeline
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
                    if (context.attachments.isEmpty()) {
                        ProjectEmptyState(Res.string.project_detail_empty_assets)
                    } else {
                        context.attachments.forEach { attachment ->
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
                        Icons.Default.History,
                    )
                    MissionMetric(
                        label = stringResource(Res.string.project_detail_progress),
                        value =
                            stringResource(
                                Res.string.project_detail_progress_value,
                                (context.progress * 100f).roundToInt(),
                                context.completedTasksCount,
                                context.totalTasksCount,
                            ),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                    )
                    MissionMetric(
                        label = stringResource(Res.string.project_detail_health),
                        value = context.healthLabel,
                        icon = Icons.Default.BarChart,
                        tone = context.healthColor,
                    )
                    MissionMetric(
                        label = stringResource(Res.string.project_detail_target_date),
                        value = formatTimestamp(context.targetDate),
                        icon = Icons.Default.CalendarToday,
                    )
                }
            }
        }
    }
}

@Composable
private fun renderProjectSidebar(context: ProjectDetailContext) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SidebarCard(
            title = stringResource(Res.string.project_detail_contextual_panel),
            icon = Icons.Default.Insights,
        ) {
            SidebarValueRow(
                label = stringResource(Res.string.project_detail_health),
                value = context.healthLabel,
                valueColor = context.healthColor,
            )
            SidebarValueRow(
                label = stringResource(Res.string.type_area),
                value = context.areaName ?: stringResource(Res.string.detail_unassign),
            )
            SidebarValueRow(
                label = stringResource(Res.string.project_detail_target_date),
                value = formatTimestamp(context.targetDate),
            )
        }

        SidebarCard(
            title = stringResource(Res.string.project_detail_relations),
            icon = Icons.Default.Hub,
        ) {
            if (context.relatedNodeIds.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_work),
                    color = TajsOSTheme.Muted,
                )
            } else {
                context.relatedNodeIds.take(8).forEach { id ->
                    val node = context.nodesById[id]?.node ?: return@forEach
                    Text(
                        text = "• ${node.title}",
                        color = TajsOSTheme.Text,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { context.onEditNode(node.id) },
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
                    tint = TajsOSTheme.Primary,
                )
                Text(formatTimestamp(context.targetDate), color = TajsOSTheme.Text)
            }
        }

        SidebarCard(
            title = stringResource(Res.string.project_detail_assets),
            icon = Icons.Default.Archive,
        ) {
            if (context.attachmentNames.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_assets),
                    color = TajsOSTheme.Muted,
                )
            } else {
                context.attachmentNames.take(6).forEach {
                    Text("• $it", color = TajsOSTheme.Text)
                }
            }
        }

        SidebarCard(
            title = stringResource(Res.string.project_detail_actions),
            icon = Icons.AutoMirrored.Filled.Label,
        ) {
            if (context.tags.isEmpty()) {
                Text(
                    stringResource(Res.string.project_detail_empty_timeline),
                    color = TajsOSTheme.Muted,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    context.tags.take(4).forEach { tag ->
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
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icon, contentDescription = null, tint = TajsOSTheme.Primary)
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
    valueColor: Color = TajsOSTheme.Text,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TajsOSTheme.Muted)
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
        Icon(icon, contentDescription = null, tint = TajsOSTheme.Primary)
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TajsOSTheme.Text,
        )
    }
    HorizontalDivider(color = TajsOSTheme.GhostBorder)
}

@Composable
private fun ProjectEmptyState(text: StringResource) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Text(
            text = stringResource(text),
            color = TajsOSTheme.Muted,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun MissionMetric(
    label: String,
    value: String,
    icon: ImageVector,
    tone: Color = TajsOSTheme.Primary,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
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
                        color = TajsOSTheme.Muted,
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TajsOSTheme.Text,
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
                    .background(TajsOSTheme.Muted, RoundedCornerShape(99.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = log.eventType.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            val date =
                kotlin.time.Instant
                    .fromEpochMilliseconds(log.timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            Text(
                text = "${date.hour.toString().padStart(2, '0')}:${
                    date.minute.toString().padStart(2, '0')
                } // ${date.day}/${date.month.number}",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) {
        return "-"
    }
    val date =
        kotlin.time.Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${date.year}-${date.month.number.toString().padStart(2, '0')}-${
        date.day.toString().padStart(2, '0')
    }"
}

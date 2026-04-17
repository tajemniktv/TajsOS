/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_no_results
import tajsos.composeapp.generated.resources.tasks_open_action
import tajsos.composeapp.generated.resources.tasks_start_focus_action
import tajsos.composeapp.generated.resources.tasks_today_due_soon
import tajsos.composeapp.generated.resources.tasks_today_empty
import tajsos.composeapp.generated.resources.tasks_today_overdue
import tajsos.composeapp.generated.resources.tasks_today_pinned
import tajsos.composeapp.generated.resources.tasks_today_subtitle
import tajsos.composeapp.generated.resources.tasks_today_title
import kotlin.time.Clock

@Composable
internal fun TasksTodayView(
    tasks: List<NodeEntity>,
    todayTaskIds: Set<Long>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onDone: (NodeEntity) -> Unit,
    onStartFocus: (NodeEntity) -> Unit,
    onSetTodayPayload: (NodeEntity, Boolean) -> Unit,
) {
    val overdue = remember(tasks) {
        val now = Clock.System.now().toEpochMilliseconds()
        tasks.filter { (it.dueAt ?: Long.MAX_VALUE) < now }
    }
    val dueSoon =
        remember(tasks) {
            val now = Clock.System.now().toEpochMilliseconds()
            val tomorrow = now + 24L * 60 * 60 * 1000
            tasks.filter {
                it.dueAt != null && (it.dueAt ?: Long.MAX_VALUE) in now..tomorrow
            }
        }
    val pinned = remember(tasks, todayTaskIds) { tasks.filter { it.id in todayTaskIds } }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Text(
            stringResource(Res.string.tasks_today_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(Res.string.tasks_today_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )
        if (overdue.isEmpty() && dueSoon.isEmpty() && pinned.isEmpty()) {
            EmptyState(message = stringResource(Res.string.tasks_today_empty))
            return@Column
        }
        TodaySection(
            stringResource(Res.string.tasks_today_overdue),
            overdue,
            projectById,
            areaById,
            onOpen,
            onDone,
            todayTaskIds,
            onStartFocus,
            onSetTodayPayload,
        )
        TodaySection(
            stringResource(Res.string.tasks_today_due_soon),
            dueSoon,
            projectById,
            areaById,
            onOpen,
            onDone,
            todayTaskIds,
            onStartFocus,
            onSetTodayPayload,
        )
        TodaySection(
            stringResource(Res.string.tasks_today_pinned),
            pinned,
            projectById,
            areaById,
            onOpen,
            onDone,
            todayTaskIds,
            onStartFocus,
            onSetTodayPayload,
        )
    }
}

@Composable
private fun TodaySection(
    title: String,
    tasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onDone: (NodeEntity) -> Unit,
    todayTaskIds: Set<Long>,
    onStartFocus: (NodeEntity) -> Unit,
    onSetTodayPayload: (NodeEntity, Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        color = TajsOSTheme.Surface,
        border = BorderStroke(1.dp, TajsOSTheme.Border),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (tasks.isEmpty()) {
                EmptyState(
                    message = stringResource(Res.string.tasks_no_results),
                    description = null,
                    fillParent = false,
                    showContainer = false,
                )
            } else {
                tasks.take(8).forEach { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TajsOSTheme.Text,
                            )
                            val context =
                                listOfNotNull(
                                    task.projectId?.let { projectById[it] },
                                    task.areaId?.let { areaById[it] },
                                    task.dueAt?.let(::shortDate),
                                ).joinToString(" • ")
                            if (context.isNotBlank()) {
                                Text(
                                    context,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TajsOSTheme.Muted,
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                            OutlinedButton(onClick = {
                                onStartFocus(task)
                            }) { Text(stringResource(Res.string.tasks_start_focus_action)) }
                            OutlinedButton(onClick = {
                                onOpen(task.id)
                            }) { Text(stringResource(Res.string.tasks_open_action)) }
                            OutlinedButton(onClick = {
                                onSetTodayPayload(task, task.id !in todayTaskIds)
                            }) {
                                Icon(Icons.Default.Star, null)
                                Text(
                                    if (task.id in todayTaskIds) {
                                        " Remove Today"
                                    } else {
                                        " Add Today"
                                    },
                                )
                            }
                            IconButton(onClick = { onDone(task) }) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
internal fun TaskTabChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
internal fun StatusPill(state: TaskState) {
    val color =
        when (state) {
            TaskState.ACTIVE -> TajsOSTheme.Primary
            TaskState.DONE -> TajsOSTheme.Success
            TaskState.ON_HOLD -> TajsOSTheme.AccentAmber
            TaskState.SOMEDAY -> TajsOSTheme.Muted
            TaskState.BLOCKED -> TajsOSTheme.Error
            TaskState.ARCHIVED -> TajsOSTheme.Muted
        }
    Box(
        modifier =
            Modifier
                .border(1.dp, color.copy(alpha = 0.55f), CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            state.storageKey.replace("_", " ").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
internal fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Muted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Text)
    }
}

@Composable
internal fun ContextRow(
    label: String,
    value: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Muted)
        Text(
            value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = TajsOSTheme.Text,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

internal fun scoreTask(
    task: NodeEntity,
    now: Long,
    todayTaskIds: Set<Long>,
): Int {
    var score = 0
    if (task.taskStateOrNull() == TaskState.ACTIVE) score += 12
    if (task.id in todayTaskIds) score += 8
    if (task.nextSmallestStep?.isNotBlank() == true) score += 6
    task.dueAt?.let {
        val delta = it - now
        score +=
            when {
                delta < 0 -> 12
                delta <= 24L * 60 * 60 * 1000 -> 10
                delta <= 72L * 60 * 60 * 1000 -> 7
                else -> 2
            }
    }
    score +=
        when (task.energyLevel) {
            1 -> 4
            2 -> 2
            else -> 0
        }
    score +=
        when (task.friction) {
            "easy" -> 3
            "unclear" -> -2
            "mentally_heavy" -> -1
            else -> 0
        }
    return score
}

internal fun shortDate(epochMillis: Long): String {
    val date =
        Instant
            .fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    return date.toString()
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StandardTaskRow(
    task: NodeEntity,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showStatusPill: Boolean = false,
    onToggleDone: ((Boolean) -> Unit)? = null,
    trailingActions: @Composable () -> Unit = {}
) {
    val isDone = task.taskStateOrNull() == TaskState.DONE
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (selected) TajsOSTheme.Primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(TajsOSTheme.SpacingMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
    ) {
        if (onToggleDone != null) {
            Checkbox(
                checked = isDone,
                onCheckedChange = onToggleDone,
                colors = CheckboxDefaults.colors(checkedColor = TajsOSTheme.Primary)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDone) TajsOSTheme.Muted else TajsOSTheme.Text,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (isDone) TextDecoration.LineThrough else null
            )

            task.nextSmallestStep?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "↳ $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val contextStr = listOfNotNull(
                    task.projectId?.let { projectById[it] },
                    task.areaId?.let { areaById[it] }
                ).joinToString(" • ")

                if (contextStr.isNotBlank()) {
                    Text(contextStr, style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Muted)
                }

                task.dueAt?.let {
                    Text("Due: ${shortDate(it)}", style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Muted)
                }

                val state = task.taskStateOrNull() ?: TaskState.ACTIVE
                if (showStatusPill || (state != TaskState.ACTIVE && state != TaskState.DONE)) {
                    StatusPill(state)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm), verticalAlignment = Alignment.CenterVertically) {
            trailingActions()
        }
    }
}

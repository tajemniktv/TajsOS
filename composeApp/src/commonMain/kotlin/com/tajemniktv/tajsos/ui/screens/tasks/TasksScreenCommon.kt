/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
        when (state)
        {
            TaskState.ACTIVE -> TactileTheme.Primary
            TaskState.DONE -> Color(0xFF2BAE66)
            TaskState.ON_HOLD -> Color(0xFFF5A623)
            TaskState.SOMEDAY -> TactileTheme.Muted
            TaskState.BLOCKED -> TactileTheme.Error
            TaskState.ARCHIVED -> TactileTheme.Muted
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
        Text(label, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Text)
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
        Text(label, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
        Text(
            value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = TactileTheme.Text,
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
            when
                {
                    delta < 0 -> 12
                    delta <= 24L * 60 * 60 * 1000 -> 10
                    delta <= 72L * 60 * 60 * 1000 -> 7
                    else -> 2
                }
    }
    score +=
        when (task.energyLevel)
        {
            1 -> 4
            2 -> 2
            else -> 0
        }
    score +=
        when (task.friction)
        {
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

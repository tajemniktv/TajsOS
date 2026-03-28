/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal enum class OpenLoopView(
    val label: String,
) {
    Inbox("INBOX"),
    Review("REVIEW"),
    All("ALL"),
    Resolved("RESOLVED"),
}

internal enum class MaintenanceView(
    val label: String,
) {
    Queue("QUEUE"),
    Recurring("RECURRING"),
    Overdue("OVERDUE"),
}

internal val openLoopTypes =
    listOf(
        "reply_needed",
        "waiting_for",
        "pending_decision",
        "must_check_later",
        "follow_up",
        "unresolved_problem",
    )

internal val maintenanceTypes =
    listOf(
        "med_refill",
        "prescription",
        "appointment",
        "bill",
        "subscription",
        "renewal",
        "form",
        "cleaning",
        "backup",
    )

@Composable
internal fun GroupedOpenLoopSection(
    title: String,
    items: List<String>,
) {
    if (items.isEmpty()) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            items.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Text)
            }
        }
    }
}

internal fun parseProtocolChecklist(content: String): List<Pair<Boolean, String>> =
    content.lines().mapNotNull { line ->
        val trimmed = line.trimStart()
        when
            {
                trimmed.startsWith("- [x] ") -> true to trimmed.removePrefix("- [x] ").trim()
                trimmed.startsWith("- [ ] ") -> false to trimmed.removePrefix("- [ ] ").trim()
                else -> null
            }
    }

internal fun formatProtocolTimestamp(timestamp: Long): String {
    val local =
        Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    val hh = local.hour.toString().padStart(2, '0')
    val mm = local.minute.toString().padStart(2, '0')
    return "${local.date} $hh:$mm"
}

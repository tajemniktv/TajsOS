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
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.view_all
import tajsos.composeapp.generated.resources.view_inbox
import tajsos.composeapp.generated.resources.view_overdue
import tajsos.composeapp.generated.resources.view_queue
import tajsos.composeapp.generated.resources.view_recurring
import tajsos.composeapp.generated.resources.view_resolved
import tajsos.composeapp.generated.resources.view_review
import kotlin.time.Instant

internal enum class OpenLoopView(
    val label: StringResource,
) {
    Inbox(Res.string.view_inbox),
    Review(Res.string.view_review),
    All(Res.string.view_all),
    Resolved(Res.string.view_resolved),
}

internal enum class MaintenanceView(
    val label: StringResource,
) {
    Queue(Res.string.view_queue),
    Recurring(Res.string.view_recurring),
    Overdue(Res.string.view_overdue),
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
        color = TajsOSTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border)
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            items.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Text)
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

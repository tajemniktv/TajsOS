/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TrackEntryEntity
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

fun normalizeProtocolLabel(label: String): String =
    label
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()

fun findProtocolTemplate(
    templates: List<TransitionProtocolTemplate>,
    label: String,
): TransitionProtocolTemplate? {
    val normalized = normalizeProtocolLabel(label)
    return templates.firstOrNull {
        normalizeProtocolLabel(it.label) == normalized || it.key == normalized.replace(" ", "_")
    }
}

fun findPlaybookTemplate(
    templates: List<PlaybookTemplate>,
    label: String,
): PlaybookTemplate? {
    val normalized = normalizeProtocolLabel(label)
    return templates.firstOrNull {
        normalizeProtocolLabel(it.label) == normalized || it.key == normalized.replace(" ", "_")
    }
}

fun buildPlaybookRelationshipContext(modeKey: String?): String? =
    buildString {
        append("playbook")
        if (!modeKey.isNullOrBlank()) append("|mode=").append(modeKey.trim().uppercase())
    }.ifBlank { null }

fun parsePlaybookModeKey(context: String?): String? =
    context?.split("|")?.firstNotNullOfOrNull { token ->
        if (token.startsWith("mode=", ignoreCase = true)) {
            token
                .substringAfter("=")
                .trim()
                .uppercase()
                .ifBlank { null }
        } else {
            null
        }
    }

fun buildProtocolChecklistContent(template: TransitionProtocolTemplate): String =
    buildString {
        appendLine("## TRANSITION CHECKLIST")
        template.checklist.forEach { step ->
            appendLine("- [ ] $step")
        }
    }.trimEnd()

fun protocolChecklistProgress(content: String): Pair<Int, Int> {
    val checklistLines =
        content.lines().map { it.trimStart() }.filter {
            it.startsWith("- [ ] ") || it.startsWith("- [x] ")
        }
    val total = checklistLines.size
    val done = checklistLines.count { it.startsWith("- [x] ") }
    return done to total
}

fun suggestPlaybookLabel(
    mode: ModeEntity?,
    entries: List<TrackEntryEntity>,
): String? {
    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
    val latest = entries.filter { it.date == today }.maxByOrNull { it.createdAt }
    if ((latest?.anxietyScore ?: 0) >= 4) {
        return "Panic-ish day protocol"
    }
    if ((latest?.energyScore ?: 5) <= 2) {
        return "Low energy but must function protocol"
    }
    return when (mode?.key)
    {
        "STUDY" -> "Can't start studying protocol"
        "ERRAND" -> "Need to leave house protocol"
        "RECOVERY", "LOW_BATTERY", "CANT_THINK" -> "Bad day protocol"
        "SOCIAL" -> "Need to reply to everyone protocol"
        else -> null
    }
}

fun recommendProtocolLabel(templates: List<TransitionProtocolTemplate>): String? {
    val localNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val key =
        when (localNow.hour)
        {
            in 5..9 -> "morning_startup"
            in 10..14 -> "before_class"
            in 15..18 -> "deep_work_entry"
            in 19..21 -> "work_to_rest"
            else -> "before_sleep"
        }
    return templates.firstOrNull { it.key == key }?.label
}

fun calculateNextRecurringDate(
    currentDue: Long,
    interval: String,
): Long {
    val offset =
        when (interval.uppercase())
        {
            "DAILY" -> 1.days
            "WEEKLY" -> 7.days
            "MONTHLY" -> 30.days
            else -> 1.days
        }
    return (Instant.fromEpochMilliseconds(currentDue) + offset).toEpochMilliseconds()
}

fun matchesQuery(
    nodeWithPin: NodeWithPin,
    query: String,
): Boolean = FilterHelper.matchesQuery(nodeWithPin, query)

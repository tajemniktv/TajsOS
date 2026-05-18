/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.FilterHelper
import com.tajemniktv.tajsos.ui.main.state.PlaybookTemplate
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/** Cached regex for replacing non-alphanumeric characters with spaces to prevent recompilation. */
private val nonAlphaNumericRegex = Regex("[^a-z0-9]+")

/**
 * Normalizes a protocol or playbook label by converting it to lowercase,
 * replacing all non-alphanumeric characters with spaces, and trimming the result.
 * This ensures consistent string matching regardless of user input format.
 *
 * @param label The raw string label to normalize.
 * @return The sanitized, lowercase alphanumeric string.
 */
fun normalizeProtocolLabel(label: String): String =
    label
        .trim()
        .lowercase()
        .replace(nonAlphaNumericRegex, " ")
        .trim()

/**
 * Searches a list of templates to find a matching [TransitionProtocolTemplate]
 * based on either its normalized label or its unique string key.
 *
 * @param templates The list of available protocol templates.
 * @param label The string label or key to search for.
 * @return The matching [TransitionProtocolTemplate], or null if no match is found.
 */
fun findProtocolTemplate(
    templates: List<TransitionProtocolTemplate>,
    label: String,
): TransitionProtocolTemplate? {
    val normalized = normalizeProtocolLabel(label)
    return templates.firstOrNull {
        normalizeProtocolLabel(it.label) == normalized || it.key == normalized.replace(" ", "_")
    }
}

/**
 * Searches a list of templates to find a matching [PlaybookTemplate]
 * based on either its normalized label or its unique string key.
 *
 * @param templates The list of available playbook templates.
 * @param label The string label or key to search for.
 * @return The matching [PlaybookTemplate], or null if no match is found.
 */
fun findPlaybookTemplate(
    templates: List<PlaybookTemplate>,
    label: String,
): PlaybookTemplate? {
    val normalized = normalizeProtocolLabel(label)
    return templates.firstOrNull {
        normalizeProtocolLabel(it.label) == normalized || it.key == normalized.replace(" ", "_")
    }
}

/**
 * Constructs a standardized context string for playbooks, optionally encoding a specific focus mode key.
 * This is used to link a playbook explicitly to a focus state.
 *
 * @param modeKey The specific focus mode key to embed in the context, or null.
 * @return A non-null formatted context string (e.g., "playbook" or "playbook|mode=WORK").
 */
fun buildPlaybookRelationshipContext(modeKey: String?): String? =
    buildString {
        append("playbook")
        if (!modeKey.isNullOrBlank()) append("|mode=").append(modeKey.trim().uppercase())
    }.ifBlank { null }

/**
 * Parses a formalized playbook context string to extract the embedded focus mode key, if one exists.
 *
 * @param context The raw context string to parse (e.g., "playbook|mode=WORK").
 * @return The extracted mode key string, or null if the key is missing or blank.
 */
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

/**
 * Transforms a loaded protocol template into a raw markdown-formatted checklist string
 * suitable for rendering within the core `content` field of a database node.
 *
 * @param template The [TransitionProtocolTemplate] defining the actionable steps.
 * @return A markdown string containing the formatted checklist.
 */
fun buildProtocolChecklistContent(template: TransitionProtocolTemplate): String =
    buildString {
        appendLine("## TRANSITION CHECKLIST")
        template.checklist.forEach { step ->
            appendLine("- [ ] $step")
        }
    }.trimEnd()

/**
 * Analyzes a markdown checklist string to dynamically calculate the number of completed items
 * versus the total number of items.
 *
 * @param content The raw markdown content containing the checklist syntax.
 * @return A [Pair] where the first integer is the completed count, and the second is the total count.
 */
fun protocolChecklistProgress(content: String): Pair<Int, Int> {
    val checklistLines =
        content.lineSequence().mapNotNull { line ->
            line.trimStart().takeIf { it.startsWith("- [ ] ") || it.startsWith("- [x] ") }
        }.toList()
    val total = checklistLines.size
    val done = checklistLines.count { it.startsWith("- [x] ") }
    return done to total
}

/**
 * Evaluates the user's recent analytical tracking entries and current active focus mode
 * to heuristically suggest a highly relevant playbook.
 *
 * For example, if the latest track entry indicates high anxiety or low energy, it will suggest
 * specific emergency coping playbooks before evaluating the focus mode.
 *
 * @param mode The currently active [ModeEntity], or null.
 * @param entries A list of recent [TrackEntryEntity] records.
 * @return A suggested playbook label string, or null if no strong suggestion is found.
 */
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

/**
 * Recommends a transition protocol based strictly on the current local time of day.
 *
 * @param templates A list of available [TransitionProtocolTemplate] blueprints.
 * @return The label of the recommended protocol, or null if none match the time criteria.
 */
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

/**
 * Calculates the next epoch timestamp by taking a base timestamp and adding a recurring interval.
 *
 * @param currentDue The epoch timestamp representing the original or current due date.
 * @param interval A string indicating the recurring interval (e.g., "DAILY", "WEEKLY", "MONTHLY").
 * @return The calculated next epoch timestamp in milliseconds.
 */
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

/**
 * A proxy function delegating to [FilterHelper.matchesQuery] to determine if a node's title,
 * content, or tags match a user-provided search string.
 *
 * @param nodeWithPin The wrapped node entity being evaluated.
 * @param query The raw text search query string.
 * @return True if the node matches the query, false otherwise.
 */
fun matchesQuery(
    nodeWithPin: NodeWithPin,
    query: String,
): Boolean = FilterHelper.matchesQuery(nodeWithPin, query)

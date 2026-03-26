/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val modeProfileJson =
    Json {
        ignoreUnknownKeys = true
    }

/**
 * Defines which UI components or node types are visible in a specific application Mode.
 *
 * @param showInbox Whether the Inbox section should be visible in this Mode.
 * @param showStats Whether productivity or completion statistics should be visible.
 * @param showNotes Whether Notes should be shown in the primary view.
 * @param showResources Whether Resources (links, references) should be shown.
 * @param showDeadlines Whether upcoming Deadlines should be highlighted.
 * @param showOpenLoops Whether unstructured Open Loops should be displayed.
 * @param maxVisibleTasks The maximum number of tasks to display at once in the main list.
 */
@Serializable
data class ModeVisibilityProfile(
    val showInbox: Boolean = true,
    val showStats: Boolean = true,
    val showNotes: Boolean = true,
    val showResources: Boolean = true,
    val showDeadlines: Boolean = true,
    val showOpenLoops: Boolean = true,
    val maxVisibleTasks: Int = 10,
)

/**
 * Defines the inclusion and sorting criteria applied to nodes when a specific Mode is active.
 *
 * @param includeAreaIds The set of Area IDs to include. If empty, no area filtering is applied.
 * @param includeTypes The set of node types (e.g., "task", "project", "note") to include.
 * @param sortStrategy The identifier indicating how the resulting nodes should be sorted (e.g., "DEFAULT", "URGENCY").
 */
@Serializable
data class ModeFilterProfile(
    val includeAreaIds: Set<Long> = emptySet(),
    val includeTypes: Set<String> = emptySet(),
    val sortStrategy: String = "DEFAULT",
)

/**
 * Defines the quick actions available to the user when a specific Mode is active.
 *
 * @param quickActions A list of string identifiers corresponding to available actions (e.g., "CREATE_TASK", "START_TIMER").
 */
@Serializable
data class ModeActionProfile(
    val quickActions: List<String> = emptyList(),
)

/**
 * Defines the AI or context-based suggestion types relevant to a specific Mode.
 *
 * @param suggestionKeys A list of suggestion identifiers to generate or display (e.g., "HIGH_ENERGY_TASKS").
 */
@Serializable
data class ModeSuggestionProfile(
    val suggestionKeys: List<String> = emptyList(),
)

/**
 * An aggregate configuration representing the entire behavior, appearance, and filtering
 * rules for an active Mode within TajsOS.
 *
 * @param modeId The unique identifier of the Mode this profile belongs to.
 * @param visibility The configuration dictating which UI components are displayed.
 * @param filtering The logic defining which nodes are queried from the database.
 * @param actions The configuration of interactive shortcuts available.
 * @param suggestions The configuration detailing what kind of automated suggestions to present.
 * @param dashboardBlocks A list of identifiers for modular dashboard components to render on the home screen.
 */
@Serializable
data class ModeQueryProfile(
    val modeId: Long,
    val visibility: ModeVisibilityProfile,
    val filtering: ModeFilterProfile,
    val actions: ModeActionProfile,
    val suggestions: ModeSuggestionProfile,
    val dashboardBlocks: List<String> = emptyList(),
)

/**
 * Constructs a fully populated [ModeQueryProfile] from its constituent database entities and JSON columns.
 *
 * @param preference The primary entity containing basic toggles and serialized JSON blocks for the Mode.
 * @param areaFilters A list of specific Area inclusion/exclusion rules associated with this Mode.
 * @param typeFilters A list of specific Node Type inclusion rules associated with this Mode.
 * @return A unified [ModeQueryProfile] object ready to be used by the domain logic or UI layer.
 */
fun buildModeQueryProfile(
    preference: ModePreferenceEntity,
    areaFilters: List<ModeAreaFilterEntity>,
    typeFilters: List<ModeTypeFilterEntity>,
): ModeQueryProfile {
    val actions = decodeStringList(preference.defaultQuickActionsJson)
    val dashboardBlocks = decodeStringList(preference.dashboardBlocksJson)
    val suggestionKeys = decodeStringList(preference.suggestionProfileJson)

    return ModeQueryProfile(
        modeId = preference.modeId,
        visibility =
            ModeVisibilityProfile(
                showInbox = preference.showInbox,
                showStats = preference.showStats,
                showNotes = preference.showNotes,
                showResources = preference.showResources,
                showDeadlines = preference.showDeadlines,
                showOpenLoops = preference.showOpenLoops,
                maxVisibleTasks = preference.maxVisibleTasks,
            ),
        filtering =
            ModeFilterProfile(
                includeAreaIds = areaFilters.filter { it.include }.map { it.areaId }.toSet(),
                includeTypes = typeFilters.filter { it.include }.map { it.nodeType }.toSet(),
                sortStrategy = preference.sortStrategy,
            ),
        actions = ModeActionProfile(quickActions = actions),
        suggestions = ModeSuggestionProfile(suggestionKeys = suggestionKeys),
        dashboardBlocks = dashboardBlocks,
    )
}

/**
 * Safely decodes a raw JSON string representing a list of strings into a Kotlin `List<String>`.
 *
 * If the input string is null, blank, or contains invalid JSON that cannot be parsed into
 * a string list, this function catches the exception and returns an empty list instead of crashing.
 *
 * @param raw The raw JSON string to decode (e.g., `["ITEM1", "ITEM2"]`).
 * @return A valid `List<String>`, or an empty list if parsing fails.
 */
private fun decodeStringList(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching { modeProfileJson.decodeFromString<List<String>>(raw) }
        .getOrElse { emptyList() }
}

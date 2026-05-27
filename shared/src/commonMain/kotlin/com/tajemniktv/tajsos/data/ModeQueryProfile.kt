/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlin.coroutines.cancellation.CancellationException

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val modeProfileJson =
    Json {
        ignoreUnknownKeys = true
    }

/**
 * Defines the visibility configuration for different sections of the UI within a specific operating mode.
 *
 * @property showInbox Whether the inbox section is visible. Defaults to true.
 * @property showStats Whether the statistics section is visible. Defaults to true.
 * @property showNotes Whether the notes section is visible. Defaults to true.
 * @property showResources Whether the resources section is visible. Defaults to true.
 * @property showDeadlines Whether the deadlines section is visible. Defaults to true.
 * @property showOpenLoops Whether the open loops section is visible. Defaults to true.
 * @property maxVisibleTasks The maximum number of tasks to display in a list. Defaults to 10.
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
 * Constructs a mode configuration by combining the stored preference with the provided area and type filters.
 *
 * This function acts as a bridge between the raw SQLite database entities ([ModePreferenceEntity],
 * [ModeAreaFilterEntity], [ModeTypeFilterEntity]) and the domain-level [ModeQueryProfile] used by
 * the UI layer. It handles the deserialization of JSON fields like quick actions and dashboard blocks.
 *
 * @param preference The persisted mode preference containing visibility flags, sort strategy, and JSON fields for quick actions, dashboard blocks, and suggestions.
 * @param areaFilters A list of area filter entities; entries with `include == true` indicate areas to include in the profile.
 * @param typeFilters A list of type filter entities; entries with `include == true` indicate node types to include in the profile.
 * @return A [ModeQueryProfile] that consolidates visibility, filtering, actions, suggestions, and dashboard block settings for the mode.
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
                includeAreaIds = areaFilters.mapNotNullTo(mutableSetOf()) { if (it.include) it.areaId else null },
                includeTypes = typeFilters.mapNotNullTo(mutableSetOf()) { if (it.include) it.nodeType else null },
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
private inline fun <T> safeDecode(block: () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

private fun decodeStringList(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    return safeDecode { modeProfileJson.decodeFromString<List<String>>(raw) } ?: emptyList()
}

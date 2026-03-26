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
 * Defines the filtering configuration for nodes within a specific operating mode.
 *
 * @property includeAreaIds The set of area IDs to include. An empty set means no areas are filtered (all are included if no other filter excludes them).
 * @property includeTypes The set of node types to include. An empty set means no types are filtered.
 * @property sortStrategy The strategy used to sort the visible nodes. Defaults to "DEFAULT".
 */
@Serializable
data class ModeFilterProfile(
    val includeAreaIds: Set<Long> = emptySet(),
    val includeTypes: Set<String> = emptySet(),
    val sortStrategy: String = "DEFAULT",
)

/**
 * Defines the quick actions available within a specific operating mode.
 *
 * @property quickActions A list of quick action identifiers available in this mode. An empty list means no quick actions are configured.
 */
@Serializable
data class ModeActionProfile(
    val quickActions: List<String> = emptyList(),
)

/**
 * Defines the suggestions configuration for a specific operating mode.
 *
 * @property suggestionKeys A list of suggestion keys to be used for generating suggestions in this mode. An empty list means no suggestions are configured.
 */
@Serializable
data class ModeSuggestionProfile(
    val suggestionKeys: List<String> = emptyList(),
)

/**
 * Represents the complete query profile for a specific operating mode, aggregating visibility, filtering, actions, suggestions, and dashboard blocks.
 *
 * @property modeId The unique identifier of the mode.
 * @property visibility The visibility profile for UI sections.
 * @property filtering The filtering profile for nodes.
 * @property actions The quick actions profile.
 * @property suggestions The suggestions profile.
 * @property dashboardBlocks A list of dashboard block identifiers to display. An empty list means no specific dashboard blocks are configured.
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
 * @param preference The persisted mode preference containing visibility flags, sort strategy, and JSON fields for quick actions, dashboard blocks, and suggestions.
 * @param areaFilters A list of area filter entities; entries with `include == true` indicate areas to include in the profile.
 * @param typeFilters A list of type filter entities; entries with `include == true` indicate node types to include in the profile.
 * @return A ModeQueryProfile that consolidates visibility, filtering, actions, suggestions, and dashboard block settings for the mode.
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

private fun decodeStringList(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching { modeProfileJson.decodeFromString<List<String>>(raw) }
        .getOrElse { emptyList() }
}

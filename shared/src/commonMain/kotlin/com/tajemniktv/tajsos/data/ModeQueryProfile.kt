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

@Serializable
data class ModeFilterProfile(
    val includeAreaIds: Set<Long> = emptySet(),
    val includeTypes: Set<String> = emptySet(),
    val sortStrategy: String = "DEFAULT",
)

@Serializable
data class ModeActionProfile(
    val quickActions: List<String> = emptyList(),
)

@Serializable
data class ModeSuggestionProfile(
    val suggestionKeys: List<String> = emptyList(),
)

@Serializable
data class ModeQueryProfile(
    val modeId: Long,
    val visibility: ModeVisibilityProfile,
    val filtering: ModeFilterProfile,
    val actions: ModeActionProfile,
    val suggestions: ModeSuggestionProfile,
    val dashboardBlocks: List<String> = emptyList(),
)

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

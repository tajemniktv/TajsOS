/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.ui.DashboardUIState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Defines supported dashboard surfaces for layout planning.
 */
enum class DashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Describes a logical dashboard block instance.
 *
 * @param id Canonical block id used by the renderer.
 */
data class DashboardBlockInstance(
    val id: String,
)

/**
 * Structured dashboard layout plan used by the renderer.
 *
 * @param primary Main block stack (mobile: full page, desktop: left column).
 * @param secondary Optional side column blocks (desktop).
 * @param footer Optional footer strip blocks.
 * @param bottomBar Optional bottom command bar blocks.
 */
data class DashboardLayoutPlan(
    val primary: List<DashboardBlockInstance> = emptyList(),
    val secondary: List<DashboardBlockInstance> = emptyList(),
    val footer: List<DashboardBlockInstance> = emptyList(),
    val bottomBar: List<DashboardBlockInstance> = emptyList(),
)

/**
 * Lightweight metadata for developer-facing dashboard block customization.
 *
 * @param id Canonical id that should be used in layout configuration.
 * @param aliases Legacy ids that map to this canonical id.
 */
data class DashboardBlockCatalogEntry(
    val id: String,
    val aliases: Set<String> = emptySet(),
)

/**
 * Registry metadata for one dashboard block.
 *
 * @param id Canonical id.
 * @param aliases Legacy aliases that resolve to this block.
 * @param supportedSurfaces Surfaces where the block can render.
 * @param isEnabled Predicate controlling pack or feature availability.
 */
private data class DashboardBlockSpec(
    val id: String,
    val aliases: Set<String> = emptySet(),
    val supportedSurfaces: Set<DashboardSurface> =
        setOf(
            DashboardSurface.MOBILE,
            DashboardSurface.DESKTOP,
        ),
    val isEnabled: (PackRegistry) -> Boolean = { true },
)

/**
 * A parseable future-facing JSON layout format.
 *
 * Existing mode preference values are simple string arrays; this format allows later per-zone
 * customization without changing field shape.
 */
@Serializable
private data class DashboardLayoutJsonV1(
    val version: Int = 1,
    val primary: List<String> = emptyList(),
    val secondary: List<String> = emptyList(),
    val footer: List<String> = emptyList(),
    val bottomBar: List<String> = emptyList(),
)

private val dashboardJson =
    Json {
        ignoreUnknownKeys = true
    }

private val dashboardBlockSpecs: List<DashboardBlockSpec> =
    listOf(
        DashboardBlockSpec("mode_controls"),
        DashboardBlockSpec(
            "today_pulse",
            aliases = setOf("today_top_3"),
        ),
        DashboardBlockSpec("load_capacity"),
        DashboardBlockSpec("area_health"),
        DashboardBlockSpec("operational"),
        DashboardBlockSpec("search"),
        DashboardBlockSpec("alerts"),
        DashboardBlockSpec("sticky"),
        DashboardBlockSpec(
            "focus",
            aliases = setOf("current_task", "timer"),
        ),
        DashboardBlockSpec("insights"),
        DashboardBlockSpec("actions"),
        DashboardBlockSpec(
            "suggestions",
            aliases = setOf("easy_wins"),
        ),
        DashboardBlockSpec(
            "knowledge",
            aliases = setOf("pinned_note"),
        ),
        DashboardBlockSpec("time_architecture"),
        DashboardBlockSpec(
            "protocols",
            isEnabled = { packs ->
                packs.isEnabled(AppPack.PROTOCOLS) || packs.isEnabled(AppPack.MAINTENANCE)
            },
        ),
        DashboardBlockSpec(
            "basics",
            aliases = setOf("survival_basics"),
        ),
        DashboardBlockSpec(
            "shopping_list",
            aliases = setOf("place_based_tasks", "errands"),
            isEnabled = { packs -> packs.isEnabled(AppPack.MAINTENANCE) },
        ),
        DashboardBlockSpec(
            "tiny_wins",
            aliases = setOf("tiny_victories"),
        ),
        DashboardBlockSpec(
            "classes",
            isEnabled = { packs -> packs.isEnabled(AppPack.STUDENT) },
        ),
        DashboardBlockSpec(
            "assignments",
            isEnabled = { packs -> packs.isEnabled(AppPack.STUDENT) },
        ),
        DashboardBlockSpec(
            "revision_targets",
            isEnabled = { packs -> packs.isEnabled(AppPack.STUDENT) },
        ),
        DashboardBlockSpec(
            "paperwork",
            aliases = setOf("bills", "renewals", "subscriptions", "bureaucracy"),
            isEnabled = { packs ->
                packs.isEnabled(AppPack.FINANCE) || packs.isEnabled(AppPack.MAINTENANCE)
            },
        ),
        DashboardBlockSpec("modules"),
        DashboardBlockSpec("operations_overview"),
        DashboardBlockSpec(
            "search_capture",
            supportedSurfaces = setOf(DashboardSurface.DESKTOP),
        ),
        DashboardBlockSpec(
            "insights_summary",
            supportedSurfaces = setOf(DashboardSurface.DESKTOP),
        ),
        DashboardBlockSpec(
            "system_clock",
            supportedSurfaces = setOf(DashboardSurface.DESKTOP),
        ),
        DashboardBlockSpec("system_footer"),
        DashboardBlockSpec(
            "command_bar",
            supportedSurfaces = setOf(DashboardSurface.DESKTOP),
        ),
    )

private val blockSpecById: Map<String, DashboardBlockSpec> =
    buildMap {
        dashboardBlockSpecs.forEach { spec ->
            put(spec.id, spec)
            spec.aliases.forEach { alias -> put(alias, spec) }
        }
    }

private val defaultMobilePrimaryIds =
    listOf(
        "mode_controls",
        "today_pulse",
        "load_capacity",
        "area_health",
        "operational",
        "search",
        "alerts",
        "sticky",
        "focus",
        "insights",
        "actions",
        "suggestions",
        "knowledge",
        "time_architecture",
        "protocols",
        "modules",
        "operations_overview",
        "system_footer",
    )

private val defaultDesktopPrimaryIds =
    listOf(
        "mode_controls",
        "today_pulse",
        "load_capacity",
        "area_health",
        "operational",
        "search",
        "alerts",
        "sticky",
        "focus",
        "actions",
        "suggestions",
        "knowledge",
        "time_architecture",
        "protocols",
    )

private val defaultDesktopSecondaryIds =
    listOf(
        "search_capture",
        "insights_summary",
        "modules",
        "operations_overview",
        "system_clock",
        "system_footer",
    )

private val defaultDesktopBottomBarIds = listOf("command_bar")
private val nonContentBlockIds =
    setOf(
        "mode_controls",
        "modules",
        "operations_overview",
        "search_capture",
        "insights_summary",
        "system_clock",
        "system_footer",
        "command_bar",
    )

/**
 * Returns all canonical dashboard block ids with aliases for discoverability.
 */
fun dashboardBlockCatalog(): List<DashboardBlockCatalogEntry> =
    dashboardBlockSpecs
        .map {
            DashboardBlockCatalogEntry(
                id = it.id,
                aliases = it.aliases,
            )
        }.sortedBy { it.id }

/**
 * Returns the default content block order used when mode preferences do not define one.
 */
fun defaultDashboardContentBlockIds(): List<String> =
    listOf(
        "today_pulse",
        "load_capacity",
        "area_health",
        "operational",
        "search",
        "alerts",
        "sticky",
        "focus",
        "insights",
        "actions",
        "suggestions",
        "knowledge",
        "time_architecture",
        "protocols",
    )

/**
 * Returns every dashboard content block that can be shown when mode constraints are intentionally
 * disabled (for example, the "ALL" operating mode).
 */
fun allDashboardContentBlockIds(): List<String> =
    dashboardBlockSpecs
        .map { it.id }
        .filterNot { it in nonContentBlockIds }

/**
 * Builds the render plan for the dashboard from mode preferences and defaults.
 *
 * It supports two input formats from `dashboardBlocksJson`:
 * 1. Legacy list of block ids.
 * 2. Versioned object with `primary/secondary/footer/bottomBar` lists.
 */
fun buildDashboardLayoutPlan(
    surface: DashboardSurface,
    dashboardState: DashboardUIState,
    enabledPacks: PackRegistry,
): DashboardLayoutPlan {
    val forceAllContent =
        dashboardState.currentMode?.key.equals(
            "ALL",
            ignoreCase = true,
        )
    val preferenceJson = dashboardState.modePreferences?.dashboardBlocksJson
    val fromModeProfile = dashboardState.modeQueryProfile?.dashboardBlocks.orEmpty()
    val forcedModeBlocks = if (forceAllContent) allDashboardContentBlockIds() else emptyList()

    val explicitLayout =
        parseVersionedLayout(
            preferenceJson,
        )
    if (explicitLayout != null) {
        return DashboardLayoutPlan(
            primary =
                normalizeBlocks(
                    explicitLayout.primary,
                    surface,
                    enabledPacks,
                ),
            secondary =
                normalizeBlocks(
                    explicitLayout.secondary,
                    surface,
                    enabledPacks,
                ),
            footer =
                normalizeBlocks(
                    explicitLayout.footer,
                    surface,
                    enabledPacks,
                ),
            bottomBar =
                normalizeBlocks(
                    explicitLayout.bottomBar,
                    surface,
                    enabledPacks,
                ),
        )
    }

    val preferenceBlocks =
        parseLegacyBlockList(
            preferenceJson,
        )
    val modeBlocks =
        forcedModeBlocks
            .ifEmpty { fromModeProfile }
            .ifEmpty { preferenceBlocks }
    val dynamicContent =
        normalizeBlocks(
            modeBlocks,
            surface,
            enabledPacks,
        )
    return when (surface)
    {
        DashboardSurface.MOBILE -> {
            val fallback =
                normalizeBlocks(
                    defaultMobilePrimaryIds,
                    surface,
                    enabledPacks,
                )
            val ordered =
                if (dynamicContent.isNotEmpty()) {
                    normalizeBlocks(
                        ids =
                            listOf("mode_controls") +
                                dynamicContent.map { it.id } +
                                listOf(
                                    "modules",
                                    "operations_overview",
                                    "system_footer",
                                ),
                        surface = surface,
                        enabledPacks = enabledPacks,
                    )
                } else {
                    fallback
                }
            DashboardLayoutPlan(
                primary = ordered,
            )
        }

        DashboardSurface.DESKTOP -> {
            val primary =
                if (dynamicContent.isNotEmpty()) {
                    normalizeBlocks(
                        ids = listOf("mode_controls") + dynamicContent.map { it.id },
                        surface = surface,
                        enabledPacks = enabledPacks,
                    )
                } else {
                    normalizeBlocks(
                        defaultDesktopPrimaryIds,
                        surface,
                        enabledPacks,
                    )
                }
            DashboardLayoutPlan(
                primary = primary,
                secondary =
                    normalizeBlocks(
                        defaultDesktopSecondaryIds,
                        surface,
                        enabledPacks,
                    ),
                bottomBar =
                    normalizeBlocks(
                        defaultDesktopBottomBarIds,
                        surface,
                        enabledPacks,
                    ),
            )
        }
    }
}

private fun normalizeBlocks(
    ids: List<String>,
    surface: DashboardSurface,
    enabledPacks: PackRegistry,
): List<DashboardBlockInstance> {
    return ids
        .mapNotNull { raw ->
            val normalized = raw.trim()
            val spec =
                blockSpecById[normalized]
                    ?: return@mapNotNull null
            if (!spec.supportedSurfaces.contains(surface)) return@mapNotNull null
            if (!spec.isEnabled(enabledPacks)) return@mapNotNull null
            DashboardBlockInstance(
                spec.id,
            )
        }.distinctBy { it.id }
}

private fun parseLegacyBlockList(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching {
        dashboardJson.decodeFromString<List<String>>(
            raw,
        )
    }.getOrElse { emptyList() }
}

private fun parseVersionedLayout(raw: String?): DashboardLayoutJsonV1? {
    if (raw.isNullOrBlank()) return null
    return runCatching {
        dashboardJson
            .decodeFromString<DashboardLayoutJsonV1>(
                raw,
            )
    }.getOrNull()
}

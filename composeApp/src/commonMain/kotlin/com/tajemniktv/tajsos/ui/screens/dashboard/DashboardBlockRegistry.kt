/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import com.tajemniktv.tajsos.ui.screens.dashboard.renderActionsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderAlertsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderAreaHealthBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderAssignmentsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderBasicsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderClassesBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderCurrentFocusBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderFocusBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderInsightsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderKnowledgeBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderLoadCapacityBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderOperationalBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderPaperworkBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderProtocolsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderRevisionTargetsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderSearchBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderShoppingListBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderStickyBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderSuggestionsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderTimeArchitectureBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderTinyWinsBlock
import com.tajemniktv.tajsos.ui.screens.dashboard.renderTodayPulseBlock

/**
 * Central registry resolving dashboard block ids to renderer functions.
 *
 * Aliases are normalized to canonical ids so mode preferences can keep legacy keys.
 */
object DashboardBlockRegistry {
    private val aliasToCanonical: Map<String, String> =
        mapOf(
            "today_top_3" to "today_pulse",
            "current_task" to "focus",
            "timer" to "focus",
            "easy_wins" to "suggestions",
            "pinned_note" to "knowledge",
            "survival_basics" to "basics",
            "place_based_tasks" to "shopping_list",
            "errands" to "shopping_list",
            "tiny_victories" to "tiny_wins",
            "bills" to "paperwork",
            "renewals" to "paperwork",
            "subscriptions" to "paperwork",
            "bureaucracy" to "paperwork",
        )

    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockRendererFn> =
        mapOf(
            "today_pulse" to ::renderTodayPulseBlock,
            "load_capacity" to ::renderLoadCapacityBlock,
            "area_health" to ::renderAreaHealthBlock,
            "operational" to ::renderOperationalBlock,
            "time_architecture" to ::renderTimeArchitectureBlock,
            "search" to ::renderSearchBlock,
            "alerts" to ::renderAlertsBlock,
            "sticky" to ::renderStickyBlock,
            "focus" to ::renderFocusBlock,
            "insights" to ::renderInsightsBlock,
            "actions" to ::renderActionsBlock,
            "suggestions" to ::renderSuggestionsBlock,
            "knowledge" to ::renderKnowledgeBlock,
            "protocols" to ::renderProtocolsBlock,
            "basics" to ::renderBasicsBlock,
            "shopping_list" to ::renderShoppingListBlock,
            "tiny_wins" to ::renderTinyWinsBlock,
            "current_focus" to ::renderCurrentFocusBlock,
            "classes" to ::renderClassesBlock,
            "assignments" to ::renderAssignmentsBlock,
            "revision_targets" to ::renderRevisionTargetsBlock,
            "paperwork" to ::renderPaperworkBlock,
        )

    /**
     * Resolves a renderer for the provided block key.
     *
     * @param blockKey Raw key from preferences/layout.
     * @return Renderer or null if the key is unknown.
     */
    fun resolve(blockKey: String): com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockRendererFn? {
        val canonical = aliasToCanonical[blockKey] ?: blockKey
        return renderers[canonical]
    }
}

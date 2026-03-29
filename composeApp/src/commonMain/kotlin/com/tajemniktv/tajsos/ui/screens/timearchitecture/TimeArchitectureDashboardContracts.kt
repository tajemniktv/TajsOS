/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.timearchitecture

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.TimeArchitectureSnapshot

/**
 * Defines the supported surfaces for time architecture dashboard layout planning.
 */
enum class TimeArchitectureDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical time architecture dashboard block.
 */
data class TimeArchitectureDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the time architecture dashboard screen.
 */
data class TimeArchitectureDashboardPlan(
    val primary: List<TimeArchitectureDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for time architecture dashboard block renderers.
 */
data class TimeArchitectureDashboardContext(
    val viewModel: MainViewModel,
    val snapshot: TimeArchitectureSnapshot,
    val selectedHorizon: TimeArchitectureHorizon,
    val onHorizonSelected: (TimeArchitectureHorizon) -> Unit,
    val onEditNode: (Long) -> Unit,
)

/**
 * Functional interface for rendering a time architecture dashboard block.
 */
typealias TimeArchitectureDashboardBlockRenderer = @Composable (TimeArchitectureDashboardContext) -> Unit

/**
 * Defines the horizons available in the time architecture view.
 */
enum class TimeArchitectureHorizon(
    val key: String?,
    val label: String,
) {
    TODAY("today", "Today"),
    WEEK("week", "Week"),
    MONTH("month", "Month"),
    SEMESTER("semester", "Semester"),
    ALL(null, "All"),
    ;

    fun count(snapshot: TimeArchitectureSnapshot): Int = items(snapshot).size

    fun items(snapshot: TimeArchitectureSnapshot): List<NodeWithPin> =
        when (this)
        {
            TODAY -> {
                snapshot.todayLayer
            }

            WEEK -> {
                snapshot.weekLayer
            }

            MONTH -> {
                snapshot.monthLayer
            }

            SEMESTER -> {
                snapshot.semesterLayer
            }

            ALL -> {
                (
                    snapshot.todayLayer +
                        snapshot.weekLayer +
                        snapshot.monthLayer +
                        snapshot.semesterLayer
                ).distinctBy { it.node.id }
            }
        }
}

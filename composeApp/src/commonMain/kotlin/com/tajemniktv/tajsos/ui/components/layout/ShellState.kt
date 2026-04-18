/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tajemniktv.tajsos.ui.SidebarMode

/**
 * Centralized state holder for top-level shell interaction.
 */
@Stable
class AppShellState(
    initialSidebarMode: SidebarMode = SidebarMode.EXPANDED,
    initialExpandedRootRoutes: Set<String> = emptySet(),
) {
    var sidebarMode by mutableStateOf(initialSidebarMode)
    var expandedRootRoutes by mutableStateOf(initialExpandedRootRoutes)
    var hoverExpanded by mutableStateOf(false)
    var modeDropdownExpanded by mutableStateOf(false)
    var notificationsExpanded by mutableStateOf(false)

    val isSidebarExpandedPresentation: Boolean
        get() =
            sidebarMode == SidebarMode.EXPANDED ||
                (sidebarMode == SidebarMode.HOVER_EXPAND && hoverExpanded)

    fun toggleRootExpanded(route: String) {
        expandedRootRoutes =
            if (expandedRootRoutes.contains(route)) {
                expandedRootRoutes - route
            } else {
                expandedRootRoutes + route
            }
    }

    fun setRootExpanded(
        route: String,
        expanded: Boolean,
    ) {
        expandedRootRoutes =
            if (expanded) {
                expandedRootRoutes + route
            } else {
                expandedRootRoutes - route
            }
    }

    fun isRootExpanded(route: String): Boolean = expandedRootRoutes.contains(route)
}

/**
 * Remembers the shell state across recompositions.
 */
@Composable
fun rememberAppShellState(
    sidebarMode: SidebarMode = SidebarMode.EXPANDED,
    initialExpandedRootRoutes: Set<String> = emptySet(),
): AppShellState {
    val shellState =
        remember {
            AppShellState(
                initialSidebarMode = sidebarMode,
                initialExpandedRootRoutes = initialExpandedRootRoutes,
            )
        }

    // Sync shellState with the provided sidebarMode from persistent settings.
    LaunchedEffect(sidebarMode) {
        shellState.sidebarMode = sidebarMode
    }

    return shellState
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Sidebar behavior modes supported by the shell.
 */
enum class SidebarMode {
    EXPANDED,
    COLLAPSED,
    HOVER_EXPAND,
}

/**
 * Centralized state holder for top-level shell interaction.
 */
@Stable
class AppShellState(
    initialSidebarMode: SidebarMode = SidebarMode.EXPANDED,
    initialExpandedRootRoute: String? = null,
) {
    var sidebarMode by mutableStateOf(initialSidebarMode)
    var expandedRootRoute by mutableStateOf(initialExpandedRootRoute)
    var hoverExpanded by mutableStateOf(false)
    var modeDropdownExpanded by mutableStateOf(false)
    var notificationsExpanded by mutableStateOf(false)

    val isSidebarExpandedPresentation: Boolean
        get() =
            sidebarMode == SidebarMode.EXPANDED ||
                (sidebarMode == SidebarMode.HOVER_EXPAND && hoverExpanded)
}

/**
 * Remembers the shell state across recompositions.
 */
@Composable
fun rememberAppShellState(
    initialSidebarMode: SidebarMode = SidebarMode.EXPANDED,
    initialExpandedRootRoute: String? = null,
): AppShellState =
    remember {
        AppShellState(
            initialSidebarMode = initialSidebarMode,
            initialExpandedRootRoute = initialExpandedRootRoute,
        )
    }

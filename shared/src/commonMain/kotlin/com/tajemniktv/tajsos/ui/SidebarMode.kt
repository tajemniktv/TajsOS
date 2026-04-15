/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

/**
 * Sidebar behavior modes supported by the shell.
 */
enum class SidebarMode {
    /**
     * Sidebar is fully visible, taking up horizontal space.
     */
    EXPANDED,
    /**
     * Sidebar is minimized to an icon-only strip to save horizontal space.
     */
    COLLAPSED,
    /**
     * Sidebar is collapsed but temporarily overlays content when hovered.
     */
    HOVER_EXPAND,
}

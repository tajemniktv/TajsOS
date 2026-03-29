/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun settingsSubSidebarSections(): List<SidebarSection> =
    listOf(
        SidebarSection(
            title = "User Profile",
            items = listOf("About", "Health"),
        ),
        SidebarSection(
            title = "System settings",
            items = listOf("Preferences", "Calendar", "Feature Packs", "Data"),
        ),
        SidebarSection(
            title = "Debug",
            items = listOf("Debug"),
        ),
    )

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun settingsSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("settings overview", "system controls", "quick toggles"),
        workflow = listOf("configuration actions", "preferences sync", "safety checks"),
        insights = listOf("config health", "drift warnings", "recommended settings"),
    )
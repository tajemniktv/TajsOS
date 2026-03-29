/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun profileSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("profile summary", "identity details", "personal defaults"),
        workflow = listOf("profile edits", "preferences", "account actions"),
        insights = listOf("profile completeness", "behavior patterns", "personal signals"),
    )

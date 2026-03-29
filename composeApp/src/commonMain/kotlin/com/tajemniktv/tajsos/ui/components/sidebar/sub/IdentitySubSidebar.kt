/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun identitySubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("identity overview", "values anchors", "role filters"),
        workflow = listOf("identity rituals", "alignment checks", "course correction"),
        insights = listOf("alignment trend", "identity drift", "coherence score"),
    )
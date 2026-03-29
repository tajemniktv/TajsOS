/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun areasSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("responsibility map", "active areas", "area filters"),
        workflow = listOf("maintenance cycles", "standards", "ownership actions"),
        insights = listOf("area stability", "neglect signals", "coverage gaps"),
    )
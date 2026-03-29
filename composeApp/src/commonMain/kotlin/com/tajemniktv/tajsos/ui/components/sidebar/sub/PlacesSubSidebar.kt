/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun placesSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("place index", "active locations", "context filters"),
        workflow = listOf("location routines", "visit plans", "context triggers"),
        insights = listOf("location fit", "travel overhead", "place utilization"),
    )
/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun calendarSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("agenda snapshot", "upcoming blocks", "conflict flags"),
        workflow = listOf("reschedule tools", "buffer planning", "calendar sync"),
        insights = listOf("load balance", "meeting density", "focus-time erosion"),
    )
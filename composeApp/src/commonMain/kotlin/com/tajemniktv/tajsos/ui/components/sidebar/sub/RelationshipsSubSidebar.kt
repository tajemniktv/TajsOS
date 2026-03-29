/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun relationshipsSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("relationships map", "active threads", "priority people"),
        workflow = listOf("follow-up actions", "shared plans", "cadence settings"),
        insights = listOf("connection health", "drop-off signals", "next best touch"),
    )

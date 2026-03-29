/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun openLoopsSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("open loops map", "critical loops", "aging loops"),
        workflow = listOf("closure actions", "owner routing", "follow-up cadence"),
        insights = listOf("loop pressure", "closure velocity", "risk loops"),
    )
/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun educationSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("learning dashboard", "active courses", "study focus"),
        workflow = listOf("session plans", "practice loops", "material queues"),
        insights = listOf("learning pace", "retention signals", "skill gaps"),
    )
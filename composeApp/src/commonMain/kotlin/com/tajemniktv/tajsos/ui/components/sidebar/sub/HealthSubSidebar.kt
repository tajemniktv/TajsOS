/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun healthSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("health dashboard", "care tasks", "wellbeing markers"),
        workflow = listOf("medication actions", "appointment flow", "habit checks"),
        insights = listOf("recovery signals", "symptom trends", "care recommendations"),
    )
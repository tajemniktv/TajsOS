/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun focusSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("focus sprint", "deep-work slots", "distraction shield"),
        workflow = listOf("session setup", "interrupt guard", "focus rituals"),
        insights = listOf("session depth", "break quality", "focus drift"),
    )
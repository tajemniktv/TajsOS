/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun decisionsSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("decision queue", "open options", "pending calls"),
        workflow = listOf("criteria checklists", "stakeholder notes", "decision templates"),
        insights = listOf("decision latency", "reversal rate", "confidence signals"),
    )
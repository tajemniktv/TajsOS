/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun rulesSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("rulebook", "active rules", "rule shortcuts"),
        workflow = listOf("rule evaluation", "exception paths", "policy updates"),
        insights = listOf("rule adherence", "conflict hotspots", "effectiveness"),
    )

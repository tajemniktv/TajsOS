/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun financesSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("finance snapshot", "accounts focus", "liquidity view"),
        workflow = listOf("bill workflows", "budget actions", "renewal tracking"),
        insights = listOf("cash trend", "spend drift", "upcoming obligations"),
    )
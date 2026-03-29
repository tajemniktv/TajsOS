/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun vaultsSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("vault index", "sensitive items", "vault filters"),
        workflow = listOf("access workflows", "review cadence", "retention rules"),
        insights = listOf("vault activity", "stale secrets", "security posture"),
    )

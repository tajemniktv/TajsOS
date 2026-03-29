/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun capacitySubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("capacity dashboard", "load map", "energy windows"),
        workflow = listOf("allocation actions", "workload balancing", "limits"),
        insights = listOf("overload risk", "recovery trends", "headroom"),
    )

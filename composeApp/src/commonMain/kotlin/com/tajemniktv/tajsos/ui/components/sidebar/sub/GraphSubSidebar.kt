/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun graphSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("relation graph", "entity spotlight", "link filters"),
        workflow = listOf("link actions", "cluster views", "graph maintenance"),
        insights = listOf("central nodes", "isolated clusters", "relation quality"),
    )
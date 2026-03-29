/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun inboxSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("capture inbox", "triage shortcuts", "newest entries"),
        workflow = listOf("processing states", "batch actions", "routing rules"),
        insights = listOf("inbox pressure", "stale entries", "clearance forecast"),
    )
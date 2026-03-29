/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun notesSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("note workspace", "recent notes", "linked references"),
        workflow = listOf("capture to note", "organize passes", "summarization actions"),
        insights = listOf("knowledge growth", "orphan notes", "link density"),
    )

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun protocolsSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("protocol library", "active runs", "trigger shortcuts"),
        workflow = listOf("run controls", "step checkpoints", "failure recovery"),
        insights = listOf("protocol reliability", "completion ratios", "friction points"),
    )
/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar

import androidx.compose.runtime.Composable

/**
 * Contextual sidebar shown for project-oriented screens.
 */
@Composable
internal fun ProjectsSidebar(
    contextHeader: String,
    panelLabel: String,
    onBackToMainSidebar: () -> Unit,
) {
    ContextSidebarScaffold(
        contextHeader = contextHeader,
        panelLabel = panelLabel,
        onBackToMainSidebar = onBackToMainSidebar,
        sections =
            listOf(
                SidebarSection(
                    title = "PRIMARY",
                    items =
                        listOf(
                            "projects overview placeholder",
                            "active projects placeholder",
                            "project quick filters placeholder",
                        ),
                ),
                SidebarSection(
                    title = "WORKFLOW",
                    items =
                        listOf(
                            "project milestones placeholder",
                            "blocked lanes placeholder",
                            "project automation placeholder",
                        ),
                ),
                SidebarSection(
                    title = "INSIGHTS",
                    items =
                        listOf(
                            "project velocity placeholder",
                            "risk hotspots placeholder",
                            "delivery recommendations placeholder",
                        ),
                ),
            ),
    )
}

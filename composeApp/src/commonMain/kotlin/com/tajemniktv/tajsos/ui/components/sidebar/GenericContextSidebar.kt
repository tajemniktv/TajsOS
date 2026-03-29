/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.sidebar.sub.subSidebarSectionsFor

/**
 * Contextual sidebar renderer that provides dedicated section sets for main/root tabs.
 * Non-main routes fall back to placeholder sections.
 */
@Composable
internal fun GenericContextSidebar(
    screen: Screen,
    contextHeader: String,
    panelLabel: String,
    onBackToMainSidebar: () -> Unit,
) {
    ContextSidebarScaffold(
        contextHeader = contextHeader,
        panelLabel = panelLabel,
        onBackToMainSidebar = onBackToMainSidebar,
        sections = subSidebarSectionsFor(screen) ?: placeholderSectionsFor(screen),
    )
}

private fun placeholderSectionsFor(screen: Screen): List<SidebarSection> {
    val screenTag = screen.route.substringBefore("/")
    return listOf(
        SidebarSection(
            title = "PRIMARY",
            items =
                listOf(
                    "$screenTag overview placeholder",
                    "$screenTag shortcuts placeholder",
                    "$screenTag quick filters placeholder",
                ),
        ),
        SidebarSection(
            title = "WORKFLOW",
            items =
                listOf(
                    "$screenTag actions placeholder",
                    "$screenTag pinned context placeholder",
                    "$screenTag automation placeholder",
                ),
        ),
        SidebarSection(
            title = "INSIGHTS",
            items =
                listOf(
                    "$screenTag metrics placeholder",
                    "$screenTag anomalies placeholder",
                    "$screenTag recommendations placeholder",
                ),
        ),
    )
}

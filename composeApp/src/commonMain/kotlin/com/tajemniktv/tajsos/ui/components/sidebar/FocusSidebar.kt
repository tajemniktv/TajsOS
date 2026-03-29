/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar

import androidx.compose.runtime.Composable

/**
 * Contextual sidebar shown for focus-oriented screens.
 */
@Composable
internal fun FocusSidebar(
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
                            "focus sprint placeholder",
                            "focus shortcuts placeholder",
                            "focus quick filters placeholder",
                        ),
                ),
                SidebarSection(
                    title = "WORKFLOW",
                    items =
                        listOf(
                            "session actions placeholder",
                            "distraction guard placeholder",
                            "focus automation placeholder",
                        ),
                ),
                SidebarSection(
                    title = "INSIGHTS",
                    items =
                        listOf(
                            "deep work metrics placeholder",
                            "interruptions placeholder",
                            "focus recommendations placeholder",
                        ),
                ),
            ),
    )
}

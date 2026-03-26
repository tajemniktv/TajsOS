/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

import androidx.compose.runtime.Composable

object InboxDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardBlockRenderer> =
        mapOf("inbox_main" to ::renderInboxMainBlock)

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderInboxMainBlock(context: com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardContext) {
    InboxMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

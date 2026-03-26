/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.protocols

import androidx.compose.runtime.Composable

object ProtocolsDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardBlockRenderer> =
        mapOf("protocols_main" to ::renderProtocolsMainBlock)

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderProtocolsMainBlock(context: com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardContext) {
    ProtocolsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

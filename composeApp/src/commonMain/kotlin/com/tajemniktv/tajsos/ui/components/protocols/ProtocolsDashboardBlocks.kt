/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.protocols

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.screens.ProtocolsMainBlock

object ProtocolsDashboardBlockRegistry {
    private val renderers: Map<String, ProtocolsDashboardBlockRenderer> =
        mapOf("protocols_main" to ::renderProtocolsMainBlock)

    fun resolve(id: String): ProtocolsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderProtocolsMainBlock(context: ProtocolsDashboardContext) {
    ProtocolsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

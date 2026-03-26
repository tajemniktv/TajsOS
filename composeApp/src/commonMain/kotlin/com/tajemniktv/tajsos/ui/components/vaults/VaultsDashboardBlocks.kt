/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.vaults

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.screens.VaultsMainBlock

object VaultsDashboardBlockRegistry {
    private val renderers: Map<String, VaultsDashboardBlockRenderer> =
        mapOf("vaults_main" to ::renderVaultsMainBlock)

    fun resolve(id: String): VaultsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderVaultsMainBlock(context: VaultsDashboardContext) {
    VaultsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

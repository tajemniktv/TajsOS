/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.vaults

@Suppress("UNUSED_PARAMETER")
fun buildVaultsDashboardPlan(surface: VaultsDashboardSurface): VaultsDashboardPlan =
    VaultsDashboardPlan(
        primary =
            listOf(
                VaultsDashboardBlock(
                    "vaults_main",
                ),
            ),
    )

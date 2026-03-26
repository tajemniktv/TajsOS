/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.vaults

fun buildVaultsDashboardPlan(surface: VaultsDashboardSurface): VaultsDashboardPlan =
    when (surface)
    {
        VaultsDashboardSurface.MOBILE -> {
            VaultsDashboardPlan(
                primary =
                    listOf(
                        VaultsDashboardBlock(
                            "vaults_main",
                        ),
                    ),
            )
        }

        VaultsDashboardSurface.DESKTOP -> {
            VaultsDashboardPlan(
                primary =
                    listOf(
                        VaultsDashboardBlock(
                            "vaults_main",
                        ),
                    ),
            )
        }
    }

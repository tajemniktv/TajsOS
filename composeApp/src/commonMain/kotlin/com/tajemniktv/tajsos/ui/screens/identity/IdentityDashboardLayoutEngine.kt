/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.identity

/**
 * Builds an identity dashboard layout plan based on the active surface.
 */
fun buildIdentityDashboardPlan(surface: IdentityDashboardSurface): IdentityDashboardPlan =
    when (surface)
    {
        IdentityDashboardSurface.MOBILE -> {
            IdentityDashboardPlan(
                primary =
                    listOf(
                        IdentityDashboardBlock("identity_header"),
                        IdentityDashboardBlock("identity_signature"),
                        IdentityDashboardBlock("identity_distinction"),
                        IdentityDashboardBlock("identity_direction"),
                        IdentityDashboardBlock("identity_coreshift"),
                    ),
            )
        }

        IdentityDashboardSurface.DESKTOP -> {
            IdentityDashboardPlan(
                primary =
                    listOf(
                        IdentityDashboardBlock("identity_header"),
                        IdentityDashboardBlock("identity_signature"),
                        IdentityDashboardBlock("identity_distinction"),
                        IdentityDashboardBlock("identity_direction"),
                        IdentityDashboardBlock("identity_coreshift"),
                    ),
            )
        }
    }

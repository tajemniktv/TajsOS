/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.identity

/**
 * Builds an identity dashboard layout plan based on the active surface.
 */
@Suppress("UNUSED_PARAMETER")
fun buildIdentityDashboardPlan(surface: IdentityDashboardSurface): IdentityDashboardPlan =
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

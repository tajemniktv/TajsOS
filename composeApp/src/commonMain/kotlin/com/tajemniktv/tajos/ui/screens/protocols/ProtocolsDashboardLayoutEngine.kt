/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.protocols

@Suppress("UNUSED_PARAMETER")
fun buildProtocolsDashboardPlan(surface: ProtocolsDashboardSurface): ProtocolsDashboardPlan =
    ProtocolsDashboardPlan(
        primary =
            listOf(
                ProtocolsDashboardBlock(
                    "protocols_main",
                ),
            ),
    )

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.protocols

fun buildProtocolsDashboardPlan(surface: ProtocolsDashboardSurface): ProtocolsDashboardPlan =
    when (surface)
    {
        ProtocolsDashboardSurface.MOBILE -> {
            ProtocolsDashboardPlan(
                primary =
                    listOf(
                        ProtocolsDashboardBlock(
                            "protocols_main",
                        ),
                    ),
            )
        }

        ProtocolsDashboardSurface.DESKTOP -> {
            ProtocolsDashboardPlan(
                primary =
                    listOf(
                        ProtocolsDashboardBlock(
                            "protocols_main",
                        ),
                    ),
            )
        }
    }

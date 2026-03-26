/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.protocols

fun buildProtocolsDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardPlan =
    when (surface)
    {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardSurface.MOBILE -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardBlock(
                            "protocols_main",
                        ),
                    ),
            )
        }

        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardSurface.DESKTOP -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsDashboardBlock(
                            "protocols_main",
                        ),
                    ),
            )
        }
    }

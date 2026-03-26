/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

fun buildInboxDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardPlan =
    when (surface)
    {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardSurface.MOBILE -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.inbox
                            .InboxDashboardBlock("inbox_main"),
                    ),
            )
        }

        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardSurface.DESKTOP -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.inbox
                            .InboxDashboardBlock("inbox_main"),
                    ),
            )
        }
    }

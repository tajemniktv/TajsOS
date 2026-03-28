/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

fun buildInboxDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardPlan =
    when (surface)
    {
        com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardSurface.MOBILE -> {
            com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.inbox
                            .InboxDashboardBlock("inbox_main"),
                    ),
            )
        }

        com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardSurface.DESKTOP -> {
            com.tajemniktv.tajsos.ui.screens.inbox.InboxDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.inbox
                            .InboxDashboardBlock("inbox_main"),
                    ),
            )
        }
    }

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

fun buildInboxDashboardPlan(surface: InboxDashboardSurface): InboxDashboardPlan =
    when (surface)
    {
        InboxDashboardSurface.MOBILE -> {
            InboxDashboardPlan(
                primary =
                    listOf(
                        InboxDashboardBlock("inbox_main"),
                    ),
            )
        }

        InboxDashboardSurface.DESKTOP -> {
            InboxDashboardPlan(
                primary =
                    listOf(
                        InboxDashboardBlock("inbox_main"),
                    ),
            )
        }
    }

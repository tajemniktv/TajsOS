/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.inbox

@Suppress("UNUSED_PARAMETER")
fun buildInboxDashboardPlan(surface: InboxDashboardSurface): InboxDashboardPlan =
    InboxDashboardPlan(
        primary =
            listOf(
                InboxDashboardBlock("inbox_main"),
            ),
    )

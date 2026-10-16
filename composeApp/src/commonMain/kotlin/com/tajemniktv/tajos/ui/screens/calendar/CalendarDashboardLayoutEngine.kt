/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.calendar

@Suppress("UNUSED_PARAMETER")
fun buildCalendarDashboardPlan(surface: CalendarDashboardSurface): CalendarDashboardPlan =
    CalendarDashboardPlan(
        primary =
            listOf(
                CalendarDashboardBlock(
                    "calendar_main",
                ),
            ),
    )

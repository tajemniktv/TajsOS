/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

fun buildCalendarDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardPlan =
    when (surface)
    {
        com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardSurface.MOBILE -> {
            com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardBlock(
                            "calendar_main",
                        ),
                    ),
            )
        }

        com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardSurface.DESKTOP -> {
            com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardBlock(
                            "calendar_main",
                        ),
                    ),
            )
        }
    }


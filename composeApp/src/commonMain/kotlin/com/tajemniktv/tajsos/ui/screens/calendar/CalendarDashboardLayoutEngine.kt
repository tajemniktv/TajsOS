/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

fun buildCalendarDashboardPlan(surface: CalendarDashboardSurface): CalendarDashboardPlan =
    when (surface) {
        CalendarDashboardSurface.MOBILE -> {
            CalendarDashboardPlan(
                primary =
                    listOf(
                        CalendarDashboardBlock(
                            "calendar_main",
                        ),
                    ),
            )
        }

        CalendarDashboardSurface.DESKTOP -> {
            CalendarDashboardPlan(
                primary =
                    listOf(
                        CalendarDashboardBlock(
                            "calendar_main",
                        ),
                    ),
            )
        }
    }

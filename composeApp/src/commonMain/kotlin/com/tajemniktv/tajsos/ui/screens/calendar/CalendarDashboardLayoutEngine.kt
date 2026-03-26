/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

fun buildCalendarDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardPlan =
    when (surface)
    {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardSurface.MOBILE -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardBlock(
                            "calendar_main",
                        ),
                    ),
            )
        }

        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardSurface.DESKTOP -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardBlock(
                            "calendar_main",
                        ),
                    ),
            )
        }
    }

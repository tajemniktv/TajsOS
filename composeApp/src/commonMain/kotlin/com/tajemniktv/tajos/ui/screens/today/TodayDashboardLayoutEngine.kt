/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.today

fun buildTodayDashboardPlan(surface: TodayDashboardSurface): TodayDashboardPlan =
    when (surface) {
        TodayDashboardSurface.MOBILE -> TodayDashboardPlan(primary = listOf(TodayDashboardBlock("today_main")))
        TodayDashboardSurface.DESKTOP -> TodayDashboardPlan(primary = listOf(TodayDashboardBlock("today_main")))
    }

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

/**
 * Builds a calendar settings layout plan.
 */
fun buildCalendarSettingsPlan(): CalendarSettingsPlan =
    CalendarSettingsPlan(
        primary =
            listOf(
                CalendarSettingsBlock("cal_settings_header"),
                CalendarSettingsBlock("cal_settings_list"),
            ),
    )

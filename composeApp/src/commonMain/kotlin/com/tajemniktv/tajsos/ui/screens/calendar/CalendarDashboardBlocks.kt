/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

import androidx.compose.runtime.Composable

object CalendarDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardBlockRenderer> =
        mapOf("calendar_main" to ::renderCalendarMainBlock)

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderCalendarMainBlock(context: com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardContext) {
    CalendarMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

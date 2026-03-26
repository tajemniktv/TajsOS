/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class CalendarDashboardSurface { MOBILE, DESKTOP }

data class CalendarDashboardBlock(
    val id: String,
)

data class CalendarDashboardPlan(
    val primary: List<com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardBlock>,
    val secondary: List<com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardBlock> = emptyList(),
)

data class CalendarDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias CalendarDashboardBlockRenderer = @Composable (com.tajemniktv.tajsos.ui.screens.calendar.CalendarDashboardContext) -> Unit

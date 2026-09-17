/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.calendar

import androidx.compose.runtime.Composable
import com.tajemniktv.tajos.data.CalendarProviderEntity
import com.tajemniktv.tajos.ui.MainViewModel

/**
 * Shared state and actions for calendar settings block renderers.
 */
data class CalendarSettingsContext(
    val viewModel: MainViewModel,
    val providers: List<CalendarProviderEntity>,
    val onAddProvider: (String, String, String?) -> Unit,
    val onDeleteProvider: (CalendarProviderEntity) -> Unit,
    val onShowAddDialog: () -> Unit,
)

/**
 * Structured layout plan for the calendar settings screen.
 */
data class CalendarSettingsPlan(
    val primary: List<CalendarSettingsBlock> = emptyList(),
)

/**
 * Identifies a logical calendar settings block.
 */
data class CalendarSettingsBlock(
    val id: String,
)

/**
 * Functional interface for rendering a calendar settings block.
 */
typealias CalendarSettingsBlockRenderer = @Composable (CalendarSettingsContext) -> Unit

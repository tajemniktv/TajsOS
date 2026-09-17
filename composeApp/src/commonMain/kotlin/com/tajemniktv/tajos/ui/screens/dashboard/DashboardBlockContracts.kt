/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.dashboard

import androidx.compose.runtime.Composable
import com.tajemniktv.tajos.data.FocusSessionEntity
import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.NodeWithPin
import com.tajemniktv.tajos.data.TrackEntryEntity
import com.tajemniktv.tajos.ui.DashboardUIState
import com.tajemniktv.tajos.ui.MainViewModel
import com.tajemniktv.tajos.ui.Screen
import com.tajemniktv.tajos.ui.main.state.InsightsData
import kotlinx.datetime.LocalDateTime

/**
 * Shared render context passed to every dashboard block renderer.
 */
data class DashboardBlockContext(
    val viewModel: MainViewModel,
    val dashboardState: DashboardUIState,
    val pinnedNodes: List<NodeWithPin>,
    val allProjects: List<NodeEntity>,
    val allAreas: List<NodeEntity>,
    val inboxNodes: List<NodeWithPin>,
    val activeReminders: List<NodeEntity>,
    val activeSession: FocusSessionEntity?,
    val insights: InsightsData,
    val moodToday: TrackEntryEntity?,
    val needsWeeklyReview: Boolean,
    val dailyProgress: Float,
    val localNow: LocalDateTime,
    val onNavigateTo: (Screen) -> Unit,
    val onEditNode: (Long) -> Unit,
    val onNavigateToProject: (Long) -> Unit,
)

/**
 * Function contract for rendering one dashboard block.
 */
typealias DashboardBlockRendererFn = @Composable (DashboardBlockContext) -> Unit

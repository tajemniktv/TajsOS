/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.DashboardUIState
import com.tajemniktv.tajsos.ui.InsightsData
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
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
typealias DashboardBlockRendererFn = @Composable (com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) -> Unit

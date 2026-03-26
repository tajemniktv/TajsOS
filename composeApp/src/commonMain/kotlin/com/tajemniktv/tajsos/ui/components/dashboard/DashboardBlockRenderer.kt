/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.dashboard

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
 * Dispatches dashboard block rendering through the block registry.
 *
 * This function stays as the integration point for screens while the actual block
 * implementations are split across dedicated renderer files.
 */
@Composable
fun DashboardBlockRenderer(
    blockKey: String,
    viewModel: MainViewModel,
    dashboardState: DashboardUIState,
    pinnedNodes: List<NodeWithPin>,
    allProjects: List<NodeEntity>,
    allAreas: List<NodeEntity>,
    inboxNodes: List<NodeWithPin>,
    activeReminders: List<NodeEntity>,
    activeSession: FocusSessionEntity?,
    insights: InsightsData,
    moodToday: TrackEntryEntity?,
    needsWeeklyReview: Boolean,
    dailyProgress: Float,
    localNow: LocalDateTime,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
) {
    val context =
        DashboardBlockContext(
            viewModel = viewModel,
            dashboardState = dashboardState,
            pinnedNodes = pinnedNodes,
            allProjects = allProjects,
            allAreas = allAreas,
            inboxNodes = inboxNodes,
            activeReminders = activeReminders,
            activeSession = activeSession,
            insights = insights,
            moodToday = moodToday,
            needsWeeklyReview = needsWeeklyReview,
            dailyProgress = dailyProgress,
            localNow = localNow,
            onNavigateTo = onNavigateTo,
            onEditNode = onEditNode,
            onNavigateToProject = onNavigateToProject,
        )
    DashboardBlockRegistry.resolve(blockKey)?.invoke(context)
}

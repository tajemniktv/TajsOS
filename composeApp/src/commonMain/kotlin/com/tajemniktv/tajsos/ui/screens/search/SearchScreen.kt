/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.components.screen.SplitScreenScaffold
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlin.time.Clock

/**
 * Central search entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of search state.
 * @param onItemClick Item click callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun SearchRoute(
    viewModel: MainViewModel,
    onItemClick: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchTypeFilter by viewModel.searchTypeFilter.collectAsState()
    val searchStatusFilter by viewModel.searchStatusFilter.collectAsState()
    val searchProjectFilter by viewModel.searchProjectFilter.collectAsState()
    val searchAreaFilter by viewModel.searchAreaFilter.collectAsState()
    val searchLinkedToFilter by viewModel.searchLinkedToFilter.collectAsState()
    val searchLocationContextFilter by viewModel.searchLocationContextFilter.collectAsState()
    val searchEnergyContextFilter by viewModel.searchEnergyContextFilter.collectAsState()
    val searchDeviceContextFilter by viewModel.searchDeviceContextFilter.collectAsState()
    val searchSocialContextFilter by viewModel.searchSocialContextFilter.collectAsState()
    val searchTimeWindowContextFilter by viewModel.searchTimeWindowContextFilter.collectAsState()
    val searchTimeHorizonFilter by viewModel.searchTimeHorizonFilter.collectAsState()
    val searchSortMode by viewModel.searchSortMode.collectAsState()
    val recentQueries by viewModel.recentSearchQueries.collectAsState()

    val projects by viewModel.allProjects.collectAsState()
    val areas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()

    val projectsById = remember(projects) { projects.associateBy { it.id } }
    val areasById = remember(areas) { areas.associateBy { it.id } }

    var showFilters by remember { mutableStateOf(true) }
    val nowMs = Clock.System.now().toEpochMilliseconds()

    val context =
        SearchDashboardContext(
            viewModel = viewModel,
            searchQuery = searchQuery,
            searchResults = searchResults,
            searchTypeFilter = searchTypeFilter,
            searchStatusFilter = searchStatusFilter,
            searchProjectFilter = searchProjectFilter,
            searchAreaFilter = searchAreaFilter,
            searchLinkedToFilter = searchLinkedToFilter,
            searchLocationContextFilter = searchLocationContextFilter,
            searchEnergyContextFilter = searchEnergyContextFilter,
            searchDeviceContextFilter = searchDeviceContextFilter,
            searchSocialContextFilter = searchSocialContextFilter,
            searchTimeWindowContextFilter = searchTimeWindowContextFilter,
            searchTimeHorizonFilter = searchTimeHorizonFilter,
            searchSortMode = searchSortMode,
            showFilters = showFilters,
            projectsById = projectsById,
            areasById = areasById,
            allNodes = allNodes,
            recentQueries = recentQueries,
            nowMs = nowMs,
            onItemClick = onItemClick,
            onToggleFilters = { showFilters = !showFilters },
        )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >= 1280.dp) SearchDashboardSurface.DESKTOP else SearchDashboardSurface.MOBILE
        val plan = remember(surface) { buildSearchDashboardPlan(surface) }

        SearchScreen(
            context = context,
            plan = plan,
            surface = surface,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless search screen content.
 *
 * @param context Search dashboard context.
 * @param plan Search dashboard plan.
 * @param surface Current UI surface mode.
 * @param onNavigate Navigation callback.
 */
@Composable
fun SearchScreen(
    context: SearchDashboardContext,
    plan: SearchDashboardPlan,
    surface: SearchDashboardSurface,
    onNavigate: (String) -> Unit,
) {
    SplitScreenScaffold(
        isSplitLayout = surface == SearchDashboardSurface.DESKTOP,
        screen = Screen.Search,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
        primary = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                plan.primary.forEach { block ->
                    SearchDashboardBlocks.resolve(block.id)?.invoke(context)
                }
            }
        },
        secondary =
            if (surface == SearchDashboardSurface.DESKTOP) {
                {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(top = 64.dp)
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                    ) {
                        plan.secondary.forEach { block ->
                            SearchDashboardBlocks.resolve(block.id)?.invoke(context)
                        }
                    }
                }
            } else {
                null
            },
    )
}

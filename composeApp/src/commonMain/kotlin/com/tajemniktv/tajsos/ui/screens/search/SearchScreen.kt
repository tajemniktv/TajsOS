/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlin.time.Clock

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onItemClick: (Long) -> Unit,
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

    val projects by viewModel.allProjects.collectAsState()
    val areas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()

    val projectsById = remember(projects) { projects.associateBy { it.id } }
    val areasById = remember(areas) { areas.associateBy { it.id } }

    val recentQueries =
        remember(searchQuery) {
            buildList {
                add("Weekly review")
                add("Overdue tasks")
                add("Project notes")
                if (searchQuery.isNotBlank()) add(searchQuery)
            }.distinct().take(4)
        }
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
            projectsById = projectsById,
            areasById = areasById,
            allNodes = allNodes,
            recentQueries = recentQueries,
            nowMs = nowMs,
            onItemClick = onItemClick,
        )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
    ) {
        val surface =
            if (maxWidth >= 1280.dp) SearchDashboardSurface.DESKTOP else SearchDashboardSurface.MOBILE
        val plan = remember(surface) { buildSearchDashboardPlan(surface) }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                plan.primary.forEach { block ->
                    SearchDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                }
            }

            if (surface == SearchDashboardSurface.DESKTOP) {
                Column(modifier = Modifier.padding(top = 64.dp)) {
                    // Offset for search field
                    plan.secondary.forEach { block ->
                        SearchDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                    }
                }
            }
        }
    }
}

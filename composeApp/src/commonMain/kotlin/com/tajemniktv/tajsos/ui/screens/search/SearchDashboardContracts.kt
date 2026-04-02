/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.search

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Defines the supported surfaces for search dashboard layout planning.
 */
enum class SearchDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical search dashboard block.
 */
data class SearchDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the search dashboard screen.
 */
data class SearchDashboardPlan(
    val primary: List<SearchDashboardBlock> = emptyList(),
    val secondary: List<SearchDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for search dashboard block renderers.
 */
data class SearchDashboardContext(
    val viewModel: MainViewModel,
    val searchQuery: String,
    val searchResults: List<NodeWithPin>,
    val searchTypeFilter: String?,
    val searchStatusFilter: String?,
    val searchProjectFilter: Long?,
    val searchAreaFilter: Long?,
    val searchLinkedToFilter: Long?,
    val searchLocationContextFilter: String?,
    val searchEnergyContextFilter: String?,
    val searchDeviceContextFilter: String?,
    val searchSocialContextFilter: String?,
    val searchTimeWindowContextFilter: String?,
    val searchTimeHorizonFilter: String?,
    val searchSortMode: String,
    val showFilters: Boolean,
    val projectsById: Map<Long, NodeEntity>,
    val areasById: Map<Long, NodeEntity>,
    val allNodes: List<NodeWithPin>,
    val recentQueries: List<String>,
    val nowMs: Long,
    val onItemClick: (Long) -> Unit,
    val onToggleFilters: () -> Unit,
)

/**
 * Functional interface for rendering a search dashboard block.
 */
typealias SearchDashboardBlockRenderer = @Composable (SearchDashboardContext) -> Unit

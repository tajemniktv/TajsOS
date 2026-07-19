/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.context_10_min
import tajsos.composeapp.generated.resources.context_brain_works
import tajsos.composeapp.generated.resources.context_campus
import tajsos.composeapp.generated.resources.context_commute
import tajsos.composeapp.generated.resources.context_emotionally_wrecked
import tajsos.composeapp.generated.resources.context_high_focus
import tajsos.composeapp.generated.resources.context_home
import tajsos.composeapp.generated.resources.context_internet
import tajsos.composeapp.generated.resources.context_laptop
import tajsos.composeapp.generated.resources.context_low_energy
import tajsos.composeapp.generated.resources.context_out
import tajsos.composeapp.generated.resources.context_phone
import tajsos.composeapp.generated.resources.context_privacy
import tajsos.composeapp.generated.resources.context_waiting
import tajsos.composeapp.generated.resources.search_clear
import tajsos.composeapp.generated.resources.search_filter_active
import tajsos.composeapp.generated.resources.search_filter_archived
import tajsos.composeapp.generated.resources.search_filter_linked_to
import tajsos.composeapp.generated.resources.search_no_results
import tajsos.composeapp.generated.resources.search_placeholder
import tajsos.composeapp.generated.resources.search_reset_filters
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_project
import tajsos.composeapp.generated.resources.type_record
import tajsos.composeapp.generated.resources.type_task
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon

object SearchDashboardBlocks {
    private val renderers: Map<String, SearchDashboardBlockRenderer> =
        mapOf(
            "search_input" to ::renderSearchInput,
            "search_recent" to ::renderSearchRecent,
            "search_filters" to ::renderSearchFilters,
            "search_results_header" to ::renderSearchResultsHeader,
            "search_results_list" to ::renderSearchResultsList,
            "search_support" to ::renderSearchSupport,
        )

    fun resolve(id: String): SearchDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderSearchInput(context: SearchDashboardContext) {
    val viewModel = context.viewModel
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = context.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(Res.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (context.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(Res.string.search_clear),
                        )
                    }
                }
            },
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = TajsOSTheme.Surface,
                    unfocusedContainerColor = TajsOSTheme.Surface,
                ),
        )
        IconButton(onClick = context.onToggleFilters, modifier = Modifier.size(48.dp).pointerHoverIcon(PointerIcon.Hand)) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Toggle filters",
            )
        }
    }
}

@Composable
private fun renderSearchRecent(context: SearchDashboardContext) {
    if (context.recentQueries.isEmpty()) return
    RecentQueriesRow(
        queries = context.recentQueries,
        onQueryClick = { context.viewModel.updateSearchQuery(it) },
    )
}

@Composable
private fun renderSearchFilters(context: SearchDashboardContext) {
    val viewModel = context.viewModel
    if (!context.showFilters) return
    val activeSummary =
        remember(context) {
            buildList {
                if (context.searchTypeFilter != null) add("type")
                if (context.searchStatusFilter != "active") add("status")
                if (context.searchProjectFilter != null) add("project")
                if (context.searchAreaFilter != null) add("area")
                if (context.searchLinkedToFilter != null) add("linked")
                if (context.searchLocationContextFilter != null) add("location")
                if (context.searchEnergyContextFilter != null) add("energy")
                if (context.searchDeviceContextFilter != null) add("device")
                if (context.searchSocialContextFilter != null) add("social")
                if (context.searchTimeWindowContextFilter != null) add("time-window")
                if (context.searchTimeHorizonFilter != null) add("horizon")
            }
        }
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    if (activeSummary.isEmpty()) {
                        "No active filters"
                    } else {
                        "Active: ${
                            activeSummary.joinToString(
                                ", ",
                            )
                        }"
                    },
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
            )
            TextButton(onClick = { viewModel.clearSearchFilters() }) {
                Text(stringResource(Res.string.search_reset_filters))
            }
        }

        // Status and Type Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item {
                FilterChip(
                    selected = context.searchStatusFilter == "active",
                    onClick = { viewModel.updateSearchStatusFilter(if (context.searchStatusFilter == "active") null else "active") },
                    label = { Text(stringResource(Res.string.search_filter_active)) },
                )
            }
            item {
                FilterChip(
                    selected = context.searchStatusFilter == "archived",
                    onClick = { viewModel.updateSearchStatusFilter(if (context.searchStatusFilter == "archived") null else "archived") },
                    label = { Text(stringResource(Res.string.search_filter_archived)) },
                )
            }
            val types = listOf("task", "note", "record", "project", "area")
            items(types, key = { it }) { type ->
                val typeLabel =
                    when (type) {
                        "task" -> stringResource(Res.string.type_task)
                        "note" -> stringResource(Res.string.type_note)
                        "record" -> stringResource(Res.string.type_record)
                        "project" -> stringResource(Res.string.type_project)
                        "area" -> stringResource(Res.string.type_area)
                        else -> type
                    }
                FilterChip(
                    selected = context.searchTypeFilter == type,
                    onClick = { viewModel.updateSearchTypeFilter(if (context.searchTypeFilter == type) null else type) },
                    label = { Text(typeLabel.uppercase()) },
                )
            }
        }

        // Context Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val contexts =
                listOf(
                    "at_home" to Res.string.context_home,
                    "on_campus" to Res.string.context_campus,
                    "out_of_home" to Res.string.context_out,
                    "laptop_required" to Res.string.context_laptop,
                    "phone_okay" to Res.string.context_phone,
                    "needs_internet" to Res.string.context_internet,
                    "needs_privacy" to Res.string.context_privacy,
                    "low_energy" to Res.string.context_low_energy,
                    "high_focus" to Res.string.context_high_focus,
                    "brain_works" to Res.string.context_brain_works,
                    "emotionally_wrecked" to Res.string.context_emotionally_wrecked,
                    "10_minute" to Res.string.context_10_min,
                    "commute_friendly" to Res.string.context_commute,
                    "waiting_room" to Res.string.context_waiting,
                )
            item {
                FilterChip(
                    selected =
                        context.searchLocationContextFilter != null ||
                            context.searchEnergyContextFilter != null ||
                            context.searchDeviceContextFilter != null ||
                            context.searchSocialContextFilter != null ||
                            context.searchTimeWindowContextFilter != null,
                    onClick = { viewModel.applyContextPreset(null) },
                    label = { Text("CONTEXT OFF") },
                )
            }
            items(contexts, key = { it.first }) { (key, labelRes) ->
                val selected =
                    context.searchLocationContextFilter == key ||
                        context.searchEnergyContextFilter == key ||
                        context.searchDeviceContextFilter == key ||
                        context.searchSocialContextFilter == key ||
                        context.searchTimeWindowContextFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.applyContextPreset(if (selected) null else key) },
                    label = { Text(stringResource(labelRes)) },
                )
            }
        }

        // Horizon Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val horizons =
                listOf(
                    "today" to "TODAY",
                    "week" to "THIS WEEK",
                    "month" to "THIS MONTH",
                    "semester" to "SEMESTER",
                    "short" to "SHORT",
                    "long" to "LONG",
                )
            item {
                FilterChip(
                    selected = context.searchTimeHorizonFilter == null,
                    onClick = { viewModel.applyTimeHorizonFilter(null) },
                    label = { Text("HORIZON OFF") },
                )
            }
            items(horizons, key = { it.first }) { (key, label) ->
                FilterChip(
                    selected = context.searchTimeHorizonFilter == key,
                    onClick = {
                        viewModel.applyTimeHorizonFilter(
                            if (context.searchTimeHorizonFilter == key) null else key,
                        )
                    },
                    label = { Text(label) },
                )
            }
        }

        // Project and Area Filters
        if (context.areasById.isNotEmpty() || context.projectsById.isNotEmpty() || context.searchLinkedToFilter != null) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (context.searchLinkedToFilter != null) {
                    val linkedNode =
                        context.allNodes.find { it.node.id == context.searchLinkedToFilter }?.node
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.updateSearchLinkedToFilter(null) },
                            label = {
                                Text(
                                    stringResource(
                                        Res.string.search_filter_linked_to,
                                        linkedNode?.title ?: "...",
                                    ),
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Link,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
                items(context.areasById.values.toList(), key = { it.id }) { area ->
                    FilterChip(
                        selected = context.searchAreaFilter == area.id,
                        onClick = { viewModel.updateSearchAreaFilter(if (context.searchAreaFilter == area.id) null else area.id) },
                        label = { Text(area.title) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }
                items(context.projectsById.values.toList(), key = { it.id }) { project ->
                    FilterChip(
                        selected = context.searchProjectFilter == project.id,
                        onClick = {
                            viewModel.updateSearchProjectFilter(
                                if (context.searchProjectFilter ==
                                    project.id
                                ) {
                                    null
                                } else {
                                    project.id
                                },
                            )
                        },
                        label = { Text(project.title) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun renderSearchResultsHeader(context: SearchDashboardContext) {
    SearchResultsHeader(
        resultCount = context.searchResults.size,
        sortMode = context.searchSortMode,
        onToggleSort = {
            context.viewModel.updateSearchSortMode(
                if (context.searchSortMode == "relevance") "updated" else "relevance",
            )
        },
        onToggleFilters = context.onToggleFilters,
    )
}

@Composable
private fun renderSearchResultsList(context: SearchDashboardContext) {
    if (
        context.searchResults.isEmpty() &&
        (
            context.searchQuery.isNotEmpty() ||
                context.searchTypeFilter != null ||
                context.searchStatusFilter != "active" ||
                context.searchProjectFilter != null ||
                context.searchAreaFilter != null ||
                context.searchLinkedToFilter != null ||
                context.searchLocationContextFilter != null ||
                context.searchEnergyContextFilter != null ||
                context.searchDeviceContextFilter != null ||
                context.searchSocialContextFilter != null ||
                context.searchTimeWindowContextFilter != null ||
                context.searchTimeHorizonFilter != null
        )
    ) {
        EmptyState(message = stringResource(Res.string.search_no_results))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 860.dp),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            items(context.searchResults, key = { it.node.id }) { nodeWithPin ->
                SearchResultCard(
                    nodeWithPin = nodeWithPin,
                    projectName = context.projectsById[nodeWithPin.node.projectId]?.title,
                    areaName = context.areasById[nodeWithPin.node.areaId]?.title,
                    nowMs = context.nowMs,
                    onOpen = { context.onItemClick(nodeWithPin.node.id) },
                    onToggleDone = { status ->
                        context.viewModel.updateNodeStatus(
                            nodeWithPin.node,
                            status,
                        )
                    },
                    onTogglePin = { isPinned ->
                        context.viewModel.setTodayPayload(
                            nodeWithPin.node,
                            isPinned,
                        )
                    },
                    onArchive = { context.viewModel.archiveNode(nodeWithPin.node) },
                )
            }
        }
    }
}

@Composable
private fun renderSearchSupport(context: SearchDashboardContext) {
    SearchSupportPanel(
        indexedCount = context.allNodes.size,
        resultCount = context.searchResults.size,
        topResult =
            context.searchResults
                .firstOrNull()
                ?.node
                ?.title,
    )
}

/**
 * Renders a compact row of recent query suggestions.
 */
@Composable
private fun RecentQueriesRow(
    queries: List<String>,
    onQueryClick: (String) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "RECENT QUERIES",
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
        )
        Spacer(modifier = Modifier.width(TajsOSTheme.SpacingSm))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            items(queries.distinct(), key = { it }) { query ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = TajsOSTheme.SurfaceHigh,
                    onClick = { onQueryClick(query) },
                ) {
                    Text(
                        text = query,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Text,
                    )
                }
            }
        }
    }
}

/**
 * Renders the result section header and utility controls.
 */
@Composable
private fun SearchResultsHeader(
    resultCount: Int,
    sortMode: String,
    onToggleSort: () -> Unit,
    onToggleFilters: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Search Results",
                style = MaterialTheme.typography.headlineSmall,
                color = TajsOSTheme.Text,
            )
            Spacer(modifier = Modifier.width(TajsOSTheme.SpacingSm))
            Text(
                "FOUND $resultCount ITEMS",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            TextButton(onClick = onToggleFilters) {
                Text(
                    "FILTER",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            TextButton(onClick = onToggleSort) {
                Text(
                    if (sortMode == "relevance") "RELEVANCE" else "UPDATED",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/**
 * Renders desktop support details for search status and insights.
 */
@Composable
private fun SearchSupportPanel(
    indexedCount: Int,
    resultCount: Int,
    topResult: String?,
) {
    val coverage =
        if (indexedCount <= 0) {
            0
        } else {
            min(
                100,
                max(1, ((resultCount.toFloat() / indexedCount.toFloat()) * 100f).toInt()),
            )
        }
    Column(
        modifier = Modifier.width(284.dp).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Surface(shape = RoundedCornerShape(TajsOSTheme.RadiusLg), color = TajsOSTheme.SurfaceLow) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "SEARCH STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "SYSTEM INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                    Text(
                        if (indexedCount > 0) "STABLE" else "BOOTING",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                    )
                }
                Text(
                    "$indexedCount objects indexed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Text,
                )
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Index coverage",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                    Text(
                        "$coverage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Text,
                    )
                }
            }
        }

        Surface(shape = RoundedCornerShape(TajsOSTheme.RadiusLg), color = TajsOSTheme.SurfaceLow) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "SEARCH INSIGHTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
                Text(
                    topResult?.let { "Most relevant right now: \"$it\"." }
                        ?: "Run a query to see linked search insights.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Text,
                )
            }
        }
    }
}

/**
 * Renders one search result row with title, metadata, excerpt, and quick actions.
 */
@Composable
private fun SearchResultCard(
    nodeWithPin: NodeWithPin,
    projectName: String?,
    areaName: String?,
    nowMs: Long,
    onOpen: () -> Unit,
    onToggleDone: (String) -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onArchive: () -> Unit,
) {
    val node = nodeWithPin.node
    val subtitle =
        buildString {
            append(node.type.uppercase())
            append(" • ")
            append(node.status.uppercase())
            if (!projectName.isNullOrBlank()) append(" • PROJECT: $projectName")
            if (!areaName.isNullOrBlank()) append(" • AREA: $areaName")
        }

    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
        color = TajsOSTheme.SurfaceLow,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(TajsOSTheme.RadiusSm))
                                .background(iconTintForType(node.type).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            iconForType(node.type),
                            contentDescription = null,
                            tint = iconTintForType(node.type),
                            modifier = Modifier.size(15.dp),
                        )
                    }
                    Column {
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TajsOSTheme.Text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "UPDATED ${formatRelativeTime(node.updatedAt, nowMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TajsOSTheme.SpacingSm))
            Text(
                text = node.content.ifBlank { "Open this item for full details and linked entities." },
                style = MaterialTheme.typography.bodyMedium,
                color = TajsOSTheme.Text.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            val chips =
                buildList {
                    nodeWithPin.tags.take(3).forEach { add("#${it.name}") }
                    if (nodeWithPin.isPinnedToToday) add("PINNED TODAY")
                }
            if (chips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(TajsOSTheme.SpacingSm))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    chips.forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                            color = TajsOSTheme.SurfaceHigh,
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Muted,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(TajsOSTheme.SpacingSm))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onOpen) {
                    Text(
                        "OPEN",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                TextButton(onClick = { onToggleDone(if (node.status == "done") "active" else "done") }) {
                    Text(
                        if (node.status == "done") "ACTIVATE" else "DONE",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                TextButton(onClick = { onTogglePin(!nodeWithPin.isPinnedToToday) }) {
                    Text(
                        if (nodeWithPin.isPinnedToToday) "REMOVE TODAY" else "ADD TODAY",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                TextButton(onClick = onArchive) {
                    Text(
                        "ARCHIVE",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

private fun iconForType(type: String): ImageVector =
    when (type.lowercase()) {
        "task" -> Icons.Default.TaskAlt
        "project" -> Icons.Default.Folder
        "note" -> Icons.Default.Description
        "record" -> Icons.AutoMirrored.Filled.InsertDriveFile
        else -> Icons.Default.Search
    }

private fun iconTintForType(type: String): Color =
    when (type.lowercase()) {
        "task" -> TajsOSTheme.AccentRed
        "project" -> TajsOSTheme.Primary
        "note" -> TajsOSTheme.AccentBlue
        "record" -> TajsOSTheme.AccentCyan
        else -> TajsOSTheme.Primary
    }

private fun formatRelativeTime(
    timestampMs: Long,
    nowMs: Long,
): String {
    if (timestampMs <= 0L || timestampMs >= nowMs) return "JUST NOW"
    val minutes = ((nowMs - timestampMs) / 60_000L).toInt()
    return when {
        minutes < 1 -> "JUST NOW"
        minutes < 60 -> "${minutes}M AGO"
        minutes < 1_440 -> "${minutes / 60}H AGO"
        minutes < 10_080 -> "${minutes / 1_440}D AGO"
        else -> "${minutes / 10_080}W AGO"
    }
}

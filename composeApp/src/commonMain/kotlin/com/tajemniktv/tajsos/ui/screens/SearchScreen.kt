/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
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
import kotlin.time.Clock

/**
 * Renders the search screen UI: a query field, filter chips (status, type, project, area, linked-to),
 * and either an empty-state message or a scrollable list of matching result cards.
 *
 * The composable observes search-related state from the provided viewModel and invokes its update
 * actions in response to user interactions (query changes, filter selection, pin/archive/status updates).
 * When a result item is tapped or long-pressed, the composable calls `onItemClick` with the node id.
 *
 * @param viewModel Provides the observable search state (query, filters, results, projects/areas/nodes)
 * and exposes actions used by the UI to update query, filters, node status, pinning, and archiving.
 * @param onItemClick Invoked with the selected node's id when a result card is clicked or long-pressed.
 */
@OptIn(ExperimentalFoundationApi::class)
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
    val contextHome = stringResource(Res.string.context_home)
    val contextCampus = stringResource(Res.string.context_campus)
    val contextOut = stringResource(Res.string.context_out)
    val contextLaptop = stringResource(Res.string.context_laptop)
    val contextPhone = stringResource(Res.string.context_phone)
    val contextInternet = stringResource(Res.string.context_internet)
    val contextPrivacy = stringResource(Res.string.context_privacy)
    val contextLowEnergy = stringResource(Res.string.context_low_energy)
    val contextHighFocus = stringResource(Res.string.context_high_focus)
    val contextBrainWorks = stringResource(Res.string.context_brain_works)
    val contextEmotionallyWrecked = stringResource(Res.string.context_emotionally_wrecked)
    val context10Min = stringResource(Res.string.context_10_min)
    val contextCommute = stringResource(Res.string.context_commute)
    val contextWaiting = stringResource(Res.string.context_waiting)
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
    val projectsById = remember(projects) { projects.associateBy { it.id } }
    val areasById = remember(areas) { areas.associateBy { it.id } }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
    ) {
        val showRightPanel = maxWidth >= 1280.dp

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(Res.string.search_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
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
                                focusedContainerColor = TactileTheme.Surface,
                                unfocusedContainerColor = TactileTheme.Surface,
                            ),
                    )
                    IconButton(onClick = { viewModel.clearSearchFilters() }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = stringResource(Res.string.search_reset_filters),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                RecentQueriesRow(
                    queries = recentQueries,
                    onQueryClick = { viewModel.updateSearchQuery(it) },
                )
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

                // Filters Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        FilterChip(
                            selected = searchStatusFilter == "active",
                            onClick = { viewModel.updateSearchStatusFilter(if (searchStatusFilter == "active") null else "active") },
                            label = { Text(stringResource(Res.string.search_filter_active)) },
                        )
                    }
                    item {
                        FilterChip(
                            selected = searchStatusFilter == "archived",
                            onClick = { viewModel.updateSearchStatusFilter(if (searchStatusFilter == "archived") null else "archived") },
                            label = { Text(stringResource(Res.string.search_filter_archived)) },
                        )
                    }
                    val types = listOf("task", "note", "record", "project", "area")
                    items(types) { type ->
                        val typeLabel =
                            when (type)
                            {
                                "task" -> stringResource(Res.string.type_task)
                                "note" -> stringResource(Res.string.type_note)
                                "record" -> stringResource(Res.string.type_record)
                                "project" -> stringResource(Res.string.type_project)
                                "area" -> stringResource(Res.string.type_area)
                                else -> type
                            }
                        FilterChip(
                            selected = searchTypeFilter == type,
                            onClick = { viewModel.updateSearchTypeFilter(if (searchTypeFilter == type) null else type) },
                            label = { Text(typeLabel.uppercase()) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val contexts =
                        listOf(
                            "at_home" to contextHome,
                            "on_campus" to contextCampus,
                            "out_of_home" to contextOut,
                            "laptop_required" to contextLaptop,
                            "phone_okay" to contextPhone,
                            "needs_internet" to contextInternet,
                            "needs_privacy" to contextPrivacy,
                            "low_energy" to contextLowEnergy,
                            "high_focus" to contextHighFocus,
                            "brain_works" to contextBrainWorks,
                            "emotionally_wrecked" to contextEmotionallyWrecked,
                            "10_minute" to context10Min,
                            "commute_friendly" to contextCommute,
                            "waiting_room" to contextWaiting,
                        )
                    item {
                        FilterChip(
                            selected =
                                searchLocationContextFilter != null ||
                                    searchEnergyContextFilter != null ||
                                    searchDeviceContextFilter != null ||
                                    searchSocialContextFilter != null ||
                                    searchTimeWindowContextFilter != null,
                            onClick = { viewModel.applyContextPreset(null) },
                            label = { Text("CONTEXT OFF") },
                        )
                    }
                    items(contexts) { (key, label) ->
                        val selected =
                            searchLocationContextFilter == key ||
                                searchEnergyContextFilter == key ||
                                searchDeviceContextFilter == key ||
                                searchSocialContextFilter == key ||
                                searchTimeWindowContextFilter == key
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.applyContextPreset(if (selected) null else key) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
                            selected = searchTimeHorizonFilter == null,
                            onClick = { viewModel.applyTimeHorizonFilter(null) },
                            label = { Text("HORIZON OFF") },
                        )
                    }
                    items(horizons) { (key, label) ->
                        FilterChip(
                            selected = searchTimeHorizonFilter == key,
                            onClick = {
                                viewModel.applyTimeHorizonFilter(
                                    if (searchTimeHorizonFilter == key) null else key,
                                )
                            },
                            label = { Text(label) },
                        )
                    }
                }

                if (projects.isNotEmpty() || areas.isNotEmpty() || searchLinkedToFilter != null) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (searchLinkedToFilter != null) {
                            val linkedNode =
                                allNodes.find { it.node.id == searchLinkedToFilter }?.node
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
                        items(areas) { area ->
                            FilterChip(
                                selected = searchAreaFilter == area.id,
                                onClick = { viewModel.updateSearchAreaFilter(if (searchAreaFilter == area.id) null else area.id) },
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
                        items(projects) { project ->
                            FilterChip(
                                selected = searchProjectFilter == project.id,
                                onClick = {
                                    viewModel.updateSearchProjectFilter(
                                        if (searchProjectFilter ==
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

                Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
                SearchResultsHeader(searchResults.size)
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

                if (
                    searchResults.isEmpty() &&
                    (
                        searchQuery.isNotEmpty() ||
                            searchTypeFilter != null ||
                            searchStatusFilter != "active" ||
                            searchProjectFilter != null ||
                            searchAreaFilter != null ||
                            searchLinkedToFilter != null ||
                            searchLocationContextFilter != null ||
                            searchEnergyContextFilter != null ||
                            searchDeviceContextFilter != null ||
                            searchSocialContextFilter != null ||
                            searchTimeWindowContextFilter != null ||
                            searchTimeHorizonFilter != null
                    )
                ) {
                    EmptyState(message = stringResource(Res.string.search_no_results))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        items(searchResults, key = { it.node.id }) { nodeWithPin ->
                            SearchResultCard(
                                nodeWithPin = nodeWithPin,
                                query = searchQuery,
                                projectName = projectsById[nodeWithPin.node.projectId]?.title,
                                areaName = areasById[nodeWithPin.node.areaId]?.title,
                                nowMs = nowMs,
                                onOpen = { onItemClick(nodeWithPin.node.id) },
                                onToggleDone = { status ->
                                    viewModel.updateNodeStatus(
                                        nodeWithPin.node,
                                        status,
                                    )
                                },
                                onTogglePin = { isPinned ->
                                    viewModel.togglePin(
                                        nodeWithPin.node,
                                        isPinned,
                                    )
                                },
                                onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                            )
                        }
                    }
                }
            }

            if (showRightPanel) {
                SearchSupportPanel(
                    indexedCount = allNodes.size,
                    resultCount = searchResults.size,
                    topResult = searchResults.firstOrNull()?.node?.title,
                )
            }
        }
    }
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
            color = TactileTheme.Muted,
        )
        Spacer(modifier = Modifier.width(TactileTheme.SpacingSm))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            items(queries) { query ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = TactileTheme.SurfaceHigh,
                    onClick = { onQueryClick(query) },
                ) {
                    Text(
                        text = query,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Text,
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
private fun SearchResultsHeader(resultCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Search Results",
                style = MaterialTheme.typography.headlineSmall,
                color = TactileTheme.Text,
            )
            Spacer(modifier = Modifier.width(TactileTheme.SpacingSm))
            Text(
                "FOUND $resultCount ITEMS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            TextButton(onClick = {}) { Text("FILTER", style = MaterialTheme.typography.labelSmall) }
            TextButton(onClick = {}) {
                Text(
                    "RELEVANCE",
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
        if (indexedCount <= 0)
        {
            0
        } else
        {
            min(
                100,
                max(1, ((resultCount.toFloat() / indexedCount.toFloat()) * 100f).toInt()),
                )
            }
    Column(
        modifier = Modifier.width(284.dp).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = TactileTheme.SurfaceLow) {
            Column(
                modifier = Modifier.padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "SEARCH STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "SYSTEM INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                    Text(
                        if (indexedCount > 0) "STABLE" else "BOOTING",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                    )
                }
                Text(
                    "$indexedCount objects indexed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Text,
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
                        color = TactileTheme.Muted,
                    )
                    Text(
                        "$coverage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Text,
                    )
                }
            }
        }

        Surface(shape = RoundedCornerShape(14.dp), color = TactileTheme.SurfaceLow) {
            Column(
                modifier = Modifier.padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "SEARCH INSIGHTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                Text(
                    topResult?.let { "Most relevant right now: \"$it\"." }
                        ?: "Run a query to see linked search insights.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Text,
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
    query: String,
    projectName: String?,
    areaName: String?,
    nowMs: Long,
    onOpen: () -> Unit,
    onToggleDone: (String) -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onArchive: () -> Unit,
) {
    val node = nodeWithPin.node
    val score = calculateMatchScore(nodeWithPin, query)
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
        shape = RoundedCornerShape(14.dp),
        color = TactileTheme.SurfaceLow,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
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
                                .clip(RoundedCornerShape(7.dp))
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
                            color = TactileTheme.Text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = TactileTheme.Primary.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = "MATCH: $score%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Primary,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "UPDATED ${formatRelativeTime(node.updatedAt, nowMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
            Text(
                text = node.content.ifBlank { "Open this item for full details and linked entities." },
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Text.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            val chips =
                buildList {
                    nodeWithPin.tags.take(3).forEach { add("#${it.name}") }
                    if (nodeWithPin.isPinnedToToday) add("PINNED TODAY")
                }
            if (chips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    chips.forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TactileTheme.SurfaceHigh,
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
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
                        if (nodeWithPin.isPinnedToToday) "UNPIN" else "PIN",
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

private fun calculateMatchScore(
    nodeWithPin: NodeWithPin,
    query: String,
): Int {
    if (query.isBlank()) return 76 + (nodeWithPin.node.id % 20).toInt()
    val q = query.trim().lowercase()
    var score = 45
    if (nodeWithPin.node.title
            .lowercase()
            .contains(q)
    )
    {
        score += 28
    }
    if (nodeWithPin.node.content
            .lowercase()
            .contains(q)
    ) {
        score += 16
    }
    if (nodeWithPin.tags.any { it.name.lowercase().contains(q) }) score += 9
    if (nodeWithPin.node.type
            .lowercase()
            .contains(q)
    ) {
        score += 6
    }
    return min(99, max(50, score))
}

private fun iconForType(type: String): ImageVector =
    when (type.lowercase())
    {
        "task" -> Icons.Default.TaskAlt
        "project" -> Icons.Default.Folder
        "note" -> Icons.Default.Description
        "record" -> Icons.AutoMirrored.Filled.InsertDriveFile
        else -> Icons.Default.Search
    }

private fun iconTintForType(type: String): Color =
    when (type.lowercase())
    {
        "task" -> Color(0xFFFF7A8B)
        "project" -> Color(0xFF9D7AFF)
        "note" -> Color(0xFF8EA4FF)
        "record" -> Color(0xFF57D7C6)
        else -> TactileTheme.Primary
    }

private fun formatRelativeTime(
    timestampMs: Long,
    nowMs: Long,
): String {
    if (timestampMs <= 0L || timestampMs >= nowMs) return "JUST NOW"
    val minutes = ((nowMs - timestampMs) / 60_000L).toInt()
    return when
    {
        minutes < 1      -> "JUST NOW"
        minutes < 60     -> "${minutes}M AGO"
        minutes < 1_440  -> "${minutes / 60}H AGO"
        minutes < 10_080 -> "${minutes / 1_440}D AGO"
            else -> "${minutes / 10_080}W AGO"
        }
}

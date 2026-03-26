/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

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

    Column(modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd)) {
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
            val types = listOf("task", "note", "project", "area", "resource", "idea")
            items(types) { type ->
                val typeLabel =
                    when (type)
                    {
                        "task" -> stringResource(Res.string.type_task)
                        "note" -> stringResource(Res.string.type_note)
                        "idea" -> stringResource(Res.string.type_idea)
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
                    val linkedNode = allNodes.find { it.node.id == searchLinkedToFilter }?.node
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
                        onClick = { viewModel.updateSearchProjectFilter(if (searchProjectFilter == project.id) null else project.id) },
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
                    NodeCard(
                        nodeWithPin = nodeWithPin,
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
                        onClick = { onItemClick(nodeWithPin.node.id) },
                        onLongClick = { onItemClick(nodeWithPin.node.id) },
                        onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                    )
                }
            }
        }
    }
}

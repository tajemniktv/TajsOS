/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun MaintenanceLayer(
    viewModel: MainViewModel,
    snapshot: com.tajemniktv.tajsos.ui.MaintenanceSnapshot,
    allAreas: List<NodeEntity>,
    maintenanceView: MaintenanceView,
    onMaintenanceView: (MaintenanceView) -> Unit,
    maintenanceTypeFilter: Set<String>? = null,
    onEditNode: (Long) -> Unit,
) {
    val itemsForView =
        when (maintenanceView)
        {
            MaintenanceView.Queue -> snapshot.active
            MaintenanceView.Recurring -> snapshot.recurring
            MaintenanceView.Overdue -> snapshot.overdue
        }
    val items =
        if (maintenanceTypeFilter == null) {
            itemsForView
        } else {
            itemsForView.filter { item ->
                val maintenanceType = item.node.node.maintenanceType
                maintenanceType != null && maintenanceType in maintenanceTypeFilter
            }
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "MAINTENANCE DASHBOARD",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Active ${snapshot.active.size} • Recurring ${snapshot.recurring.size} • Overdue ${snapshot.overdue.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Admin debt meter: ${snapshot.adminDebtMeter}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.adminDebtMeter >= 70) TactileTheme.Error else TactileTheme.Text,
            )
            snapshot.overdueWarning?.let { warning ->
                Text(
                    warning,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (snapshot.breakIfIgnored.isNotEmpty()) {
                Text(
                    "Things that break if ignored: ${
                        snapshot.breakIfIgnored.joinToString(", ") {
                            (it.node.node.maintenanceType ?: "manual").replace(
                                "_",
                                " ",
                            )
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        MaintenanceView.entries.forEach { view ->
            FilterChip(
                selected = maintenanceView == view,
                onClick = { onMaintenanceView(view) },
                label = { Text(view.label) },
            )
        }
    }

    if (items.isEmpty()) {
        EmptyState(message = "No maintenance items in ${maintenanceView.label.lowercase()}.")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (maintenanceTypeFilter == null) {
            item {
                GroupedOpenLoopSection(
                    title = "TRACKERS BY TYPE",
                    items =
                        snapshot.byType.entries.map { entry ->
                            "${entry.key.replace("_", " ").uppercase()} • ${entry.value.size}"
                        },
                )
            }
            item {
                GroupedOpenLoopSection(
                    title = "TRACKERS BY URGENCY",
                    items =
                        snapshot.byUrgency.entries.map { entry ->
                            "${entry.key.uppercase()} • ${entry.value.size}"
                        },
                )
            }
            item {
                GroupedOpenLoopSection(
                    title = "TRACKERS BY AREA",
                    items =
                        snapshot.byArea.entries.map { entry ->
                            val name =
                                if (entry.key == null) {
                                    "UNASSIGNED"
                                } else {
                                    allAreas.find { it.id == entry.key }?.title ?: "UNKNOWN"
                                }
                            "$name • ${entry.value.size}"
                        },
                )
            }
            if (snapshot.expirationReminders.isNotEmpty()) {
                item {
                    GroupedOpenLoopSection(
                        title = "EXPIRATION REMINDERS",
                        items =
                            snapshot.expirationReminders
                                .take(5)
                                .map { "${it.node.node.title} • ${it.dueInDays ?: 0}d" },
                    )
                }
            }
        }

        items(items, key = { it.node.node.id }) { item ->
            MaintenanceCard(
                item = item,
                areaName = allAreas.find { it.id == item.node.node.areaId }?.title,
                onEditNode = onEditNode,
                onSetType = { type -> viewModel.updateMaintenanceType(item.node.node, type) },
                onSetRecurring = { interval ->
                    viewModel.setMaintenanceRecurring(
                        item.node.node,
                        interval,
                    )
                },
                onSetOverdue = { timestamp ->
                    viewModel.setMaintenanceOverdueAt(
                        item.node.node,
                        timestamp,
                    )
                },
                onResolve = { viewModel.updateNodeStatus(item.node.node, "done") },
                onArchive = { viewModel.archiveNode(item.node.node) },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun OpenLoopsLayer(
    viewModel: MainViewModel,
    snapshot: com.tajemniktv.tajsos.ui.OpenLoopsSnapshot,
    allAreas: List<NodeEntity>,
    allNodes: List<NodeWithPin>,
    openLoopView: OpenLoopView,
    onOpenLoopView: (OpenLoopView) -> Unit,
    onEditNode: (Long) -> Unit,
) {
    val loops =
        when (openLoopView)
        {
            OpenLoopView.Inbox -> snapshot.inbox
            OpenLoopView.Review -> snapshot.review
            OpenLoopView.All -> snapshot.active
            OpenLoopView.Resolved -> snapshot.resolved
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "OPEN LOOPS LAYER",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Active ${snapshot.active.size} • Inbox ${snapshot.inbox.size} • Review ${snapshot.review.size} • Resolved ${snapshot.resolved.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Decay index: ${snapshot.averageDecayScore}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.averageDecayScore >= 60) TactileTheme.Error else TactileTheme.Text,
            )
            snapshot.overloadWarning?.let { overloadWarning ->
                Text(
                    overloadWarning,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (snapshot.resolved.isNotEmpty()) {
                OutlinedButton(
                    onClick = { viewModel.archiveResolvedOpenLoops() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ARCHIVE RESOLVED OPEN LOOPS")
                }
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        OpenLoopView.entries.forEach { view ->
            FilterChip(
                selected = openLoopView == view,
                onClick = { onOpenLoopView(view) },
                label = { Text(view.label) },
            )
        }
    }

    if (loops.isEmpty()) {
        EmptyState(message = "No open loops in ${openLoopView.label.lowercase()}.")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        items(loops, key = { it.node.node.id }) { loop ->
            OpenLoopCard(
                item = loop,
                areaName = allAreas.find { it.id == loop.node.node.areaId }?.title,
                onEditNode = onEditNode,
                onSetType = { type -> viewModel.updateOpenLoopType(loop.node.node, type) },
                onConvertTask = { viewModel.convertOpenLoopToTask(loop.node.node.id) },
                onConvertDecision = { viewModel.convertOpenLoopToDecision(loop.node.node.id) },
                onConvertNote = { viewModel.convertOpenLoopToNote(loop.node.node.id) },
                onResolve = { viewModel.resolveOpenLoop(loop.node.node.id) },
                onArchive = { viewModel.archiveNode(loop.node.node) },
            )
        }

        if (openLoopView == OpenLoopView.All) {
            item {
                GroupedOpenLoopSection(
                    title = "BY AREA",
                    items =
                        snapshot.byArea.entries.map { entry ->
                            val areaName =
                                if (entry.key == null) {
                                    "UNASSIGNED"
                                } else {
                                    allAreas.find { it.id == entry.key }?.title ?: "UNKNOWN"
                                }
                            "$areaName • ${entry.value.size}"
                        },
                )
            }
            item {
                GroupedOpenLoopSection(
                    title = "BY PERSON",
                    items =
                        snapshot.byPerson.entries.map { entry ->
                            val personName =
                                allNodes.find { it.node.id == entry.key }?.node?.title ?: "UNKNOWN"
                            "$personName • ${entry.value.size}"
                        },
                )
            }
            item {
                GroupedOpenLoopSection(
                    title = "BY URGENCY",
                    items =
                        snapshot.byUrgency.entries.map { entry ->
                            "${entry.key.uppercase()} • ${entry.value.size}"
                        },
                )
            }
        }
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.MaintenanceCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme

private val financeMaintenanceTypes =
    setOf(
        "bill",
        "subscription",
        "renewal",
    )

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun FinancesScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val maintenanceSnapshot by viewModel.maintenanceSnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    var maintenanceView by remember { mutableStateOf(MaintenanceView.Queue) }

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "FINANCES WORKSPACE",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Track bills, subscriptions, renewals, and finance-related maintenance.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        MaintenanceLayer(
            viewModel = viewModel,
            snapshot = maintenanceSnapshot,
            allAreas = allAreas,
            maintenanceView = maintenanceView,
            onMaintenanceView = { maintenanceView = it },
            maintenanceTypeFilter = financeMaintenanceTypes,
            onEditNode = onEditNode,
        )
    }
}

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
                maintenanceTypes = maintenanceTypes,
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

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.TimeArchitectureSnapshot
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun TimeArchitectureScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val timeArchitectureSnapshot by viewModel.timeArchitectureSnapshot.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "TIME ARCHITECTURE",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Work across today, week, month, and semester horizons without losing temporal structure.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        TimeArchitectureLayer(
            viewModel = viewModel,
            snapshot = timeArchitectureSnapshot,
            onEditNode = onEditNode,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun TimeArchitectureLayer(
    viewModel: MainViewModel,
    snapshot: TimeArchitectureSnapshot,
    onEditNode: (Long) -> Unit,
) {
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
                "TIME ARCHITECTURE",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Today ${snapshot.todayLayer.size} • Week ${snapshot.weekLayer.size} • Month ${snapshot.monthLayer.size} • Semester ${snapshot.semesterLayer.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Monthly reset: ${snapshot.monthlyResetDate}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            if (snapshot.examPeriodMode) {
                Text(
                    "EXAM PERIOD MODE ACTIVE",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        AssistChip(onClick = { viewModel.runMonthlyReset() }, label = { Text("RUN MONTHLY RESET") })
        AssistChip(
            onClick = { viewModel.addLifePeriodMarker("Life period marker") },
            label = { Text("ADD PERIOD MARKER") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter("today") },
            label = { Text("HORIZON TODAY") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter("week") },
            label = { Text("HORIZON WEEK") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter("month") },
            label = { Text("HORIZON MONTH") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter("semester") },
            label = { Text("HORIZON SEMESTER") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter(null) },
            label = { Text("CLEAR HORIZON") },
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        item {
            GroupedOpenLoopSection(
                title = "WEEKLY MAP",
                items =
                    if (snapshot.weeklyMap.isEmpty()) {
                        listOf("No due map for this week")
                    } else {
                        snapshot.weeklyMap.map { (day, count) -> "$day • $count" }
                    },
            )
        }

        if (snapshot.countdowns.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "COUNTDOWNS",
                    items = snapshot.countdowns.map { "${it.node.node.title} • ${it.daysLeft}d" },
                )
            }
        }

        if (snapshot.shortHorizonTasks.isNotEmpty()) {
            items(snapshot.shortHorizonTasks, key = { it.node.id }) { item ->
                NodeCard(
                    nodeWithPin = item,
                    onToggleDone = { status -> viewModel.updateNodeStatus(item.node, status) },
                    onTogglePin = { isPinned -> viewModel.togglePin(item.node, isPinned) },
                    onClick = { onEditNode(item.node.id) },
                    onLongClick = { onEditNode(item.node.id) },
                    onArchive = { viewModel.archiveNode(item.node) },
                )
            }
        }

        if (snapshot.longHorizonTasks.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LONG HORIZON",
                    items =
                        snapshot.longHorizonTasks.map {
                            val dueLabel =
                                it.node.dueAt?.let(::formatProtocolTimestamp) ?: "unscheduled"
                            "${it.node.title} • due $dueLabel"
                        },
                )
            }
        }

        if (snapshot.projectPhases.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "PROJECT PHASE MODE",
                    items =
                        snapshot.projectPhases.map { item ->
                            "${item.project.title} • ${
                                item.phaseLabel.replace("_", " ").uppercase()
                            }"
                        },
                )
            }
            items(snapshot.projectPhases, key = { it.project.id }) { phase ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = BorderStroke(1.dp, TactileTheme.Border),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                phase.project.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TactileTheme.Text,
                            )
                            Text(
                                phase.phaseLabel.replace("_", " ").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (phase.isActivePhase) TactileTheme.Success else TactileTheme.Muted,
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = phase.isActivePhase,
                                onClick = { viewModel.setProjectActivePhase(phase.project, true) },
                                label = { Text("ACTIVE") },
                            )
                            FilterChip(
                                selected = !phase.isActivePhase,
                                onClick = { viewModel.setProjectActivePhase(phase.project, false) },
                                label = { Text("INACTIVE") },
                            )
                        }
                    }
                }
            }
        }

        if (snapshot.temporaryFocusPeriods.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "TEMPORARY FOCUS PERIODS",
                    items = snapshot.temporaryFocusPeriods.map { it.node.title },
                )
            }
        }
        items(snapshot.weekLayer.take(6), key = { it.node.id }) { item ->
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
                        item.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { viewModel.setTemporaryFocusPeriod(item.node, 7) },
                            label = { Text("7D FOCUS") },
                        )
                        AssistChip(
                            onClick = { viewModel.setTemporaryFocusPeriod(item.node, 14) },
                            label = { Text("14D FOCUS") },
                        )
                        AssistChip(
                            onClick = { viewModel.clearTemporaryFocusPeriod(item.node) },
                            label = { Text("CLEAR FOCUS") },
                        )
                        AssistChip(onClick = { onEditNode(item.node.id) }, label = { Text("OPEN") })
                    }
                }
            }
        }
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.TimeArchitectureSnapshot
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * Dedicated system screen for temporal architecture.
 *
 * This screen intentionally stays separate from calendar/task execution surfaces and focuses on
 * horizon modeling, cadence resets, and anchor markers.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun TimeArchitectureScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val timeArchitectureSnapshot by viewModel.timeArchitectureSnapshot.collectAsState()
    var selectedHorizon by remember { mutableStateOf(TimeArchitectureHorizon.MONTH) }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        item {
            Text(
                text = "TIME ARCHITECTURE",
                style = MaterialTheme.typography.displaySmall,
                color = TactileTheme.Text,
            )
        }
        item {
            Text(
                text = "Temporal structure for horizons, reset cadence, and anchor markers.",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
        item {
            HorizonSwitcher(
                selected = selectedHorizon,
                onSelected = { horizon ->
                    selectedHorizon = horizon
                    viewModel.applyTimeHorizonFilter(horizon.key)
                },
                snapshot = timeArchitectureSnapshot,
            )
        }
        item {
            TimeArchitectureLayer(
                viewModel = viewModel,
                snapshot = timeArchitectureSnapshot,
                selectedHorizon = selectedHorizon,
                onEditNode = onEditNode,
            )
        }
    }
}

/**
 * Primary map layer for temporal horizons plus cadence/reset and anchors.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TimeArchitectureLayer(
    viewModel: MainViewModel,
    snapshot: TimeArchitectureSnapshot,
    selectedHorizon: TimeArchitectureHorizon,
    onEditNode: (Long) -> Unit,
) {
    val horizonItems = selectedHorizon.items(snapshot)
    val weeklyBuckets =
        if (snapshot.weeklyMap.isEmpty()) {
            emptyList()
        } else {
            snapshot.weeklyMap.entries.toList().sortedBy { it.key }
        }

    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TactileTheme.SurfaceLow,
            shape = RoundedCornerShape(TactileTheme.RadiusLg),
            border = BorderStroke(1.dp, TactileTheme.GhostBorder.copy(alpha = 0.18f)),
        ) {
            Column(
                modifier = Modifier.padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Structural Time Map",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TactileTheme.Text,
                        )
                        Text(
                            "Current horizon: ${selectedHorizon.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted,
                        )
                    }
                    Text(
                        "Today ${snapshot.todayLayer.size} • Week ${snapshot.weekLayer.size} • Month ${snapshot.monthLayer.size} • Semester ${snapshot.semesterLayer.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    StructuralCard(
                        title = "Active Window",
                        label = "${horizonItems.size} items in ${selectedHorizon.label.lowercase()}",
                        modifier = Modifier.weight(1f),
                        tone = TactileTheme.SurfaceHigh,
                    ) {
                        if (horizonItems.isEmpty()) {
                            Text(
                                text = "No items in this horizon.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted,
                            )
                        } else {
                            horizonItems.take(3).forEach { item ->
                                Text(
                                    text = "• ${item.node.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TactileTheme.Text,
                                )
                            }
                        }
                    }

                    StructuralCard(
                        title = "Reserve Window",
                        label = "Long-range load and semester pressure",
                        modifier = Modifier.weight(1f),
                        tone = TactileTheme.SurfaceHighest,
                    ) {
                        val reserveCount = snapshot.longHorizonTasks.size + snapshot.semesterLayer.size
                        Text(
                            text = "$reserveCount long-horizon commitments tracked",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Text,
                        )
                        if (snapshot.examPeriodMode) {
                            Text(
                                text = "Exam-period pressure detected",
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Error,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                if (weeklyBuckets.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        weeklyBuckets.forEach { (day, count) ->
                            Surface(
                                color = TactileTheme.SurfaceHigh,
                                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                                modifier = Modifier.width(118.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        day,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TactileTheme.Muted,
                                    )
                                    Text(
                                        "$count",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TactileTheme.Text,
                                    )
                                    Text(
                                        "scheduled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TactileTheme.Muted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                color = TactileTheme.SurfaceLow,
                shape = RoundedCornerShape(TactileTheme.RadiusLg),
                border = BorderStroke(1.dp, TactileTheme.GhostBorder.copy(alpha = 0.15f)),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Resets & Cadence",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TactileTheme.Text,
                    )
                    CadenceRow(
                        title = "Daily startup",
                        detail = "${snapshot.todayLayer.size} items in today's horizon",
                        status = "Active",
                    )
                    CadenceRow(
                        title = "Weekly alignment",
                        detail = "${snapshot.weekLayer.size} items mapped this week",
                        status = "Live",
                    )
                    CadenceRow(
                        title = "Monthly reset",
                        detail = if (snapshot.monthlyResetDate.isNotBlank()) snapshot.monthlyResetDate else "Not set",
                        status = "Pending",
                    )
                    Button(onClick = { viewModel.runMonthlyReset() }) {
                        Text("Run Monthly Reset")
                    }
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                color = TactileTheme.SurfaceLow,
                shape = RoundedCornerShape(TactileTheme.RadiusLg),
                border = BorderStroke(1.dp, TactileTheme.GhostBorder.copy(alpha = 0.15f)),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Temporal Anchors",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TactileTheme.Text,
                        )
                        AssistChip(
                            onClick = { viewModel.addLifePeriodMarker("New period marker") },
                            label = { Text("Add Anchor") },
                        )
                    }
                    val anchorRows = snapshot.buildAnchorRows()
                    if (anchorRows.isEmpty()) {
                        Text(
                            "No anchor markers yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted,
                        )
                    } else {
                        anchorRows.take(6).forEach { anchor ->
                            AnchorRow(
                                title = anchor.title,
                                coordinate = anchor.coordinate,
                                level = anchor.level,
                            )
                        }
                    }
                }
            }
        }

        if (snapshot.projectPhases.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.SurfaceLow,
                shape = RoundedCornerShape(TactileTheme.RadiusLg),
                border = BorderStroke(1.dp, TactileTheme.GhostBorder.copy(alpha = 0.15f)),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Project Horizon Signals",
                        style = MaterialTheme.typography.titleLarge,
                        color = TactileTheme.Text,
                    )
                    snapshot.projectPhases.take(3).forEach { phase ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    phase.project.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TactileTheme.Text,
                                )
                                Text(
                                    phase.phaseLabel.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TactileTheme.Muted,
                                )
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = phase.isActivePhase,
                                    onClick = {
                                        viewModel.setProjectActivePhase(phase.project, true)
                                    },
                                    label = { Text("Active") },
                                )
                                FilterChip(
                                    selected = !phase.isActivePhase,
                                    onClick = {
                                        viewModel.setProjectActivePhase(phase.project, false)
                                    },
                                    label = { Text("Buffer") },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (horizonItems.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.SurfaceLow,
                shape = RoundedCornerShape(TactileTheme.RadiusLg),
                border = BorderStroke(1.dp, TactileTheme.GhostBorder.copy(alpha = 0.15f)),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Horizon Queue • ${selectedHorizon.label}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TactileTheme.Text,
                    )
                    horizonItems.take(6).forEach { item ->
                        HorizonQueueRow(
                            title = item.node.title,
                            onOpen = { onEditNode(item.node.id) },
                            onSet7Day = { viewModel.setTemporaryFocusPeriod(item.node, 7) },
                            onSet14Day = { viewModel.setTemporaryFocusPeriod(item.node, 14) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizonSwitcher(
    selected: TimeArchitectureHorizon,
    onSelected: (TimeArchitectureHorizon) -> Unit,
    snapshot: TimeArchitectureSnapshot,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TimeArchitectureHorizon.entries
            .filterNot { it == TimeArchitectureHorizon.ALL }
            .forEach { horizon ->
                FilterChip(
                    selected = selected == horizon,
                    onClick = { onSelected(horizon) },
                    label = { Text("${horizon.label} (${horizon.count(snapshot)})") },
                )
            }
        AssistChip(
            onClick = { onSelected(TimeArchitectureHorizon.ALL) },
            label = { Text("All Horizons") },
        )
    }
}

@Composable
private fun StructuralCard(
    title: String,
    label: String,
    modifier: Modifier = Modifier,
    tone: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        color = tone,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            content()
        }
    }
}

@Composable
private fun CadenceRow(
    title: String,
    detail: String,
    status: String,
) {
    Surface(
        color = TactileTheme.SurfaceHigh,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TactileTheme.Text)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
            }
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
        }
    }
}

@Composable
private fun AnchorRow(
    title: String,
    coordinate: String,
    level: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .padding(top = 6.dp)
                    .width(8.dp)
                    .height(8.dp)
                    .background(TactileTheme.Primary, shape = RoundedCornerShape(50)),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TactileTheme.Text)
            Text(coordinate, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
        }
        Text(
            level.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Primary,
        )
    }
}

@Composable
private fun HorizonQueueRow(
    title: String,
    onOpen: () -> Unit,
    onSet7Day: () -> Unit,
    onSet14Day: () -> Unit,
) {
    Surface(
        color = TactileTheme.SurfaceHigh,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TactileTheme.Text,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = onSet7Day, label = { Text("7d") })
                AssistChip(onClick = onSet14Day, label = { Text("14d") })
                AssistChip(onClick = onOpen, label = { Text("Open") })
            }
        }
    }
}

private enum class TimeArchitectureHorizon(val key: String?, val label: String) {
    TODAY("today", "Today"),
    WEEK("week", "Week"),
    MONTH("month", "Month"),
    SEMESTER("semester", "Semester"),
    ALL(null, "All"),
    ;

    fun count(snapshot: TimeArchitectureSnapshot): Int = items(snapshot).size

    fun items(snapshot: TimeArchitectureSnapshot): List<NodeWithPin> =
        when (this) {
            TODAY -> snapshot.todayLayer
            WEEK -> snapshot.weekLayer
            MONTH -> snapshot.monthLayer
            SEMESTER -> snapshot.semesterLayer
            ALL ->
                (
                    snapshot.todayLayer +
                        snapshot.weekLayer +
                        snapshot.monthLayer +
                        snapshot.semesterLayer
                ).distinctBy { it.node.id }
        }
}

private data class AnchorMarkerRow(
    val title: String,
    val coordinate: String,
    val level: String,
)

private fun TimeArchitectureSnapshot.buildAnchorRows(): List<AnchorMarkerRow> {
    val markerRows =
        lifePeriodMarkers.map { marker ->
            AnchorMarkerRow(
                title = marker.node.title,
                coordinate = marker.node.dueAt?.let(::formatProtocolTimestamp) ?: "No fixed date",
                level = "Anchor",
            )
        }
    val countdownRows =
        countdowns.map { countdown ->
            AnchorMarkerRow(
                title = countdown.node.node.title,
                coordinate = "In ${countdown.daysLeft}d",
                level = if (countdown.daysLeft <= 3) "Critical" else "Upcoming",
            )
        }
    return (countdownRows + markerRows).distinctBy { it.title + it.coordinate }
}

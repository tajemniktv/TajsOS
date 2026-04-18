/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.timearchitecture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.TimeArchitectureSnapshot
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.screens.formatProtocolTimestamp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.time_architecture_active_label
import tajsos.composeapp.generated.resources.time_architecture_active_window
import tajsos.composeapp.generated.resources.time_architecture_daily_detail
import tajsos.composeapp.generated.resources.time_architecture_daily_startup
import tajsos.composeapp.generated.resources.time_architecture_all_horizons
import tajsos.composeapp.generated.resources.time_architecture_current_horizon
import tajsos.composeapp.generated.resources.time_architecture_desc
import tajsos.composeapp.generated.resources.time_architecture_exam_pressure
import tajsos.composeapp.generated.resources.time_architecture_map_title
import tajsos.composeapp.generated.resources.time_architecture_monthly_detail
import tajsos.composeapp.generated.resources.time_architecture_monthly_reset
import tajsos.composeapp.generated.resources.time_architecture_no_items
import tajsos.composeapp.generated.resources.time_architecture_no_anchor_markers
import tajsos.composeapp.generated.resources.time_architecture_not_set
import tajsos.composeapp.generated.resources.time_architecture_reserve_label
import tajsos.composeapp.generated.resources.time_architecture_reserve_tracked
import tajsos.composeapp.generated.resources.time_architecture_reserve_window
import tajsos.composeapp.generated.resources.time_architecture_resets_cadence
import tajsos.composeapp.generated.resources.time_architecture_status_summary
import tajsos.composeapp.generated.resources.time_architecture_temporal_anchors
import tajsos.composeapp.generated.resources.time_architecture_title
import tajsos.composeapp.generated.resources.time_architecture_weekly_alignment
import tajsos.composeapp.generated.resources.time_architecture_weekly_detail
import tajsos.composeapp.generated.resources.time_architecture_add_anchor
import tajsos.composeapp.generated.resources.time_architecture_run_monthly_reset
import com.tajemniktv.tajsos.ui.components.common.EmptyState

object TimeArchitectureDashboardBlocks {
    private val renderers: Map<String, TimeArchitectureDashboardBlockRenderer> =
        mapOf(
            "time_header" to ::renderTimeHeader,
            "time_horizon_switcher" to ::renderTimeHorizonSwitcher,
            "time_map" to ::renderTimeMap,
            "time_cadence_anchors" to ::renderTimeCadenceAnchors,
            "time_project_phases" to ::renderTimeProjectPhases,
            "time_horizon_queue" to ::renderTimeHorizonQueue,
        )

    fun resolve(id: String): TimeArchitectureDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderTimeHeader(context: TimeArchitectureDashboardContext) {
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        Text(
            text = stringResource(Res.string.time_architecture_title),
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text,
        )
        Text(
            text = stringResource(Res.string.time_architecture_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun renderTimeHorizonSwitcher(context: TimeArchitectureDashboardContext) {
    val selected = context.selectedHorizon
    val snapshot = context.snapshot
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
                    onClick = { context.onHorizonSelected(horizon) },
                    label = { Text("${horizon.label} (${horizon.count(snapshot)})") },
                )
            }
        AssistChip(
            onClick = { context.onHorizonSelected(TimeArchitectureHorizon.ALL) },
            label = { Text(stringResource(Res.string.time_architecture_all_horizons)) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun renderTimeMap(context: TimeArchitectureDashboardContext) {
    val snapshot = context.snapshot
    val selectedHorizon = context.selectedHorizon
    val horizonItems = remember(selectedHorizon, snapshot) { selectedHorizon.items(snapshot) }
    val weeklyBuckets =
        remember(snapshot.weeklyMap) {
            if (snapshot.weeklyMap.isEmpty()) {
                emptyList()
            } else {
                snapshot.weeklyMap.entries
                    .toList()
                    .sortedBy { it.key }
            }
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.SurfaceLow,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
        border = BorderStroke(1.dp, TajsOSTheme.CardStroke.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(Res.string.time_architecture_map_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TajsOSTheme.Text,
                    )
                    Text(
                        stringResource(
                            Res.string.time_architecture_current_horizon,
                            selectedHorizon.label,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                }
                Text(
                    stringResource(
                        Res.string.time_architecture_status_summary,
                        snapshot.todayLayer.size.toString(),
                        snapshot.weekLayer.size.toString(),
                        snapshot.monthLayer.size.toString(),
                        snapshot.semesterLayer.size.toString(),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                StructuralCard(
                    title = stringResource(Res.string.time_architecture_active_window),
                    label =
                        stringResource(
                            Res.string.time_architecture_active_label,
                            horizonItems.size.toString(),
                            selectedHorizon.label.lowercase(),
                        ),
                    modifier = Modifier.weight(1f),
                    tone = TajsOSTheme.SurfaceHigh,
                ) {
                    if (horizonItems.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.time_architecture_no_items),
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Muted,
                        )
                    } else {
                        horizonItems.take(3).forEach { item ->
                            Text(
                                text = "• ${item.node.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TajsOSTheme.Text,
                            )
                        }
                    }
                }

                StructuralCard(
                    title = stringResource(Res.string.time_architecture_reserve_window),
                    label = stringResource(Res.string.time_architecture_reserve_label),
                    modifier = Modifier.weight(1f),
                    tone = TajsOSTheme.SurfaceHighest,
                ) {
                    val reserveCount = snapshot.longHorizonTasks.size + snapshot.semesterLayer.size
                    Text(
                        text =
                            stringResource(
                                Res.string.time_architecture_reserve_tracked,
                                reserveCount.toString(),
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Text,
                    )
                    if (snapshot.examPeriodMode) {
                        Text(
                            text = stringResource(Res.string.time_architecture_exam_pressure),
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Error,
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
                            color = TajsOSTheme.SurfaceHigh,
                            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                            modifier = Modifier.width(118.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TajsOSTheme.Muted,
                                )
                                Text(
                                    "$count",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TajsOSTheme.Text,
                                )
                                Text(
                                    "scheduled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TajsOSTheme.Muted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun renderTimeCadenceAnchors(context: TimeArchitectureDashboardContext) {
    val snapshot = context.snapshot
    val viewModel = context.viewModel
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        TimeArchitectureCadenceCard(
            modifier = Modifier.weight(1f),
            snapshot = snapshot,
            onRunMonthlyReset = { viewModel.runMonthlyReset() }
        )

        TimeArchitectureAnchorsCard(
            modifier = Modifier.weight(1f),
            snapshot = snapshot,
            onAddAnchor = { viewModel.addLifePeriodMarker("New period marker") }
        )
    }
}

@Composable
private fun TimeArchitectureCadenceCard(
    modifier: Modifier = Modifier,
    snapshot: TimeArchitectureSnapshot,
    onRunMonthlyReset: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = TajsOSTheme.SurfaceLow,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
        border = BorderStroke(1.dp, TajsOSTheme.CardStroke.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(Res.string.time_architecture_resets_cadence),
                style = MaterialTheme.typography.headlineSmall,
                color = TajsOSTheme.Text,
            )
            CadenceRow(
                title = stringResource(Res.string.time_architecture_daily_startup),
                detail = stringResource(Res.string.time_architecture_daily_detail, snapshot.todayLayer.size),
                status = "Active",
            )
            CadenceRow(
                title = stringResource(Res.string.time_architecture_weekly_alignment),
                detail = stringResource(Res.string.time_architecture_weekly_detail, snapshot.weekLayer.size),
                status = "Live",
            )
            CadenceRow(
                title = stringResource(Res.string.time_architecture_monthly_reset),
                detail =
                    stringResource(
                        Res.string.time_architecture_monthly_detail,
                        snapshot.monthlyResetDate.ifBlank {
                            stringResource(Res.string.time_architecture_not_set)
                        },
                    ),
                status = "Pending",
            )
            Button(onClick = onRunMonthlyReset) {
                Text(stringResource(Res.string.time_architecture_run_monthly_reset))
            }
        }
    }
}

@Composable
private fun TimeArchitectureAnchorsCard(
    modifier: Modifier = Modifier,
    snapshot: TimeArchitectureSnapshot,
    onAddAnchor: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = TajsOSTheme.SurfaceLow,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
        border = BorderStroke(1.dp, TajsOSTheme.CardStroke.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(Res.string.time_architecture_temporal_anchors),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TajsOSTheme.Text,
                )
                AssistChip(
                    onClick = onAddAnchor,
                    label = { Text(stringResource(Res.string.time_architecture_add_anchor)) },
                )
            }
            val anchorRows = snapshot.buildAnchorRows()
            if (anchorRows.isEmpty()) {
                EmptyState(
                    message = stringResource(Res.string.time_architecture_no_anchor_markers),
                    description = null,
                    fillParent = false,
                    showContainer = false,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun renderTimeProjectPhases(context: TimeArchitectureDashboardContext) {
    val snapshot = context.snapshot
    val viewModel = context.viewModel
    if (snapshot.projectPhases.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.SurfaceLow,
            shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
            border = BorderStroke(1.dp, TajsOSTheme.CardStroke.copy(alpha = 0.15f)),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "Project Horizon Signals",
                    style = MaterialTheme.typography.titleLarge,
                    color = TajsOSTheme.Text,
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
                                color = TajsOSTheme.Text,
                            )
                            Text(
                                phase.phaseLabel.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Muted,
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
}

@Composable
private fun renderTimeHorizonQueue(context: TimeArchitectureDashboardContext) {
    val snapshot = context.snapshot
    val selectedHorizon = context.selectedHorizon
    val horizonItems = remember(selectedHorizon, snapshot) { selectedHorizon.items(snapshot) }
    val viewModel = context.viewModel
    val onEditNode = context.onEditNode

    if (horizonItems.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.SurfaceLow,
            shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
            border = BorderStroke(1.dp, TajsOSTheme.CardStroke.copy(alpha = 0.15f)),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Horizon Queue • ${selectedHorizon.label}",
                    style = MaterialTheme.typography.titleLarge,
                    color = TajsOSTheme.Text,
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
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
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
        color = TajsOSTheme.SurfaceHigh,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TajsOSTheme.Text)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Muted)
            }
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
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
                    .background(TajsOSTheme.Primary, shape = RoundedCornerShape(50)),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TajsOSTheme.Text)
            Text(coordinate, style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Muted)
        }
        Text(
            level.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Primary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HorizonQueueRow(
    title: String,
    onOpen: () -> Unit,
    onSet7Day: () -> Unit,
    onSet14Day: () -> Unit,
) {
    Surface(
        color = TajsOSTheme.SurfaceHigh,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        modifier =
            Modifier
                .fillMaxWidth()
                .mouseClickable(
                    onClick = onOpen,
                    onSecondaryClick = onOpen,
                    middleClickFallbackToPrimary = true,
                ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TajsOSTheme.Text,
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


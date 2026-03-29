/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.openloops

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.OpenLoopsSnapshot
import com.tajemniktv.tajsos.ui.components.cards.OpenLoopCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.screens.OpenLoopView
import com.tajemniktv.tajsos.ui.screens.openLoopTypes
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.dash_open_loops
import tajsos.composeapp.generated.resources.lens_unresolved_archive_resolved
import tajsos.composeapp.generated.resources.lens_unresolved_decay_index
import tajsos.composeapp.generated.resources.lens_unresolved_empty
import tajsos.composeapp.generated.resources.lens_unresolved_group_area
import tajsos.composeapp.generated.resources.lens_unresolved_group_person
import tajsos.composeapp.generated.resources.lens_unresolved_group_urgency
import tajsos.composeapp.generated.resources.lens_unresolved_stats

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun OpenLoopsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                OpenLoopsDashboardSurface.DESKTOP
            } else {
                OpenLoopsDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildOpenLoopsDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onEditNode) {
                OpenLoopsDashboardContext(
                    viewModel,
                    onEditNode,
                )
            }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                OpenLoopsDashboardBlockRegistry
                    .resolve(
                        block.id,
                    )?.invoke(context)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun OpenLoopsLayer(
    viewModel: MainViewModel,
    snapshot: OpenLoopsSnapshot,
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
                stringResource(Res.string.dash_open_loops),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(
                    Res.string.lens_unresolved_stats,
                    snapshot.active.size,
                    snapshot.inbox.size,
                    snapshot.review.size,
                    snapshot.resolved.size,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                stringResource(Res.string.lens_unresolved_decay_index, snapshot.averageDecayScore),
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
                    Text(stringResource(Res.string.lens_unresolved_archive_resolved))
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
                label = { Text(stringResource(view.label)) },
            )
        }
    }

    if (loops.isEmpty()) {
        EmptyState(
            message =
                stringResource(
                    Res.string.lens_unresolved_empty,
                    stringResource(openLoopView.label).lowercase(),
                ),
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        items(loops, key = { it.node.node.id }) { loop ->
            OpenLoopCard(
                item = loop,
                areaName = allAreas.find { it.id == loop.node.node.areaId }?.title,
                openLoopTypes = openLoopTypes,
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
                    title = stringResource(Res.string.lens_unresolved_group_area),
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
                    title = stringResource(Res.string.lens_unresolved_group_person),
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
                    title = stringResource(Res.string.lens_unresolved_group_urgency),
                    items =
                        snapshot.byUrgency.entries.map { entry ->
                            "${entry.key.uppercase()} • ${entry.value.size}"
                        },
                )
            }
        }
    }
}

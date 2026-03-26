/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.nodes.TaskRow
import com.tajemniktv.tajsos.ui.components.today.TodayDashboardBlockRegistry
import com.tajemniktv.tajsos.ui.components.today.TodayDashboardContext
import com.tajemniktv.tajsos.ui.components.today.TodayDashboardSurface
import com.tajemniktv.tajsos.ui.components.today.buildTodayDashboardPlan
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Renders the "Today" screen showing up to three nodes scheduled for today and placeholder slots when fewer than three exist.
 *
 * Shows an empty state when there are no today nodes. For each shown node, the row supports swipe-to-complete (start-to-end) which marks the node done, toggling done status, unpinning, editing via click/long-click, and archiving through the provided callbacks; remaining slots are rendered as bordered placeholders labeled with their slot index.
 *
 * @param viewModel View model providing `todayNodes` and actions used to update node status, pinning, and archiving.
 * @param onEditNode Callback invoked with a node ID when the user requests to edit a node (click or long-click).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) TodayDashboardSurface.DESKTOP else TodayDashboardSurface.MOBILE
        val plan = remember(surface) { buildTodayDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { TodayDashboardContext(viewModel, onEditNode) }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                TodayDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodayMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val todayNodes by viewModel.todayNodes.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TactileTheme.SpacingMd),
    ) {
        Text(
            stringResource(Res.string.today_payload),
            style = MaterialTheme.typography.displayLarge,
            color = TactileTheme.Text,
        )
        Spacer(Modifier.height(TactileTheme.SpacingLg))

        if (todayNodes.isEmpty()) {
            EmptyState(message = stringResource(Res.string.today_empty))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(todayNodes.take(3), key = { it.id }) { node ->
                    val dismissState =
                        rememberSwipeToDismissBoxState(
                            initialValue = SwipeToDismissBoxValue.Settled,
                            positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold,
                        )

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                            viewModel.updateNodeStatus(node, "done")
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromEndToStart = false,
                        backgroundContent = {
                            val color =
                                if (dismissState.dismissDirection ==
                                    SwipeToDismissBoxValue.StartToEnd
                                ) {
                                    TactileTheme.Success
                                } else {
                                    Color.Transparent
                                }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color),
                            )
                        },
                    ) {
                        TaskRow(
                            node = node,
                            onToggleDone = { status: String ->
                                viewModel.updateNodeStatus(
                                    node,
                                    status,
                                )
                            },
                            onUnpin = { viewModel.togglePin(node, false) },
                            onClick = { onEditNode(node.id) },
                            onLongClick = { onEditNode(node.id) },
                            onArchive = { viewModel.archiveNode(node) },
                        )
                    }
                }
                if (todayNodes.size < 3) {
                    items(3 - todayNodes.size) { index ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(vertical = 4.dp)
                                .border(
                                    1.dp,
                                    TactileTheme.Border,
                                    RoundedCornerShape(TactileTheme.RadiusMd),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(
                                    Res.string.today_slot_standby,
                                    todayNodes.size + index + 1,
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = TactileTheme.Muted.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

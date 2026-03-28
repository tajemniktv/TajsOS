/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.today_empty
import tajsos.composeapp.generated.resources.today_payload
import tajsos.composeapp.generated.resources.today_slot_standby

object TodayDashboardBlockRegistry {
    private val renderers: Map<String, TodayDashboardBlockRenderer> =
        mapOf("today_main" to ::renderTodayMainBlock)

    fun resolve(id: String): TodayDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderTodayMainBlock(context: TodayDashboardContext) {
    TodayMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
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

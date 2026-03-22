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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.EmptyState
import com.tajemniktv.tajsos.ui.components.TaskRow
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: MainViewModel, onEditNode: (Long) -> Unit) {
    val todayNodes by viewModel.todayNodes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd)
    ) {
        Text(
            "TODAY'S PAYLOAD",
            style = MaterialTheme.typography.displayLarge,
            color = TactileTheme.Text
        )
        Spacer(Modifier.height(TactileTheme.SpacingLg))

        if (todayNodes.isEmpty()) {
            EmptyState(message = "NO PAYLOAD DETECTED // TODAY IS EMPTY")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(todayNodes.take(3), key = { it.id }) { node ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        initialValue = SwipeToDismissBoxValue.Settled,
                        positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
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
                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) TactileTheme.Success else Color.Transparent
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                            )
                        }
                    ) {
                        TaskRow(
                            node = node,
                            onToggleDone = { status: String ->
                                viewModel.updateNodeStatus(
                                    node,
                                    status
                                )
                            },
                            onUnpin = { viewModel.togglePin(node, false) },
                            onClick = { onEditNode(node.id) },
                            onLongClick = { onEditNode(node.id) },
                            onArchive = { viewModel.archiveNode(node) }
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
                                    RoundedCornerShape(TactileTheme.RadiusMd)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SLOT ${todayNodes.size + index + 1} // STANDBY",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = TactileTheme.Muted.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

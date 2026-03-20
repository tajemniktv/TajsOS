/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
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

/**
 * TodayScreen shows the prioritized "Payload" for the current day.
 * 
 * Phase 1 Implementation:
 * - Limited to top 3 slots to prevent overwhelm (ADHD-friendly design).
 * - Added Empty State for open schedules.
 * - Swipe-to-done gesture for fast interaction.
 * - Archive functionality for finished payload items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel) {
    val todayItems by viewModel.todayItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
    ) {
        Text(
            "TODAY'S PAYLOAD",
            style = MaterialTheme.typography.displayLarge,
            color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
        )
        Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))

        if (todayItems.isEmpty()) {
            _root_ide_package_.com.tajemniktv.tajsos.ui.components.EmptyState(message = "NO PAYLOAD DETECTED // TODAY IS EMPTY")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm)) {
                items(todayItems.take(3), key = { it.id }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        initialValue = SwipeToDismissBoxValue.Settled,
                        positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
                    )

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                            viewModel.updateItemStatus(item, "done")
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromEndToStart = false,
                        backgroundContent = {
                            val color =
                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Success else Color.Transparent
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                            )
                        }
                    ) {
                        _root_ide_package_.com.tajemniktv.tajsos.ui.components.TaskRow(
                            item = item,
                            onToggleDone = { status: String ->
                                viewModel.updateItemStatus(
                                    item,
                                    status
                                )
                            },
                            onUnpin = { viewModel.togglePin(item, false) },
                            onArchive = { viewModel.archiveItem(item) }
                        )
                    }
                }
                if (todayItems.size < 3) {
                    items(3 - todayItems.size) { index ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(vertical = 4.dp)
                                .border(
                                    1.dp,
                                    _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Border,
                                    RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SLOT ${todayItems.size + index + 1} // STANDBY",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

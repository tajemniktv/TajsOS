/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.main.state.EntryType
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.cal_agenda_title
import tajsos.composeapp.generated.resources.pending_nodes_due_soon
import kotlin.time.Clock
import kotlin.time.Instant
import com.tajemniktv.tajsos.ui.components.common.EmptyState

object CalendarDashboardBlocks {
    private val renderers: Map<String, CalendarDashboardBlockRenderer> =
        mapOf("calendar_main" to ::renderCalendarMainBlock)

    fun resolve(id: String): CalendarDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderCalendarMainBlock(context: CalendarDashboardContext) {
    CalendarMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

@Composable
internal fun CalendarMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    var currentMonth by remember {
        mutableStateOf(
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
        )
    }
    val calendarEntries by viewModel.calendarEntries.collectAsState()
    val activeNodes by viewModel.activeNodes.collectAsState()
    var selectedDate by remember { mutableStateOf(currentMonth) }
    val todayEpoch =
        remember(selectedDate) {
            selectedDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }
    val pendingNodes =
        remember(activeNodes, todayEpoch) {
            activeNodes
                .map { it.node }
                .filter { node ->
                    node.type == "task" &&
                        node.status == "active" &&
                        (node.dueAt?.let { it >= todayEpoch } == true)
                }.sortedBy { it.dueAt }
                .take(6)
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                TajsOSTheme.CalendarGradientStart,
                                TajsOSTheme.CalendarGradientMid,
                                TajsOSTheme.CalendarGradientEnd,
                            ),
                    ),
                ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd)) {
            if (maxWidth > 1100.dp) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                ) {
                    Surface(
                        modifier = Modifier.weight(1.75f).fillMaxHeight(),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        color = TajsOSTheme.CalendarPanel,
                        border =
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TajsOSTheme.GhostBorder.copy(alpha = 0.15f),
                            ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                        ) {
                            CalendarHeader(
                                currentMonth = currentMonth,
                                onPreviousMonth = {
                                    currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH)
                                },
                                onNextMonth = {
                                    currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
                                },
                                onTodayClick = {
                                    val today =
                                        Clock.System
                                            .now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                            .date
                                    currentMonth = today
                                    selectedDate = today
                                },
                                onSyncClick = { viewModel.syncCalendars() },
                            )
                            MonthView(
                                currentMonth = currentMonth,
                                selectedDate = selectedDate,
                                entries = calendarEntries,
                                onDateSelected = { selectedDate = it },
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        color = TajsOSTheme.CalendarPanelSoft,
                        border =
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TajsOSTheme.GhostBorder.copy(alpha = 0.15f),
                            ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = TajsOSTheme.Primary,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(
                                        Res.string.cal_agenda_title,
                                        selectedDate.toString(),
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TajsOSTheme.Primary,
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AgendaView(
                                    selectedDate = selectedDate,
                                    entries = calendarEntries,
                                    onEntryClick = { entry ->
                                        if (entry.type == EntryType.INTERNAL) {
                                            entry.originalId?.let { onEditNode(it) }
                                        }
                                    },
                                )
                            }

                            HorizontalDivider(color = TajsOSTheme.Border.copy(alpha = 0.6f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = TajsOSTheme.Accent,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "PENDING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TajsOSTheme.Accent,
                                )
                            }
                            if (pendingNodes.isEmpty()) {
                                EmptyState(
                                    message = stringResource(Res.string.pending_nodes_due_soon),
                                    description = null,
                                    fillParent = false,
                                    showContainer = false,
                                )
                            } else {
                                pendingNodes.forEach { node ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape =
                                            androidx.compose.foundation.shape.RoundedCornerShape(
                                                12.dp,
                                            ),
                                        color = TajsOSTheme.CalendarPanelStrong,
                                        border =
                                            androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                TajsOSTheme.GhostBorder.copy(alpha = 0.15f),
                                            ),
                                    ) {
                                        Row(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                node.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TajsOSTheme.Text,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                            )
                                            val dueText =
                                                node.dueAt?.let {
                                                    Instant
                                                        .fromEpochMilliseconds(it)
                                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                                        .date
                                                        .toString()
                                                } ?: "--"
                                            Text(
                                                dueText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TajsOSTheme.Muted,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        color = TajsOSTheme.CalendarPanel,
                        border =
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TajsOSTheme.GhostBorder.copy(alpha = 0.15f),
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                        ) {
                            CalendarHeader(
                                currentMonth = currentMonth,
                                onPreviousMonth = {
                                    currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH)
                                },
                                onNextMonth = {
                                    currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
                                },
                                onTodayClick = {
                                    val today =
                                        Clock.System
                                            .now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                            .date
                                    currentMonth = today
                                    selectedDate = today
                                },
                                onSyncClick = { viewModel.syncCalendars() },
                            )
                            MonthView(
                                currentMonth = currentMonth,
                                selectedDate = selectedDate,
                                entries = calendarEntries,
                                onDateSelected = { selectedDate = it },
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        color = TajsOSTheme.CalendarPanelSoft,
                        border =
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TajsOSTheme.GhostBorder.copy(alpha = 0.15f),
                            ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                        ) {
                            Text(
                                stringResource(
                                    Res.string.cal_agenda_title,
                                    selectedDate.toString(),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Primary,
                            )
                            AgendaView(
                                selectedDate = selectedDate,
                                entries = calendarEntries,
                                onEntryClick = { entry ->
                                    if (entry.type == EntryType.INTERNAL) {
                                        entry.originalId?.let { onEditNode(it) }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

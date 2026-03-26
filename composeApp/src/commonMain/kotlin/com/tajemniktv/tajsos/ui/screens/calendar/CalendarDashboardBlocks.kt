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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.EntryType
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.cal_agenda_title
import kotlin.time.Clock
import kotlin.time.Instant

object CalendarDashboardBlockRegistry {
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
                        colors = listOf(Color(0xFF090A12), Color(0xFF0D1021), Color(0xFF090A12)),
                    ),
                ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd)) {
            if (maxWidth > 1100.dp) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                ) {
                    Surface(
                        modifier = Modifier.weight(1.75f).fillMaxHeight(),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        color = Color(0x26172035),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334D5C8A)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
                        color = Color(0x1F172035),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334D5C8A)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = TactileTheme.Primary,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(
                                        Res.string.cal_agenda_title,
                                        selectedDate.toString(),
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TactileTheme.Primary,
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

                            HorizontalDivider(color = TactileTheme.Border.copy(alpha = 0.6f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = TactileTheme.Accent,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "PENDING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TactileTheme.Accent,
                                )
                            }
                            if (pendingNodes.isEmpty()) {
                                Text(
                                    "No pending nodes due soon.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TactileTheme.Muted,
                                )
                            } else {
                                pendingNodes.forEach { node ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape =
                                            androidx.compose.foundation.shape.RoundedCornerShape(
                                                12.dp,
                                            ),
                                        color = Color(0x261B223B),
                                        border =
                                            androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                TactileTheme.Border.copy(alpha = 0.7f),
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
                                                color = TactileTheme.Text,
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
                                                color = TactileTheme.Muted,
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
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        color = Color(0x26172035),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334D5C8A)),
                    ) {
                        Column(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        color = Color(0x1F172035),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334D5C8A)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        ) {
                            Text(
                                stringResource(
                                    Res.string.cal_agenda_title,
                                    selectedDate.toString(),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Primary,
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

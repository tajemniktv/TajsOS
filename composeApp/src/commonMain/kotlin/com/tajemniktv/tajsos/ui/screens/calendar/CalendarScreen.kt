/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.main.state.CalendarEntry
import com.tajemniktv.tajsos.ui.main.state.EntryType
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.cal_all_day
import tajsos.composeapp.generated.resources.cal_next
import tajsos.composeapp.generated.resources.cal_no_events
import tajsos.composeapp.generated.resources.cal_previous
import tajsos.composeapp.generated.resources.cal_sync
import tajsos.composeapp.generated.resources.cal_today
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Composes the calendar screen UI: a header with month navigation and sync, a month grid, and an agenda for the selected day.
 *
 * The composable maintains local state for the currently displayed month and the currently selected date, collects calendar entries
 * from the provided view model, and wires interactions:
 * - navigating months and jumping to today update the displayed month and selection,
 * - sync triggers `viewModel.syncCalendars()`,
 * - selecting a date updates the agenda,
 * - tapping an agenda entry invokes `onEditNode` only when the entry is `EntryType.INTERNAL` and has a non-null `originalId`.
 *
 * @param onEditNode Callback invoked with an entry's `originalId` when an editable internal entry is selected.
 */
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                CalendarDashboardSurface.DESKTOP
            } else {
                CalendarDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildCalendarDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onEditNode) {
                CalendarDashboardContext(
                    viewModel,
                    onEditNode,
                )
            }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                CalendarDashboardBlockRegistry
                    .resolve(
                        block.id,
                    )?.invoke(context)
            }
        }
    }
}

/**
 * Header row displaying the current month and controls for "Today", sync, and month navigation.
 *
 * @param currentMonth The month (LocalDate) shown in the header; its month name and year are displayed.
 * @param onPreviousMonth Callback invoked when the previous-month button is pressed.
 * @param onNextMonth Callback invoked when the next-month button is pressed.
 * @param onTodayClick Callback invoked when the "Today" button is pressed.
 * @param onSyncClick Callback invoked when the sync (refresh) button is pressed.
 */
@Composable
fun CalendarHeader(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onSyncClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                "${currentMonth.month.name} ${currentMonth.year}",
                style = MaterialTheme.typography.headlineMedium,
                color = TactileTheme.CalendarHeaderText,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onTodayClick) {
                Text(
                    stringResource(Res.string.cal_today),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
            }
            IconButton(onClick = onSyncClick) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.cal_sync),
                    tint = TactileTheme.Muted,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = stringResource(Res.string.cal_previous),
                )
            }
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = stringResource(Res.string.cal_next),
                )
            }
        }
    }
}

/**
 * Displays a month calendar grid for the given month with weekday headers, selectable day cells, and visual indicators for entries.
 *
 * The grid shows six weeks (7 columns) with days for the specified month placed in their weekday positions. The cell for `selectedDate` and the current system date are visually highlighted. Days that have one or more `entries` show up to three accent dots, with an additional smaller dot when there are more than three entries. Tapping a day invokes `onDateSelected`.
 *
 * @param currentMonth The month (year and month fields are used) to display in the grid.
 * @param selectedDate The date that should be shown as selected/highlighted.
 * @param entries A list of calendar entries; entries whose start time falls on a particular day will produce indicators on that day's cell.
 * @param onDateSelected Callback invoked with the tapped `LocalDate`.
 */
@Composable
fun MonthView(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    entries: List<CalendarEntry>,
    onDateSelected: (LocalDate) -> Unit,
) {
    val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
    val lastDayOfMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    val daysInMonth = lastDayOfMonth.day
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal // 0 = Mon, 6 = Sun

    val days =
        (0 until 42).map { i ->
            val dayNumber = i - firstDayOfWeek + 1
            if (dayNumber in 1..daysInMonth) {
                LocalDate(currentMonth.year, currentMonth.month, dayNumber)
            } else {
                null
            }
        }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
    ) {
        val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
        items(weekDays) { day ->
            Text(
                day,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
            )
        }

        items(days) { date ->
            if (date != null) {
                val isSelected = date == selectedDate
                val isToday =
                    date ==
                        Clock.System
                            .now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                val dayEntries =
                    entries.filter {
                        Instant
                            .fromEpochMilliseconds(it.startAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date == date
                    }

                Box(
                    modifier =
                        Modifier
                            .aspectRatio(1f)
                            .padding(3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) {
                                    TactileTheme.CalendarSelectedDay
                                } else if (isToday) {
                                    TactileTheme.CalendarTodayDay
                                } else {
                                    TactileTheme.CalendarIdleDay
                                },
                            ).clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            date.day.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color =
                                if (isSelected) {
                                    TactileTheme.CalendarSelectedText
                                } else if (isToday) {
                                    TactileTheme.Accent
                                } else {
                                    TactileTheme.Text
                                },
                        )
                        if (dayEntries.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(dayEntries.size.coerceAtMost(3)) {
                                    Box(
                                        Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(TactileTheme.Accent),
                                    )
                                }
                                if (dayEntries.size > 3) {
                                    Box(
                                        Modifier
                                            .size(2.dp)
                                            .clip(CircleShape)
                                            .background(TactileTheme.Accent),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(Modifier.aspectRatio(1f))
            }
        }
    }
}

/**
 * Display an agenda for the specified date.
 *
 * Shows a centered "no events" message when there are no entries for `selectedDate`; otherwise
 * renders a vertically spaced list of the entries for that date. Each row invokes `onEntryClick`
 * when selected.
 *
 * @param selectedDate The date whose agenda should be shown.
 * @param entries A list of calendar entries to filter and display.
 * @param onEntryClick Callback invoked with the tapped entry.
 */
@Composable
fun AgendaView(
    selectedDate: LocalDate,
    entries: List<CalendarEntry>,
    onEntryClick: (CalendarEntry) -> Unit,
) {
    val dayEntries =
        entries.filter {
            Instant
                .fromEpochMilliseconds(it.startAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date == selectedDate
        }

    if (dayEntries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.cal_no_events),
                color = TactileTheme.Muted,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            items(dayEntries) { entry ->
                AgendaRow(entry, onClick = { onEntryClick(entry) })
            }
        }
    }
}

/**
 * Renders a clickable agenda row for a calendar entry showing its time, title, optional description,
 * and an indicator for external entries.
 *
 * @param entry The calendar entry whose data (start time, title, description, type) is displayed.
 * @param onClick Callback invoked when the row is tapped.
 */
@Composable
fun AgendaRow(
    entry: CalendarEntry,
    onClick: () -> Unit,
) {
    val startTime =
        Instant
            .fromEpochMilliseconds(entry.startAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr =
        if (entry.isAllDay) {
            stringResource(Res.string.cal_all_day)
        } else {
            "${startTime.hour}:${
                startTime.minute.toString().padStart(2, '0')
            }"
        }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = TactileTheme.CalendarPanelStrong,
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.GhostBorder.copy(alpha = 0.15f),
            ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.width(60.dp),
            )
            VerticalDivider(
                modifier = Modifier.height(24.dp).padding(horizontal = 8.dp),
                color = TactileTheme.Muted,
            )
            Column {
                Text(
                    entry.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TactileTheme.Text,
                )
                val description = entry.description
                if (!description.isNullOrBlank()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (entry.type == EntryType.EXTERNAL) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TactileTheme.Primary),
                )
            }
        }
    }
}

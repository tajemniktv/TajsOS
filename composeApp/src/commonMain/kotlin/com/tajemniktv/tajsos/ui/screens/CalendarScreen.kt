/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.EntryType
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
)
{
    var currentMonth by remember {
        mutableStateOf(
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
        )
    }
    val calendarEntries by viewModel.calendarEntries.collectAsState()
    var selectedDate by remember { mutableStateOf(currentMonth) }

    Column(
        modifier =
                Modifier
                    .fillMaxSize()
                    .padding(TactileTheme.SpacingMd),
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

        Spacer(Modifier.height(TactileTheme.SpacingLg))

        Text(
            stringResource(Res.string.cal_agenda_title, selectedDate.toString()),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
        )

        Spacer(Modifier.height(TactileTheme.SpacingSm))

        AgendaView(
            selectedDate = selectedDate,
            entries = calendarEntries,
            onEntryClick = { entry ->
                if (entry.type == EntryType.INTERNAL)
                {
                    entry.originalId?.let { onEditNode(it) }
                }
            },
        )
    }
}

@Composable
fun CalendarHeader(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onSyncClick: () -> Unit,
)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                "${currentMonth.month.name} ${currentMonth.year}",
                style = MaterialTheme.typography.headlineMedium,
                color = TactileTheme.Primary,
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

@Composable
fun MonthView(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    entries: List<com.tajemniktv.tajsos.ui.CalendarEntry>,
    onDateSelected: (LocalDate) -> Unit,
)
{
    val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
    val lastDayOfMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    val daysInMonth = lastDayOfMonth.day
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal // 0 = Mon, 6 = Sun

    val days =
            (0 until 42).map { i ->
                val dayNumber = i - firstDayOfWeek + 1
                if (dayNumber in 1..daysInMonth)
                {
                    LocalDate(currentMonth.year, currentMonth.month, dayNumber)
                } else
                {
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
            if (date != null)
            {
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
                                .padding(2.dp)
                                .clip(RoundedCornerShape(TactileTheme.RadiusSm))
                                .background(
                                    if (isSelected)
                                    {
                                        TactileTheme.Primary.copy(alpha = 0.2f)
                                    } else if (isToday)
                                    {
                                        TactileTheme.Muted.copy(alpha = 0.1f)
                                    } else
                                    {
                                        Color.Transparent
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
                                    if (isSelected)
                                    {
                                        TactileTheme.Primary
                                    } else if (isToday)
                                    {
                                        TactileTheme.Accent
                                    } else
                                    {
                                        TactileTheme.Text
                                    },
                        )
                        if (dayEntries.isNotEmpty())
                        {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(dayEntries.size.coerceAtMost(3)) {
                                    Box(
                                        Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(TactileTheme.Accent),
                                    )
                                }
                                if (dayEntries.size > 3)
                                {
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
            } else
            {
                Box(Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
fun AgendaView(
    selectedDate: LocalDate,
    entries: List<com.tajemniktv.tajsos.ui.CalendarEntry>,
    onEntryClick: (com.tajemniktv.tajsos.ui.CalendarEntry) -> Unit,
)
{
    val dayEntries =
            entries.filter {
                Instant
                    .fromEpochMilliseconds(it.startAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date == selectedDate
            }

    if (dayEntries.isEmpty())
    {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.cal_no_events),
                color = TactileTheme.Muted,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    } else
    {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            items(dayEntries) { entry ->
                AgendaRow(entry, onClick = { onEntryClick(entry) })
            }
        }
    }
}

@Composable
fun AgendaRow(
    entry: com.tajemniktv.tajsos.ui.CalendarEntry,
    onClick: () -> Unit,
)
{
    val startTime =
            Instant
                .fromEpochMilliseconds(entry.startAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr =
            if (entry.isAllDay)
            {
                stringResource(Res.string.cal_all_day)
            } else
            {
                "${startTime.hour}:${
                    startTime.minute.toString().padStart(2, '0')
                }"
            }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        color = TactileTheme.Surface,
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
                if (!description.isNullOrBlank())
                {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (entry.type == EntryType.EXTERNAL)
            {
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

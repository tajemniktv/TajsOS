/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.ModuleButton
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val trackEntries by viewModel.trackEntries.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val inboxNodes by viewModel.inboxNodes.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val calendarEntries by viewModel.calendarEntries.collectAsState()
    val dashboardState by viewModel.dashboardUIState.collectAsState()

    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
    val moodToday = trackEntries.find { it.date == today }
    val tasksCount = dashboardState.tasksCount
    val notesCount = dashboardState.notesCount
    val pinnedKnowledge = dashboardState.pinnedKnowledge
    val upcomingDeadlines = dashboardState.upcomingDeadlines
    val overdueNodes = dashboardState.overdueNodes
    val relevantNote = dashboardState.relevantNote
    val readLaterVault = dashboardState.readLaterVault
    val quoteVault = dashboardState.quoteVault
    val ideaIncubator = dashboardState.ideaIncubator

    val allReviews by viewModel.allReviews.collectAsState()
    val lastWeeklyReview = allReviews.find { it.type == "weekly" }

    val insights by viewModel.insights.collectAsState()

    val scrollState = rememberScrollState()

    val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val vibeString = when (currentHour) {
        in 5..11 -> Res.string.dash_vibe_morning
        in 12..17 -> Res.string.dash_vibe_afternoon
        in 18..22 -> Res.string.dash_vibe_evening
        else -> Res.string.dash_vibe_night
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(Res.string.dash_command),
                style = MaterialTheme.typography.displayMedium,
                color = TactileTheme.Text,
            )
            Text(
                text = stringResource(vibeString),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 0. Search Bar (Search-first approach)
        OutlinedTextField(
            value = "",
            onValueChange = {
                viewModel.updateSearchQuery(it)
                onNavigateTo(Screen.Search)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    stringResource(Res.string.dash_search_placeholder),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = TactileTheme.Primary
                )
            },
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = TactileTheme.Border,
                focusedBorderColor = TactileTheme.Primary,
                unfocusedContainerColor = TactileTheme.Surface,
                focusedContainerColor = TactileTheme.Surface
            )
        )

        // 0.1 Time-based Reset Card
        if (currentHour in 5..11) {
            Surface(
                onClick = { onNavigateTo(Screen.Review) },
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Primary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Primary.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = TactileTheme.Primary
                    )
                    Spacer(Modifier.width(TactileTheme.SpacingMd))
                    Column {
                        Text(
                            stringResource(Res.string.dash_morning_reset),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Primary
                        )
                        Text(
                            stringResource(Res.string.dash_morning_reset_desc),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else if (currentHour in 18..23) {
            Surface(
                onClick = { onNavigateTo(Screen.Review) },
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Accent.copy(alpha = 0.05f),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Accent.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Brightness3,
                        contentDescription = null,
                        tint = TactileTheme.Accent
                    )
                    Spacer(Modifier.width(TactileTheme.SpacingMd))
                    Column {
                        Text(
                            stringResource(Res.string.dash_evening_shutdown),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Accent
                        )
                        Text(
                            stringResource(Res.string.dash_evening_shutdown_desc),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 0.2 Quick Links
        if (allAreas.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_quick_links),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
                ) {
                    allAreas.take(5).forEach { area ->
                        AssistChip(
                            onClick = {
                                onNavigateTo(Screen.Search); viewModel.updateSearchAreaFilter(
                                area.id
                            )
                            },
                            label = {
                                Text(
                                    area.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(TactileTheme.RadiusSm)
                        )
                    }
                }
            }
        }

        // 1. Life Summary
        val now = Clock.System.now().toEpochMilliseconds()
        val weekMillis = 7 * 24 * 60 * 60 * 1000L
        val needsWeeklyReview =
            lastWeeklyReview == null || (now - lastWeeklyReview.completedAt) > weekMillis

        if (needsWeeklyReview) {
            Surface(
                onClick = { onNavigateTo(Screen.Review) },
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Primary.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EventRepeat,
                        contentDescription = null,
                        tint = TactileTheme.Primary
                    )
                    Spacer(Modifier.width(TactileTheme.SpacingMd))
                    Column {
                        Text(
                            stringResource(Res.string.dash_review_pending),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Primary
                        )
                        Text(
                            stringResource(Res.string.dash_review_pending_desc),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // State-Aware Suggestions
        if (moodToday != null) {
            val suggestions =
                mutableListOf<Pair<org.jetbrains.compose.resources.StringResource, () -> Unit>>()
            if ((moodToday.anxietyScore ?: 0) >= 4) {
                suggestions.add(Res.string.dash_suggestion_stress to { onNavigateTo(Screen.Review) })
            }
            if ((moodToday.focusScore ?: 5) <= 2) {
                suggestions.add(Res.string.dash_suggestion_low_focus to {
                    viewModel.clearSearchFilters()
                    viewModel.updateSearchMaxMinutesFilter(5)
                    onNavigateTo(Screen.Search)
                })
            }
            if (!moodToday.tookMeds && (Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).hour >= 10)
            ) {
                suggestions.add(Res.string.dash_suggestion_meds to { onNavigateTo(Screen.Track) })
            }

            if (suggestions.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Accent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TactileTheme.Accent.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                        Text(
                            stringResource(Res.string.dash_suggestions_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Accent
                        )
                        suggestions.forEach { (textRes, action) ->
                            TextButton(onClick = action, contentPadding = PaddingValues(0.dp)) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = TactileTheme.Accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(textRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TactileTheme.Text
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TactileTheme.Surface,
            shape = RoundedCornerShape(TactileTheme.RadiusLg),
            border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border),
        ) {
            Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
                Text(
                    text = stringResource(Res.string.dash_life_summary),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = TactileTheme.Primary,
                )
                Spacer(Modifier.height(TactileTheme.SpacingMd))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${insights.weeklyCaptures}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TactileTheme.Text
                        )
                        Text(
                            text = stringResource(Res.string.dash_captures_week),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${insights.weeklyCompletions}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TactileTheme.Success
                        )
                        Text(
                            text = stringResource(Res.string.dash_done_week),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted
                        )
                    }
                }
                Spacer(Modifier.height(TactileTheme.SpacingMd))
                LinearProgressIndicator(
                    progress = {
                        if (insights.weeklyCaptures > 0)
                            (insights.weeklyCompletions.toFloat() / insights.weeklyCaptures.toFloat()).coerceIn(
                                0f,
                                1f
                            )
                        else 0f
                    },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = TactileTheme.Primary,
                    trackColor = TactileTheme.Border,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // 2. Recovery Mode
        val lowEnergyTasks = dashboardState.lowEnergyTasks

        // 2.1 State-Aware Shortcuts
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            Text(
                text = stringResource(Res.string.dash_state_actions),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
            ) {
                Surface(
                    onClick = {
                        viewModel.clearSearchFilters()
                        viewModel.updateSearchFrictionFilter("easy")
                        viewModel.updateSearchEnergyFilter(1)
                        onNavigateTo(Screen.Search)
                    },
                    modifier = Modifier.weight(1f),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border)
                ) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = TactileTheme.Success
                        )
                        Text(
                            stringResource(Res.string.dash_overwhelmed),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            stringResource(Res.string.dash_overwhelmed_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted
                        )
                    }
                }
                Surface(
                    onClick = {
                        viewModel.clearSearchFilters()
                        viewModel.updateSearchEnergyFilter(1)
                        onNavigateTo(Screen.Search)
                    },
                    modifier = Modifier.weight(1f),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border)
                ) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = TactileTheme.Primary
                        )
                        Text(
                            stringResource(Res.string.dash_cannot_think),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            stringResource(Res.string.dash_cannot_think_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted
                        )
                    }
                }
                Surface(
                    onClick = {
                        viewModel.clearSearchFilters()
                        viewModel.updateSearchMaxMinutesFilter(10)
                        onNavigateTo(Screen.Search)
                    },
                    modifier = Modifier.weight(1f),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border)
                ) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = TactileTheme.Accent
                        )
                        Text(
                            stringResource(Res.string.dash_10_minutes),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            stringResource(Res.string.dash_10_minutes_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted
                        )
                    }
                }
            }
        }

        if (moodToday?.energyScore != null && moodToday.energyScore!! <= 2 && lowEnergyTasks.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Success.copy(alpha = 0.1f),
                shape = RoundedCornerShape(TactileTheme.RadiusLg),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Success.copy(alpha = 0.3f)
                ),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
                    Text(
                        text = stringResource(Res.string.dash_recovery_mode),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        color = TactileTheme.Success,
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingMd))
                    Text(
                        text = stringResource(Res.string.dash_recovery_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingMd))
                    lowEnergyTasks.take(2).forEach { nodeWithPin ->
                        Surface(
                            onClick = { onEditNode(nodeWithPin.node.id) },
                            color = TactileTheme.Surface,
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(TactileTheme.SpacingMd),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = TactileTheme.Success,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    nodeWithPin.node.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Operating / Next Context
        if (activeSession != null || todayNodes.isNotEmpty() || allSessions.isNotEmpty()) {
            val activeItem =
                activeSession?.let { session ->
                    todayNodes.find { it.id == session.nodeId }
                        ?: allNodes
                            .find { it.node.id == session.nodeId }
                            ?.node
                } ?: todayNodes.firstOrNull() ?: allSessions.firstOrNull()?.let { session ->
                    allNodes
                        .find { it.node.id == session.nodeId }
                        ?.node
                }

            activeItem?.let { item ->
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (activeSession != null) {
                                        onNavigateTo(Screen.Focus)
                                    } else {
                                        viewModel.startFocusSession(item.id)
                                    }
                                },
                                onLongClick = { onEditNode(item.id) },
                            ),
                    color = if (activeSession != null) TactileTheme.Primary.copy(alpha = 0.15f) else TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusLg),
                    border =
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (activeSession != null) TactileTheme.Primary else TactileTheme.Border,
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(TactileTheme.SpacingLg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeSession != null) stringResource(Res.string.dash_operating) else stringResource(
                                    Res.string.dash_next_context
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                                color = if (activeSession != null) TactileTheme.Accent else TactileTheme.Primary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.title.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = TactileTheme.Text,
                            )
                        }

                        Button(
                            onClick = { if (activeSession != null) onNavigateTo(Screen.Focus) else viewModel.startFocusSession(item.id) },
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = if (activeSession != null) TactileTheme.Primary else TactileTheme.Surface,
                                    contentColor = if (activeSession != null) TactileTheme.Background else TactileTheme.Primary,
                                ),
                            border = if (activeSession == null) androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TactileTheme.Primary
                            ) else null,
                        ) {
                            Text(
                                if (activeSession != null) stringResource(Res.string.dash_view) else stringResource(
                                    Res.string.dash_engage
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Inbox Warning
        if (inboxNodes.isNotEmpty()) {
            Surface(
                onClick = { onNavigateTo(Screen.Inbox) },
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (inboxNodes.size > 10) TactileTheme.Error.copy(alpha = 0.5f) else TactileTheme.Accent.copy(
                        alpha = 0.5f
                    )
                ),
            ) {
                Row(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (inboxNodes.size > 10) Icons.Default.Warning else Icons.Default.MailOutline,
                        contentDescription = null,
                        tint = if (inboxNodes.size > 10) TactileTheme.Error else TactileTheme.Accent
                    )
                    Spacer(modifier = Modifier.width(TactileTheme.SpacingMd))
                    Text(
                        text = if (inboxNodes.size > 10) stringResource(
                            Res.string.dash_inbox_overflow,
                            inboxNodes.size
                        ) else stringResource(Res.string.dash_inbox_new, inboxNodes.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (inboxNodes.size > 10) TactileTheme.Error else TactileTheme.Accent,
                    )
                }
            }
        }

        // 5. Batch Suggestion
        val batchableTasks = dashboardState.batchableTasks
        if (batchableTasks.isNotEmpty()) {
            val firstBatch = batchableTasks.values.first()
            val areaName =
                allAreas.find { it.id == firstBatch.first().node.areaId }?.title ?: stringResource(
                    Res.string.screen_area
                ).uppercase()
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_batch_suggestion, areaName),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Accent.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TactileTheme.Accent.copy(alpha = 0.2f)
                    ),
                ) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                        Text(
                            stringResource(Res.string.dash_batch_desc, firstBatch.size, areaName),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(4.dp))
                        firstBatch.take(3).forEach {
                            Text(
                                "• ${it.node.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted
                            )
                        }
                    }
                }
            }
        }

        // 6. Quick Wins
        val quickWins = dashboardState.quickWins
        if (quickWins.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_quick_wins),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Success,
                )
                quickWins.take(2).forEach { nodeWithPin ->
                    Surface(
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Success.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = TactileTheme.Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 7. Deep Work
        val deepWork = dashboardState.deepWork
        if (deepWork.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_deep_work),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                deepWork.take(2).forEach { nodeWithPin ->
                    Surface(
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Primary.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = TactileTheme.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 8. Top 3 For Today
        if (todayNodes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_top_3),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                todayNodes.take(3).forEach { node ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEditNode(node.id) },
                            onLongClick = { onEditNode(node.id) },
                        ),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Muted.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = false,
                                onCheckedChange = { viewModel.updateNodeStatus(node, "done") },
                                colors = CheckboxDefaults.colors(
                                    uncheckedColor = TactileTheme.Primary,
                                    checkmarkColor = TactileTheme.Background
                                )
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingSm))
                            Text(
                                node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 9. Overdue
        if (overdueNodes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_overdue),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error
                )
                overdueNodes.take(3).forEach { nodeWithPin ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEditNode(nodeWithPin.node.id) },
                            onLongClick = { onEditNode(nodeWithPin.node.id) },
                        ),
                        color = TactileTheme.Error.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Error.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = TactileTheme.Error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 10. Active Reminders
        if (activeReminders.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_active_reminders),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error
                )
                activeReminders.forEach { node ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEditNode(node.id) },
                            onLongClick = { onEditNode(node.id) },
                        ),
                        color = TactileTheme.Error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Error.copy(alpha = 0.3f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = TactileTheme.Error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.updateNode(node.copy(reminderAt = null)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(Res.string.dash_dismiss),
                                    tint = TactileTheme.Error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 11. Pinned Knowledge
        if (pinnedKnowledge.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_pinned_knowledge),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                pinnedKnowledge.take(2).forEach { nodeWithPin ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = { onEditNode(nodeWithPin.node.id) },
                                onLongClick = { onEditNode(nodeWithPin.node.id) },
                            ),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Muted.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = TactileTheme.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                text = nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text
                            )
                        }
                    }
                }
            }
        }

        // 11.1 Vaults (Read Later, Quotes, Ideas)
        if (readLaterVault.isNotEmpty() || quoteVault.isNotEmpty() || ideaIncubator.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_knowledge_vaults),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
                ) {
                    if (readLaterVault.isNotEmpty()) {
                        Surface(
                            onClick = {
                                viewModel.updateSearchQuery("")
                                viewModel.updateSearchTypeFilter("note")
                                viewModel.updateSearchStatusFilter("active")
                                onNavigateTo(Screen.Search)
                            },
                            modifier = Modifier.weight(1f),
                            color = TactileTheme.Surface,
                            shape = RoundedCornerShape(TactileTheme.RadiusMd),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TactileTheme.Border
                            )
                        ) {
                            Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                                Icon(
                                    Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = TactileTheme.Accent
                                )
                                Text(
                                    stringResource(Res.string.dash_read_later),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    stringResource(
                                        Res.string.dash_items_count,
                                        readLaterVault.size
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TactileTheme.Muted
                                )
                            }
                        }
                    }
                    if (quoteVault.isNotEmpty()) {
                        Surface(
                            onClick = { onNavigateTo(Screen.Search) },
                            modifier = Modifier.weight(1f),
                            color = TactileTheme.Surface,
                            shape = RoundedCornerShape(TactileTheme.RadiusMd),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TactileTheme.Border
                            )
                        ) {
                            Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                                Icon(
                                    Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = TactileTheme.Primary
                                )
                                Text(
                                    stringResource(Res.string.dash_quotes),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    stringResource(Res.string.dash_items_count, quoteVault.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TactileTheme.Muted
                                )
                            }
                        }
                    }
                    if (ideaIncubator.isNotEmpty()) {
                        Surface(
                            onClick = { onNavigateTo(Screen.Search) },
                            modifier = Modifier.weight(1f),
                            color = TactileTheme.Surface,
                            shape = RoundedCornerShape(TactileTheme.RadiusMd),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TactileTheme.Border
                            )
                        ) {
                            Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = TactileTheme.Success
                                )
                                Text(
                                    stringResource(Res.string.dash_ideas),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    stringResource(
                                        Res.string.dash_unset_count,
                                        ideaIncubator.size
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TactileTheme.Muted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 11.2 Foundational Principle
        val foundationalNotes = dashboardState.foundationalNotes
        if (foundationalNotes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_foundational),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent
                )
                foundationalNotes.forEach { nodeWithPin ->
                    Surface(
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Accent.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Accent.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Accent
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                nodeWithPin.node.content.take(200) + if (nodeWithPin.node.content.length > 200) "..." else "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // 11.3 Resource Highlights
        val resourceHighlights = dashboardState.resourceHighlights
        if (resourceHighlights.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_resource_highlights),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                resourceHighlights.forEach { nodeWithPin ->
                    Surface(
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border)
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.LibraryBooks,
                                contentDescription = null,
                                tint = TactileTheme.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 12. Upcoming Deadlines
        if (upcomingDeadlines.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_upcoming_deadlines),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent
                )
                upcomingDeadlines.forEach { nodeWithPin ->
                    val due = nodeWithPin.node.dueAt?.let {
                        Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEditNode(nodeWithPin.node.id) },
                            onLongClick = { onEditNode(nodeWithPin.node.id) },
                        ),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Accent.copy(alpha = 0.3f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = TactileTheme.Accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                                modifier = Modifier.weight(1f)
                            )
                            if (due != null) {
                                Text(
                                    "${due.day}/${due.month.number}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TactileTheme.Accent
                                )
                            }
                        }
                    }
                }
            }
        }

        // 13. Resets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
        ) {
            Surface(
                onClick = { onNavigateTo(Screen.Review) },
                modifier = Modifier.weight(1f),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Primary.copy(alpha = 0.3f)
                ),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = TactileTheme.Primary
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                    Text(
                        stringResource(Res.string.dash_review_reset),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Text
                    )
                }
            }
            Surface(
                onClick = { onNavigateTo(Screen.Track) },
                modifier = Modifier.weight(1f),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Accent.copy(alpha = 0.3f)
                ),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = TactileTheme.Accent
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                    Text(
                        stringResource(Res.string.dash_check_in),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Text
                    )
                }
            }
        }

        // 14. Relevant Project
        if (allProjects.isNotEmpty()) {
            val mostRelevantProject = allProjects.sortedByDescending { it.updatedAt }.firstOrNull()
            mostRelevantProject?.let { project ->
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    Text(
                        text = stringResource(Res.string.dash_relevant_project),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary
                    )
                    Surface(
                        onClick = { onNavigateToProject(project.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Primary.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = TactileTheme.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text
                            )
                        }
                    }
                }
            }
        }

        // 15. Relevant Note
        if (relevantNote != null) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = stringResource(Res.string.dash_relevant_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Surface(
                    onClick = { onEditNode(relevantNote.node.id) },
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TactileTheme.Primary.copy(alpha = 0.2f)
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(TactileTheme.SpacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = TactileTheme.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(TactileTheme.SpacingMd))
                        Text(
                            text = relevantNote.node.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TactileTheme.Text
                        )
                    }
                }
            }
        }

        // 16. Modules Grid
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_today),
                    icon = Icons.Default.DateRange,
                    status = if (todayNodes.isNotEmpty()) stringResource(
                        Res.string.dash_module_tasks_count,
                        todayNodes.size
                    ) else stringResource(Res.string.dash_module_empty),
                    onClick = { onNavigateTo(Screen.Today) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_focus),
                    icon = Icons.Default.PlayArrow,
                    status = if (todayNodes.isNotEmpty()) stringResource(Res.string.dash_module_ready) else stringResource(
                        Res.string.dash_module_waiting
                    ),
                    onClick = { onNavigateTo(Screen.Focus) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_track),
                    icon = Icons.Default.CheckCircle,
                    status = moodToday?.let { stringResource(Res.string.dash_module_logged) }
                        ?: stringResource(Res.string.dash_module_pending),
                    onClick = { onNavigateTo(Screen.Track) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_tasks),
                    icon = Icons.AutoMirrored.Filled.List,
                    status = if (tasksCount > 0) stringResource(
                        Res.string.dash_module_total_count,
                        tasksCount
                    ) else stringResource(Res.string.dash_module_none),
                    onClick = { onNavigateTo(Screen.Tasks) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_notes),
                    icon = Icons.Default.Edit,
                    status = if (notesCount > 0) stringResource(
                        Res.string.dash_module_total_count,
                        notesCount
                    ) else stringResource(Res.string.dash_module_none),
                    onClick = { onNavigateTo(Screen.Notes) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_proj),
                    icon = Icons.AutoMirrored.Filled.List,
                    status = if (allProjects.isNotEmpty()) stringResource(
                        Res.string.dash_module_active_count,
                        allProjects.size
                    ) else stringResource(Res.string.dash_module_empty),
                    onClick = { onNavigateTo(Screen.Projects) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_area),
                    icon = Icons.Default.LocationOn,
                    status = if (allAreas.isNotEmpty()) stringResource(
                        Res.string.dash_module_total_count,
                        allAreas.size
                    ) else stringResource(Res.string.dash_module_empty),
                    onClick = { onNavigateTo(Screen.Areas) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_cal),
                    icon = Icons.Default.Event,
                    status = if (calendarEntries.isNotEmpty()) stringResource(
                        Res.string.dash_module_items_count,
                        calendarEntries.size
                    ) else stringResource(Res.string.dash_module_empty),
                    onClick = { onNavigateTo(Screen.Calendar) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_stats),
                    icon = Icons.Default.Info,
                    status = stringResource(Res.string.dash_view),
                    onClick = { onNavigateTo(Screen.Insights) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.screen_history),
                    icon = Icons.Default.History,
                    status = if (allSessions.isNotEmpty()) {
                        stringResource(
                            Res.string.dash_history_last,
                            Instant.fromEpochMilliseconds(allSessions.first().startedAt)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        )
                    } else {
                        stringResource(Res.string.dash_module_empty)
                    },
                    onClick = { viewModel.resumeLastSession() },
                )
            }
        }
    }
}

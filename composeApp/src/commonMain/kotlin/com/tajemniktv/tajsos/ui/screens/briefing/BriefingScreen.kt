/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.briefing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.briefing_action_capture
import tajsos.composeapp.generated.resources.briefing_action_capture_subtitle
import tajsos.composeapp.generated.resources.briefing_action_continue
import tajsos.composeapp.generated.resources.briefing_action_continue_subtitle
import tajsos.composeapp.generated.resources.briefing_action_notes
import tajsos.composeapp.generated.resources.briefing_action_notes_subtitle
import tajsos.composeapp.generated.resources.briefing_action_tasks
import tajsos.composeapp.generated.resources.briefing_action_tasks_subtitle
import tajsos.composeapp.generated.resources.briefing_capture_placeholder
import tajsos.composeapp.generated.resources.briefing_event_count
import tajsos.composeapp.generated.resources.briefing_greeting_afternoon
import tajsos.composeapp.generated.resources.briefing_greeting_evening
import tajsos.composeapp.generated.resources.briefing_greeting_morning
import tajsos.composeapp.generated.resources.briefing_greeting_night
import tajsos.composeapp.generated.resources.briefing_mode_line
import tajsos.composeapp.generated.resources.briefing_next_event
import tajsos.composeapp.generated.resources.briefing_no_recent
import tajsos.composeapp.generated.resources.briefing_no_resume
import tajsos.composeapp.generated.resources.briefing_no_upcoming
import tajsos.composeapp.generated.resources.briefing_note_count
import tajsos.composeapp.generated.resources.briefing_priorities_count
import tajsos.composeapp.generated.resources.briefing_recent
import tajsos.composeapp.generated.resources.briefing_recent_updated_days
import tajsos.composeapp.generated.resources.briefing_recent_updated_hours
import tajsos.composeapp.generated.resources.briefing_recent_updated_now
import tajsos.composeapp.generated.resources.briefing_resume
import tajsos.composeapp.generated.resources.briefing_resume_hint
import tajsos.composeapp.generated.resources.briefing_upcoming
import tajsos.composeapp.generated.resources.common_no_active_mode
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Calm, desktop-first orientation screen showing a lightweight daily briefing.
 */
@Composable
fun BriefingScreen(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNewEntry: () -> Unit,
) {
    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val calendarEntries by viewModel.calendarEntries.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val now = Clock.System.now().toEpochMilliseconds()
    val upcomingEvent =
        remember(calendarEntries, now) {
            calendarEntries
                .asSequence()
                .filter { it.startAt >= now }
                .sortedBy { it.startAt }
                .firstOrNull()
        }
    val recentNodes =
        remember(allNodes) {
            allNodes
                .asSequence()
                .sortedByDescending { it.node.updatedAt }
                .take(2)
                .toList()
        }
    val resumeNode =
        remember(dashboardState, recentNodes) {
            dashboardState.relevantNote ?: dashboardState.forgottenWisdom ?: recentNodes.firstOrNull()
        }
    val priorityCount =
        remember(todayNodes, dashboardState) {
            val activeToday = todayNodes.count { it.status != "done" && it.status != "archived" }
            if (activeToday > 0) activeToday else dashboardState.quickWins.size
        }
    val nowLocal = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val greetingText = briefingGreeting(nowLocal.hour)
    val userName = userProfile.resolveDisplayName()
    val modeName = currentMode?.name ?: stringResource(Res.string.common_no_active_mode)

    val quickActions =
        listOf(
            BriefingAction(
                titleRes = Res.string.briefing_action_continue,
                subtitle = todayNodes.firstOrNull()?.title ?: stringResource(Res.string.briefing_action_continue_subtitle),
                icon = Icons.Default.PlayArrow,
                onClick = { onNavigateTo(Screen.Today) },
            ),
            BriefingAction(
                titleRes = Res.string.briefing_action_notes,
                subtitle = stringResource(Res.string.briefing_action_notes_subtitle, dashboardState.notesCount),
                icon = Icons.Default.Description,
                onClick = { onNavigateTo(Screen.Notes) },
            ),
            BriefingAction(
                titleRes = Res.string.briefing_action_capture,
                subtitle = stringResource(Res.string.briefing_action_capture_subtitle),
                icon = Icons.Default.Edit,
                onClick = onNewEntry,
            ),
            BriefingAction(
                titleRes = Res.string.briefing_action_tasks,
                subtitle = stringResource(Res.string.briefing_action_tasks_subtitle, dashboardState.tasksCount),
                icon = Icons.Default.Checklist,
                onClick = onNavigateToTasks,
            ),
        )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val showSignalRail = maxWidth >= 1160.dp

        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            BriefingMainPane(
                modifier = Modifier.weight(1f),
                greetingText = greetingText,
                userName = userName,
                modeLine = stringResource(Res.string.briefing_mode_line, modeName),
                prioritiesLine = stringResource(Res.string.briefing_priorities_count, priorityCount),
                eventsLine = stringResource(Res.string.briefing_event_count, if (upcomingEvent == null) 0 else 1),
                notesLine = stringResource(Res.string.briefing_note_count, dashboardState.notesCount),
                quickActions = quickActions,
                onCapture = onNewEntry,
            )

            if (showSignalRail) {
                BriefingSignalRail(
                    modifier = Modifier.width(300.dp).fillMaxHeight(),
                    upcomingTitle = upcomingEvent?.title,
                    upcomingTime = upcomingEvent?.startAt?.let(::formatClockTime),
                    recentNodes = recentNodes,
                    resumeNode = resumeNode,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun BriefingMainPane(
    modifier: Modifier,
    greetingText: String,
    userName: String,
    modeLine: String,
    prioritiesLine: String,
    eventsLine: String,
    notesLine: String,
    quickActions: List<BriefingAction>,
    onCapture: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .background(TajsOSTheme.Background)
                .padding(horizontal = 40.dp, vertical = 28.dp),
    ) {
        BriefingAtmosphere(modifier = Modifier.matchParentSize())
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.widthIn(max = 680.dp),
            ) {
                Text(
                    text = "$greetingText, $userName.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = modeLine,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TajsOSTheme.Muted,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prioritiesLine,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TajsOSTheme.Primary,
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TajsOSTheme.Muted,
                    )
                    Text(
                        text = eventsLine,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TajsOSTheme.Muted,
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TajsOSTheme.Muted,
                    )
                    Text(
                        text = notesLine,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TajsOSTheme.Muted,
                    )
                }
            }

            Spacer(Modifier.height(84.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                quickActions.forEach { action ->
                    BriefingActionCard(action = action)
                }
            }

            Spacer(Modifier.weight(1f))
            BriefingCaptureField(onClick = onCapture)
        }
    }
}

@Composable
private fun BriefingSignalRail(
    modifier: Modifier,
    upcomingTitle: String?,
    upcomingTime: String?,
    recentNodes: List<NodeWithPin>,
    resumeNode: NodeWithPin?,
) {
    Surface(
        modifier = modifier.padding(start = 2.dp),
        color = TajsOSTheme.SurfaceLow.copy(alpha = 0.55f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            BriefingSectionLabel(text = stringResource(Res.string.briefing_upcoming))
            if (upcomingTitle == null) {
                Text(
                    text = stringResource(Res.string.briefing_no_upcoming),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            } else {
                Text(
                    text = stringResource(Res.string.briefing_next_event, upcomingTime.orEmpty()),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                )
                Text(
                    text = upcomingTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            BriefingSectionLabel(text = stringResource(Res.string.briefing_recent))
            if (recentNodes.isEmpty()) {
                Text(
                    text = stringResource(Res.string.briefing_no_recent),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(recentNodes, key = { it.node.id }) { node ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TajsOSTheme.Muted,
                                modifier = Modifier.size(15.dp).padding(top = 3.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = node.node.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TajsOSTheme.Text,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = relativeUpdatedText(node.node.updatedAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TajsOSTheme.Muted,
                                )
                            }
                        }
                    }
                }
            }

            BriefingSectionLabel(text = stringResource(Res.string.briefing_resume))
            if (resumeNode == null) {
                Text(
                    text = stringResource(Res.string.briefing_no_resume),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TajsOSTheme.SurfaceHigh.copy(alpha = 0.7f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = resumeNode.node.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TajsOSTheme.Text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = resumeNode.node.content.ifBlank { stringResource(Res.string.briefing_resume_hint) },
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Muted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BriefingSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TajsOSTheme.Primary,
    )
}

@Composable
private fun BriefingActionCard(action: BriefingAction) {
    Surface(
        onClick = action.onClick,
        shape = RoundedCornerShape(14.dp),
        color = TajsOSTheme.SurfaceLow.copy(alpha = 0.72f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.width(188.dp).height(144.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TajsOSTheme.Primary.copy(alpha = 0.2f),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = TajsOSTheme.Primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(action.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun BriefingCaptureField(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = TajsOSTheme.SurfaceHighest.copy(alpha = 0.9f),
        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().height(58.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = TajsOSTheme.Muted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(Res.string.briefing_capture_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Muted,
                )
            }
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = TajsOSTheme.Muted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun BriefingAtmosphere(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.radialGradient(
                        colors =
                            listOf(
                                TajsOSTheme.Primary.copy(alpha = 0.24f),
                                TajsOSTheme.AccentBlue.copy(alpha = 0.08f),
                                Color.Transparent,
                            ),
                    ),
                ),
    )
}

@Composable
private fun briefingGreeting(hour: Int): String =
    when (hour)
    {
        in 5..11 -> stringResource(Res.string.briefing_greeting_morning)
        in 12..17 -> stringResource(Res.string.briefing_greeting_afternoon)
        in 18..22 -> stringResource(Res.string.briefing_greeting_evening)
        else -> stringResource(Res.string.briefing_greeting_night)
    }

@Composable
private fun relativeUpdatedText(updatedAt: Long): String {
    val diffHours = ((Clock.System.now().toEpochMilliseconds() - updatedAt) / 3_600_000L).coerceAtLeast(0L)
    if (diffHours == 0L) {
        return stringResource(Res.string.briefing_recent_updated_now)
    }
    if (diffHours < 24L) {
        return stringResource(Res.string.briefing_recent_updated_hours, diffHours)
    }
    return stringResource(Res.string.briefing_recent_updated_days, diffHours / 24L)
}

private fun formatClockTime(timestamp: Long): String {
    val local = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    return local.time
        .toString()
        .take(5)
}

private data class BriefingAction(
    val titleRes: StringResource,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.briefing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.LocalMainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
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
import tajsos.composeapp.generated.resources.briefing_greeting
import tajsos.composeapp.generated.resources.briefing_greeting_afternoon
import tajsos.composeapp.generated.resources.briefing_greeting_evening
import tajsos.composeapp.generated.resources.briefing_greeting_morning
import tajsos.composeapp.generated.resources.briefing_greeting_night
import tajsos.composeapp.generated.resources.briefing_mode_inactive
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
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Calm, desktop-first orientation screen showing a lightweight daily briefing.
 *
 * Displays the daily briefing screen built from ViewModel state and supplied navigation/capture callbacks.
 *
 * Collects dashboard, today's/all nodes, calendar entries, current mode, and user profile from the provided
 * MainViewModel and renders a greeting, summary lines (priorities, events, notes), a set of quick-action cards,
 * and signal cards for the next upcoming event, recent nodes, and a resume node. User interactions invoke the
 * provided navigation and entry-creation callbacks.
 *
 * @param onNavigate Callback invoked with a destination route.
 * @param onNewEntry Callback invoked to start creating a new entry/capture from the briefing UI.
 */
@Composable
fun BriefingRoute(
    onNavigate: (String) -> Unit,
    onNewEntry: () -> Unit,
) {
    val viewModel = LocalMainViewModel.current
    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val calendarEntries by viewModel.calendarEntries.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val timeZone = TimeZone.currentSystemDefault()
    val nowInstant = Clock.System.now()
    val nowEpochMillis = nowInstant.toEpochMilliseconds()
    val todayDate = nowInstant.toLocalDateTime(timeZone).date
    val todayEventCount =
        remember(calendarEntries, todayDate, timeZone) {
            calendarEntries.count {
                Instant
                    .fromEpochMilliseconds(it.startAt)
                    .toLocalDateTime(timeZone)
                    .date == todayDate
            }
        }
    val upcomingEvent =
        remember(calendarEntries, nowEpochMillis) {
            calendarEntries
                .asSequence()
                .filter { it.startAt >= nowEpochMillis }
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
            dashboardState.relevantNote ?: dashboardState.forgottenWisdom
                ?: recentNodes.firstOrNull()
        }
    val priorityCount =
        remember(todayNodes, dashboardState) {
            val activeToday =
                todayNodes.count {
                    when (it.taskStateOrNull()) {
                        TaskState.DONE,
                        TaskState.ARCHIVED,
                        -> false

                        else -> true
                    }
                }
            if (activeToday > 0) activeToday else dashboardState.quickWins.size
        }
    val nowLocal = nowInstant.toLocalDateTime(timeZone)
    val greetingText = briefingGreeting(nowLocal.hour)
    val userName = userProfile.resolveDisplayName()
    val modeLine =
        currentMode?.name?.let { stringResource(Res.string.briefing_mode_line, it) }
            ?: stringResource(Res.string.briefing_mode_inactive)

    val quickActions =
        listOf(
            BriefingAction(
                titleRes = Res.string.briefing_action_continue,
                subtitle =
                    todayNodes.firstOrNull()?.title
                        ?: stringResource(Res.string.briefing_action_continue_subtitle),
                icon = Icons.Default.PlayArrow,
                onClick = { onNavigate(Screen.Today.route) },
            ),
            BriefingAction(
                titleRes = Res.string.briefing_action_notes,
                subtitle =
                    pluralStringResource(
                        Res.plurals.briefing_action_notes_subtitle,
                        dashboardState.notesCount,
                        dashboardState.notesCount,
                    ),
                icon = Icons.Default.Description,
                onClick = { onNavigate(Screen.Notes.route) },
            ),
            BriefingAction(
                titleRes = Res.string.briefing_action_capture,
                subtitle = stringResource(Res.string.briefing_action_capture_subtitle),
                icon = Icons.Default.Edit,
                onClick = onNewEntry,
            ),
            BriefingAction(
                titleRes = Res.string.briefing_action_tasks,
                subtitle =
                    pluralStringResource(
                        Res.plurals.briefing_action_tasks_subtitle,
                        dashboardState.tasksCount,
                        dashboardState.tasksCount,
                    ),
                icon = Icons.Default.Checklist,
                onClick = { onNavigate(Screen.Tasks.route) },
            ),
        )

    BriefingScreen(
        greetingText = greetingText,
        userName = userName,
        modeLine = modeLine,
        prioritiesLine =
            pluralStringResource(
                Res.plurals.briefing_priorities_count,
                priorityCount,
                priorityCount,
            ),
        eventsLine =
            pluralStringResource(
                Res.plurals.briefing_event_count,
                todayEventCount,
                todayEventCount,
            ),
        notesLine =
            pluralStringResource(
                Res.plurals.briefing_note_count,
                dashboardState.notesCount,
                dashboardState.notesCount,
            ),
        quickActions = quickActions,
        upcomingTitle = upcomingEvent?.title,
        upcomingTime = upcomingEvent?.startAt?.let(::formatClockTime),
        recentNodes = recentNodes,
        resumeNode = resumeNode,
        onCapture = onNewEntry,
        onNavigate = onNavigate,
    )
}

/**
 * Composable screen for the daily briefing, presenting situational awareness to the user.
 *
 * @param greetingText Localized greeting (e.g., "Good morning").
 * @param userName Display name shown alongside the greeting.
 * @param modeLine Single-line status or mode label shown under the greeting.
 * @param prioritiesLine Summary text for today's priority tasks.
 * @param eventsLine Summary text for today's events.
 * @param notesLine Summary text for recent notes.
 * @param quickActions List of action descriptors used to render the quick-action cards.
 * @param upcomingTitle Title of the next calendar entry, or `null` if none.
 * @param upcomingTime Formatted time string for the upcoming entry, or `null` if none.
 * @param recentNodes Two most recently updated nodes to display in the Recent card.
 * @param resumeNode Node suggested for resuming work, or `null` if none.
 * @param onCapture Callback invoked when the capture field is clicked.
 * @param onNavigate Callback invoked with a destination route.
 */
@Composable
fun BriefingScreen(
    greetingText: String,
    userName: String,
    modeLine: String,
    prioritiesLine: String,
    eventsLine: String,
    notesLine: String,
    quickActions: List<BriefingAction>,
    upcomingTitle: String?,
    upcomingTime: String?,
    recentNodes: List<NodeWithPin>,
    resumeNode: NodeWithPin?,
    onCapture: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Briefing,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(0.dp),
        // Briefing has custom padding
    ) {
        BriefingMainPane(
            modifier = Modifier.fillMaxSize(),
            greetingText = greetingText,
            userName = userName,
            modeLine = modeLine,
            prioritiesLine = prioritiesLine,
            eventsLine = eventsLine,
            notesLine = notesLine,
            quickActions = quickActions,
            upcomingTitle = upcomingTitle,
            upcomingTime = upcomingTime,
            recentNodes = recentNodes,
            resumeNode = resumeNode,
            onCapture = onCapture,
        )
    }
}

/**
 * Renders the main briefing layout: header (greeting, mode, summary lines), a row of quick-action cards,
 * three signal cards (upcoming, recent, resume), and a bottom capture field.
 *
 * @param modifier Modifier applied to the root container.
 * @param greetingText Localized greeting (e.g., "Good morning").
 * @param userName Display name shown alongside the greeting.
 * @param modeLine Single-line status or mode label shown under the greeting.
 * @param prioritiesLine Summary text for today's priority tasks.
 * @param eventsLine Summary text for today's events.
 * @param notesLine Summary text for recent notes.
 * @param quickActions List of action descriptors used to render the quick-action cards.
 * @param upcomingTitle Title of the next calendar entry, or `null` if none.
 * @param upcomingTime Formatted time string for the upcoming entry, or `null` if none.
 * @param recentNodes Two most recently updated nodes to display in the Recent card.
 * @param resumeNode Node suggested for resuming work, or `null` if none.
 * @param onCapture Callback invoked when the capture field is clicked.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun BriefingMainPane(
    modifier: Modifier = Modifier,
    greetingText: String,
    userName: String,
    modeLine: String,
    prioritiesLine: String,
    eventsLine: String,
    notesLine: String,
    quickActions: List<BriefingAction>,
    upcomingTitle: String?,
    upcomingTime: String?,
    recentNodes: List<NodeWithPin>,
    resumeNode: NodeWithPin?,
    onCapture: () -> Unit,
) {
    Box(
        modifier =
            modifier
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
                    text = stringResource(Res.string.briefing_greeting, greetingText, userName),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = modeLine,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TajsOSTheme.Muted,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = TajsOSTheme.Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
                    ) {
                        Text(
                            text = prioritiesLine,
                            style = MaterialTheme.typography.labelMedium,
                            color = TajsOSTheme.Primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    Surface(
                        color = TajsOSTheme.SurfaceLow,
                        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
                        border =
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TajsOSTheme.Border.copy(alpha = 0.5f),
                            ),
                    ) {
                        Text(
                            text = eventsLine,
                            style = MaterialTheme.typography.labelMedium,
                            color = TajsOSTheme.Text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    Surface(
                        color = TajsOSTheme.SurfaceLow,
                        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
                        border =
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TajsOSTheme.Border.copy(alpha = 0.5f),
                            ),
                    ) {
                        Text(
                            text = notesLine,
                            style = MaterialTheme.typography.labelMedium,
                            color = TajsOSTheme.Text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(84.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                quickActions.forEach {
                    BriefingActionCard(action = it)
                }
            }

            Spacer(Modifier.height(30.dp))
            BriefingSignalSections(
                upcomingTitle = upcomingTitle,
                upcomingTime = upcomingTime,
                recentNodes = recentNodes,
                resumeNode = resumeNode,
            )

            Spacer(Modifier.weight(1f))
            BriefingCaptureField(onClick = onCapture)
        }
    }
}

/**
 * Renders three briefing signal cards: Upcoming, Recent, and Resume.
 *
 * Each card displays localized labels and either a placeholder message when its data is absent
 * or the corresponding content: the next event time and title, a list of recent nodes with
 * relative update times, and a resumable node with title and content snippet.
 *
 * @param upcomingTitle The title of the next upcoming event, or `null` when none is available.
 * @param upcomingTime A formatted time string for the upcoming event, or `null`.
 * @param recentNodes A list of recent nodes to display; each item shows its title and relative updated text.
 * @param resumeNode The node suggested for resuming work, or `null` when there is no resume candidate.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun BriefingSignalSections(
    upcomingTitle: String?,
    upcomingTime: String?,
    recentNodes: List<NodeWithPin>,
    resumeNode: NodeWithPin?,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().widthIn(max = 980.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BriefingSignalCard(
            modifier = Modifier.widthIn(min = 220.dp, max = 310.dp),
        ) {
            BriefingSectionLabel(text = stringResource(Res.string.briefing_upcoming))
            Spacer(Modifier.height(10.dp))
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
                Spacer(Modifier.height(6.dp))
                Text(
                    text = upcomingTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        BriefingSignalCard(
            modifier = Modifier.widthIn(min = 220.dp, max = 310.dp),
        ) {
            BriefingSectionLabel(text = stringResource(Res.string.briefing_recent))
            Spacer(Modifier.height(10.dp))
            if (recentNodes.isEmpty()) {
                Text(
                    text = stringResource(Res.string.briefing_no_recent),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recentNodes.forEach { node ->
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
        }

        BriefingSignalCard(
            modifier = Modifier.widthIn(min = 220.dp, max = 310.dp),
        ) {
            BriefingSectionLabel(text = stringResource(Res.string.briefing_resume))
            Spacer(Modifier.height(10.dp))
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

/**
 * Displays content inside a padded column on a rounded, semi-transparent surface matching the briefing theme.
 *
 * @param modifier Modifier applied to the outer surface.
 * @param content Slot for the card's vertical content; invoked with a ColumnScope.
 */
@Composable
private fun BriefingSignalCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = TajsOSTheme.SurfaceLow.copy(alpha = 0.55f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TajsOSTheme.Border.copy(alpha = 0.5f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

/**
 * Renders a section label in uppercase using the theme's small label typography and primary color.
 *
 * @param text The label text to display.
 */
@Composable
private fun BriefingSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TajsOSTheme.Primary,
    )
}

/**
 * Displays a clickable briefing action card showing an icon, a title, and a subtitle.
 *
 * The card invokes `action.onClick` when pressed and presents the visual elements defined by the provided `BriefingAction`.
 *
 * @param action The action model containing the title resource, subtitle text, icon, and click handler to render and execute.
 */
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

/**
 * Renders a clickable capture field used to start creating a new entry.
 *
 * Displays an edit icon and placeholder text on the left and a schedule icon on the right.
 *
 * @param onClick Callback invoked when the field is clicked.
 */
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

/**
 * Draws a rounded container with a subtle radial gradient used as the briefing background.
 *
 * @param modifier Modifier applied to the container (e.g., sizing, padding, or layout adjustments).
 */
@Composable
private fun BriefingAtmosphere(modifier: Modifier = Modifier) {
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }
    val stops =
        remember {
            arrayOf(
                0.0f to TajsOSTheme.Primary.copy(alpha = 0.15f),
                0.2f to TajsOSTheme.Primary.copy(alpha = 0.12f),
                0.4f to TajsOSTheme.Primary.copy(alpha = 0.10f),
                0.6f to TajsOSTheme.AccentBlue.copy(alpha = 0.08f),
                0.8f to TajsOSTheme.AccentBlue.copy(alpha = 0.04f),
                1.0f to Color.Transparent,
            )
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(20.dp))
                .hazeSource(state = hazeState),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(colorStops = stops)),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .hazeEffect(
                        state = hazeState,
                        block = {
                            noiseFactor = 0.25f
                        },
                    ),
        )
    }
}

/**
 * Selects a localized greeting string appropriate for the given hour of day.
 *
 * @param hour The hour of day in 24-hour format (typically 0–23).
 * @return The localized greeting string for the provided hour.
 */
@Composable
private fun briefingGreeting(hour: Int): String =
    when (briefingPeriodForHour(hour)) {
        "morning" -> stringResource(Res.string.briefing_greeting_morning)
        "afternoon" -> stringResource(Res.string.briefing_greeting_afternoon)
        "evening" -> stringResource(Res.string.briefing_greeting_evening)
        else -> stringResource(Res.string.briefing_greeting_night)
    }

/**
 * Produces a localized human-readable label describing how long ago a timestamp occurred.
 *
 * @param updatedAt Time of the event in milliseconds since the Unix epoch.
 * @return A localized string: `"updated now"` if less than one hour, `"updated X hours"` if less than 24 hours, or `"updated Y days"` otherwise.
 */
@Composable
private fun relativeUpdatedText(updatedAt: Long): String {
    val diffHours = relativeHourDiff(Clock.System.now().toEpochMilliseconds(), updatedAt)
    if (diffHours == 0L) {
        return stringResource(Res.string.briefing_recent_updated_now)
    }
    if (diffHours < 24L) {
        return pluralStringResource(
            Res.plurals.briefing_recent_updated_hours,
            diffHours.toInt(),
            diffHours,
        )
    }
    return pluralStringResource(
        Res.plurals.briefing_recent_updated_days,
        (diffHours / 24L).toInt(),
        diffHours / 24L,
    )
}

/**
 * Formats an epoch-millisecond timestamp into the local time string "HH:MM".
 *
 * @param timestamp Epoch milliseconds since Unix epoch.
 * @return The local time portion formatted as `HH:MM`.
 */
internal fun formatClockTime(timestamp: Long): String {
    val local =
        Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    return local.time
        .toString()
        .take(5)
}

/**
 * Returns the period-of-day bucket for the given hour, used to select a greeting.
 *
 * @param hour Hour of day in 24-hour format (0–23).
 * @return One of `"morning"`, `"afternoon"`, `"evening"`, or `"night"`.
 */
internal fun briefingPeriodForHour(hour: Int): String =
    when (hour) {
        in 5..11 -> "morning"
        in 12..17 -> "afternoon"
        in 18..22 -> "evening"
        else -> "night"
    }

/**
 * Calculates the number of whole hours between [nowMs] and [updatedAt].
 *
 * @param nowMs Current time in epoch milliseconds.
 * @param updatedAt Past time in epoch milliseconds.
 * @return Non-negative number of whole hours elapsed since [updatedAt].
 */
internal fun relativeHourDiff(
    nowMs: Long,
    updatedAt: Long,
): Long = ((nowMs - updatedAt) / 3_600_000L).coerceAtLeast(0L)

/**
 * Represents a quick action available on the briefing screen.
 *
 * @property titleRes The localized label for the action.
 * @property subtitle A descriptive secondary text.
 * @property icon The iconography for the action button.
 * @property onClick Callback invoked when the action is selected.
 */
data class BriefingAction(
    val titleRes: StringResource,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

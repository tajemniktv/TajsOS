/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.nodes.TaskBrief
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.delay
import kotlin.time.Clock

/**
 * A sticky note style card for quick information.
 *
 * @param title The title of the note.
 * @param content The content text of the note.
 * @param onClick Callback when the card is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun StickyNoteCard(
    title: String,
    content: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(200.dp),
        color = TajsOSTheme.Accent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Accent.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = TajsOSTheme.Accent,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                color = TajsOSTheme.Text,
            )
        }
    }
}

/**
 * A dashboard card showing the "Daily Pulse" progress and top active tasks.
 *
 * @param progress The progress value (0f..1f).
 * @param tasks The list of tasks to display.
 * @param onToggleTask Callback when a task is toggled.
 * @param onTaskClick Callback when a task is clicked.
 * @param onClick Callback when the card header/body is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun TodayPulseCard(
    progress: Float,
    tasks: List<NodeWithPin>,
    onToggleTask: (NodeWithPin) -> Unit,
    onTaskClick: (Long) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        "Daily Pulse",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TajsOSTheme.Text,
                    )
                }
                ProgressRing(progress = progress)
            }
            Spacer(Modifier.height(TajsOSTheme.SpacingLg))
            Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
                tasks
                    .asSequence()
                    .filter { it.node.status == "active" }
                    .take(3)
                    .forEach { nodeWithPin ->
                        TaskBrief(
                            title = nodeWithPin.node.title,
                            isDone = false,
                            onToggle = { onToggleTask(nodeWithPin) },
                        ) { onTaskClick(nodeWithPin.node.id) }
                    }
                if (tasks.none { it.node.status == "active" }) {
                    Text(
                        "System clear for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TajsOSTheme.Success,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

/**
 * A dashboard card showing the current focus session status.
 *
 * @param viewModel The main view model for data access.
 * @param activeSession The currently active focus session, if any.
 * @param onToggleFocus Callback when the focus switch is toggled.
 * @param onClick Callback when the card is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun FocusCard(
    viewModel: MainViewModel,
    activeSession: FocusSessionEntity?,
    onToggleFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val activeTask =
        activeSession?.let { session -> allNodes.find { it.node.id == session.nodeId }?.node }

    var currentMillis by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(activeSession) {
        while (activeSession != null) {
            currentMillis = Clock.System.now().toEpochMilliseconds()
            delay(1000L)
        }
    }

    DashCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        "FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        if (activeSession != null) "Deep Work Phase" else "System Standby",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TajsOSTheme.Text,
                    )
                }
                if (activeSession != null) {
                    val duration =
                        (currentMillis - activeSession.startedAt) / 1000
                    val h = duration / 3600
                    val m = (duration % 3600) / 60
                    val s = duration % 60
                    Text(
                        "${h.toString().padStart(2, '0')}:${
                            m.toString().padStart(2, '0')
                        }:${s.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TajsOSTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(TajsOSTheme.SpacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (activeTask != null) "Active: ${activeTask.title}" else "Ready to engage",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = activeSession != null,
                    onCheckedChange = { onToggleFocus() },
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TajsOSTheme.Primary,
                            uncheckedThumbColor = TajsOSTheme.Muted,
                            uncheckedTrackColor = TajsOSTheme.Surface,
                        ),
                )
            }
        }
    }
}

/**
 * A dashboard card providing a summary of life metrics (captures vs completions).
 *
 * @param captures The number of captures in the current week.
 * @param completions The number of completions in the current week.
 * @param onClick Callback when the card is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun LifeSummaryCard(
    captures: Int,
    completions: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingLg)) {
            Text(
                "LIFE SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(TajsOSTheme.SpacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        captures.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = TajsOSTheme.Text,
                    )
                    Text(
                        "CAPTURES / WEEK",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        completions.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = TajsOSTheme.Success,
                    )
                    Text(
                        "DONE / WEEK",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }
            Spacer(Modifier.height(TajsOSTheme.SpacingMd))
            LinearProgressIndicator(
                progress = {
                    if (captures > 0) {
                        (completions.toFloat() / captures.toFloat()).coerceIn(
                            0f,
                            1f,
                        )
                    } else {
                        0f
                    }
                },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = TajsOSTheme.Primary,
                trackColor = TajsOSTheme.Border,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

/**
 * A metric card with an icon, label, and primary value.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param label The primary label of the metric.
 * @param value The value of the metric.
 * @param secondaryLabel An optional secondary label or badge.
 * @param icon The icon to display.
 * @param iconColor The color of the icon and secondary label.
 * @param onClick Callback when the card is clicked.
 */
@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    secondaryLabel: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
) {
    DashCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    secondaryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(TajsOSTheme.SpacingMd))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TajsOSTheme.Text,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * A card representing a "vault" or storage area with an item count.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param title The title of the vault.
 * @param count The number of items in the vault.
 * @param icon The icon to display.
 * @param onClick Callback when the card is clicked.
 */
@Composable
fun VaultCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
            Icon(
                icon,
                contentDescription = null,
                tint = TajsOSTheme.Accent,
                modifier = Modifier.size(20.dp),
            )
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
            )
            Text(
                "$count ITEMS",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
                fontSize = 8.sp,
            )
        }
    }
}

/**
 * A ring component visualizing progress (0f..1f).
 *
 * @param progress The progress value (0f..1f).
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val anim by animateFloatAsState(targetValue = progress)
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(60.dp)) {
        Canvas(modifier = Modifier.size(60.dp)) {
            drawCircle(color = Color.White.copy(alpha = 0.1f), style = Stroke(width = 4.dp.toPx()))
            drawArc(
                color = TajsOSTheme.Primary,
                startAngle = -90f,
                sweepAngle = 360 * anim,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TajsOSTheme.Text,
        )
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.*
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.nodes.TaskBrief
import com.tajemniktv.tajsos.ui.design.components.DashCard
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme
import kotlin.time.Clock

@Composable
fun StickyNoteCard(title: String, content: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        color = TactileTheme.Accent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Accent.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = TactileTheme.Accent,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                color = TactileTheme.Text
            )
        }
    }
}

@Composable
fun TodayPulseCard(
    progress: Float,
    tasks: List<NodeWithPin>,
    onToggleTask: (NodeWithPin) -> Unit,
    onTaskClick: (Long) -> Unit,
    onClick: () -> Unit
) {
    DashCard(onClick = onClick) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Daily Pulse",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TactileTheme.Text
                    )
                }
                ProgressRing(progress = progress)
            }
            Spacer(Modifier.height(TactileTheme.SpacingLg))
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                tasks.filter { it.node.status == "active" }.take(3).forEach { nodeWithPin ->
                    TaskBrief(
                        title = nodeWithPin.node.title,
                        isDone = false,
                        onToggle = { onToggleTask(nodeWithPin) },
                        onClick = { onTaskClick(nodeWithPin.node.id) })
                }
                if (tasks.none { it.node.status == "active" }) {
                    Text(
                        "System clear for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TactileTheme.Success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FocusCard(
    viewModel: MainViewModel,
    activeSession: FocusSessionEntity?,
    onToggleFocus: () -> Unit,
    onClick: () -> Unit
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val activeTask =
        activeSession?.let { session -> allNodes.find { it.node.id == session.nodeId }?.node }

    DashCard(onClick = onClick) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        if (activeSession != null) "Deep Work Phase" else "System Standby",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TactileTheme.Text
                    )
                }
                if (activeSession != null) {
                    val duration =
                        (Clock.System.now().toEpochMilliseconds() - activeSession.startedAt) / 1000
                    val h = duration / 3600;
                    val m = (duration % 3600) / 60;
                    val s = duration % 60
                    Text(
                        "${h.toString().padStart(2, '0')}:${
                            m.toString().padStart(2, '0')
                        }:${s.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (activeTask != null) "Active: ${activeTask.title}" else "Ready to engage",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = activeSession != null,
                    onCheckedChange = { onToggleFocus() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TactileTheme.Primary,
                        uncheckedThumbColor = TactileTheme.Muted,
                        uncheckedTrackColor = TactileTheme.Surface
                    )
                )
            }
        }
    }
}

@Composable
fun LifeSummaryCard(captures: Int, completions: Int, onClick: () -> Unit) {
    DashCard(onClick = onClick) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Text(
                "LIFE SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "$captures",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TactileTheme.Text
                    ); Text(
                    "CAPTURES / WEEK",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$completions",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TactileTheme.Success
                    ); Text(
                    "DONE / WEEK",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
                }
            }
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            LinearProgressIndicator(
                progress = {
                    if (captures > 0) (completions.toFloat() / captures.toFloat()).coerceIn(
                        0f,
                        1f
                    ) else 0f
                },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = TactileTheme.Primary,
                trackColor = TactileTheme.Border,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    secondaryLabel: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    DashCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    secondaryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TactileTheme.Text,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VaultCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border)
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Icon(
                icon,
                contentDescription = null,
                tint = TactileTheme.Accent,
                modifier = Modifier.size(20.dp)
            ); Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        ); Text(
            "$count ITEMS",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
            fontSize = 8.sp
        )
        }
    }
}

@Composable
fun ProgressRing(progress: Float) {
    val anim by animateFloatAsState(targetValue = progress)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
        Canvas(modifier = Modifier.size(60.dp)) {
            drawCircle(color = Color.White.copy(alpha = 0.1f), style = Stroke(width = 4.dp.toPx()))
            drawArc(
                color = TactileTheme.Primary,
                startAngle = -90f,
                sweepAngle = 360 * anim,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TactileTheme.Text
        )
    }
}

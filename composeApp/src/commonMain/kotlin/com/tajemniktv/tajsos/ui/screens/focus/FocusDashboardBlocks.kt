/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.AbortSlider
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.focus_engage
import tajsos.composeapp.generated.resources.focus_next_tiny_step
import tajsos.composeapp.generated.resources.focus_no_active_task
import tajsos.composeapp.generated.resources.focus_recent_sessions
import tajsos.composeapp.generated.resources.focus_session_duration
import tajsos.composeapp.generated.resources.focus_unknown_task

object FocusDashboardBlockRegistry {
    private val renderers: Map<String, FocusDashboardBlockRenderer> =
        mapOf("focus_main" to ::renderFocusMainBlock)

    fun resolve(id: String): FocusDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderFocusMainBlock(context: FocusDashboardContext) {
    FocusMainBlock(viewModel = context.viewModel)
}

/**
 * Displays the focus cockpit with a circular timer dial, active task controls, and recent sessions.
 *
 * The screen preserves existing focus behavior by using the same view model actions for starting,
 * stopping, and editing focus content while presenting the data with a denser visual layout.
 *
 * @param viewModel The MainViewModel exposing focus state, nodes, sessions, and actions.
 */
@Composable
internal fun FocusMainBlock(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    val currentNode =
        remember(activeSession, todayNodes, allNodes) {
            activeSession?.let { session ->
                todayNodes.find { it.id == session.nodeId }
                    ?: allNodes.find { it.node.id == session.nodeId }?.node
            } ?: todayNodes.firstOrNull()
        }

    var timeSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            while (true) {
                val now =
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds()
                val session = activeSession
                if (session != null) {
                    timeSeconds = ((now - session.startedAt) / 1000L).toInt()
                }
                delay(1000L)
            }
        } else {
            timeSeconds = 0
        }
    }

    if (currentNode == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.focus_no_active_task),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
            )
        }
        return
    }

    val minutes = timeSeconds / 60
    val seconds = timeSeconds % 60
    val timeString = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    val targetDurationSec = 25 * 60
    val progress =
        if (activeSession == null) 0f else (timeSeconds % targetDurationSec) / targetDurationSec.toFloat()
    val finishedSessions = allSessions.count { it.endedAt != null }
    val totalFocusedMinutes = allSessions.sumOf { it.durationSec } / 60

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(0xFF090A12),
                                    Color(0xFF0D1021),
                                    Color(0xFF08090F),
                                ),
                        ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = TactileTheme.SpacingLg,
                        vertical = TactileTheme.SpacingMd,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "DEEP WORK MODE",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        color = TactileTheme.Primary.copy(alpha = 0.8f),
                    ),
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))
            Text(
                text = currentNode.title,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.SemiBold,
                    ),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(TactileTheme.SpacingLg))
            FocusTimerDial(
                timeString = timeString,
                progress = progress,
                finishedSessions = finishedSessions,
                totalFocusedMinutes = totalFocusedMinutes,
            )
            Spacer(Modifier.height(TactileTheme.SpacingLg))

            OutlinedTextField(
                value = currentNode.content,
                onValueChange = { viewModel.updateNode(currentNode.copy(content = it)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                label = {
                    Text(
                        stringResource(Res.string.focus_next_tiny_step),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TactileTheme.Text),
                singleLine = true,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TactileTheme.Primary,
                        unfocusedBorderColor = TactileTheme.Border,
                        focusedContainerColor = Color(0x22262B3A),
                        unfocusedContainerColor = Color(0x1A262B3A),
                        focusedTextColor = TactileTheme.Text,
                        unfocusedTextColor = TactileTheme.Text,
                    ),
            )

            Spacer(Modifier.height(TactileTheme.SpacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
            ) {
                FocusActionCard(
                    modifier = Modifier.weight(1f),
                    icon = if (activeSession == null) Icons.Default.PlayArrow else Icons.Default.Stop,
                    label = if (activeSession == null) stringResource(Res.string.focus_engage) else "STOP",
                    onClick = {
                        if (activeSession == null) {
                            viewModel.startFocusSession(currentNode.id)
                        } else {
                            viewModel.stopFocusSession()
                        }
                    },
                    isDanger = activeSession != null,
                )
                FocusStatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.focus_recent_sessions),
                    value = finishedSessions.toString().padStart(2, '0'),
                )
                FocusStatCard(
                    modifier = Modifier.weight(1f),
                    label = "FOCUS MIN",
                    value = totalFocusedMinutes.toString().padStart(2, '0'),
                )
            }

            if (activeSession != null) {
                Spacer(Modifier.height(TactileTheme.SpacingMd))
                AbortSlider(onAbort = { viewModel.stopFocusSession() })
            }

            Spacer(Modifier.height(TactileTheme.SpacingMd))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(allSessions.take(5)) { session ->
                    if (session.endedAt != null) {
                        val sessionNode =
                            remember(session, allNodes) {
                                allNodes.find { it.node.id == session.nodeId }?.node
                            }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x1AFFFFFF),
                            border =
                                androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    TactileTheme.Border,
                                ),
                            modifier = Modifier.width(230.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text =
                                        sessionNode?.title
                                            ?: stringResource(Res.string.focus_unknown_task),
                                    style = MaterialTheme.typography.labelLarge.copy(color = TactileTheme.Text),
                                    maxLines = 1,
                                )
                                Text(
                                    text =
                                        stringResource(
                                            Res.string.focus_session_duration,
                                            session.durationSec / 60,
                                            session.durationSec % 60,
                                        ),
                                    style = MaterialTheme.typography.labelSmall.copy(color = TactileTheme.Muted),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTimerDial(
    timeString: String,
    progress: Float,
    finishedSessions: Int,
    totalFocusedMinutes: Int,
) {
    Box(
        modifier =
            Modifier
                .size(360.dp)
                .background(Color(0x120D1233), RoundedCornerShape(28.dp))
                .border(1.dp, Color(0x332C3358), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val strokeWidth = 12.dp.toPx()
            val sweep = 360f * progress.coerceIn(0f, 1f)
            drawArc(
                color = Color(0x1FB4A9FF),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
            )
            drawArc(
                brush = Brush.linearGradient(colors = listOf(Color(0xFF8B5CF6), Color(0xFFAA77FF))),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeString,
                style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontSize = 86.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-2).sp,
                    ),
                color = TactileTheme.Text,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                FocusDialMetric(
                    value = finishedSessions.toString().padStart(2, '0'),
                    label = "SESSIONS",
                )
                FocusDialMetric(
                    value = totalFocusedMinutes.toString().padStart(2, '0'),
                    label = "FOCUS MIN",
                )
            }
        }
    }
}

@Composable
private fun FocusDialMetric(
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(color = TactileTheme.Text),
        )
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    color = TactileTheme.Muted,
                    letterSpacing = 1.sp,
                ),
        )
    }
}

@Composable
private fun FocusActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isDanger: Boolean = false,
) {
    ElevatedCard(
        modifier =
            modifier.border(
                width = 1.dp,
                color = TactileTheme.Border,
                shape = RoundedCornerShape(18.dp),
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0x1E1F2436)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Button(
            onClick = onClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(98.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = if (isDanger) Color(0xFFFF6B8A) else TactileTheme.Primary,
                ),
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier =
                        Modifier
                            .size(38.dp)
                            .background(Color(0x20000000), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = label,
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                )
            }
        }
    }
}

@Composable
private fun FocusStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    ElevatedCard(
        modifier =
            modifier
                .height(98.dp)
                .border(
                    width = 1.dp,
                    color = TactileTheme.Border,
                    shape = RoundedCornerShape(18.dp),
                ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0x1E1F2436)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        color = TactileTheme.Muted,
                        letterSpacing = 1.sp,
                    ),
            )
        }
    }
}

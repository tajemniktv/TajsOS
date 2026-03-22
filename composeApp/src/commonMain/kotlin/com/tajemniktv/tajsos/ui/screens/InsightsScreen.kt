/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun InsightsScreen(viewModel: MainViewModel, onNavigateToProject: (Long) -> Unit) {
    val insights by viewModel.insights.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
    ) {
        item {
            Text(
                "INSIGHTS & REVIEW",
                style = MaterialTheme.typography.displayMedium,
                color = TactileTheme.Text
            )
            Text(
                "LAST 7 DAYS PATTERNS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(TactileTheme.SpacingLg))
        }

        item {
            CompletionCard(
                insights.weeklyCaptures,
                insights.weeklyCompletions
            )
        }

        item {
            FocusInsightCard(
                insights.weeklyFocusHours,
                insights.bestFocusHour
            )
        }

        item {
            StateAveragesCard(
                insights.avgMood,
                insights.avgEnergy,
                insights.avgFocus
            )
        }

        if (insights.neglectedProjects.isNotEmpty()) {
            item {
                Text(
                    "NEGLECTED PROJECTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
            }
            items(insights.neglectedProjects) { project ->
                NeglectedProjectItem(project) {
                    onNavigateToProject(
                        project.id
                    )
                }
            }
        }

        if (recentLogs.isNotEmpty()) {
            item {
                Text(
                    "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
            }
            items(recentLogs) { log ->
                ActivityLogItem(log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(TactileTheme.SpacingLg))
        }
    }
}

@Composable
fun CompletionCard(captures: Int, completions: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text("CAPACITY", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("$captures", style = MaterialTheme.typography.displaySmall, color = TactileTheme.Text)
                    Text("CAPTURES", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$completions", style = MaterialTheme.typography.displaySmall, color = TactileTheme.Success)
                    Text("COMPLETIONS", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
                }
            }
            Spacer(Modifier.height(16.dp))
            val rate = if (captures > 0) completions.toFloat() / captures else 0f
            LinearProgressIndicator(
                progress = { rate },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (rate >= 0.8f) TactileTheme.Success else TactileTheme.Primary,
                trackColor = TactileTheme.Muted.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun FocusInsightCard(hours: Double, bestHour: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text("EXECUTION", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${((hours * 10).toInt() / 10.0)}", style = MaterialTheme.typography.displaySmall, color = TactileTheme.Text)
                    Text("FOCUS HOURS", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
                }
                if (bestHour != -1) {
                    Column(horizontalAlignment = Alignment.End) {
                        val formattedHour = if (bestHour < 10) "0$bestHour:00" else "$bestHour:00"
                        Text(formattedHour, style = MaterialTheme.typography.displaySmall, color = TactileTheme.Primary)
                        Text("PEAK FOCUS HOUR", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
                    }
                }
            }
        }
    }
}

@Composable
fun StateAveragesCard(mood: Double, energy: Double, focus: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text("BIOMETRICS", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricItem("MOOD", mood)
                MetricItem("ENERGY", energy)
                MetricItem("FOCUS", focus)
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${((value * 10).toInt() / 10.0)}", style = MaterialTheme.typography.headlineMedium, color = TactileTheme.Text)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
    }
}

@Composable
fun ActivityLogItem(log: EventLogEntity) {
    val time = kotlin.time.Instant.fromEpochMilliseconds(log.timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr = "${time.hour}:${time.minute.toString().padStart(2, '0')}"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Muted.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.width(48.dp)
            )
            Text(
                log.eventType.replace("_", " "),
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NeglectedProjectItem(project: NodeEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Error.copy(alpha = 0.3f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = TactileTheme.Error)
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column {
                Text(project.title.uppercase(), style = MaterialTheme.typography.titleMedium, color = TactileTheme.Text)
                Text("NEEDS ATTENTION", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Error)
            }
        }
    }
}

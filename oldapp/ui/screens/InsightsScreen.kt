/*
 * Copyright (c) TajemnikTV 2026. All rights reserved. 
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
import com.tajemniktv.tajsos.data.ProjectEntity
import java.util.Locale

/**
 * InsightsScreen handles Phase 6: Insight and Review.
 * It surfaces patterns from passive data (focus sessions, completions)
 * and active state tracking to help the user make better decisions.
 */
@Composable
fun InsightsScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel, onNavigateToProject: (Long) -> Unit) {
    val insights by viewModel.insights.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
    ) {
        item {
            Text(
                "INSIGHTS & REVIEW",
                style = MaterialTheme.typography.displayMedium,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
            )
            Text(
                "LAST 7 DAYS PATTERNS",
                style = MaterialTheme.typography.labelSmall,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
            )
            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))
        }

        item {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.CompletionCard(
                insights.weeklyCaptures,
                insights.weeklyCompletions
            )
        }

        item {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.FocusInsightCard(
                insights.weeklyFocusHours,
                insights.bestFocusHour
            )
        }

        item {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.StateAveragesCard(
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
                    color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
                )
            }
            items(insights.neglectedProjects) { project ->
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.NeglectedProjectItem(project) {
                    onNavigateToProject(
                        project.id
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))
        }
    }
}

@Composable
fun CompletionCard(captures: Int, completions: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
            Text("CAPACITY", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("$captures", style = MaterialTheme.typography.displaySmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text)
                    Text("CAPTURES", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$completions", style = MaterialTheme.typography.displaySmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Success)
                    Text("COMPLETIONS", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
                }
            }
            Spacer(Modifier.height(16.dp))
            val rate = if (captures > 0) completions.toFloat() / captures else 0f
            LinearProgressIndicator(
                progress = { rate },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (rate >= 0.8f) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Success else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                trackColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun FocusInsightCard(hours: Double, bestHour: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
            Text("EXECUTION", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(String.format(Locale.getDefault(), "%.1f", hours), style = MaterialTheme.typography.displaySmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text)
                    Text("FOCUS HOURS", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
                }
                if (bestHour != -1) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(String.format(Locale.getDefault(), "%02d:00", bestHour), style = MaterialTheme.typography.displaySmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
                        Text("PEAK FOCUS HOUR", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
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
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
            Text("BIOMETRICS", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.MetricItem("MOOD", mood)
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.MetricItem("ENERGY", energy)
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.MetricItem("FOCUS", focus)
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(String.format(Locale.getDefault(), "%.1f", value), style = MaterialTheme.typography.headlineMedium, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text)
        Text(label, style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
    }
}

@Composable
fun NeglectedProjectItem(project: ProjectEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Error.copy(alpha = 0.3f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Error)
            Spacer(Modifier.width(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))
            Column {
                Text(project.name.uppercase(), style = MaterialTheme.typography.titleMedium, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text)
                Text("NEEDS ATTENTION", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Error)
            }
        }
    }
}

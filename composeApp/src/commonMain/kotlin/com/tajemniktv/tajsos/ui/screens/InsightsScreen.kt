/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@Composable
fun InsightsScreen(viewModel: MainViewModel, onNavigateToProject: (Long) -> Unit) {
    val insights by viewModel.insights.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
    ) {
        item {
            Text(
                stringResource(Res.string.insights_title),
                style = MaterialTheme.typography.displayMedium,
                color = TactileTheme.Text
            )
            Text(
                stringResource(Res.string.insights_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(TactileTheme.SpacingLg))
        }

        if (insights.autoPreparedReview.isNotBlank()) {
            item {
                AutoReviewCard(insights.autoPreparedReview)
            }
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
                insights.bestFocusHour,
                insights.avgSessionMinutes
            )
        }

        item {
            EfficiencyCard(
                insights.archiveRate,
                insights.postponeFrequency,
                insights.backlogPressure,
                insights.mostProductiveHour,
                insights.chaosScore,
                insights.contextSwitchingRate
            )
        }

        item {
            VaultInsightCard(
                insights.inboxGrowth,
                insights.weeklyCaptures
            )
        }

        item {
            AdvancedSystemCard(
                insights.contextStability,
                insights.passiveBehaviorSummary
            )
        }

        item {
            StateAveragesCard(
                insights.avgMood,
                insights.avgEnergy,
                insights.avgFocus
            )
        }

        item {
            CorrelationsCard(
                insights.moodVsCompletions,
                insights.sleepVsFocus,
                insights.energyVsCaptures,
                insights.anxietyVsAvoidance,
                insights.medsEffectiveness
            )
        }

        item {
            Text(
                stringResource(Res.string.insights_cards_title),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
        }

        if (insights.mostPostponedAreaId != null) {
            val area = allAreas.find { it.id == insights.mostPostponedAreaId }
            if (area != null) {
                item {
                    InsightPatternCard(
                        title = stringResource(Res.string.insights_friction_alert_title),
                        message = stringResource(
                            Res.string.insights_friction_alert_msg,
                            area.title
                        ),
                        icon = Icons.Default.History,
                        color = TactileTheme.Error
                    )
                }
            }
        }

        if (insights.captureTimePattern != null) {
            item {
                InsightPatternCard(
                    title = stringResource(Res.string.insights_creative_peak_title),
                    message = stringResource(
                        Res.string.insights_creative_peak_msg,
                        insights.captureTimePattern!!
                    ),
                    icon = Icons.Default.Lightbulb,
                    color = TactileTheme.Success
                )
            }
        }

        if (insights.projectsWithoutTasks.isNotEmpty()) {
            item {
                InsightPatternCard(
                    title = stringResource(Res.string.insights_stagnant_knowledge_title),
                    message = stringResource(
                        Res.string.insights_stagnant_knowledge_msg,
                        insights.projectsWithoutTasks.first().title
                    ),
                    icon = Icons.Default.Warning,
                    color = TactileTheme.Accent
                )
            }
        }

        if (insights.neglectedAreas.isNotEmpty()) {
            val area = insights.neglectedAreas.first()
            item {
                InsightPatternCard(
                    title = stringResource(Res.string.insights_radar_drop_title),
                    message = stringResource(Res.string.insights_radar_drop_msg, area.title),
                    icon = Icons.Default.LocationOff,
                    color = TactileTheme.Error
                )
            }
        }

        if (insights.neglectedProjects.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.insights_neglected_projects),
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

        val highEntropyProjects = insights.projectEntropy.filter { it.value > 0.5 }
        if (highEntropyProjects.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.insights_high_entropy_projects),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error
                )
            }
            items(highEntropyProjects.keys.toList()) { projectId ->
                val project =
                    viewModel.allProjects.collectAsState().value.find { it.id == projectId }
                if (project != null) {
                    ProjectEntropyItem(project, highEntropyProjects[projectId] ?: 0.0) {
                        onNavigateToProject(project.id)
                    }
                }
            }
        }

        if (recentLogs.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.insights_recent_activity),
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
fun AutoReviewCard(review: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Primary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Primary.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_auto_prepared_review),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                review,
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Text
            )
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
            Text(
                stringResource(Res.string.insights_capacity),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("$captures", style = MaterialTheme.typography.displaySmall, color = TactileTheme.Text)
                    Text(
                        stringResource(Res.string.insights_captures),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$completions", style = MaterialTheme.typography.displaySmall, color = TactileTheme.Success)
                    Text(
                        stringResource(Res.string.insights_completions),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
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
fun FocusInsightCard(hours: Double, bestHour: Int, avgMin: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_focus_execution),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${((hours * 10).toInt() / 10.0)}", style = MaterialTheme.typography.displaySmall, color = TactileTheme.Text)
                    Text(
                        stringResource(Res.string.insights_total_hours),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$avgMin",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Primary
                    )
                    Text(
                        stringResource(Res.string.insights_avg_session_min),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
            }
            if (bestHour != -1) {
                Spacer(Modifier.height(8.dp))
                val formattedHour = if (bestHour < 10) "0$bestHour:00" else "$bestHour:00"
                Text(
                    stringResource(Res.string.insights_peak_focus, formattedHour),
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted
                )
            }
        }
    }
}

@Composable
fun EfficiencyCard(
    archiveRate: Double,
    postpones: Int,
    pressure: Double,
    productiveHour: Int,
    chaos: Int,
    switching: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Muted.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_efficiency_chaos),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(archiveRate * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TactileTheme.Text
                    )
                    Text(
                        stringResource(Res.string.insights_archive),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$postpones",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TactileTheme.Error
                    )
                    Text(
                        stringResource(Res.string.insights_postpones),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$chaos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (chaos > 50) TactileTheme.Error else TactileTheme.Text
                    )
                    Text(
                        stringResource(Res.string.insights_chaos),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    val pressureLabel =
                        if (pressure > 5.0) "HIGH" else if (pressure > 2.0) "MED" else "LOW"
                    Text(
                        stringResource(Res.string.insights_pressure, pressureLabel),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (pressure > 5.0) TactileTheme.Error else TactileTheme.Muted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(Res.string.insights_switching, switching),
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted
                    )
                }
            }
            if (productiveHour != -1) {
                Spacer(Modifier.height(8.dp))
                val formattedHour =
                    if (productiveHour < 10) "0$productiveHour:00" else "$productiveHour:00"
                Text(
                    stringResource(Res.string.insights_peak_completions, formattedHour),
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Accent
                )
            }
        }
    }
}

@Composable
fun VaultInsightCard(inboxGrowth: Int, weeklyCaptures: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Muted.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_intake_dynamics),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "$inboxGrowth",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Accent
                    )
                    Text(
                        stringResource(Res.string.insights_new_inbox_items),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val ratio =
                        if (weeklyCaptures > 0) (inboxGrowth.toDouble() / weeklyCaptures * 100).toInt() else 0
                    Text(
                        "$ratio%",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Text
                    )
                    Text(
                        stringResource(Res.string.insights_unprocessed_rate),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedSystemCard(stability: Double, summary: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Muted.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_system_stability),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val stabilityLabel =
                        if (stability > 0.7) "ROCK SOLID" else if (stability > 0.4) "STABLE" else "FLUID"
                    Text(
                        stabilityLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TactileTheme.Text
                    )
                    Text(
                        stringResource(Res.string.insights_context_stability),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
                CircularProgressIndicator(
                    progress = { stability.toFloat() },
                    modifier = Modifier.size(40.dp),
                    color = if (stability > 0.5) TactileTheme.Success else TactileTheme.Primary,
                    trackColor = TactileTheme.Border,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            if (summary.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Text.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ProjectEntropyItem(project: NodeEntity, entropy: Double, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Muted.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = TactileTheme.Text
                )
                Text(
                    "Entropy: ${(entropy * 100).toInt()}% (Unstructured/Postponed)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
            }
            LinearProgressIndicator(
                progress = { entropy.toFloat() },
                modifier = Modifier.width(60.dp).height(4.dp),
                color = if (entropy > 0.7) TactileTheme.Error else TactileTheme.Primary,
                trackColor = TactileTheme.Border,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
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
            Text(
                stringResource(Res.string.insights_biometrics),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricItem(stringResource(Res.string.track_label_mood), mood)
                MetricItem(stringResource(Res.string.track_label_energy), energy)
                MetricItem(stringResource(Res.string.track_label_focus), focus)
            }
        }
    }
}

@Composable
fun CorrelationsCard(
    moodVsComp: Double,
    sleepVsFocus: Double,
    energyVsCapt: Double,
    avoid: Double,
    medsEffect: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Muted.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_lifestyle_correlations),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(16.dp))

            CorrelationItem(
                "${stringResource(Res.string.track_label_mood)} → ${stringResource(Res.string.insights_completions)}",
                moodVsComp,
                "Higher mood on productive days",
                "Productive days don't affect mood"
            )
            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = TactileTheme.Muted.copy(alpha = 0.1f)
            )
            CorrelationItem(
                "${
                    stringResource(
                        Res.string.track_label_sleep,
                        ""
                    )
                } → ${stringResource(Res.string.track_label_focus)}",
                sleepVsFocus,
                "Good sleep boosts focus time",
                "Sleep doesn't seem to impact focus"
            )
            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = TactileTheme.Muted.copy(alpha = 0.1f)
            )
            CorrelationItem(
                "${stringResource(Res.string.track_label_energy)} → ${stringResource(Res.string.insights_captures)}",
                energyVsCapt,
                "High energy leads to more capture",
                "Capture rate is energy-independent"
            )
            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = TactileTheme.Muted.copy(alpha = 0.1f)
            )
            CorrelationItem(
                "${stringResource(Res.string.track_meds)} → ${stringResource(Res.string.track_label_focus)}",
                medsEffect,
                "Medication improves your focus",
                "No clear meds impact on focus"
            )

            if (avoid > 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(Res.string.insights_avoidance_pattern, avoid.toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error
                )
            }
        }
    }
}

@Composable
fun CorrelationItem(label: String, value: Double, positiveMsg: String, neutralMsg: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
        Text(
            if (value > 0.5) positiveMsg else neutralMsg,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value > 0.5) TactileTheme.Success else TactileTheme.Text
        )
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
                Text(
                    stringResource(Res.string.insights_needs_attention),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error
                )
            }
        }
    }
}

@Composable
fun InsightPatternCard(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = color)
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Text
                )
            }
        }
    }
}

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
import com.tajemniktv.tajsos.ui.AreaHealthMetrics
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Render the Insights screen, displaying computed insight cards, pattern alerts, project lists, and recent activity
 * based on the provided view model state.
 *
 * The UI reacts to the view model's current insights, recent logs, and areas, conditionally showing cards and
 * lists (e.g., auto-review, completion/focus/efficiency/vault/system summaries, pattern alerts, neglected and
 * high-entropy projects, and recent activity). Project items call the navigation callback when selected.
 *
 * @param viewModel Source of truth for insights, recent activity, and area/project data.
 * @param onNavigateToProject Callback invoked with a project's id when the user selects a project item.
 */
@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    onNavigateToProject: (Long) -> Unit,
) {
    val insights by viewModel.insights.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val areaSnapshot by viewModel.areaHealthSnapshot.collectAsState()

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        item {
            Text(
                stringResource(Res.string.insights_title),
                style = MaterialTheme.typography.displayMedium,
                color = TactileTheme.Text,
            )
            Text(
                stringResource(Res.string.insights_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
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
                insights.weeklyCompletions,
            )
        }

        item {
            FocusInsightCard(
                insights.weeklyFocusHours,
                insights.bestFocusHour,
                insights.avgSessionMinutes,
            )
        }

        item {
            EfficiencyCard(
                insights.archiveRate,
                insights.postponeFrequency,
                insights.backlogPressure,
                insights.mostProductiveHour,
                insights.chaosScore,
                insights.contextSwitchingRate,
            )
        }

        item {
            VaultInsightCard(
                insights.inboxGrowth,
                insights.weeklyCaptures,
            )
        }

        item {
            AdvancedSystemCard(
                insights.contextStability,
                insights.passiveBehaviorSummary,
            )
        }

        item {
            StateAveragesCard(
                insights.avgMood,
                insights.avgEnergy,
                insights.avgFocus,
            )
        }

        item {
            CorrelationsCard(
                insights.moodVsCompletions,
                insights.sleepVsFocus,
                insights.energyVsCaptures,
                insights.anxietyVsAvoidance,
                insights.medsEffectiveness,
            )
        }

        item {
            Text(
                stringResource(Res.string.insights_cards_title),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
        }

        if (areaSnapshot.areas.isNotEmpty()) {
            item {
                AreaHealthSystemCard(
                    dominantArea = allAreas.find { it.id == areaSnapshot.dominantAreaId }?.title,
                    imbalanceScore = areaSnapshot.imbalanceScore,
                    imbalanceLabel = areaSnapshot.imbalanceLabel,
                    disappearingCount = areaSnapshot.disappearingAreaIds.size,
                )
            }
            items(areaSnapshot.areas.take(4)) { area ->
                AreaHealthInsightItem(area)
            }
        }

        if (insights.mostPostponedAreaId != null) {
            val area = allAreas.find { it.id == insights.mostPostponedAreaId }
            if (area != null) {
                item {
                    InsightPatternCard(
                        title = stringResource(Res.string.insights_friction_alert_title),
                        message =
                            stringResource(
                                Res.string.insights_friction_alert_msg,
                                area.title,
                            ),
                        icon = Icons.Default.History,
                        color = TactileTheme.Error,
                    )
                }
            }
        }

        if (insights.captureTimePattern != null) {
            item {
                InsightPatternCard(
                    title = stringResource(Res.string.insights_creative_peak_title),
                    message =
                        stringResource(
                            Res.string.insights_creative_peak_msg,
                            insights.captureTimePattern!!,
                        ),
                    icon = Icons.Default.Lightbulb,
                    color = TactileTheme.Success,
                )
            }
        }

        if (insights.projectsWithoutTasks.isNotEmpty()) {
            item {
                InsightPatternCard(
                    title = stringResource(Res.string.insights_stagnant_knowledge_title),
                    message =
                        stringResource(
                            Res.string.insights_stagnant_knowledge_msg,
                            insights.projectsWithoutTasks.first().title,
                        ),
                    icon = Icons.Default.Warning,
                    color = TactileTheme.Accent,
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
                    color = TactileTheme.Error,
                )
            }
        }

        if (insights.neglectedProjects.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.insights_neglected_projects),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
            }
            items(insights.neglectedProjects) { project ->
                NeglectedProjectItem(project) {
                    onNavigateToProject(
                        project.id,
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
                    color = TactileTheme.Error,
                )
            }
            items(highEntropyProjects.keys.toList()) { projectId ->
                val project =
                    viewModel.allProjects
                        .collectAsState()
                        .value
                        .find { it.id == projectId }
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
                    color = TactileTheme.Primary,
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

/**
 * Displays a styled card containing an auto-prepared review message.
 *
 * @param review The review text to display inside the card; may be blank.
 */
@Composable
fun AutoReviewCard(review: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Primary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Primary.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_auto_prepared_review),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                review,
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Text,
            )
        }
    }
}

/**
 * Displays a themed card showing capture and completion counts alongside a progress indicator of completion rate.
 *
 * @param captures Total number of captures.
 * @param completions Number of captures that were completed; used to compute the completion progress shown.
 */
@Composable
fun CompletionCard(
    captures: Int,
    completions: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_capacity),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        "$captures",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Text,
                    )
                    Text(
                        stringResource(Res.string.insights_captures),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$completions",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Success,
                    )
                    Text(
                        stringResource(Res.string.insights_completions),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            val rate = if (captures > 0) completions.toFloat() / captures else 0f
            LinearProgressIndicator(
                progress = { rate },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (rate >= 0.8f) TactileTheme.Success else TactileTheme.Primary,
                trackColor = TactileTheme.Muted.copy(alpha = 0.2f),
            )
        }
    }
}

/**
 * Displays a card summarizing focus insights: total focus hours, average session length, and an optional peak hour.
 *
 * @param hours Total focus time in hours.
 * @param bestHour Hour of day with peak focus (0–23); provide `-1` when no peak is available.
 * @param avgMin Average session duration in minutes.
 */
@Composable
fun FocusInsightCard(
    hours: Double,
    bestHour: Int,
    avgMin: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_focus_execution),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        "${((hours * 10).toInt() / 10.0)}",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Text,
                    )
                    Text(
                        stringResource(Res.string.insights_total_hours),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$avgMin",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Primary,
                    )
                    Text(
                        stringResource(Res.string.insights_avg_session_min),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
            }
            if (bestHour != -1) {
                Spacer(Modifier.height(8.dp))
                val formattedHour = if (bestHour < 10) "0$bestHour:00" else "$bestHour:00"
                Text(
                    stringResource(Res.string.insights_peak_focus, formattedHour),
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }
}

/**
 * Renders a card showing efficiency-related metrics: archive rate, number of postpones,
 * chaos score, pressure level, switching metric, and an optional peak productive hour.
 *
 * @param archiveRate Fractional archive rate (0.0–1.0) shown as a percentage.
 * @param postpones Number of postponed items.
 * @param pressure Numeric pressure score used to derive a pressure label.
 * @param productiveHour Hour of day (0–23) representing peak completions, or `-1` if not available.
 * @param chaos Chaos score (typically 0–100) indicating disorder level.
 * @param switching Switching metric shown with one decimal of precision.
 */
@Composable
fun EfficiencyCard(
    archiveRate: Double,
    postpones: Int,
    pressure: Double,
    productiveHour: Int,
    chaos: Int,
    switching: Double,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_efficiency_chaos),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(archiveRate * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TactileTheme.Text,
                    )
                    Text(
                        stringResource(Res.string.insights_archive),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$postpones",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TactileTheme.Error,
                    )
                    Text(
                        stringResource(Res.string.insights_postpones),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$chaos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (chaos > 50) TactileTheme.Error else TactileTheme.Text,
                    )
                    Text(
                        stringResource(Res.string.insights_chaos),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    val pressureLabel =
                        if (pressure > 5.0) {
                            "HIGH"
                        } else if (pressure > 2.0) {
                            "MED"
                        } else {
                            "LOW"
                        }
                    Text(
                        stringResource(Res.string.insights_pressure, pressureLabel),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (pressure > 5.0) TactileTheme.Error else TactileTheme.Muted,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(
                            Res.string.insights_switching,
                            // Pre-format to 1 decimal place as CMP formatter only supports %d and %s
                            ((switching * 10).toInt() / 10.0).toString(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
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
                    color = TactileTheme.Accent,
                )
            }
        }
    }
}

/**
 * Displays an insights card showing inbox growth and the percentage of unprocessed captures.
 *
 * The card presents the raw `inboxGrowth` count and computes an unprocessed rate as
 * (inboxGrowth / weeklyCaptures * 100) rounded down to an integer percent; if `weeklyCaptures`
 * is zero or negative the rate is shown as 0%.
 *
 * @param inboxGrowth Number of new inbox items during the measured period.
 * @param weeklyCaptures Total captures during the same period used to compute the unprocessed rate.
 */
@Composable
fun VaultInsightCard(
    inboxGrowth: Int,
    weeklyCaptures: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_intake_dynamics),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        "$inboxGrowth",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Accent,
                    )
                    Text(
                        stringResource(Res.string.insights_new_inbox_items),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val ratio =
                        if (weeklyCaptures > 0) (inboxGrowth.toDouble() / weeklyCaptures * 100).toInt() else 0
                    Text(
                        "$ratio%",
                        style = MaterialTheme.typography.displaySmall,
                        color = TactileTheme.Text,
                    )
                    Text(
                        stringResource(Res.string.insights_unprocessed_rate),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
            }
        }
    }
}

/**
 * Displays a card summarizing system stability with a circular progress indicator and an optional summary.
 *
 * The visual label and indicator reflect the provided stability score:
 * - "ROCK SOLID" when stability > 0.7
 * - "STABLE" when stability > 0.4
 * - "FLUID" otherwise
 * The circular indicator uses the success color when stability > 0.5 and the primary color otherwise.
 *
 * @param stability Stability score expected in the range 0.0..1.0; drives the label, progress, and indicator color.
 * @param summary Optional explanatory text; rendered only when `summary` is not blank.
 */
@Composable
fun AdvancedSystemCard(
    stability: Double,
    summary: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_system_stability),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val stabilityLabel =
                        if (stability > 0.7) {
                            "ROCK SOLID"
                        } else if (stability > 0.4) {
                            "STABLE"
                        } else {
                            "FLUID"
                        }
                    Text(
                        stabilityLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TactileTheme.Text,
                    )
                    Text(
                        stringResource(Res.string.insights_context_stability),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                CircularProgressIndicator(
                    progress = { stability.toFloat() },
                    modifier = Modifier.size(40.dp),
                    color = if (stability > 0.5) TactileTheme.Success else TactileTheme.Primary,
                    trackColor = TactileTheme.Border,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
            if (summary.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Text.copy(alpha = 0.8f),
                )
            }
        }
    }
}

/**
 * Displays a clickable card for a project that shows its title, an entropy percentage label,
 * and a horizontal progress bar representing unstructured/postponed entropy.
 *
 * The progress bar is tinted as an alert when `entropy > 0.7` and uses a muted/primary tint otherwise.
 *
 * @param project The project node whose title is displayed.
 * @param entropy A value between 0.0 and 1.0 representing the proportion of unstructured/postponed work; shown as a percent label and as the progress amount.
 * @param onClick Callback invoked when the card is clicked.
 */
@Composable
fun ProjectEntropyItem(
    project: NodeEntity,
    entropy: Double,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.1f),
            ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = TactileTheme.Text,
                )
                Text(
                    "Entropy: ${(entropy * 100).toInt()}% (Unstructured/Postponed)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                )
            }
            LinearProgressIndicator(
                progress = { entropy.toFloat() },
                modifier = Modifier.width(60.dp).height(4.dp),
                color = if (entropy > 0.7) TactileTheme.Error else TactileTheme.Primary,
                trackColor = TactileTheme.Border,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
}

/**
 * Displays a biometrics card showing average mood, energy, and focus metrics.
 *
 * Each value is rendered using MetricItem and presented side-by-side within the card.
 *
 * @param mood Average mood value to display.
 * @param energy Average energy value to display.
 * @param focus Average focus (cognitive) value to display.
 */
@Composable
fun StateAveragesCard(
    mood: Double,
    energy: Double,
    focus: Double,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_biometrics),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MetricItem(stringResource(Res.string.track_label_affective_state), mood)
                MetricItem(stringResource(Res.string.track_label_energy_reserves), energy)
                MetricItem(stringResource(Res.string.track_label_cognitive_lock), focus)
            }
        }
    }
}

/**
 * Displays a card of behavioral correlations between lifestyle signals and productivity metrics.
 *
 * @param moodVsComp Strength (0.0–1.0) of correlation between mood and task completions; higher means stronger positive association.
 * @param sleepVsFocus Strength (0.0–1.0) of correlation between sleep/recovery cycles and focus time; higher means stronger positive association.
 * @param energyVsCapt Strength (0.0–1.0) of correlation between energy reserves and capture activity; higher means stronger positive association.
 * @param avoid Numeric count (as a Double) of detected avoidance incidents; presented as an integer when shown.
 * @param medsEffect Strength (0.0–1.0) of correlation between medication use and cognitive focus; higher means stronger positive association.
 */
@Composable
fun CorrelationsCard(
    moodVsComp: Double,
    sleepVsFocus: Double,
    energyVsCapt: Double,
    avoid: Double,
    medsEffect: Double,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.insights_lifestyle_correlations),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(16.dp))

            CorrelationItem(
                "${stringResource(Res.string.track_label_affective_state)} → ${stringResource(Res.string.insights_completions)}",
                moodVsComp,
                "Higher mood on productive days",
                "Productive days don't affect mood",
            )
            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = TactileTheme.Muted.copy(alpha = 0.1f),
            )
            CorrelationItem(
                "${
                    stringResource(
                        Res.string.track_label_recovery_cycles,
                    )
                } → ${stringResource(Res.string.track_label_cognitive_lock)}",
                sleepVsFocus,
                "Good sleep boosts focus time",
                "Sleep doesn't seem to impact focus",
            )
            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = TactileTheme.Muted.copy(alpha = 0.1f),
            )
            CorrelationItem(
                "${stringResource(Res.string.track_label_energy_reserves)} → ${stringResource(Res.string.insights_captures)}",
                energyVsCapt,
                "High energy leads to more capture",
                "Capture rate is energy-independent",
            )
            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = TactileTheme.Muted.copy(alpha = 0.1f),
            )
            CorrelationItem(
                "${stringResource(Res.string.track_meds)} → ${stringResource(Res.string.track_label_cognitive_lock)}",
                medsEffect,
                "Medication improves your focus",
                "No clear meds impact on focus",
            )

            if (avoid > 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(Res.string.insights_avoidance_pattern, avoid.toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error,
                )
            }
        }
    }
}

/**
 * Displays a correlation label and a message that reflects whether the correlation is strong.
 *
 * Shows `positiveMsg` when `value` is greater than 0.5, otherwise shows `neutralMsg`.
 *
 * @param label Short title for the correlation.
 * @param value Correlation strength, typically in the range 0.0–1.0; higher values indicate stronger positive correlation.
 * @param positiveMsg Message to display when the correlation is considered strong.
 * @param neutralMsg Message to display when the correlation is not considered strong.
 */
@Composable
fun CorrelationItem(
    label: String,
    value: Double,
    positiveMsg: String,
    neutralMsg: String,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
        Text(
            if (value > 0.5) positiveMsg else neutralMsg,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value > 0.5) TactileTheme.Success else TactileTheme.Text,
        )
    }
}

/**
 * Displays a centered metric consisting of a numeric value and a label.
 *
 * The numeric value is shown rounded down to one decimal place.
 *
 * @param label Text label displayed below the value.
 * @param value Numeric value to display (rendered rounded down to one decimal).
 */
@Composable
fun MetricItem(
    label: String,
    value: Double,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${((value * 10).toInt() / 10.0)}",
            style = MaterialTheme.typography.headlineMedium,
            color = TactileTheme.Text,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
    }
}

/**
 * Renders a single activity log row showing the event time and a human-readable event type.
 *
 * The entry displays the log's timestamp converted from epoch milliseconds to local time as `H:MM`
 * (minutes zero-padded) in a fixed-width column, and the event type with underscores replaced by spaces.
 *
 * @param log The event log entity whose `timestamp` (epoch milliseconds) and `eventType` are displayed.
 */
@Composable
fun ActivityLogItem(log: EventLogEntity) {
    val time =
        kotlin.time.Instant
            .fromEpochMilliseconds(log.timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr = "${time.hour}:${time.minute.toString().padStart(2, '0')}"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.1f),
            ),
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.width(48.dp),
            )
            Text(
                log.eventType.replace("_", " "),
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Displays a clickable card highlighting a project that needs attention.
 *
 * Shows the project's title in uppercase alongside a warning icon and an attention label,
 * using error-themed styling and border to emphasize urgency. Invokes the provided callback
 * when the card is clicked.
 *
 * @param project The project entity whose title is displayed.
 * @param onClick Callback invoked when the item is clicked.
 */
@Composable
fun NeglectedProjectItem(
    project: NodeEntity,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TactileTheme.Error.copy(alpha = 0.3f),
            ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = TactileTheme.Error)
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column {
                Text(
                    project.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TactileTheme.Text,
                )
                Text(
                    stringResource(Res.string.insights_needs_attention),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                )
            }
        }
    }
}

/**
 * Displays a full-width, themed card that highlights an insight pattern with an icon, title, and supporting message.
 *
 * The provided `color` is used as the card accent (icon and title) and with reduced alpha for the card background and border.
 *
 * @param title The headline text for the insight.
 * @param message The descriptive text shown beneath the title.
 * @param icon The vector icon displayed to the left of the title.
 * @param color The accent color for the icon and title; also used (with reduced alpha) for the card background and border.
 */
@Composable
fun InsightPatternCard(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = color)
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Text,
                )
            }
        }
    }
}

@Composable
private fun AreaHealthSystemCard(
    dominantArea: String?,
    imbalanceScore: Int,
    imbalanceLabel: String,
    disappearingCount: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                "LIFE AREAS HEALTH",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Dominant area this week: ${dominantArea ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Text,
            )
            Text(
                "Imbalance: $imbalanceScore% (${imbalanceLabel.uppercase()})",
                style = MaterialTheme.typography.bodySmall,
                color = if (imbalanceScore >= 60) TactileTheme.Error else TactileTheme.Muted,
            )
            if (disappearingCount > 0) {
                Text(
                    "Areas disappearing from radar: $disappearingCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error,
                )
            }
        }
    }
}

@Composable
private fun AreaHealthInsightItem(area: AreaHealthMetrics) {
    val color =
        when (area.status)
        {
            "on_fire" -> TactileTheme.Error
            "overloaded" -> TactileTheme.Accent
            "neglected" -> TactileTheme.Muted
            "active" -> TactileTheme.Primary
            else -> TactileTheme.Success
        }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                area.areaTitle.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = TactileTheme.Text,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${area.status.uppercase()} • load ${area.stressLoad}%",
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
            Text(
                "Open loops ${area.openLoops} • Deadlines ${area.deadlines} • Recent ${area.recentActivity}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }
}

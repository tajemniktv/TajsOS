/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.AreaHealthMetrics
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.insights_archive
import tajsos.composeapp.generated.resources.insights_auto_prepared_review
import tajsos.composeapp.generated.resources.insights_avg_session_min
import tajsos.composeapp.generated.resources.insights_avoidance_pattern
import tajsos.composeapp.generated.resources.insights_biometrics
import tajsos.composeapp.generated.resources.insights_capacity
import tajsos.composeapp.generated.resources.insights_captures
import tajsos.composeapp.generated.resources.insights_chaos
import tajsos.composeapp.generated.resources.insights_completions
import tajsos.composeapp.generated.resources.insights_context_stability
import tajsos.composeapp.generated.resources.insights_efficiency_chaos
import tajsos.composeapp.generated.resources.insights_focus_execution
import tajsos.composeapp.generated.resources.insights_intake_dynamics
import tajsos.composeapp.generated.resources.insights_lifestyle_correlations
import tajsos.composeapp.generated.resources.insights_new_inbox_items
import tajsos.composeapp.generated.resources.insights_peak_completions
import tajsos.composeapp.generated.resources.insights_peak_focus
import tajsos.composeapp.generated.resources.insights_postpones
import tajsos.composeapp.generated.resources.insights_pressure
import tajsos.composeapp.generated.resources.insights_switching
import tajsos.composeapp.generated.resources.insights_system_stability
import tajsos.composeapp.generated.resources.insights_total_hours
import tajsos.composeapp.generated.resources.insights_unprocessed_rate
import tajsos.composeapp.generated.resources.track_label_affective_state
import tajsos.composeapp.generated.resources.track_label_cognitive_lock
import tajsos.composeapp.generated.resources.track_label_energy_reserves
import tajsos.composeapp.generated.resources.track_label_recovery_cycles
import tajsos.composeapp.generated.resources.track_meds

@Composable
fun AutoReviewCard(review: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Primary.copy(alpha = 0.05f),
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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
            Text(review, style = MaterialTheme.typography.bodyMedium, color = TactileTheme.Text)
        }
    }
}

@Composable
fun CompletionCard(
    captures: Int,
    completions: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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

@Composable
fun FocusInsightCard(
    hours: Double,
    bestHour: Int,
    avgMin: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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
                MetricSummary(
                    "${(archiveRate * 100).toInt()}%",
                    stringResource(Res.string.insights_archive),
                    TactileTheme.Text,
                )
                MetricSummary(
                    "$postpones",
                    stringResource(Res.string.insights_postpones),
                    TactileTheme.Error,
                )
                MetricSummary(
                    "$chaos",
                    stringResource(Res.string.insights_chaos),
                    if (chaos > 50) TactileTheme.Error else TactileTheme.Text,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
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
                Text(
                    stringResource(
                        Res.string.insights_switching,
                        ((switching * 10).toInt() / 10.0).toString(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
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

@Composable
fun VaultInsightCard(
    inboxGrowth: Int,
    weeklyCaptures: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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

@Composable
fun AdvancedSystemCard(
    stability: Double,
    summary: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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

@Composable
fun StateAveragesCard(
    mood: Double,
    energy: Double,
    focus: Double,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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
                "${stringResource(Res.string.track_label_recovery_cycles)} → ${stringResource(Res.string.track_label_cognitive_lock)}",
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

@Composable
private fun CorrelationItem(
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

@Composable
private fun MetricItem(
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

@Composable
private fun MetricSummary(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
    }
}

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
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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
fun AreaHealthSystemCard(
    dominantArea: String?,
    imbalanceScore: Int,
    imbalanceLabel: String,
    disappearingCount: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
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
fun AreaHealthInsightCard(area: AreaHealthMetrics) {
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
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusSm),
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

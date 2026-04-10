/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.AdvancedSystemCard
import com.tajemniktv.tajsos.ui.components.cards.AreaHealthInsightCard
import com.tajemniktv.tajsos.ui.components.cards.AreaHealthSystemCard
import com.tajemniktv.tajsos.ui.components.cards.AutoReviewCard
import com.tajemniktv.tajsos.ui.components.cards.CompletionCard
import com.tajemniktv.tajsos.ui.components.cards.CorrelationsCard
import com.tajemniktv.tajsos.ui.components.cards.EfficiencyCard
import com.tajemniktv.tajsos.ui.components.cards.FocusInsightCard
import com.tajemniktv.tajsos.ui.components.cards.InsightPatternCard
import com.tajemniktv.tajsos.ui.components.cards.StateAveragesCard
import com.tajemniktv.tajsos.ui.components.cards.VaultInsightCard
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.insights_cards_title
import tajsos.composeapp.generated.resources.insights_creative_peak_msg
import tajsos.composeapp.generated.resources.insights_creative_peak_title
import tajsos.composeapp.generated.resources.insights_friction_alert_msg
import tajsos.composeapp.generated.resources.insights_friction_alert_title
import tajsos.composeapp.generated.resources.insights_high_entropy_projects
import tajsos.composeapp.generated.resources.insights_neglected_projects
import tajsos.composeapp.generated.resources.insights_radar_drop_msg
import tajsos.composeapp.generated.resources.insights_radar_drop_title
import tajsos.composeapp.generated.resources.insights_recent_activity
import tajsos.composeapp.generated.resources.insights_stagnant_knowledge_msg
import tajsos.composeapp.generated.resources.insights_stagnant_knowledge_title
import tajsos.composeapp.generated.resources.insights_subtitle
import tajsos.composeapp.generated.resources.insights_title

object InsightsDashboardBlockRegistry {
    private val renderers: Map<String, InsightsDashboardBlockRenderer> =
        mapOf("insights_main" to ::renderInsightsMainBlock)

    fun resolve(id: String): InsightsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderInsightsMainBlock(context: InsightsDashboardContext) {
    InsightsMainBlock(
        viewModel = context.viewModel,
        onNavigateToProject = context.onNavigateToProject,
    )
}

@Composable
internal fun InsightsMainBlock(
    viewModel: MainViewModel,
    onNavigateToProject: (Long) -> Unit,
) {
    val insights by viewModel.insights.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val areaSnapshot by viewModel.areaHealthSnapshot.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        item {
            Text(
                stringResource(Res.string.insights_title),
                style = MaterialTheme.typography.displayMedium,
                color = TajsOSTheme.Text,
            )
            Text(
                stringResource(Res.string.insights_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
            )
            Spacer(Modifier.height(TajsOSTheme.SpacingLg))
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
                color = TajsOSTheme.Primary,
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
            items(areaSnapshot.areas.take(4), key = { "area_${it.areaId}" }) { area ->
                AreaHealthInsightCard(area)
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
                        color = TajsOSTheme.Error,
                    )
                }
            }
        }

        val captureTimePattern = insights.captureTimePattern
        if (captureTimePattern != null) {
            item {
                InsightPatternCard(
                    title = stringResource(Res.string.insights_creative_peak_title),
                    message =
                        stringResource(
                            Res.string.insights_creative_peak_msg,
                            captureTimePattern,
                        ),
                    icon = Icons.Default.Lightbulb,
                    color = TajsOSTheme.Success,
                )
            }
        }

        insights.projectsWithoutTasks.firstOrNull()?.let { firstProject ->
            item {
                InsightPatternCard(
                    title = stringResource(Res.string.insights_stagnant_knowledge_title),
                    message =
                        stringResource(
                            Res.string.insights_stagnant_knowledge_msg,
                            firstProject.title,
                        ),
                    icon = Icons.Default.Warning,
                    color = TajsOSTheme.Accent,
                )
            }
        }

        insights.neglectedAreas.firstOrNull()?.let { area ->
            item {
                InsightPatternCard(
                    title = stringResource(Res.string.insights_radar_drop_title),
                    message = stringResource(Res.string.insights_radar_drop_msg, area.title),
                    icon = Icons.Default.LocationOff,
                    color = TajsOSTheme.Error,
                )
            }
        }

        if (insights.neglectedProjects.isNotEmpty()) {
            item {
                Text(
                    stringResource(Res.string.insights_neglected_projects),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
            }
            items(insights.neglectedProjects, key = { "neglected_${it.id}" }) { project ->
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
                    color = TajsOSTheme.Error,
                )
            }
            items(highEntropyProjects.keys.toList(), key = { "entropy_$it" }) { projectId ->
                val project = allProjects.find { it.id == projectId }
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
                    color = TajsOSTheme.Primary,
                )
            }
            items(recentLogs, key = { "log_${it.id}" }) { log ->
                ActivityLogItem(log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(TajsOSTheme.SpacingLg))
        }
    }
}

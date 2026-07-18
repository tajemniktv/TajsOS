/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.components.common.SelectorDialog
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.detail_archive
import tajsos.composeapp.generated.resources.review_archived_summary
import tajsos.composeapp.generated.resources.review_capacity_summary
import tajsos.composeapp.generated.resources.review_complete
import tajsos.composeapp.generated.resources.review_daily
import tajsos.composeapp.generated.resources.review_energy_level
import tajsos.composeapp.generated.resources.review_execution_ratio
import tajsos.composeapp.generated.resources.review_focus_summary
import tajsos.composeapp.generated.resources.review_monthly
import tajsos.composeapp.generated.resources.review_mood_level
import tajsos.composeapp.generated.resources.review_next
import tajsos.composeapp.generated.resources.review_placeholder_thoughts
import tajsos.composeapp.generated.resources.review_prompt_energy_level
import tajsos.composeapp.generated.resources.review_prompt_how_feeling
import tajsos.composeapp.generated.resources.review_prompt_numbers_feedback
import tajsos.composeapp.generated.resources.review_select_type
import tajsos.composeapp.generated.resources.review_step_archive
import tajsos.composeapp.generated.resources.review_step_blockers
import tajsos.composeapp.generated.resources.review_step_cleanup
import tajsos.composeapp.generated.resources.review_step_goals
import tajsos.composeapp.generated.resources.review_step_mood_energy
import tajsos.composeapp.generated.resources.review_step_plan
import tajsos.composeapp.generated.resources.review_step_stats
import tajsos.composeapp.generated.resources.review_step_wins
import tajsos.composeapp.generated.resources.review_weekly
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

object ReviewDashboardBlocks {
    private val renderers: Map<String, ReviewDashboardBlockRenderer> =
        mapOf(
            "review_selector" to ::renderReviewSelector,
            "review_flow" to ::renderReviewFlow,
        )

    fun resolve(id: String): ReviewDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderReviewSelector(context: ReviewDashboardContext) {
    val dailyLabel = stringResource(Res.string.review_daily)
    val weeklyLabel = stringResource(Res.string.review_weekly)
    val monthlyLabel = stringResource(Res.string.review_monthly)

    SelectorDialog(
        show = context.reviewType == null,
        onDismiss = context.onBack,
        title = stringResource(Res.string.review_select_type),
        options = listOf("daily", "weekly", "monthly"),
        selectedOption = null,
        onSelect = { context.onReviewTypeSelect(it) },
        optionName = {
            when (it) {
                "daily" -> dailyLabel
                "weekly" -> weeklyLabel
                "monthly" -> monthlyLabel
                else -> it
            }
        },
        optionIcon = {
            when (it) {
                "daily" -> Icons.Default.WbSunny
                "weekly" -> Icons.Default.DateRange
                "monthly" -> Icons.Default.CalendarMonth
                else -> Icons.Default.RateReview
            }
        },
        optionSubtext = { "REV_SYST_${it.uppercase()}" },
    )
}

@Composable
private fun renderReviewFlow(context: ReviewDashboardContext) {
    val currentStep = context.currentStep
    val steps = context.steps
    val answers = context.answers
    val mood = context.mood
    val energy = context.energy
    val insights = context.insights
    val dashboardState = context.dashboardState
    val viewModel = context.viewModel

    Column(modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd)) {
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            color = TajsOSTheme.Primary,
        )

        Text(
            stringResource(steps[currentStep]),
            style = MaterialTheme.typography.headlineSmall,
            color = TajsOSTheme.Primary,
        )

        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f)) {
            when (steps[currentStep]) {
                Res.string.review_step_mood_energy -> {
                    Column {
                        Text(stringResource(Res.string.review_prompt_how_feeling))
                        Slider(
                            value = mood.toFloat(),
                            onValueChange = { context.onMoodChange(it.toInt()) },
                            valueRange = 1f..5f,
                            steps = 3,
                        )
                        Text(stringResource(Res.string.review_mood_level, mood.toString()))
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(Res.string.review_prompt_energy_level))
                        Slider(
                            value = energy.toFloat(),
                            onValueChange = { context.onEnergyChange(it.toInt()) },
                            valueRange = 1f..5f,
                            steps = 3,
                        )
                        Text(stringResource(Res.string.review_energy_level, energy.toString()))
                    }
                }

                Res.string.review_step_stats -> {
                    Column {
                        Text(
                            stringResource(
                                Res.string.review_capacity_summary,
                                insights.weeklyCompletions.toString(),
                            ),
                        )
                        Text(
                            stringResource(
                                Res.string.review_execution_ratio,
                                (insights.captureToActionRatio * 100).toInt().toString(),
                            ),
                        )
                        Text(
                            stringResource(
                                Res.string.review_focus_summary,
                                insights.weeklyFocusHours.toInt().toString(),
                            ),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(Res.string.review_prompt_numbers_feedback))
                        TextField(
                            value = answers[currentStep] ?: "",
                            onValueChange = { context.onAnswerChange(currentStep, it) },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                        )
                    }
                }

                Res.string.review_step_archive -> {
                    val archivedSuggestions = dashboardState.archivedThisWeek
                    Column {
                        Text(
                            stringResource(
                                Res.string.review_archived_summary,
                                archivedSuggestions.size.toString(),
                            ),
                        )
                        Column {
                            dashboardState.neglectedThisWeek.take(5).forEach { item ->
                                ListItem(
                                    headlineContent = { Text(item.node.title) },
                                    trailingContent = {
                                        IconButton(
                                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = { viewModel.archiveNode(item.node) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(Res.string.detail_archive),
                                                tint = TajsOSTheme.Error,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                        TextField(
                            value = answers[currentStep] ?: "",
                            onValueChange = { context.onAnswerChange(currentStep, it) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                        )
                    }
                }

                Res.string.review_step_wins, Res.string.review_step_blockers, Res.string.review_step_plan, Res.string.review_step_cleanup, Res.string.review_step_goals -> {
                    TextField(
                        value = answers[currentStep] ?: "",
                        onValueChange = { context.onAnswerChange(currentStep, it) },
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        placeholder = { Text(stringResource(Res.string.review_placeholder_thoughts)) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (currentStep < steps.size - 1) {
                Button(onClick = context.onNext) {
                    Text(stringResource(Res.string.review_next))
                }
            } else {
                val completeLabel = stringResource(Res.string.review_complete)
                Button(
                    onClick = context.onComplete,
                ) {
                    Text(completeLabel)
                }
            }
        }
    }
}

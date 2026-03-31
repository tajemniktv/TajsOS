/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.review_step_archive
import tajsos.composeapp.generated.resources.review_step_blockers
import tajsos.composeapp.generated.resources.review_step_cleanup
import tajsos.composeapp.generated.resources.review_step_goals
import tajsos.composeapp.generated.resources.review_step_mood_energy
import tajsos.composeapp.generated.resources.review_step_plan
import tajsos.composeapp.generated.resources.review_step_stats
import tajsos.composeapp.generated.resources.review_step_wins

@Composable
fun ReviewScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    var reviewType by remember { mutableStateOf<String?>(null) }
    var currentStep by remember { mutableStateOf(0) }
    var mood by remember { mutableStateOf(3) }
    var energy by remember { mutableStateOf(3) }
    val answers = remember { mutableStateMapOf<Int, String>() }

    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val insights by viewModel.insights.collectAsState()

    val dailySteps =
        listOf(
            Res.string.review_step_mood_energy,
            Res.string.review_step_wins,
            Res.string.review_step_blockers,
            Res.string.review_step_plan,
        )
    val weeklySteps =
        listOf(
            Res.string.review_step_stats,
            Res.string.review_step_cleanup,
            Res.string.review_step_archive,
            Res.string.review_step_goals,
        )

    val steps = if (reviewType == "daily") dailySteps else weeklySteps

    val context =
        ReviewDashboardContext(
            viewModel = viewModel,
            reviewType = reviewType,
            currentStep = currentStep,
            steps = steps,
            mood = mood,
            energy = energy,
            answers = answers,
            dashboardState = dashboardState,
            insights = insights,
            onReviewTypeSelect = { reviewType = it },
            onStepChange = { currentStep = it },
            onMoodChange = { mood = it },
            onEnergyChange = { energy = it },
            onAnswerChange = { step, answer -> answers[step] = answer },
            onNext = { if (currentStep < steps.size - 1) currentStep++ },
            onComplete = {
                val content =
                    steps.indices.joinToString("\n\n") { i ->
                        "Step $i:\n${answers[i] ?: "N/A"}"
                    }
                viewModel.completeReview(reviewType ?: "daily", content, mood, energy)
                onBack()
            },
            onBack = onBack,
        )

    val surface = ReviewDashboardSurface.MOBILE // Default for now
    val plan = remember(surface, reviewType) { buildReviewDashboardPlan(surface, reviewType) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(TajsOSTheme.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                ReviewDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}

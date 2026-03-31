/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.review

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.DashboardUIState
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.main.state.InsightsData
import org.jetbrains.compose.resources.StringResource

/**
 * Defines the supported surfaces for review dashboard layout planning.
 */
enum class ReviewDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical review dashboard block.
 */
data class ReviewDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the review dashboard screen.
 */
data class ReviewDashboardPlan(
    val primary: List<ReviewDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for review dashboard block renderers.
 */
data class ReviewDashboardContext(
    val viewModel: MainViewModel,
    val reviewType: String?,
    val currentStep: Int,
    val steps: List<StringResource>,
    val mood: Int,
    val energy: Int,
    val answers: Map<Int, String>,
    val dashboardState: DashboardUIState,
    val insights: InsightsData,
    val onReviewTypeSelect: (String) -> Unit,
    val onStepChange: (Int) -> Unit,
    val onMoodChange: (Int) -> Unit,
    val onEnergyChange: (Int) -> Unit,
    val onAnswerChange: (Int, String) -> Unit,
    val onNext: () -> Unit,
    val onComplete: () -> Unit,
    val onBack: () -> Unit,
)

/**
 * Functional interface for rendering a review dashboard block.
 */
typealias ReviewDashboardBlockRenderer = @Composable (ReviewDashboardContext) -> Unit

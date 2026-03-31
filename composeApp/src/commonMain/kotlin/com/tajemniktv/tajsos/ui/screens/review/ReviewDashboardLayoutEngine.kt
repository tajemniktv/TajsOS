/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.review

/**
 * Builds a review dashboard layout plan based on the active surface and state.
 */
fun buildReviewDashboardPlan(
    surface: ReviewDashboardSurface,
    reviewType: String?,
): ReviewDashboardPlan {
    val primary = mutableListOf<ReviewDashboardBlock>()

    if (reviewType == null) {
        primary.add(ReviewDashboardBlock("review_selector"))
    } else {
        primary.add(ReviewDashboardBlock("review_flow"))
    }

    return ReviewDashboardPlan(primary = primary)
}

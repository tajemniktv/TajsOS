/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.identity

import androidx.compose.runtime.Composable
import com.tajemniktv.tajos.ui.MainViewModel
import com.tajemniktv.tajos.ui.main.state.CombinedDirectionSnapshot
import com.tajemniktv.tajos.ui.main.state.CoreLifeOSShiftSnapshot
import com.tajemniktv.tajos.ui.main.state.LifeOSSecondBrainSnapshot
import com.tajemniktv.tajos.ui.main.state.LifeOSSignatureSnapshot

/**
 * Defines the supported surfaces for identity dashboard layout planning.
 */
enum class IdentityDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical identity dashboard block.
 */
data class IdentityDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the identity dashboard screen.
 */
data class IdentityDashboardPlan(
    val primary: List<IdentityDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for identity dashboard block renderers.
 */
data class IdentityDashboardContext(
    val viewModel: MainViewModel,
    val signatureSnapshot: LifeOSSignatureSnapshot,
    val secondBrainSnapshot: LifeOSSecondBrainSnapshot,
    val directionSnapshot: CombinedDirectionSnapshot,
    val coreShiftSnapshot: CoreLifeOSShiftSnapshot,
    val onEditNode: (Long) -> Unit,
)

/**
 * Functional interface for rendering an identity dashboard block.
 */
typealias IdentityDashboardBlockRenderer = @Composable (IdentityDashboardContext) -> Unit

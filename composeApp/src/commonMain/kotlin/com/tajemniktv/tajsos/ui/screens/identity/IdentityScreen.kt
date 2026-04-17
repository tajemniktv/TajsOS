/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Central identity entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of identity state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun IdentityRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val lifeOSSignatureSnapshot by viewModel.lifeOSSignatureSnapshot.collectAsState()
    val lifeOSSecondBrainSnapshot by viewModel.lifeOSSecondBrainSnapshot.collectAsState()
    val combinedDirectionSnapshot by viewModel.combinedDirectionSnapshot.collectAsState()
    val coreLifeOSShiftSnapshot by viewModel.coreLifeOSShiftSnapshot.collectAsState()

    val context =
        IdentityDashboardContext(
            viewModel = viewModel,
            signatureSnapshot = lifeOSSignatureSnapshot,
            secondBrainSnapshot = lifeOSSecondBrainSnapshot,
            directionSnapshot = combinedDirectionSnapshot,
            coreShiftSnapshot = coreLifeOSShiftSnapshot,
            onEditNode = onEditNode,
        )

    val surface = IdentityDashboardSurface.MOBILE // Default for now
    val plan = remember(surface) { buildIdentityDashboardPlan(surface) }

    IdentityScreen(
        context = context,
        plan = plan,
        onNavigate = onNavigate,
    )
}

/**
 * Stateless identity screen content.
 *
 * @param context Identity dashboard context.
 * @param plan Identity dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun IdentityScreen(
    context: IdentityDashboardContext,
    plan: IdentityDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Identity,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            plan.primary.forEach { block ->
                item(key = block.id) {
                    IdentityDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}

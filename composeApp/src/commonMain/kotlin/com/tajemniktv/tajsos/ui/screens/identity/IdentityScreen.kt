/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@Composable
fun IdentityScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd).padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        plan.primary.forEach { block ->
            item(key = block.id) {
                IdentityDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun IdentityScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val lifeOSSignatureSnapshot by viewModel.lifeOSSignatureSnapshot.collectAsState()
    val lifeOSSecondBrainSnapshot by viewModel.lifeOSSecondBrainSnapshot.collectAsState()
    val combinedDirectionSnapshot by viewModel.combinedDirectionSnapshot.collectAsState()
    val coreLifeOSShiftSnapshot by viewModel.coreLifeOSShiftSnapshot.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        item {
            Text(
                text = "IDENTITY & OPERATING MODEL",
                style = MaterialTheme.typography.displaySmall,
                color = TactileTheme.Text,
            )
        }
        item {
            Text(
                text = "Review signature, second-brain distinction, direction commitments, and core-shift progress together.",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
        item {
            SignatureLayer(
                viewModel = viewModel,
                snapshot = lifeOSSignatureSnapshot,
                onEditNode = onEditNode,
            )
        }
        item {
            DistinctionLayer(snapshot = lifeOSSecondBrainSnapshot)
        }
        item {
            DirectionLayer(snapshot = combinedDirectionSnapshot)
        }
        item {
            CoreShiftLayer(snapshot = coreLifeOSShiftSnapshot)
        }
    }
}

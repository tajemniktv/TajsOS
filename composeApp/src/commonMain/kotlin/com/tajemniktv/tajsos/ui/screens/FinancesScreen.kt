/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

private val financeMaintenanceTypes =
    setOf(
        "bill",
        "subscription",
        "renewal",
    )

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun FinancesScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val maintenanceSnapshot by viewModel.maintenanceSnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    var maintenanceView by remember { mutableStateOf(MaintenanceView.Queue) }

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "FINANCES WORKSPACE",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Track bills, subscriptions, renewals, and finance-related maintenance.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        MaintenanceLayer(
            viewModel = viewModel,
            snapshot = maintenanceSnapshot,
            allAreas = allAreas,
            maintenanceView = maintenanceView,
            onMaintenanceView = { maintenanceView = it },
            maintenanceTypeFilter = financeMaintenanceTypes,
            onEditNode = onEditNode,
        )
    }
}

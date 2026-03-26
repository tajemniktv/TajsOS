/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun OperationsScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit,
) {
    val capacitySnapshot by viewModel.capacitySnapshot.collectAsState()
    val lifeOSSignatureSnapshot by viewModel.lifeOSSignatureSnapshot.collectAsState()
    val lifeOSSecondBrainSnapshot by viewModel.lifeOSSecondBrainSnapshot.collectAsState()
    val combinedDirectionSnapshot by viewModel.combinedDirectionSnapshot.collectAsState()
    val relationshipSnapshot by viewModel.relationshipSnapshot.collectAsState()
    val coreLifeOSShiftSnapshot by viewModel.coreLifeOSShiftSnapshot.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "OPERATIONS WORKSPACE",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "System overview for execution, logistics, and operating model layers.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        GroupedOpenLoopSection(
            title = "MODULE STATUS",
            items =
                listOf(
                    "Capacity load ${capacitySnapshot.loadScore}% • Fragmentation ${capacitySnapshot.fragmentationScore}%",
                    "Relationships ${relationshipSnapshot.people.size} • Follow-up ${relationshipSnapshot.followUpNeeded.size}",
                    "LifeOS signature ${lifeOSSignatureSnapshot.modeOfLifeLabel.uppercase()}",
                    "Second brain posture ${lifeOSSecondBrainSnapshot.postureLabel.uppercase()}",
                    "Direction ${combinedDirectionSnapshot.completionPercent}% • Core shift ${coreLifeOSShiftSnapshot.completionPercent}%",
                ),
        )

        val modules =
            listOf(
                Screen.OpenLoops to "Resolve active loops, inbox spillover, and review debt.",
                Screen.Protocols to "Run and maintain transition protocols and playbooks.",
                Screen.TimeArchitecture to "Manage horizons, countdowns, and focus periods.",
                Screen.Places to "Coordinate errands, travel packs, and place-based logistics.",
                Screen.Finances to "Keep bills, renewals, and subscriptions under control.",
                Screen.Relationships to "Maintain follow-ups, shared plans, and contact rhythm.",
                Screen.Study to "Keep coursework, revision, and study execution visible.",
                Screen.Rules to "Store and pin personal principles and decision rules.",
                Screen.Vaults to "Keep reference material, paperwork, and retrieval systems clean.",
                Screen.Capacity to "Track load, fragmentation, and realistic throughput.",
                Screen.Identity to "Review signature, distinction, direction, and core-shift state.",
            )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            items(modules, key = { it.first.route }) { (screen, summary) ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = BorderStroke(1.dp, TactileTheme.Border),
                    onClick = { onNavigate(screen) },
                ) {
                    Column(
                        modifier = Modifier.padding(TactileTheme.SpacingMd),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = screen.route.replace("_", " ").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted,
                        )
                    }
                }
            }
        }
    }
}

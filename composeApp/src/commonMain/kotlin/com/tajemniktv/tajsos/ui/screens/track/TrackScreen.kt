/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

@Composable
fun TrackScreen(viewModel: MainViewModel) {
    val trackEntries by viewModel.trackEntries.collectAsState()
    val medications by viewModel.medications.collectAsState()

    val today =
        kotlin.time.Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
    val todayEntry = trackEntries.find { it.date == today }

    var energy by remember(todayEntry) {
        mutableFloatStateOf(
            todayEntry?.energyScore?.toFloat() ?: 3f,
        )
    }
    var affective by remember(todayEntry) {
        mutableFloatStateOf(
            todayEntry?.moodScore?.toFloat() ?: 3f,
        )
    }
    var cognitive by remember(todayEntry) {
        mutableFloatStateOf(
            todayEntry?.focusScore?.toFloat() ?: 5f,
        )
    }
    var systemTension by remember(todayEntry) {
        mutableFloatStateOf(
            todayEntry?.anxietyScore?.toFloat() ?: 1f,
        )
    }
    var recovery by remember(todayEntry) { mutableFloatStateOf(todayEntry?.sleepScore ?: 7.5f) }

    var note by remember(todayEntry) { mutableStateOf(todayEntry?.symptomNote ?: "") }

    val selectedMedIds = remember { mutableStateOf(setOf<Long>()) }
    val allMedsTaken = medications.all { it.isOptional || selectedMedIds.value.contains(it.id) }

    val context =
        TrackDashboardContext(
            viewModel = viewModel,
            trackEntries = trackEntries,
            medications = medications,
            energy = energy,
            affective = affective,
            cognitive = cognitive,
            systemTension = systemTension,
            recovery = recovery,
            note = note,
            selectedMedIds = selectedMedIds.value,
            allMedsTaken = allMedsTaken,
            onEnergyChange = { energy = it },
            onAffectiveChange = { affective = it },
            onCognitiveChange = { cognitive = it },
            onSystemTensionChange = { systemTension = it },
            onRecoveryChange = { recovery = it },
            onNoteChange = { note = it },
            onToggleMed = { id ->
                selectedMedIds.value =
                    if (selectedMedIds.value.contains(id)) {
                        selectedMedIds.value - id
                    } else {
                        selectedMedIds.value + id
                    }
            },
            onToggleAllMeds = {
                selectedMedIds.value =
                    if (allMedsTaken) emptySet() else medications.map { it.id }.toSet()
            },
            onSave = {
                viewModel.addTrackEntry(
                    energyPulse = energy.roundToInt(),
                    affectivePulse = affective.roundToInt(),
                    cognitivePulse = cognitive.roundToInt(),
                    systemPulse = systemTension.roundToInt(),
                    recoveryPulse = recovery,
                    tookMeds = allMedsTaken,
                    note = note,
                    medicationIds = selectedMedIds.value,
                )
            },
        )

    val surface = TrackDashboardSurface.MOBILE // Default for now
    val plan = remember(surface) { buildTrackDashboardPlan(surface) }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TajsOSTheme.SpacingMd)
                .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        plan.primary.forEach { block ->
            item(key = block.id) {
                TrackDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}

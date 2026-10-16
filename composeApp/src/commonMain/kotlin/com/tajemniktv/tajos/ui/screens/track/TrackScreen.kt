/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajos.ui.MainViewModel
import com.tajemniktv.tajos.ui.Screen
import com.tajemniktv.tajos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajos.ui.theme.TajsOSTheme
import kotlin.math.roundToInt

/**
 * Central track entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of track state.
 * @param onNavigate Navigation callback.
 */
@Composable
fun TrackRoute(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
) {
    val trackEntries by viewModel.trackEntries.collectAsState()
    val medications by viewModel.medications.collectAsState()

    val localDate by viewModel.localDate.collectAsState()
    val today = localDate.toString()
    val todayEntry = trackEntries.find { it.date == today }

    var energy by remember(today, todayEntry) {
        mutableFloatStateOf(
            todayEntry?.energyScore?.toFloat() ?: 3f,
        )
    }
    var affective by remember(today, todayEntry) {
        mutableFloatStateOf(
            todayEntry?.moodScore?.toFloat() ?: 3f,
        )
    }
    var cognitive by remember(today, todayEntry) {
        mutableFloatStateOf(
            todayEntry?.focusScore?.toFloat() ?: 5f,
        )
    }
    var systemTension by remember(today, todayEntry) {
        mutableFloatStateOf(
            todayEntry?.anxietyScore?.toFloat() ?: 1f,
        )
    }
    var recovery by remember(today, todayEntry) { mutableFloatStateOf(todayEntry?.sleepScore ?: 7.5f) }

    var note by remember(today, todayEntry) { mutableStateOf(todayEntry?.symptomNote ?: "") }

    var selectedMedicationIds by remember(today, todayEntry?.id) { mutableStateOf<Set<Long>?>(null) }
    LaunchedEffect(today, todayEntry?.id) {
        selectedMedicationIds = todayEntry?.let { entry ->
            viewModel.getMedicationsForEntry(entry.id).first().filter { it.wasTaken }.map { it.medicationId }.toSet()
        } ?: emptySet()
    }
    val selectedMedIds = selectedMedicationIds.orEmpty()
    val allMedsTaken = selectedMedicationIds != null && medications.all { it.isOptional || selectedMedIds.contains(it.id) }

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
            selectedMedIds = selectedMedIds,
            allMedsTaken = allMedsTaken,
            onEnergyChange = { energy = it },
            onAffectiveChange = { affective = it },
            onCognitiveChange = { cognitive = it },
            onSystemTensionChange = { systemTension = it },
            onRecoveryChange = { recovery = it },
            onNoteChange = { note = it },
            onToggleMed = { id ->
                selectedMedicationIds?.let { selected ->
                    selectedMedicationIds = if (id in selected) selected - id else selected + id
                }
            },
            onToggleAllMeds = {
                selectedMedicationIds?.let { selected ->
                    val allTaken = medications.all { it.isOptional || it.id in selected }
                    selectedMedicationIds = if (allTaken) emptySet() else medications.map { it.id }.toSet()
                }
            },
            saveEnabled = selectedMedicationIds != null,
            onSave = {
                if (selectedMedicationIds != null) viewModel.addTrackEntry(
                    energyPulse = energy.roundToInt(),
                    affectivePulse = affective.roundToInt(),
                    cognitivePulse = cognitive.roundToInt(),
                    systemPulse = systemTension.roundToInt(),
                    recoveryPulse = recovery,
                    tookMeds = allMedsTaken,
                    note = note,
                    medicationIds = selectedMedIds,
                )
            },
        )

    val surface = TrackDashboardSurface.MOBILE // Default for now
    val plan = remember(surface) { buildTrackDashboardPlan(surface) }

    TrackScreen(
        context = context,
        plan = plan,
        onNavigate = onNavigate,
    )
}

/**
 * Stateless track screen content.
 *
 * @param context Track dashboard context.
 * @param plan Track dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun TrackScreen(
    context: TrackDashboardContext,
    plan: TrackDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Track,
        onNavigate = onNavigate,
        scrollBehavior = com.tajemniktv.tajos.ui.components.screen.ScreenScrollBehavior.None,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            plan.primary.forEach { block ->
                item(key = block.id) {
                    TrackDashboardBlocks.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.track

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Defines the supported surfaces for track dashboard layout planning.
 */
enum class TrackDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical track dashboard block.
 */
data class TrackDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the track dashboard screen.
 */
data class TrackDashboardPlan(
    val primary: List<TrackDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for track dashboard block renderers.
 */
data class TrackDashboardContext(
    val viewModel: MainViewModel,
    val trackEntries: List<TrackEntryEntity>,
    val medications: List<MedicationEntity>,
    val energy: Float,
    val affective: Float,
    val cognitive: Float,
    val systemTension: Float,
    val recovery: Float,
    val note: String,
    val selectedMedIds: Set<Long>,
    val allMedsTaken: Boolean,
    val onEnergyChange: (Float) -> Unit,
    val onAffectiveChange: (Float) -> Unit,
    val onCognitiveChange: (Float) -> Unit,
    val onSystemTensionChange: (Float) -> Unit,
    val onRecoveryChange: (Float) -> Unit,
    val onNoteChange: (String) -> Unit,
    val onToggleMed: (Long) -> Unit,
    val onToggleAllMeds: () -> Unit,
    val onSave: () -> Unit,
)

/**
 * Functional interface for rendering a track dashboard block.
 */
typealias TrackDashboardBlockRenderer = @Composable (TrackDashboardContext) -> Unit

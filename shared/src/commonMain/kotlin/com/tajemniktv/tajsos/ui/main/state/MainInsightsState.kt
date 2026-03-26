/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlinx.serialization.Serializable

data class DecisionStaleItem(
    val node: NodeWithPin,
    val ageDays: Int,
)

@Serializable
data class InsightsData(
    val weeklyCaptures: Int = 0,
    val weeklyCompletions: Int = 0,
    val weeklyFocusHours: Double = 0.0,
    val bestFocusHour: Int = -1,
    val avgMood: Double = 0.0,
    val avgEnergy: Double = 0.0,
    val avgFocus: Double = 0.0,
    val neglectedProjects: List<NodeEntity> = emptyList(),
    val captureToActionRatio: Double = 0.0,
    val autoPreparedReview: String = "",
    val avgSessionMinutes: Int = 0,
    val inboxGrowth: Int = 0,
    val archiveRate: Double = 0.0,
    val completionsByArea: Map<Long, Int> = emptyMap(),
    val completionsByProject: Map<Long, Int> = emptyMap(),
    val mostProductiveHour: Int = -1,
    val postponeFrequency: Int = 0,
    val backlogPressure: Double = 0.0,
    val chaosScore: Int = 0,
    val contextSwitchingRate: Double = 0.0,
    val moodVsCompletions: Double = 0.0,
    val sleepVsFocus: Double = 0.0,
    val energyVsCaptures: Double = 0.0,
    val anxietyVsAvoidance: Double = 0.0,
    val medsEffectiveness: Double = 0.0,
    val mostPostponedAreaId: Long? = null,
    val captureTimePattern: String? = null,
    val projectsWithoutTasks: List<NodeEntity> = emptyList(),
    val neglectedAreas: List<NodeEntity> = emptyList(),
    val projectEntropy: Map<Long, Double> = emptyMap(),
    val contextStability: Double = 0.0,
    val passiveBehaviorSummary: String = "",
)

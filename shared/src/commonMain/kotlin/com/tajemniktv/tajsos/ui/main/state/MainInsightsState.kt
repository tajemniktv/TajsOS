/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlinx.serialization.Serializable

/**
 * A data class representing a stale decision node that requires review or action.
 *
 * @param node The [NodeWithPin] wrapper containing the stale decision entity.
 * @param ageDays The number of days the decision has remained inactive or unresolved.
 */
data class DecisionStaleItem(
    val node: NodeWithPin,
    val ageDays: Int,
)

/**
 * A comprehensive data class aggregating various analytics, usage statistics, and system insights for the application.
 *
 * @param weeklyCaptures The total number of new items captured into the system within the last week.
 * @param weeklyCompletions The total number of tasks or nodes completed in the last week.
 * @param weeklyFocusHours The cumulative hours spent in focus sessions in the last week.
 * @param bestFocusHour The time of day (0-23) where focus and completion rates are statistically highest.
 * @param avgMood The calculated average user mood rating over a specific period.
 * @param avgEnergy The calculated average user energy level rating over a specific period.
 * @param avgFocus The calculated average focus score or self-reported focus quality.
 * @param neglectedProjects A list of project entities that haven't been touched or updated recently.
 * @param captureToActionRatio The ratio comparing newly captured items versus completed items to gauge backlog growth.
 * @param autoPreparedReview An automated AI or system-generated review summary based on recent activity.
 * @param avgSessionMinutes The average duration of focus or work sessions in minutes.
 * @param inboxGrowth The net increase or decrease of the inbox item count.
 * @param archiveRate The rate at which items are being moved to an archived or non-actionable state.
 * @param completionsByArea A map detailing the number of completed tasks associated with specific area IDs.
 * @param completionsByProject A map detailing the number of completed tasks associated with specific project IDs.
 * @param mostProductiveHour The specific hour block (0-23) where the highest productivity output is achieved.
 * @param postponeFrequency The frequency at which due dates are delayed or items are postponed.
 * @param backlogPressure A calculated metric indicating how overwhelmingly large the backlog of outstanding tasks has become.
 * @param chaosScore A metric denoting system disorganization (e.g., untagged items, missing contexts).
 * @param contextSwitchingRate The estimated frequency of shifting between different types of tasks or contexts.
 * @param moodVsCompletions A correlated metric analyzing whether mood affects task completion rates.
 * @param sleepVsFocus A correlated metric analyzing whether tracked sleep affects focus scores.
 * @param energyVsCaptures A correlated metric analyzing the relationship between energy levels and capture behavior.
 * @param anxietyVsAvoidance A correlated metric capturing avoidance patterns against self-reported anxiety levels.
 * @param medsEffectiveness A tracked metric mapping medication intake against focus or productivity.
 * @param mostPostponedAreaId The unique ID of the specific Area of Life where items are delayed most frequently.
 * @param captureTimePattern A descriptive summary of when captures typically occur during the day (e.g., "Evening").
 * @param projectsWithoutTasks A list of project nodes that are active but lack any actionable subtasks.
 * @param neglectedAreas A list of area nodes that have no recent activity or focus applied to them.
 * @param projectEntropy A map assigning an entropy score to projects indicating how unstructured or disorganized they are.
 * @param contextStability A measure of how consistently tasks are executed within their planned or optimal contexts.
 * @param passiveBehaviorSummary An automated summary outlining passive engagement (e.g., excessive browsing instead of active focus).
 */
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

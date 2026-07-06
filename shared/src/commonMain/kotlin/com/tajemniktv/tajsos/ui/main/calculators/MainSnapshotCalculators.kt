/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.*
import com.tajemniktv.tajsos.ui.main.state.CapacitySnapshot
import com.tajemniktv.tajsos.ui.main.state.CombinedDirectionSnapshot
import com.tajemniktv.tajsos.ui.main.state.CoreLifeOSShiftItem
import com.tajemniktv.tajsos.ui.main.state.CoreLifeOSShiftSnapshot
import com.tajemniktv.tajsos.ui.main.state.DirectionCommitmentStatus
import com.tajemniktv.tajsos.ui.main.state.DistinctionQuestionState
import com.tajemniktv.tajsos.ui.main.state.InsightsData
import com.tajemniktv.tajsos.ui.main.state.LifeOSSecondBrainSnapshot
import com.tajemniktv.tajsos.ui.main.state.LifeOSSignatureSnapshot
import com.tajemniktv.tajsos.ui.main.state.LoadTrendPoint
import com.tajemniktv.tajsos.ui.main.state.PersonalRulesSnapshot
import com.tajemniktv.tajsos.ui.main.state.PhysicalLogisticsSnapshot
import com.tajemniktv.tajsos.ui.main.state.PlaceLogisticsItem
import com.tajemniktv.tajsos.ui.main.state.PlaybookSnapshot
import com.tajemniktv.tajsos.ui.main.state.RelationshipSnapshot
import com.tajemniktv.tajsos.ui.main.state.RelationshipStatusItem
import com.tajemniktv.tajsos.ui.main.state.StudentBoardState
import com.tajemniktv.tajsos.ui.main.state.StudentCourseSummary
import com.tajemniktv.tajsos.ui.main.state.StudentMasteryItem
import com.tajemniktv.tajsos.ui.main.state.StudentProgressItem
import com.tajemniktv.tajsos.ui.main.state.StudentSemesterSummary
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolsSnapshot
import com.tajemniktv.tajsos.ui.main.state.VaultsSnapshot
import kotlinx.datetime.*
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Aggregates analytical tracking data to compute a comprehensive [InsightsData] payload
 * detailing focus hours, completion rates, backlog pressure, and identified chaotic patterns.
 *
 * @param nodes The complete list of active nodes in the system.
 * @param sessions A list of recorded [FocusSessionEntity] records tracking deep work time.
 * @param tracks A list of recorded [TrackEntryEntity] records detailing mood, energy, and sleep.
 * @param projects A curated list of nodes strictly identified as "project" entities.
 * @return An analytical [InsightsData] snapshot summarizing the user's historical performance.
 */
fun calculateInsights(
    nodes: List<NodeWithPin>,
    sessions: List<FocusSessionEntity>,
    tracks: List<TrackEntryEntity>,
    projects: List<NodeEntity>,
): InsightsData {
    // Cached system timezone for performance during iterations
    val sysZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toEpochMilliseconds()
    val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)

    /** Cached system timezone for performance during iterations */
    val recentNodes = nodes.filter { it.node.createdAt >= sevenDaysAgo }
    val recentCompletions =
        nodes.filter {
            it.node.status == "done" && (it.node.completedAt ?: 0) >= sevenDaysAgo // NON-NLS
        }

    val recentSessions = sessions.filter { it.startedAt >= sevenDaysAgo && it.endedAt != null }
    val weeklyFocusSec = recentSessions.sumOf { it.durationSec.toLong() }
    val avgSessionMin =
        if (recentSessions.isNotEmpty()) {
            (recentSessions.map { it.durationSec }.average() / 60).toInt()
        } else {
            0
        }

    val hourlyDistribution = IntArray(24)
    sessions.filter { it.endedAt != null }.forEach {
        val hour =
            Instant
                .fromEpochMilliseconds(it.startedAt)
                .toLocalDateTime(sysZone)
                .hour
        hourlyDistribution[hour]++
    }

    val bestFocusHour = hourlyDistribution.indices.maxByOrNull { hourlyDistribution[it] } ?: -1

    val completionHourlyDist = IntArray(24)
    recentCompletions.forEach {
        val hour =
            Instant
                .fromEpochMilliseconds(it.node.completedAt ?: 0)
                .toLocalDateTime(sysZone)
                .hour
        completionHourlyDist[hour]++
    }
    val mostProductiveHour =
        completionHourlyDist.indices.maxByOrNull { completionHourlyDist[it] } ?: -1

    val sevenDaysAgoDate =
        Instant
            .fromEpochMilliseconds(sevenDaysAgo)
            .toLocalDateTime(sysZone)
            .date
    val recentTracks = tracks.filter { it.date >= sevenDaysAgoDate.toString() }

    val avgMood =
        recentTracks.mapNotNull { it.moodScore }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
    val avgEnergy =
        recentTracks.mapNotNull { it.energyScore }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
    val avgFocus =
        recentTracks.mapNotNull { it.focusScore }.takeIf { it.isNotEmpty() }?.average() ?: 0.0

    val nodesByProjectId = nodes.groupBy { it.node.projectId }
    val neglectedProjects =
        projects.filter { project ->
            val projectNodes = nodesByProjectId[project.id] ?: emptyList()
            val hasActiveItems = projectNodes.any { it.node.status == "active" } // NON-NLS
            val hasRecentCompletions =
                projectNodes.any {
                    it.node.status == "done" &&
                        (
                            it.node.completedAt
                                ?: 0
                        ) >= sevenDaysAgo // NON-NLS
                }
            hasActiveItems && !hasRecentCompletions
        }

    val completionsByArea =
        recentCompletions
            .mapNotNull { item -> item.node.areaId?.let { it to item } }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.size }
    val completionsByProject =
        recentCompletions
            .mapNotNull { item -> item.node.projectId?.let { it to item } }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.size }

    val inboxGrowth = recentNodes.count { it.node.inboxState }
    val archivedCount =
        nodes.count {
            it.node.status == "archived" && (it.node.archivedAt ?: 0) >= sevenDaysAgo // NON-NLS
        }
    val archiveRate =
        if (recentNodes.isNotEmpty()) archivedCount.toDouble() / recentNodes.size else 0.0

    val activeTasks =
        nodes.count { it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE }
    val recentTaskCompletions = recentCompletions.count { it.node.isTaskItem() }
    val backlogPressure =
        if (recentTaskCompletions > 0) activeTasks.toDouble() / recentTaskCompletions else activeTasks.toDouble()

    val overdueCount =
        nodes.count { it.node.dueAt != null && it.node.dueAt < now && it.node.status == "active" } // NON-NLS
    val chaosScore =
        (overdueCount * 10) + (inboxGrowth * 5) + (if (backlogPressure > 5) 50 else 0)

    val uniqueContextsPerDay =
        recentSessions
            .groupBy {
                Instant
                    .fromEpochMilliseconds(it.startedAt)
                    .toLocalDateTime(sysZone)
                    .date
            }.mapValues {
                it.value
                    .mapNotNull { s -> nodes.find { n -> n.node.id == s.nodeId }?.node?.projectId }
                    .distinct()
                    .size
            }
    val contextSwitchingRate =
        if (uniqueContextsPerDay.isNotEmpty()) uniqueContextsPerDay.values.average() else 0.0

    // Light Manual Statistics (Roadmap Section 7)
    // Correlating track entries with activity
    val dailyCompletions =
        recentCompletions
            .groupBy {
                Instant
                    .fromEpochMilliseconds(it.node.completedAt ?: 0)
                    .toLocalDateTime(sysZone)
                    .date
                    .toString()
            }.mapValues { it.value.size }

    val dailyCaptures =
        recentNodes
            .groupBy {
                Instant
                    .fromEpochMilliseconds(it.node.createdAt)
                    .toLocalDateTime(sysZone)
                    .date
                    .toString()
            }.mapValues { it.value.size }

    val dailyFocus =
        recentSessions
            .groupBy {
                Instant
                    .fromEpochMilliseconds(it.startedAt)
                    .toLocalDateTime(sysZone)
                    .date
                    .toString()
            }.mapValues { it.value.sumOf { s -> s.durationSec } / 3600.0 }

    val moodVsCompletions =
        if (recentTracks.isNotEmpty()) {
            val moodOnBusyDays =
                recentTracks
                    .filter { (dailyCompletions[it.date] ?: 0) >= 3 }
                    .mapNotNull { it.moodScore }
                    .takeIf { it.isNotEmpty() }
                    ?.average() ?: Double.NaN
            val moodOnSlowDays =
                recentTracks
                    .filter { (dailyCompletions[it.date] ?: 0) == 0 }
                    .mapNotNull { it.moodScore }
                    .takeIf { it.isNotEmpty() }
                    ?.average() ?: Double.NaN
            if (!moodOnBusyDays.isNaN() && !moodOnSlowDays.isNaN()) moodOnBusyDays - moodOnSlowDays else 0.0
        } else {
            0.0
        }

    val sleepVsFocus =
        if (recentTracks.isNotEmpty()) {
            val focusOnGoodSleep =
                recentTracks
                    .filter { (it.sleepScore ?: 0f) >= 7f }
                    .map { dailyFocus[it.date] ?: 0.0 }
                    .takeIf { it.isNotEmpty() }
                    ?.average()
                    ?: Double.NaN
            val focusOnBadSleep =
                recentTracks
                    .filter { (it.sleepScore ?: 0f) < 7f }
                    .map { dailyFocus[it.date] ?: 0.0 }
                    .takeIf { it.isNotEmpty() }
                    ?.average()
                    ?: Double.NaN
            if (!focusOnGoodSleep.isNaN() && !focusOnBadSleep.isNaN()) focusOnGoodSleep - focusOnBadSleep else 0.0
        } else {
            0.0
        }

    val energyVsCaptures =
        if (recentTracks.isNotEmpty()) {
            val capturesOnHighEnergy =
                recentTracks
                    .filter { (it.energyScore ?: 0) >= 4 }
                    .map { dailyCaptures[it.date] ?: 0 }
                    .takeIf { it.isNotEmpty() }
                    ?.average()
                    ?: Double.NaN
            val capturesOnLowEnergy =
                recentTracks
                    .filter { (it.energyScore ?: 0) <= 2 }
                    .map { dailyCaptures[it.date] ?: 0 }
                    .takeIf { it.isNotEmpty() }
                    ?.average()
                    ?: Double.NaN
            if (!capturesOnHighEnergy.isNaN() && !capturesOnLowEnergy.isNaN()) capturesOnHighEnergy - capturesOnLowEnergy else 0.0
        } else {
            0.0
        }

    val anxietyVsAvoidance =
        if (recentTracks.isNotEmpty()) {
            // Using low mood/energy as a proxy for high anxiety/stress if not explicitly tracked
            val postponesOnBadDays =
                recentTracks.filter { (it.moodScore ?: 5) <= 2 }.sumOf { track ->
                    recentNodes
                        .filter {
                            val d =
                                Instant
                                    .fromEpochMilliseconds(it.node.updatedAt)
                                    .toLocalDateTime(sysZone)
                                    .date
                                    .toString()
                            d == track.date && it.node.postponeCount > 0
                        }.size
                }
            postponesOnBadDays.toDouble()
        } else {
            0.0
        }

    val medsEffectiveness =
        if (recentTracks.isNotEmpty()) {
            val focusWithMeds =
                recentTracks
                    .filter { it.tookMeds }
                    .mapNotNull { it.focusScore }
                    .takeIf { it.isNotEmpty() }
                    ?.average() ?: Double.NaN
            val focusWithoutMeds =
                recentTracks
                    .filter { !it.tookMeds }
                    .mapNotNull { it.focusScore }
                    .takeIf { it.isNotEmpty() }
                    ?.average() ?: Double.NaN
            if (!focusWithMeds.isNaN() && !focusWithoutMeds.isNaN()) focusWithMeds - focusWithoutMeds else 0.0
        } else {
            0.0
        }

    // Insight Cards Logic
    val mostPostponedAreaId =
        nodes
            .mapNotNull { item ->
                item.node.areaId
                    ?.takeIf { item.node.postponeCount > 0 }
                    ?.let { it to item }
            }.groupBy({ it.first }, { it.second })
            .maxByOrNull { entry -> entry.value.sumOf { it.node.postponeCount } }
            ?.key

    val knowledgeCaptureHours =
        nodes
            .filter { it.node.isKnowledgeItem() && it.node.createdAt >= sevenDaysAgo }
            .map {
                Instant
                    .fromEpochMilliseconds(it.node.createdAt)
                    .toLocalDateTime(sysZone)
                    .hour
            }

    val captureTimePattern =
        if (knowledgeCaptureHours.isNotEmpty()) {
            val morning = knowledgeCaptureHours.count { it in 6..11 }
            val afternoon = knowledgeCaptureHours.count { it in 12..17 }
            val evening = knowledgeCaptureHours.count { it in 18..23 }
            val night = knowledgeCaptureHours.count { it in 0..5 }
            val max = listOf(morning, afternoon, evening, night).maxOrNull() ?: 0
            when (max)
            {
                morning -> "Morning"
                afternoon -> "Afternoon"
                evening -> "Evening"
                else -> "Night"
            }
        } else {
            null
        }

    val nodesByProjectIdForInsights = nodes.groupBy { it.node.projectId }
    val projectsWithoutTasks =
        projects.filter { project ->
            val projectNodes = nodesByProjectIdForInsights[project.id] ?: emptyList()
            val hasNotes = projectNodes.any { it.node.isKnowledgeItem() }
            val hasTasks =
                projectNodes.any { it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE }
            hasNotes && !hasTasks
        }

    // Area nodes used below to detect neglected areas
    val areas = nodes.mapNotNull { item -> item.node.takeIf { it.isAreaItem() } }
    val nodesByAreaId = nodes.groupBy { it.node.areaId }
    val neglectedAreas =
        areas.filter { area ->
            val areaNodes = nodesByAreaId[area.id] ?: emptyList()
            val hasRecentActivity = areaNodes.any { it.node.updatedAt >= sevenDaysAgo }
            !hasRecentActivity
        }

    // Advanced Insight Concepts
    val projectEntropy =
        projects.associate { project ->
            val projectNodes =
                (nodesByProjectIdForInsights[project.id] ?: emptyList()).filter { it.node.status == "active" }
            if (projectNodes.isEmpty()) {
                project.id to 0.0
            } else {
                val messyNodes =
                    projectNodes.count {
                        it.node.dueAt == null || it.node.postponeCount > 2 || it.tags.isEmpty()
                    }
                project.id to (messyNodes.toDouble() / projectNodes.size)
            }
        }

    val contextStability =
        if (contextSwitchingRate > 0) 1.0 / (1.0 + contextSwitchingRate) else 1.0

    val behaviorSummary =
        buildString {
            if (mostProductiveHour != -1) {
                append("You typically finish tasks around $mostProductiveHour:00. ")
            }
            if (archiveRate > 0.3) {
                append("You have a healthy habit of archiving items. ")
            } else if (backlogPressure > 10) {
                append("Your backlog is growing faster than you can process it. Consider a cleanup. ")
            }
            if (contextStability < 0.3) {
                append("You context-switch frequently. Deep focus sessions might be harder to maintain. ")
            }
        }

    val review =
        buildString {
            append("This week you captured ${recentNodes.size} items and completed ${recentCompletions.size}. ")
            val recentKnowledge = recentNodes.count { it.node.isKnowledgeItem() }
            if (recentKnowledge > 0) {
                append("You also added $recentKnowledge new knowledge items to your library. ")
            }
            if (weeklyFocusSec > 0) {
                append("You spent ${((weeklyFocusSec / 3600.0) * 10).toInt() / 10.0} hours in deep focus. ")
            }
            if (neglectedProjects.isNotEmpty()) {
                append("Note that ${neglectedProjects.size} projects are slipping through the cracks. ")
            }
            if (avgMood > 0) {
                append("Your average mood was ${((avgMood * 10).toInt() / 10.0)}/5.0. ")
            }
            if (recentNodes.isNotEmpty()) {
                val ratio =
                    (recentCompletions.size.toDouble() / recentNodes.size.toDouble() * 100).toInt()
                append("Current execution ratio: $ratio%. ")
            }
            if (backlogPressure > 5.0) {
                append("Warning: Your backlog pressure is high ($backlogPressure). ")
            }
            if (medsEffectiveness > 0.5) {
                append("Focus seems significantly better on days you take medication. ")
            }
            if (captureTimePattern != null) {
                append("You are most creative in the $captureTimePattern. ")
            }
        }

    return InsightsData(
        weeklyCaptures = recentNodes.size,
        weeklyCompletions = recentCompletions.size,
        weeklyFocusHours = weeklyFocusSec / 3600.0,
        bestFocusHour = bestFocusHour,
        avgMood = avgMood,
        avgEnergy = avgEnergy,
        avgFocus = avgFocus,
        neglectedProjects = neglectedProjects,
        captureToActionRatio = if (recentNodes.isNotEmpty()) recentCompletions.size.toDouble() / recentNodes.size.toDouble() else 0.0,
        autoPreparedReview = review,
        avgSessionMinutes = avgSessionMin,
        inboxGrowth = inboxGrowth,
        archiveRate = archiveRate,
        completionsByArea = completionsByArea,
        completionsByProject = completionsByProject,
        mostProductiveHour = mostProductiveHour,
        postponeFrequency = recentNodes.sumOf { it.node.postponeCount },
        backlogPressure = backlogPressure,
        chaosScore = chaosScore,
        contextSwitchingRate = contextSwitchingRate,
        moodVsCompletions = moodVsCompletions,
        sleepVsFocus = sleepVsFocus,
        energyVsCaptures = energyVsCaptures,
        anxietyVsAvoidance = anxietyVsAvoidance,
        medsEffectiveness = medsEffectiveness,
        mostPostponedAreaId = mostPostponedAreaId,
        captureTimePattern = captureTimePattern,
        projectsWithoutTasks = projectsWithoutTasks,
        neglectedAreas = neglectedAreas,
        projectEntropy = projectEntropy,
        contextStability = contextStability,
        passiveBehaviorSummary = behaviorSummary,
    )
}

/**
 * Groups all nodes by their configured Area of Responsibility assignment,
 * calculating stress loads, neglect days, and balancing scores to produce an [AreaHealthSnapshot].
 *
 * @param nodes The complete list of active nodes in the system.
 * @param areas A curated list of active nodes identified as Areas of Responsibility.
 * @return An [AreaHealthSnapshot] detailing which life domains are thriving and which are neglected.
 */
fun calculateAreaHealthSnapshot(
    nodes: List<NodeWithPin>,
    areas: List<NodeEntity>,
): com.tajemniktv.tajsos.ui.AreaHealthSnapshot {
    if (areas.isEmpty()) {
        return com.tajemniktv.tajsos.ui
            .AreaHealthSnapshot()
    }

    val now = Clock.System.now().toEpochMilliseconds()
    val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
    val dueSoonHorizon = now + (7 * 24 * 60 * 60 * 1000L)

    val computed =
        areas
            .map { area ->
                val areaNodes =
                    nodes.filter { it.node.areaId == area.id && it.node.type != "area" }
                val activeNodes = areaNodes.filter { it.node.status == "active" }
                val openLoops = activeNodes.count { it.node.type == "open_loop" }
                val deadlines = activeNodes.count { it.node.dueAt != null }
                val overdueDeadlines =
                    activeNodes.count { (it.node.dueAt ?: Long.MAX_VALUE) < now }
                val dueSoon =
                    activeNodes.count {
                        val due = it.node.dueAt
                        due != null && due in now..dueSoonHorizon
                    }
                val recentActivity = areaNodes.count { it.node.updatedAt >= sevenDaysAgo }
                val doneThisWeek =
                    areaNodes.count {
                        it.node.status == "done" && (it.node.completedAt ?: 0) >= sevenDaysAgo
                    }
                val lastActivityAt =
                    (
                        areaNodes.maxOfOrNull { it.node.updatedAt }
                            ?: area.updatedAt
                    ).takeIf { it > 0 }
                val neglectedDays =
                    (
                        (
                            (
                                    now -
                                    (
                                        lastActivityAt
                                            ?: now
                                    )
                            ).coerceAtLeast(0L)
                        ) /
                            (24 * 60 * 60 * 1000L)
                    ).toInt()

                val stressLoad =
                    (
                        (activeNodes.size * 2) +
                            (openLoops * 10) +
                            (deadlines * 4) +
                            (dueSoon * 8) +
                            (overdueDeadlines * 20) +
                            activeNodes.sumOf { (it.node.postponeCount.coerceAtMost(3) * 4) }
                    ).coerceIn(0, 100)

                val status =
                    when
                        {
                            overdueDeadlines >= 3 || stressLoad >= 85 -> "on_fire"
                            stressLoad >= 70 || activeNodes.size >= 15 -> "overloaded"
                            neglectedDays >= 14 && activeNodes.isNotEmpty() -> "neglected"
                            activeNodes.isNotEmpty() || recentActivity > 0 -> "active"
                            else -> "stable"
                        }

                val isDisappearing =
                    neglectedDays >= 10 &&
                        recentActivity == 0 &&
                        (activeNodes.isNotEmpty() || openLoops > 0 || deadlines > 0)

                com.tajemniktv.tajsos.ui.AreaHealthMetrics(
                    areaId = area.id,
                    areaTitle = area.title,
                    status = status,
                    activeItems = activeNodes.size,
                    openLoops = openLoops,
                    deadlines = deadlines,
                    overdueDeadlines = overdueDeadlines,
                    stressLoad = stressLoad,
                    recentActivity = recentActivity,
                    neglectedDays = neglectedDays,
                    doneThisWeek = doneThisWeek,
                    lastActivityAt = lastActivityAt,
                    isDisappearing = isDisappearing,
                )
            }.sortedByDescending { it.stressLoad }

    val dominantAreaId =
        computed
            .maxByOrNull { area ->
                (area.recentActivity * 2) + (area.doneThisWeek * 3) + area.activeItems
            }?.areaId

    val disappearingAreaIds =
        computed.filter { it.isDisappearing }.mapTo(mutableSetOf()) { it.areaId }

    val avgLoad =
        if (computed.isNotEmpty()) {
            computed.map { it.stressLoad }.average()
        } else {
            0.0
        }
    val variance =
        if (computed.size > 1) {
            computed.map { (it.stressLoad - avgLoad) * (it.stressLoad - avgLoad) }.average()
        } else {
            0.0
        }
    val imbalanceScore = (sqrt(variance) * 2).toInt().coerceIn(0, 100)
    val imbalanceLabel =
        when
            {
                imbalanceScore >= 60 -> "critical"
                imbalanceScore >= 30 -> "tilted"
                else -> "balanced"
            }

    return com.tajemniktv.tajsos.ui.AreaHealthSnapshot(
        areas = computed,
        dominantAreaId = dominantAreaId,
        disappearingAreaIds = disappearingAreaIds,
        imbalanceScore = imbalanceScore,
        imbalanceLabel = imbalanceLabel,
    )
}

/**
 * Scans the user's node database for unresolved inputs (items in the inbox or tasks without context),
 * grouping them by urgency and aging decay to generate an [OpenLoopsSnapshot].
 *
 * @param nodes The complete list of active nodes in the system.
 * @param people A curated list of nodes strictly identified as "person" entities.
 * @param relations The complete list of formal bidirectional database links.
 * @return An [OpenLoopsSnapshot] identifying cognitive friction caused by unprocessed items.
 */
fun calculateOpenLoopsSnapshot(
    nodes: List<NodeWithPin>,
    relations: List<RelationEntity>,
): com.tajemniktv.tajsos.ui.OpenLoopsSnapshot {
    val now = Clock.System.now().toEpochMilliseconds()
    val nodesById = nodes.associateBy { it.node.id }

    /**
     * Iterates through all relations linked to an open loop to identify if it is currently waiting on a specific person.
     * @param openLoopId The unique ID of the open loop node.
     * @return A Pair containing the linked person's ID and name, or null if no dependency exists.
     */
    fun findRelatedPerson(openLoopId: Long): Pair<Long, String>? {
        val relatedPersonId =
            relations.firstNotNullOfOrNull { relation ->
                val otherId =
                    when (openLoopId)
                        {
                            relation.fromNodeId -> relation.toNodeId
                            relation.toNodeId -> relation.fromNodeId
                            else -> null
                        } ?: return@firstNotNullOfOrNull null
                val other = nodesById[otherId]?.node ?: return@firstNotNullOfOrNull null
                if (other.type == "person") other.id else null
            } ?: return null
        val personName = nodesById[relatedPersonId]?.node?.title ?: "Unknown"
        return relatedPersonId to personName
    }

    val openLoopItems =
        nodes
            .filter { it.node.type == "open_loop" && it.node.status != "archived" }
            .map { openLoop ->
                val person = findRelatedPerson(openLoop.node.id)
                val urgency = openLoopUrgency(openLoop.node, now)
                val ageDays =
                    ((now - openLoop.node.createdAt).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
                val stalenessAnchor =
                    openLoop.node.openLoopStalenessAt ?: openLoop.node.updatedAt
                val stalenessDays =
                    ((now - stalenessAnchor).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
                com.tajemniktv.tajsos.ui.OpenLoopStatusItem(
                    node = openLoop,
                    urgency = urgency,
                    ageDays = ageDays,
                    stalenessDays = stalenessDays,
                    decayScore = openLoopDecayScore(openLoop.node, now),
                    relatedPersonId = person?.first,
                    relatedPersonName = person?.second,
                )
            }

    val active =
        openLoopItems
            .filter { it.node.node.status == "active" }
            .sortedByDescending { it.decayScore }
    val inbox = active.filter { it.node.node.inboxState }
    val review =
        active
            .filter { item ->
                item.stalenessDays >= 5 ||
                    item.ageDays >= 7 ||
                    item.urgency == "critical" ||
                    item.urgency == "high"
            }.sortedByDescending { it.decayScore }
    val resolved =
        openLoopItems
            .filter { it.node.node.status == "done" }
            .sortedByDescending { it.node.node.completedAt ?: 0L }

    val byArea = active.groupBy { it.node.node.areaId }
    val byPerson =
        active
            .mapNotNull { item -> item.relatedPersonId?.let { it to item } }
            .groupBy({ it.first }, { it.second })
    val byUrgency =
        linkedMapOf(
            "critical" to active.filter { it.urgency == "critical" },
            "high" to active.filter { it.urgency == "high" },
            "medium" to active.filter { it.urgency == "medium" },
            "low" to active.filter { it.urgency == "low" },
        ).filterValues { it.isNotEmpty() }

    val averageDecayScore =
        if (active.isNotEmpty()) active.map { it.decayScore }.average().toInt() else 0
    val overloadWarning =
        when
            {
                active.size >= 12 -> "TOO MANY OPEN LOOPS // REDUCE FRONTS"

                (
                    byUrgency["critical"]?.size
                        ?: 0
                ) >= 4 -> "MULTIPLE CRITICAL OPEN LOOPS // PRIORITIZE RESOLUTION"

                averageDecayScore >= 60 -> "OPEN LOOPS DECAYING // RUN REVIEW"

                else -> null
            }

    return com.tajemniktv.tajsos.ui.OpenLoopsSnapshot(
        active = active,
        inbox = inbox,
        review = review,
        resolved = resolved,
        byArea = byArea,
        byPerson = byPerson,
        byUrgency = byUrgency,
        overloadWarning = overloadWarning,
        averageDecayScore = averageDecayScore,
    )
}

/**
 * Calculates the urgency identifier for an open loop based on due dates and staleness.
 * Assumes items are more urgent if due within the next 24 hours or stale for 14+ days.
 *
 * @param node The [NodeEntity] representing the open loop.
 * @param now The current epoch timestamp in milliseconds.
 * @return A string identifier representing the calculated urgency level.
 */
fun openLoopUrgency(
    node: NodeEntity,
    now: Long,
): String {
    val dueAt = node.dueAt
    val stalenessAnchor = node.openLoopStalenessAt ?: node.updatedAt
    val stalenessDays =
        ((now - stalenessAnchor).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()

    return when
        {
            dueAt != null && dueAt < now -> "critical"
            dueAt != null && dueAt < now + (24 * 60 * 60 * 1000L) -> "critical"
            stalenessDays >= 14 -> "critical"
            dueAt != null && dueAt < now + (3 * 24 * 60 * 60 * 1000L) -> "high"
            stalenessDays >= 7 -> "high"
            dueAt != null && dueAt < now + (7 * 24 * 60 * 60 * 1000L) -> "medium"
            stalenessDays >= 3 -> "medium"
            else -> "low"
        }
}

/**
 * Calculates a numerical decay score for an open loop based on staleness.
 *
 * Items that have not been updated or explicitly marked as fresh recently will accumulate
 * a higher decay score, signaling that they require review or culling.
 *
 * @param node The node entity representing the open loop.
 * @param now The current epoch timestamp in milliseconds.
 * @return An integer from 0 to 100 representing the decay severity (higher means more stale).
 */
fun openLoopDecayScore(
    node: NodeEntity,
    now: Long,
): Int {
    val ageDays = ((now - node.createdAt).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
    val stalenessAnchor = node.openLoopStalenessAt ?: node.updatedAt
    val stalenessDays =
        ((now - stalenessAnchor).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
    val urgencyBoost =
        when (openLoopUrgency(node, now))
        {
            "critical" -> 30
            "high" -> 18
            "medium" -> 8
            else -> 0
        }
    return (
        (ageDays * 3) +
            (stalenessDays * 5) +
            (node.postponeCount.coerceAtMost(4) * 7) +
            urgencyBoost
    ).coerceIn(0, 100)
}

/**
 * Evaluates all active maintenance nodes (chores, administration, subscriptions)
 * and calculates their specific due dates and overdue statuses to generate a [MaintenanceSnapshot].
 *
 * @param nodes The complete list of active nodes in the system.
 * @return A [MaintenanceSnapshot] summarizing the user's administrative and routine debt.
 */
fun calculateMaintenanceSnapshot(nodes: List<NodeWithPin>): com.tajemniktv.tajsos.ui.MaintenanceSnapshot {
    val now = Clock.System.now().toEpochMilliseconds()
    val soonHorizon = now + (7 * 24 * 60 * 60 * 1000L)
    val criticalTypes =
        setOf("bill", "prescription", "renewal", "subscription", "form", "appointment")

    val activeItems =
        nodes
            .filter { it.node.type == "maintenance" && it.node.status == "active" }
            .map { item ->
                val urgency = maintenanceUrgency(item.node, now)
                val anchor = item.node.maintenanceOverdueAt ?: item.node.dueAt
                val overdueDays =
                    if (anchor != null && anchor < now) {
                        ((now - anchor) / (24 * 60 * 60 * 1000L)).toInt()
                    } else {
                        0
                    }
                val dueInDays =
                    if (anchor != null && anchor >= now) {
                        ((anchor - now) / (24 * 60 * 60 * 1000L)).toInt()
                    } else {
                        null
                    }
                com.tajemniktv.tajsos.ui.MaintenanceStatusItem(
                    node = item,
                    urgency = urgency,
                    isRecurring = item.node.isRecurring || item.node.maintenanceInterval != null,
                    overdueDays = overdueDays,
                    dueInDays = dueInDays,
                )
            }.sortedByDescending {
                when (it.urgency)
                    {
                        "critical" -> 4
                        "high" -> 3
                        "medium" -> 2
                        else -> 1
                } *
                        100 +
                        it.overdueDays
            }

    val recurring = activeItems.filter { it.isRecurring }
    val overdue = activeItems.filter { it.overdueDays > 0 || it.urgency == "critical" }
    val expirationReminders =
        activeItems
            .filter { item ->
                val due = item.node.node.maintenanceOverdueAt ?: item.node.node.dueAt
                due != null && due in now..soonHorizon
            }.sortedBy {
                it.node.node.maintenanceOverdueAt ?: it.node.node.dueAt ?: Long.MAX_VALUE
            }

    val breakIfIgnored =
        activeItems
            .filter {
                (
                    it.node.node.maintenanceType
                        ?: "manual"
                        ) in criticalTypes ||
                        it.urgency == "critical"
            }.take(6)

    val byType = activeItems.groupBy { it.node.node.maintenanceType ?: "manual" }
    val byArea = activeItems.groupBy { it.node.node.areaId }
    val byUrgency =
        linkedMapOf(
            "critical" to activeItems.filter { it.urgency == "critical" },
            "high" to activeItems.filter { it.urgency == "high" },
            "medium" to activeItems.filter { it.urgency == "medium" },
            "low" to activeItems.filter { it.urgency == "low" },
        ).filterValues { it.isNotEmpty() }

    val adminDebtMeter =
        (
            (activeItems.size * 4) +
                (overdue.size * 12) +
                ((byUrgency["critical"]?.size ?: 0) * 18)
        ).coerceIn(0, 100)
    val overdueWarning =
        when
            {
                (
                    byUrgency["critical"]?.size
                        ?: 0
                ) >= 3 -> "CRITICAL MAINTENANCE OVERDUE // ACT TODAY"

                overdue.size >= 5 -> "MAINTENANCE DEBT SPIKING // RUN ADMIN BLOCK"

                adminDebtMeter >= 70 -> "ADMIN DEBT HIGH // REDUCE RISK ITEMS"

                else -> null
            }

    return com.tajemniktv.tajsos.ui.MaintenanceSnapshot(
        active = activeItems,
        recurring = recurring,
        overdue = overdue,
        byType = byType,
        byArea = byArea,
        byUrgency = byUrgency,
        expirationReminders = expirationReminders,
        breakIfIgnored = breakIfIgnored,
        adminDebtMeter = adminDebtMeter,
        overdueWarning = overdueWarning,
    )
}

/**
 * Determines the string-based urgency level for a given maintenance item.
 * Uses predefined "criticalTypes" such as bills or prescriptions to escalate urgency compared to routine chores.
 *
 * @param node The [NodeEntity] representing the maintenance chore.
 * @param now The current epoch timestamp in milliseconds.
 * @return A string identifier indicating the calculated urgency level.
 */
fun maintenanceUrgency(
    node: NodeEntity,
    now: Long,
): String {
    val due = node.maintenanceOverdueAt ?: node.dueAt
    val type = node.maintenanceType ?: "manual"
    return when
        {
            due != null && due < now -> "critical"

            due != null && due < now + (24 * 60 * 60 * 1000L) -> "critical"

            type in
                setOf(
                    "bill",
                    "prescription",
                    "renewal",
                ) &&
                    due != null &&
                    due < now + (3 * 24 * 60 * 60 * 1000L) -> "high"

            due != null && due < now + (3 * 24 * 60 * 60 * 1000L) -> "high"

            due != null && due < now + (7 * 24 * 60 * 60 * 1000L) -> "medium"

            else -> "low"
        }
}

/**
 * Scans all node entities evaluating explicit due dates, implicit horizons,
 * and project phase assignments to build a [TimeArchitectureSnapshot] stratifying tasks by timeline.
 *
 * To preserve UI performance and minimize garbage collection pauses, string sorting uses
 * `compareTo(..., ignoreCase = true)` rather than allocating lowercase copies.
 *
 * @param nodes The complete list of active nodes in the system.
 * @param projects A curated list of nodes strictly identified as "project" entities.
 * @return A [TimeArchitectureSnapshot] organizing nodes into layers (e.g., today, week, semester).
 */
fun calculateTimeArchitectureSnapshot(
    nodes: List<NodeWithPin>,
    todayLayerNodes: List<NodeEntity>,
    projects: List<NodeEntity>,
): com.tajemniktv.tajsos.ui.TimeArchitectureSnapshot {
    val now = Clock.System.now().toEpochMilliseconds()
    val dayMs = 24 * 60 * 60 * 1000L
    val weekHorizon = now + (7 * dayMs)
    val monthHorizon = now + (30 * dayMs)
    val semesterHorizon = now + (120 * dayMs)
    val todayIds = todayLayerNodes.mapTo(mutableSetOf()) { it.id }

    val activeNodes = nodes.filter { it.node.status == "active" }
    val dueNodes =
        activeNodes
            .filter { it.node.dueAt != null }
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
    val todayLayer = activeNodes.filter { it.node.id in todayIds }
    val weekLayer = dueNodes.filter { (it.node.dueAt ?: Long.MAX_VALUE) in now..weekHorizon }
    val monthLayer = dueNodes.filter { (it.node.dueAt ?: Long.MAX_VALUE) in now..monthHorizon }
    val semesterLayer =
        dueNodes.filter { (it.node.dueAt ?: Long.MAX_VALUE) in now..semesterHorizon }
    val shortHorizonTasks = weekLayer.filter { it.node.isTaskItem() }.take(8)
    val longHorizonTasks = dueNodes.filter { (it.node.dueAt ?: 0L) > monthHorizon }.take(8)
    val seasonalGoals =
        activeNodes.filter { item ->
            item.node.noteType == "goal_seasonal" ||
                item.tags.any { tag -> tag.normalizedName == "seasonal_goal" }
        }
    val temporaryFocusPeriods =
        activeNodes
            .filter { item ->
                val startAt = item.node.startAt ?: return@filter false
                val dueAt = item.node.dueAt ?: return@filter false
                dueAt > startAt && (dueAt - startAt) <= (14 * dayMs)
            }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
    val lifePeriodMarkers =
        nodes
            .filter { item ->
                item.node.noteType == "period_marker" ||
                    item.tags.any { tag -> tag.normalizedName == "life_period_marker" }
            }.sortedByDescending { it.node.updatedAt }
            .take(8)
    val countdowns =
        dueNodes
            .map { item ->
                val due = item.node.dueAt ?: now
                val daysLeft = ((due - now).coerceAtLeast(0L) / dayMs)
                com.tajemniktv.tajsos.ui.TimeCountdownItem(
                    node = item,
                    daysLeft = daysLeft,
                )
            }.take(8)
    val examPeriodMode =
        countdowns.any { countdown ->
            countdown.daysLeft <= 30 &&
                (
                    countdown.node.node.title
                        .contains("exam", ignoreCase = true) ||
                        countdown.node.tags.any { tag ->
                            tag.normalizedName.contains(
                                "exam",
                            )
                        }
                )
        }
    val weeklyMap =
        weekLayer
            .groupingBy { item ->
                val due = item.node.dueAt ?: now
                Instant
                    .fromEpochMilliseconds(due)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date.dayOfWeek.name
            }.eachCount()
            .entries
            .sortedBy { it.key }
            .associate { it.toPair() }
    val projectPhases =
        projects
            .map { project ->
                val phase = project.projectStatus ?: "active"
                val isActive = phase in setOf("active", "exploratory")
                com.tajemniktv.tajsos.ui.ProjectPhaseItem(
                    project = project,
                    isActivePhase = isActive,
                    phaseLabel = if (isActive) "active_phase" else "inactive_phase",
                )
            }.sortedWith { a, b -> a.project.title.compareTo(b.project.title, ignoreCase = true) }

    return com.tajemniktv.tajsos.ui.TimeArchitectureSnapshot(
        todayLayer = todayLayer,
        weekLayer = weekLayer,
        monthLayer = monthLayer,
        semesterLayer = semesterLayer,
        examPeriodMode = examPeriodMode,
        projectPhases = projectPhases,
        countdowns = countdowns,
        monthlyResetDate = nextMonthlyResetDate(),
        weeklyMap = weeklyMap,
        seasonalGoals = seasonalGoals,
        temporaryFocusPeriods = temporaryFocusPeriods,
        shortHorizonTasks = shortHorizonTasks,
        longHorizonTasks = longHorizonTasks,
        lifePeriodMarkers = lifePeriodMarkers,
    )
}

/**
 * Computes the string representation (YYYY-MM-DD) of the first day of the following calendar month,
 * used for monthly resets and long-term planning horizons.
 *
 * @return The calculated next monthly reset date.
 */
fun nextMonthlyResetDate(): String {
    val now = Clock.System.now()
    val zone = TimeZone.currentSystemDefault()
    val nextMonth = now.plus(1, DateTimeUnit.MONTH, zone).toLocalDateTime(zone)
    val nextReset = LocalDate(nextMonth.year, nextMonth.month, 1)
    return nextReset.toString()
}

/**
 * Evaluates all person-anchored nodes and their explicitly assigned relational events
 * to generate a [RelationshipSnapshot]. Assesses when someone was last contacted and calculates follow-up pressure.
 *
 * @param nodes The complete list of active nodes in the system.
 * @param relations The complete list of relational edges tying nodes together.
 * @return A structured [RelationshipSnapshot] describing the health of active tracked relationships.
 */
fun calculateRelationshipSnapshot(
    nodes: List<NodeWithPin>,
    relations: List<RelationEntity>,
): RelationshipSnapshot {
    val now = Clock.System.now().toEpochMilliseconds()
    val dayMs = 24 * 60 * 60 * 1000L
    val byId = nodes.associateBy { it.node.id }

    val people =
        nodes
            .filter { it.node.isRelationshipAnchor() && it.node.status == "active" }
            .sortedWith { a, b -> a.node.title.compareTo(b.node.title, ignoreCase = true) }

    /**
     * Filters the global node list to extract all tasks or notes explicitly mentioning or linked to a specific person.
     * @param personId The unique ID of the person entity.
     * @return A list of node entities formally related to the person.
     */
    fun relatedForPerson(personId: Long): List<NodeWithPin> =
        relations
            .mapNotNull { relation ->
                when
                    {
                        relation.fromNodeId == personId -> byId[relation.toNodeId]
                        relation.toNodeId == personId -> byId[relation.fromNodeId]
                        else -> null
                    }
            }.filter { !it.node.isRelationshipAnchor() }
            .distinctBy { it.node.id }

    val peopleItems =
        people.map { person ->
            val relatedNodes = relatedForPerson(person.node.id)
            val replyQueueCount =
                relatedNodes.count {
                    it.node.isTaskItem() &&
                        it.node.taskStateOrNull() == TaskState.ACTIVE &&
                        (it.node.openLoopType == "reply_needed" || it.node.openLoopType == "follow_up")
                }
            val sharedPlansCount =
                relatedNodes.count {
                    it.node.status == "active" &&
                        (
                            it.tags.any { tag -> tag.normalizedName == "shared_plan" } ||
                                it.node.title.contains("shared", ignoreCase = true)
                        )
                }
            val askAboutCount =
                relatedNodes.count {
                    it.node.isNoteItem() &&
                        (
                            it.node.noteType == "ask_next_time" ||
                                it.tags.any { tag -> tag.normalizedName == "ask_next_time" }
                        )
                }
            val lastContact = person.node.lastContactAt
            val daysSince =
                lastContact?.let { ((now - it).coerceAtLeast(0L) / dayMs).toInt() }
            val followUpAt = person.node.dueAt
            val followUpDueInDays =
                followUpAt?.let { ((it - now) / dayMs).toInt() }
            val relationshipType =
                when
                    {
                        person.tags.any { it.normalizedName == "professor" } -> "professor"
                        person.tags.any { it.normalizedName == "family" } -> "family"
                        person.tags.any { it.normalizedName == "friend" } -> "friend"
                        else -> null
                    }
            RelationshipStatusItem(
                person = person,
                relationshipType = relationshipType,
                daysSinceLastContact = daysSince,
                followUpDueInDays = followUpDueInDays,
                isImportant =
                    person.tags.any { tag -> tag.normalizedName == "important_relationship" } ||
                        person.node.relationshipContext?.contains(
                            "important",
                            ignoreCase = true,
                        ) == true,
                linkedItemsCount = relatedNodes.size,
                pendingReplyCount = replyQueueCount,
                sharedPlansCount = sharedPlansCount,
                askAboutNextTimeCount = askAboutCount,
            )
        }

    val followUpNeeded =
        peopleItems
            .filter { item ->
                val stale = (item.daysSinceLastContact ?: 0) >= 14
                val followUpDue = (item.followUpDueInDays ?: Int.MAX_VALUE) <= 3
                stale || followUpDue || item.pendingReplyCount > 0
            }.sortedWith(
                compareByDescending<RelationshipStatusItem> { it.pendingReplyCount }
                    .thenByDescending { it.daysSinceLastContact ?: 0 },
            )

    val upcomingImportantDates =
        peopleItems
            .filter { item ->
                val dueIn = item.followUpDueInDays ?: return@filter false
                dueIn in 0..30
            }.sortedBy { it.followUpDueInDays }

    val allRelatedItemsByPerson =
        peopleItems.associate { item ->
            item.person.node.id to relatedForPerson(item.person.node.id)
        }
    val replyQueue =
        allRelatedItemsByPerson.values
            .flatten()
            .filter {
                it.node.taskStateOrNull() == TaskState.ACTIVE &&
                    it.node.isTaskItem() &&
                    (it.node.openLoopType == "reply_needed" || it.node.openLoopType == "follow_up")
            }.distinctBy { it.node.id }
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    val sharedPlans =
        allRelatedItemsByPerson.values
            .flatten()
            .filter {
                it.node.status == "active" &&
                    (
                        it.tags.any { tag -> tag.normalizedName == "shared_plan" } ||
                            it.node.title.contains("shared", ignoreCase = true)
                    )
            }.distinctBy { it.node.id }

    val importantRelationships =
        peopleItems
            .filter { it.isImportant }
            .sortedWith { a, b -> a.person.node.title.compareTo(b.person.node.title, ignoreCase = true) }

    val professors =
        peopleItems
            .filter { it.relationshipType == "professor" }
            .sortedWith { a, b -> a.person.node.title.compareTo(b.person.node.title, ignoreCase = true) }

    val friendsAndFamily =
        peopleItems
            .filter { it.relationshipType == "friend" || it.relationshipType == "family" }
            .sortedWith { a, b -> a.person.node.title.compareTo(b.person.node.title, ignoreCase = true) }

    val gentlePrompt =
        when
            {
                followUpNeeded.size >= 8 -> "Several connections need a touchpoint. Pick 1-2 gentle follow-ups today."
                followUpNeeded.isNotEmpty() -> "A small social maintenance pass could reduce unresolved social work."
                else -> null
            }

    return RelationshipSnapshot(
        people = peopleItems,
        importantRelationships = importantRelationships,
        followUpNeeded = followUpNeeded,
        upcomingImportantDates = upcomingImportantDates,
        replyQueue = replyQueue,
        sharedPlans = sharedPlans,
        professors = professors,
        friendsAndFamily = friendsAndFamily,
        gentlePrompt = gentlePrompt,
    )
}

/**
 * Evaluates all tasks and entities tied to physical locations (e.g., errands, travel, local chores).
 * Maps tasks to places to generate a [PhysicalLogisticsSnapshot] enabling batched execution.
 *
 * Performance is optimized by leveraging `compareTo` with `ignoreCase = true` during sorting
 * to prevent unnecessary string instantiations.
 *
 * @param nodes The complete list of active nodes in the system.
 * @param relations Relational edges to map tasks to their location nodes.
 * @param templates A list of system templates (e.g., packing lists, trip templates).
 * @return A [PhysicalLogisticsSnapshot] categorizing tasks and items by physical place.
 */
fun calculatePhysicalLogisticsSnapshot(
    nodes: List<NodeWithPin>,
    relations: List<RelationEntity>,
    templates: List<TemplateEntity>,
): PhysicalLogisticsSnapshot {
    val byId = nodes.associateBy { it.node.id }
    val activeNodes = nodes.filter { it.node.status == "active" }
    val activeTasks = activeNodes.filter { it.node.isTaskItem() }

    /**
     * Filters the global node list to extract all active tasks related to a specific physical location node.
     * Includes tasks physically bound via explicit links AND tasks heuristically included when their locationContext
     * matches campus/home patterns and the place title contains corresponding keywords.
     * @param placeId The unique ID of the physical place node.
     * @return A list of task nodes physically bound to or heuristically associated with the specified place.
     */
    fun relatedTasksForPlace(placeId: Long): List<NodeWithPin> {
        val relationTaskIds =
            relations
                .mapNotNull { relation ->
                    when (placeId)
                    {
                        relation.fromNodeId -> relation.toNodeId
                        relation.toNodeId -> relation.fromNodeId
                        else -> null
                    }
                }.toSet()
        return activeTasks.filter { task ->
            task.node.id in relationTaskIds ||
                (
                        task.node.locationContext == "on_campus" &&
                                (
                            byId[placeId]?.node?.title?.contains(
                                "campus",
                                ignoreCase = true,
                            ) == true
                        )
                ) ||
                (
                    task.node.locationContext == "at_home" &&
                        (
                            byId[placeId]?.node?.title?.contains(
                                            "home",
                                ignoreCase = true,
                            ) == true
                        )
                )
        }
    }

    val placeNodes = activeNodes.filter { it.node.isPlaceAnchor() }
    val placeItems =
        placeNodes
            .map { place ->
                val relatedTasks = relatedTasksForPlace(place.node.id)
                PlaceLogisticsItem(
                    place = place,
                    relatedTasks = relatedTasks,
                    remindersCount = relatedTasks.count { it.node.reminderAt != null },
                )
            }.sortedWith { a, b -> a.place.node.title.compareTo(b.place.node.title, ignoreCase = true) }

    val campusLocations =
        placeItems.filter {
            it.place.node.locationContext == "on_campus" ||
                it.place.tags.any { tag -> tag.normalizedName == "campus" } ||
                it.place.node.title
                    .contains("campus", ignoreCase = true)
        }
    val homeZones =
        placeItems.filter {
            it.place.node.locationContext == "at_home" ||
                it.place.tags.any { tag -> tag.normalizedName == "home" } ||
                it.place.node.title
                    .contains("home", ignoreCase = true)
        }

    val placeBasedTasks =
        activeTasks
            .filter { task ->
                task.node.locationContext != null ||
                    relations.any { relation ->
                        relation.fromNodeId == task.node.id &&
                                byId[relation.toNodeId]?.node?.isPlaceAnchor() == true ||
                                relation.toNodeId == task.node.id &&
                            byId[relation.fromNodeId]?.node?.isPlaceAnchor() == true
                    }
            }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    val outOfHomeTaskClusters =
        placeBasedTasks
            .filter { it.node.locationContext == "out_of_home" }
            .groupBy { task ->
                val linkedPlaceName =
                    relations.firstNotNullOfOrNull { relation ->
                        val otherId =
                            when (task.node.id)
                                {
                                    relation.fromNodeId -> relation.toNodeId
                                    relation.toNodeId -> relation.fromNodeId
                                    else -> null
                                } ?: return@firstNotNullOfOrNull null
                        val other = byId[otherId]?.node ?: return@firstNotNullOfOrNull null
                        if (other.isPlaceAnchor()) other.title else null
                    }
                linkedPlaceName ?: "GENERAL OUT-OF-HOME"
            }

    val errandClusters =
        activeTasks
            .filter {
                it.node.locationContext == "out_of_home" ||
                    it.tags.any { tag -> tag.normalizedName in setOf("errand", "shopping") }
            }.groupBy { task ->
                if (task.tags.any { it.normalizedName == "shopping" }) {
                    "SHOPPING"
                } else {
                    (
                        task.node.areaId?.toString()
                            ?: "GENERAL"
                    )
                }
            }

    /**
     * A fast lookup utility verifying if a specific node contains any of the provided tracking tags.
     * @param node The wrapped node entity with its associated tags.
     * @param tags A vararg of string tags to check (e.g., 'packing', 'dont_forget').
     * @return True if at least one matching tag is attached.
     */
    fun hasLogisticsTag(
        node: NodeWithPin,
        vararg tags: String,
    ): Boolean = node.tags.any { it.normalizedName in tags.toSet() }

    val whatToBringLists =
        activeNodes.filter {
            (it.node.isKnowledgeItem() || it.node.isTaskItem()) &&
                (
                    hasLogisticsTag(it, "what_to_bring") ||
                        it.node.title.contains("bring", ignoreCase = true)
                )
        }
    val packingLists =
        activeNodes.filter {
            (it.node.isKnowledgeItem() || it.node.isTaskItem()) &&
                (
                    hasLogisticsTag(it, "packing_list") ||
                        it.node.title.contains(
                            "pack",
                            ignoreCase = true,
                        )
                )
        }
    val leaveHomeChecklists =
        activeNodes.filter {
            (it.node.isKnowledgeItem() || it.node.isTaskItem()) &&
                (
                    hasLogisticsTag(
                        it,
                        "leave_home_checklist",
                    ) ||
                            it.node.title.contains("leave home", ignoreCase = true)
                )
        }
    val dontForgetSets =
        activeNodes.filter {
            hasLogisticsTag(it, "dont_forget_set") ||
                it.node.title.contains(
                    "don't forget",
                    ignoreCase = true,
                )
        }
    val eventPreparationLists =
        activeNodes.filter {
            hasLogisticsTag(it, "event_prep") ||
                it.node.title.contains(
                    "event prep",
                    ignoreCase = true,
                )
        }
    val classSpecificBringLists =
        activeNodes.filter {
            hasLogisticsTag(it, "class_bring") ||
                (
                    it.node.title.contains("class", ignoreCase = true) &&
                        it.node.title.contains("bring", ignoreCase = true)
                )
        }
    val physicalLogisticsNotes =
        activeNodes.filter {
            it.node.isKnowledgeItem() &&
                (
                    it.node.noteType == "logistics" ||
                        hasLogisticsTag(it, "logistics")
                )
        }

    val locationSpecificReminders =
        placeBasedTasks
            .filter { it.node.reminderAt != null }
            .sortedBy { it.node.reminderAt ?: Long.MAX_VALUE }

    val travelPackTemplateReady =
        templates.any { it.name.contains("travel pack", ignoreCase = true) }

    return PhysicalLogisticsSnapshot(
        places = placeItems,
        campusLocations = campusLocations,
        homeZones = homeZones,
        placeBasedTasks = placeBasedTasks,
        outOfHomeTaskClusters = outOfHomeTaskClusters,
        errandClusters = errandClusters,
        whatToBringLists = whatToBringLists,
        packingLists = packingLists,
        leaveHomeChecklists = leaveHomeChecklists,
        dontForgetSets = dontForgetSets,
        eventPreparationLists = eventPreparationLists,
        classSpecificBringLists = classSpecificBringLists,
        physicalLogisticsNotes = physicalLogisticsNotes,
        travelPackTemplateReady = travelPackTemplateReady,
        locationSpecificReminders = locationSpecificReminders,
    )
}

/**
 * Aggregates all knowledge nodes marked as operating principles, rules, or core philosophies
 * to generate a [PersonalRulesSnapshot].
 *
 * @param nodes The complete list of active nodes in the system.
 * @param relations Relational edges linking rules to specific protocols or playbooks.
 * @return A structured [PersonalRulesSnapshot].
 */
fun calculatePersonalRulesSnapshot(
    nodes: List<NodeWithPin>,
    relations: List<RelationEntity>,
): PersonalRulesSnapshot {
    val activeRules =
        nodes
            .filter { it.node.status == "active" }
            .filter { item ->
                item.node.type in setOf("rule", "principle", "note") &&
                    (
                        item.tags.any { it.normalizedName.startsWith("rule_") } ||
                            item.tags.any {
                                it.normalizedName in
                                    setOf(
                                        "principle",
                                        "operating_principle",
                                    )
                            } ||
                            item.node.noteType in setOf("principle", "rule")
                    )
            }.sortedByDescending { it.node.updatedAt }

    /**
     * Filters the global node list to isolate all nodes actively tagged with a specific string.
     * @param tag The exact string name of the tag to search for.
     * @return A subset list of nodes containing the requested tag.
     */
    fun byTag(tag: String): List<NodeWithPin> = activeRules.filter { item -> item.tags.any { it.normalizedName == tag } }

    val antiGoals = byTag("rule_anti_goal")
    val redFlags = byTag("rule_red_flag")
    val greenFlags = byTag("rule_green_flag")
    val priorities = byTag("rule_priority")
    val tendToForget = byTag("rule_tend_to_forget")
    val messesMeUp = byTag("rule_messes_me_up")
    val helpsOffBalance = byTag("rule_helps_off_balance")
    val decisionPrinciples = byTag("rule_decision_principle")
    val constraints = byTag("rule_constraint")
    val foundationalRules = byTag("rule_foundational")
    val recoveryReminders = byTag("rule_recovery_reminder")
    val distrustBrainNotes = byTag("rule_distrust_brain")
    val whatWorksNotes = byTag("rule_what_works")
    val pinnedPrinciples = activeRules.filter { it.node.isPinned }
    val playbookLinksCount =
        relations.count { relation ->
            relation.relationType == "PRINCIPLE_FOR_PLAYBOOK" ||
                relation.relationType == "PLAYBOOK_SUPPORTS_PRINCIPLE"
        }

    return PersonalRulesSnapshot(
        vault = activeRules,
        antiGoals = antiGoals,
        redFlags = redFlags,
        greenFlags = greenFlags,
        priorities = priorities,
        tendToForget = tendToForget,
        messesMeUp = messesMeUp,
        helpsOffBalance = helpsOffBalance,
        decisionPrinciples = decisionPrinciples,
        constraints = constraints,
        foundationalRules = foundationalRules,
        recoveryReminders = recoveryReminders,
        distrustBrainNotes = distrustBrainNotes,
        whatWorksNotes = whatWorksNotes,
        pinnedPrinciples = pinnedPrinciples,
        playbookLinksCount = playbookLinksCount,
    )
}

/**
 * Separates long-term, read-later, or specific informational nodes into curated "Vaults".
 * Identifies quotes, incubations, highlights, and forgotten notes to form a [VaultsSnapshot].
 *
 * @param nodes The complete list of active nodes in the system.
 * @return A [VaultsSnapshot] organizing knowledge items into distinct collections.
 */
fun calculateVaultsSnapshot(nodes: List<NodeWithPin>): VaultsSnapshot {
    val active = nodes.filter { it.node.status == "active" }

    /**
     * Verifies if a specific node contains a specific string tag.
     * @param node The wrapped node entity with its tags.
     * @param tag The exact string tag name to search for.
     * @return True if the tag is present.
     */
    fun hasTag(
        node: NodeWithPin,
        tag: String,
    ): Boolean = node.tags.any { it.normalizedName == tag }

    fun matchesAny(
        text: String,
        keywords: Collection<String>,
    ): Boolean = keywords.any { keyword -> text.contains(keyword, ignoreCase = true) }

    val knowledgeItems = active.filter { it.node.isKnowledgeItem() }

    val referenceLibrary =
        knowledgeItems.filter {
            hasTag(it, "reference") ||
                hasTag(it, "vault_document") ||
                hasTag(it, "vault_receipts_paperwork") ||
                hasTag(it, "vault_account_reference") ||
                it.node.noteType == "reference" ||
                matchesAny(
                    it.node.title,
                    listOf("document", "paperwork", "receipt", "reference", "policy", "manual"),
                )
        }
    val importantLinks =
        knowledgeItems.filter {
            hasTag(it, "important_links") ||
                hasTag(it, "vault_links") ||
                it.node.mediaType == "link"
        }
    val healthReference =
        knowledgeItems.filter {
            hasTag(it, "health_reference") ||
                hasTag(it, "vault_medical") ||
                matchesAny(
                    it.node.title,
                    listOf(
                        "medical",
                        "health",
                        "doctor",
                        "prescription",
                        "symptom",
                        "insurance",
                    ),
                )
        }
    val institutionalReference =
        knowledgeItems.filter {
            hasTag(it, "institutional_reference") ||
                hasTag(it, "vault_university") ||
                hasTag(it, "vault_ids_forms") ||
                matchesAny(
                    it.node.title,
                    listOf(
                        "university",
                        "campus",
                        "student",
                        "form",
                        "passport",
                        "visa",
                        "id",
                        "account",
                    ),
                )
        }
    val processTracking =
        active.filter { it.node.isKnowledgeItem() || it.node.isTaskItem() }.filter {
            hasTag(it, "process_tracking") ||
                hasTag(it, "vault_application_status") ||
                matchesAny(
                    it.node.title,
                    listOf(
                        "application",
                        "approval",
                        "renewal",
                        "status",
                        "visa",
                        "enrollment",
                    ),
                ) ||
                it.node.content.contains("Status:", ignoreCase = true)
        }
    val officialDeadlines =
        active
            .filter {
                it.node.dueAt != null &&
                    (
                        hasTag(it, "official_deadline") ||
                            hasTag(it, "vault_official_deadline") ||
                            hasTag(it, "process_tracking") ||
                            matchesAny(
                                it.node.title,
                                listOf(
                                    "deadline",
                                    "renewal",
                                    "tax",
                                    "application",
                                    "visa",
                                    "enrollment",
                                ),
                            )
                    )
            }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
    val retrievalQueue =
        active
            .filter {
                hasTag(it, "must_find_later") || it.node.isPinned
            }.sortedByDescending { it.node.updatedAt }

    return VaultsSnapshot(
        referenceLibrary = referenceLibrary,
        importantLinks = importantLinks,
        healthReference = healthReference,
        institutionalReference = institutionalReference,
        processTracking = processTracking,
        officialDeadlines = officialDeadlines,
        retrievalQueue = retrievalQueue,
    )
}

/**
 * Assesses the user's current systemic workload—including fragmentation from context switching,
 * overwhelming numbers of active projects, and unresolved admin debt—to generate a [CapacitySnapshot].
 *
 * @param inputs A bundled dataset providing current node volumes, maintenance debt, and open loops.
 * @return A [CapacitySnapshot] identifying execution pressure and potential burnout risks.
 */
fun calculateCapacitySnapshot(
    nodes: List<NodeWithPin>,
    projects: List<NodeEntity>,
    areas: List<NodeEntity>,
    maintenance: com.tajemniktv.tajsos.ui.MaintenanceSnapshot,
    openLoops: com.tajemniktv.tajsos.ui.OpenLoopsSnapshot,
    trackEntries: List<TrackEntryEntity>,
    currentMode: ModeEntity?,
    allModes: List<ModeEntity>,
): CapacitySnapshot {
    val now = Clock.System.now().toEpochMilliseconds()
    val weekMs = 7L * 24 * 60 * 60 * 1000
    val activeTasks =
        nodes.filter { it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE }
    val activeProjects =
        projects.filter { it.status == "active" || it.projectStatus == "active" }
    val overdueCount =
        nodes.count { it.node.status == "active" && (it.node.dueAt ?: Long.MAX_VALUE) < now }
    val loadScore =
        (
            (activeTasks.size * 2) +
                (openLoops.active.size * 3) +
                (maintenance.overdue.size * 4) +
                (overdueCount * 5)
        ).coerceIn(0, 100)
    val fragmentationScore =
        (
                activeTasks.distinctBy { it.node.projectId }.size *
                        7 +
                        activeTasks.distinctBy { it.node.areaId }.size *
                        4
        ).coerceIn(0, 100)

    val tooManyActiveProjectsWarning =
        if (activeProjects.size >= 7) "TOO MANY ACTIVE PROJECTS // REDUCE CONCURRENT FRONTS" else null
    val adminDebtWarning = maintenance.overdueWarning
    val openLoopsOverloadWarning = openLoops.overloadWarning
    val capacityMismatch =
        if (loadScore >= 75 && (currentMode?.key == "FOCUS" || currentMode?.key == "DEEP_WORK")) {
            "CAPACITY MISMATCH // CURRENT MODE TOO AMBITIOUS FOR LOAD"
        } else if (loadScore <= 35 && currentMode?.key == "RECOVERY") {
            "CAPACITY MISMATCH // YOU CAN SAFELY SHIFT TO EXECUTION MODE"
        } else {
            null
        }

    val weeklyCreatedActive =
        nodes.count { it.node.status == "active" && it.node.createdAt >= now - weekMs }
    val weeklyDone =
        nodes.count { it.node.status == "done" && (it.node.completedAt ?: 0L) >= now - weekMs }
    val unrealisticWeekSignal =
        if (weeklyCreatedActive > weeklyDone * 2 + 5) "THIS WEEK IS UNREALISTIC // INTAKE OUTPACES EXECUTION" else null
    val tooManyActiveFrontsIndicator =
        // Uses distinctBy instead of groupBy to avoid intermediate map/list allocations for better performance
        if (activeTasks.distinctBy { it.node.areaId }.size >= 6) "TOO MANY ACTIVE FRONTS" else null
    val attentionFragmentedIndicator =
        if (fragmentationScore >= 55) "ATTENTION IS TOO FRAGMENTED" else null
    val weeklyStructuralOverloadWarning =
        if (loadScore >= 80 && fragmentationScore >= 60) "WEEKLY STRUCTURAL OVERLOAD DETECTED" else null

    val loadByArea =
        areas
            .associate { area ->
                val areaTasks = activeTasks.count { it.node.areaId == area.id }
                val areaOpenLoops = openLoops.active.count { it.node.node.areaId == area.id }
                val areaOverdue =
                    nodes.count {
                        it.node.areaId == area.id &&
                            it.node.status == "active" &&
                            (it.node.dueAt ?: Long.MAX_VALUE) < now
                    }
                area.id to
                    ((areaTasks * 2) + (areaOpenLoops * 3) + (areaOverdue * 5)).coerceIn(
                        0,
                        100,
                    )
            }.toMutableMap<Long?, Int>()
    val unassignedLoad =
        (
                activeTasks.count { it.node.areaId == null } *
                        2 +
                        openLoops.active.count { it.node.node.areaId == null } *
                        3
        ).coerceIn(0, 100)
    loadByArea[null] = unassignedLoad

    /**
     * Computes a heuristic load score for a specific focus mode based on precomputed signals.
     * The score is derived from combining load, fragmentation, admin debt, and open loop metrics according to mode-specific logic.
     * @param key The unique string identifier of the focus mode.
     * @return An integer score (0-100) representing the computed workload heuristic for that mode.
     */
    fun modeLoadForKey(key: String): Int =
        when (key)
        {
            "FOCUS", "DEEP_WORK" -> {
                (loadScore + fragmentationScore / 2).coerceIn(
                    0,
                    100,
                )
            }

            "RECOVERY", "LOW_BATTERY", "CANT_THINK" -> {
                (loadScore - 15).coerceAtLeast(0)
            }

            "ADMIN" -> {
                (maintenance.adminDebtMeter + loadScore / 4).coerceIn(
                    0,
                    100,
                )
            }

            "SOCIAL" -> {
                (openLoops.active.size * 6).coerceIn(
                    0,
                    100,
                )
            }

            else -> {
                loadScore
            }
        }

    val loadByMode =
        allModes.associate { mode ->
            mode.key to modeLoadForKey(mode.key)
        }

    val trendBuckets =
        (0..3)
            .map { index ->
                val bucketEnd = now - (index * weekMs)
                val bucketStart = bucketEnd - weekMs
                val entry =
                    trackEntries
                        .filter { it.createdAt in bucketStart..bucketEnd }
                        .maxByOrNull { it.createdAt }
                val fallbackLoad =
                    (
                            nodes.count { it.node.status == "active" && it.node.createdAt <= bucketEnd } *
                                    2 +
                            nodes.count {
                                it.node.status == "active" &&
                                    (
                                        it.node.dueAt
                                            ?: Long.MAX_VALUE
                                                ) < bucketEnd
                            } *
                            3
                    ).coerceIn(0, 100)
                val fallbackFrag =
                    nodes
                        .filter { it.node.status == "active" && it.node.createdAt <= bucketEnd }
                        .groupBy { it.node.projectId }
                        .size
                        .times(8)
                        .coerceIn(0, 100)
                LoadTrendPoint(
                    label = "W-${index + 1}",
                    load = entry?.loadScore ?: fallbackLoad,
                    fragmentation = entry?.fragmentationScore ?: fallbackFrag,
                )
            }.reversed()

    val suggestions =
        buildList {
            if (loadScore >= 75) add("Reduce new intake and close open loops before adding new projects.")
            if (fragmentationScore >= 55) add("Batch by area/context to reduce switching cost.")
            if (maintenance.adminDebtMeter >= 60) add("Run a focused admin block to cut maintenance debt.")
            if (openLoops.active.size >= 10) add("Schedule an open-loop review sweep today.")
            if (tooManyActiveProjectsWarning != null) add("Freeze or park at least one active project.")
        }

    return CapacitySnapshot(
        loadScore = loadScore,
        fragmentationScore = fragmentationScore,
        tooManyActiveProjectsWarning = tooManyActiveProjectsWarning,
        adminDebtWarning = adminDebtWarning,
        openLoopsOverloadWarning = openLoopsOverloadWarning,
        capacityMismatch = capacityMismatch,
        unrealisticWeekSignal = unrealisticWeekSignal,
        tooManyActiveFrontsIndicator = tooManyActiveFrontsIndicator,
        attentionFragmentedIndicator = attentionFragmentedIndicator,
        weeklyStructuralOverloadWarning = weeklyStructuralOverloadWarning,
        loadByArea = loadByArea,
        loadByMode = loadByMode,
        loadTrend = trendBuckets,
        capacityAwareSuggestions = suggestions,
    )
}

/**
 * Aggregates core behavioral metrics, area balances, and key habits to compute a user's
 * "LifeOS Signature", representing their overarching systemic operational health.
 *
 * @param modes All available modes.
 * @param areaHealth Calculated area health snapshot.
 * @param openLoops The calculated open loops snapshot.
 * @param pendingDecisions A list of pending decisions.
 * @param maintenance The calculated maintenance snapshot.
 * @param relationships The calculated relationship snapshot.
 * @param vaults The calculated vaults snapshot.
 * @param capacity The calculated capacity snapshot.
 * @param playbooks The calculated playbook snapshot.
 * @param currentMode The currently active focus mode.
 * @param trackEntries A list of track entries.
 * @param nodes The complete list of active nodes in the system.
 * @return A compiled [LifeOSSignatureSnapshot] detailing system coherence.
 */
fun calculateLifeOSSignatureSnapshot(
    modes: List<ModeEntity>,
    areaHealth: com.tajemniktv.tajsos.ui.AreaHealthSnapshot,
    openLoops: com.tajemniktv.tajsos.ui.OpenLoopsSnapshot,
    pendingDecisions: List<NodeWithPin>,
    maintenance: com.tajemniktv.tajsos.ui.MaintenanceSnapshot,
    relationships: RelationshipSnapshot,
    vaults: VaultsSnapshot,
    capacity: CapacitySnapshot,
    playbooks: PlaybookSnapshot,
    currentMode: ModeEntity?,
    trackEntries: List<TrackEntryEntity>,
    nodes: List<NodeWithPin>,
): LifeOSSignatureSnapshot {
    val dueTasks =
        nodes.filter {
            it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE && it.node.dueAt != null
        }
    val withWorkDate = dueTasks.filter { it.node.startAt != null }
    val coverage =
        if (dueTasks.isEmpty()) 100 else ((withWorkDate.size * 100.0) / dueTasks.size).toInt()
    val workDateDueItems =
        dueTasks
            .filter { it.node.startAt == null }
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
            .take(10)

    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
    val latestEntry = trackEntries.filter { it.date == today }.maxByOrNull { it.createdAt }
    val energy = latestEntry?.energyScore ?: 3
    val anxiety = latestEntry?.anxietyScore ?: 2

    val modeOfLife =
        when
            {
            anxiety >= 4 ||
                    energy <= 2 ||
                    currentMode?.key in
                    setOf(
                        "RECOVERY",
                        "LOW_BATTERY",
                        "CANT_THINK",
                    )
                -> {
                    "stabilization"
                }

                capacity.loadScore >= 80 && capacity.fragmentationScore >= 60 -> {
                    "firefighting"
                }

                currentMode?.key in
                    setOf(
                        "FOCUS",
                        "DEEP_WORK",
                        "STUDY",
                    ) &&
                        capacity.loadScore < 75 ->
                {
                    "execution"
                }

                currentMode?.key == "SOCIAL" -> {
                    "relationship_maintenance"
                }

                currentMode?.key == "ADMIN" -> {
                    "admin_control"
                }

                else -> {
                    "navigation"
                }
            }
    val modeReason =
        when (modeOfLife)
        {
            "stabilization" -> "Energy/anxiety profile points to recovery-first operations."
            "firefighting" -> "High load and fragmentation indicate active overload response."
            "execution" -> "Current mode and capacity suggest focused output window."
            "relationship_maintenance" -> "Social mode active; prioritize people and follow-ups."
            "admin_control" -> "Admin mode active; prioritize forms, renewals, and debt cleanup."
            else -> "System is in adaptive navigation mode."
        }

    return LifeOSSignatureSnapshot(
        operatingModesEnabled = modes.isNotEmpty(),
        areaHealthEnabled = areaHealth.areas.isNotEmpty(),
        openLoopsEnabled = openLoops.active.isNotEmpty() || openLoops.resolved.isNotEmpty(),
        decisionSystemEnabled = pendingDecisions.isNotEmpty() || nodes.any { it.node.type == "decision" },
        maintenanceEnabled = maintenance.active.isNotEmpty() || maintenance.overdue.isNotEmpty(),
        contextAwareFilteringEnabled =
            nodes.any {
                it.node.locationContext != null ||
                    it.node.energyContext != null ||
                    it.node.deviceContext != null ||
                    it.node.socialContext != null ||
                    it.node.timeWindowContext != null
            },
        transitionProtocolsEnabled = playbooks.templates.isNotEmpty(),
        recoveryModeEnabled =
            modes.any {
                it.key in
                    setOf(
                        "RECOVERY",
                        "LOW_BATTERY",
                        "CANT_THINK",
                    )
            },
        relationshipLayerEnabled = relationships.people.isNotEmpty() || relationships.replyQueue.isNotEmpty(),
        logisticsVaultEnabled =
            vaults.referenceLibrary.isNotEmpty() ||
                vaults.importantLinks.isNotEmpty() ||
                vaults.retrievalQueue.isNotEmpty(),
        loadCapacityEnabled = capacity.loadScore > 0 || capacity.fragmentationScore > 0,
        personalPrinciplesPlaybooksEnabled =
            nodes.any { it.node.type in setOf("rule", "principle") } &&
                playbooks.playbooks.isNotEmpty(),
        modeOfLifeLabel = modeOfLife,
        modeOfLifeReason = modeReason,
        workDateDueCoveragePercent = coverage,
        workDateDueItems = workDateDueItems,
    )
}

/**
 * Evaluates the user's "Second Brain" knowledge layer, summarizing their captured notes,
 * highlights, and resources.
 *
 * @param nodes The complete list of active nodes in the system.
 * @param relations Relational mapping data.
 * @param dashboard The current dashboard UI state.
 * @param areaHealth The calculated area health snapshot.
 * @param openLoops The calculated open loops snapshot.
 * @param maintenance The calculated maintenance snapshot.
 * @param capacity The calculated capacity snapshot.
 * @param protocols The calculated transition protocols snapshot.
 * @param playbooks The calculated playbook snapshot.
 * @param currentMode The currently active focus mode.
 * @param signature The calculated lifeOS signature.
 * @param vaults The calculated vaults snapshot.
 * @return A compiled [LifeOSSecondBrainSnapshot] representing knowledge capture health.
 */
fun calculateLifeOSSecondBrainSnapshot(
    nodes: List<NodeWithPin>,
    relations: List<RelationEntity>,
    dashboard: com.tajemniktv.tajsos.ui.DashboardUIState,
    areaHealth: com.tajemniktv.tajsos.ui.AreaHealthSnapshot,
    openLoops: com.tajemniktv.tajsos.ui.OpenLoopsSnapshot,
    maintenance: com.tajemniktv.tajsos.ui.MaintenanceSnapshot,
    capacity: CapacitySnapshot,
    protocols: TransitionProtocolsSnapshot,
    playbooks: PlaybookSnapshot,
    currentMode: ModeEntity?,
    signature: LifeOSSignatureSnapshot,
    vaults: VaultsSnapshot,
): LifeOSSecondBrainSnapshot {
    val knowledgeCount =
        nodes.count {
            it.node.status == "active" && it.node.isKnowledgeItem()
        }
    val savedCount = nodes.size
    val connectedIds =
        relations
            .flatMap { listOf(it.fromNodeId, it.toNodeId) }
            .toSet()
            .intersect(nodes.map { it.node.id }.toSet())
    val findLaterCount =
        vaults.referenceLibrary.size +
            vaults.importantLinks.size +
            vaults.retrievalQueue.size

    val secondBrain =
        listOf(
            DistinctionQuestionState(
                question = "What do I know?",
                answer =
                    if (knowledgeCount > 0) {
                        "$knowledgeCount active knowledge items across notes and records."
                    } else {
                        "Knowledge layer is empty; capture notes or records first."
                    },
                answered = knowledgeCount > 0,
            ),
            DistinctionQuestionState(
                question = "What did I save?",
                answer =
                    if (savedCount > 0) {
                        "$savedCount nodes stored across memory and action layers."
                    } else {
                        "No saved nodes yet."
                    },
                answered = savedCount > 0,
            ),
            DistinctionQuestionState(
                question = "What is this connected to?",
                answer =
                    if (relations.isNotEmpty()) {
                        "${relations.size} links across ${connectedIds.size} connected nodes."
                    } else {
                        "No explicit links yet; relation graph needs wiring."
                    },
                answered = relations.isNotEmpty(),
            ),
            DistinctionQuestionState(
                question = "Where can I find this later?",
                answer =
                    if (findLaterCount > 0) {
                        "$findLaterCount reference or retrieval items are easy to find later."
                    } else {
                        "Reference retrieval is empty; save key links, notes, or deadlines here."
                    },
                answered = findLaterCount > 0,
            ),
        )

    val pressureArea =
        areaHealth.areas
            .sortedByDescending { metrics ->
                when (metrics.status)
                {
                    "on_fire" -> 5
                    "overloaded" -> 4
                    "neglected" -> 3
                    "active" -> 2
                    else -> 1
                }
            }.firstOrNull()

    val nowAction =
        when
            {
                dashboard.overdueNodes.isNotEmpty() -> {
                    "Handle overdue first: ${dashboard.overdueNodes.firstOrNull()?.node?.title.orEmpty()}."
                }

                dashboard.upcomingDeadlines.isNotEmpty() -> {
                    "Advance the nearest deadline: ${dashboard.upcomingDeadlines.firstOrNull()?.node?.title.orEmpty()}."
                }

                dashboard.suggestedContextTasks.isNotEmpty() -> {
                    "Run a context batch (${dashboard.suggestedContextKey ?: "current context"})."
                }

                openLoops.review.isNotEmpty() -> {
                    "Review and close stale open loops."
                }

                else -> {
                    "Run a short maintenance sweep and pick one next task."
                }
            }

    val decayingSignals =
        buildList {
            if (openLoops.review.isNotEmpty()) add("${openLoops.review.size} open loops waiting review")
            if (maintenance.overdue.isNotEmpty()) add("${maintenance.overdue.size} overdue maintenance items")
            if (areaHealth.disappearingAreaIds.isNotEmpty()) add("${areaHealth.disappearingAreaIds.size} areas fading from radar")
        }
    val parkedCount =
        nodes.count {
            it.node.status in setOf("on_hold", "someday")
        }

    val lifeOS =
        listOf(
            DistinctionQuestionState(
                question = "What should happen now?",
                answer = nowAction,
            ),
            DistinctionQuestionState(
                question = "What part of life needs attention?",
                answer =
                    pressureArea?.let {
                        "${it.areaTitle} is ${
                            it.status.replace(
                                '_',
                                ' ',
                            )
                        } (stress ${it.stressLoad})."
                    } ?: "No area pressure signal yet.",
                answered = pressureArea != null,
            ),
            DistinctionQuestionState(
                question = "What am I carrying?",
                answer =
                    "Load ${capacity.loadScore}, fragmentation ${capacity.fragmentationScore}, open loops ${openLoops.active.size}, decisions ${dashboard.pendingDecisions.size}, maintenance ${maintenance.active.size}.",
            ),
            DistinctionQuestionState(
                question = "What is decaying?",
                answer =
                    if (decayingSignals.isNotEmpty()) {
                        decayingSignals.joinToString(" • ")
                    } else {
                        "No acute decay signal right now."
                    },
                answered = decayingSignals.isNotEmpty(),
            ),
            DistinctionQuestionState(
                question = "What mode am I in?",
                answer =
                    "Mode ${(currentMode?.key ?: "UNSET")} • life posture ${signature.modeOfLifeLabel.uppercase()}.",
                answered = currentMode != null,
            ),
            DistinctionQuestionState(
                question = "What protocol helps here?",
                answer =
                    protocols.recommendedLabel?.let { "Run protocol: $it." }
                        ?: playbooks.suggestedPlaybookLabel?.let { "Run playbook: $it." }
                        ?: "No protocol suggestion available; use a short reset protocol.",
                answered = protocols.recommendedLabel != null || playbooks.suggestedPlaybookLabel != null,
            ),
            DistinctionQuestionState(
                question = "What can I safely ignore?",
                answer =
                    if (parkedCount > 0) {
                        "$parkedCount parked items (on_hold/someday) can stay deferred for now."
                    } else {
                        "No parked buffer; consider parking low-priority work."
                    },
                answered = parkedCount > 0,
            ),
            DistinctionQuestionState(
                question = "How do I move through today without dropping everything?",
                answer =
                    "Use mode ${currentMode?.key ?: "NAVIGATION"}, execute one protocol, and keep focus on one overdue/deadline cluster.",
            ),
        )

    val secondCoverage =
        if (secondBrain.isEmpty()) 0 else ((secondBrain.count { it.answered } * 100.0) / secondBrain.size).toInt()
    val lifeCoverage =
        if (lifeOS.isEmpty()) 0 else ((lifeOS.count { it.answered } * 100.0) / lifeOS.size).toInt()
    val posture =
        when
            {
                secondCoverage >= 75 && lifeCoverage >= 75 -> "balanced_hybrid"
                secondCoverage > lifeCoverage -> "memory_heavy"
                lifeCoverage > secondCoverage -> "operations_heavy"
                else -> "underconfigured"
            }

    return LifeOSSecondBrainSnapshot(
        secondBrainQuestions = secondBrain,
        lifeOSQuestions = lifeOS,
        secondBrainCoveragePercent = secondCoverage,
        lifeOSCoveragePercent = lifeCoverage,
        postureLabel = posture,
    )
}

/**
 * Combines active projects, time architectures, and structural commitments into a unified view
 * of the user's forward-looking direction and trajectory.
 *
 * @param distinction The calculated second brain snapshot.
 * @param signature The calculated lifeOS signature.
 * @param dashboard The current dashboard UI state.
 * @param logistics The calculated physical logistics snapshot.
 * @param capacity The calculated capacity snapshot.
 * @param relationships The calculated relationship snapshot.
 * @param protocols The calculated transition protocols snapshot.
 * @param maintenance The calculated maintenance snapshot.
 * @param openLoops The calculated open loops snapshot.
 * @return A compiled [CombinedDirectionSnapshot] summarizing the user's vector.
 */
fun calculateCombinedDirectionSnapshot(
    distinction: LifeOSSecondBrainSnapshot,
    signature: LifeOSSignatureSnapshot,
    dashboard: com.tajemniktv.tajsos.ui.DashboardUIState,
    logistics: PhysicalLogisticsSnapshot,
    capacity: CapacitySnapshot,
    relationships: RelationshipSnapshot,
    protocols: TransitionProtocolsSnapshot,
    maintenance: com.tajemniktv.tajsos.ui.MaintenanceSnapshot,
    openLoops: com.tajemniktv.tajsos.ui.OpenLoopsSnapshot,
): CombinedDirectionSnapshot {
    val storageReady =
        distinction.secondBrainCoveragePercent >= 75 &&
            dashboard.notesCount > 0
    val lifeOsShellReady =
        signature.operatingModesEnabled &&
            signature.maintenanceEnabled &&
            signature.transitionProtocolsEnabled
    val rememberLifeReady =
        distinction.secondBrainCoveragePercent >= 75 &&
            relationships.people.isNotEmpty()
    val runLifeReady =
        distinction.lifeOSCoveragePercent >= 75 &&
            (dashboard.upcomingDeadlines.isNotEmpty() || dashboard.suggestedContextTasks.isNotEmpty())
    val recoveryReady =
        signature.recoveryModeEnabled &&
            (protocols.templates.any { it.key.contains("recovery") } || maintenance.overdue.isNotEmpty())
    val practicalMotionReady =
        signature.contextAwareFilteringEnabled &&
            (
                logistics.placeBasedTasks.isNotEmpty() ||
                    logistics.whatToBringLists.isNotEmpty() ||
                    logistics.leaveHomeChecklists.isNotEmpty()
            )

    val commitments =
        listOf(
            DirectionCommitmentStatus(
                commitment = "Keep the Second Brain layer for storage, notes, connections, and memory",
                satisfied = storageReady,
                evidence =
                    "Second Brain coverage ${distinction.secondBrainCoveragePercent}% • Notes ${dashboard.notesCount} • Relations context ${
                        if (distinction.secondBrainQuestions
                                .any { it.question == "What is this connected to?" && it.answered }
                        )
                            {
                                "present"
                            } else {
                            "missing"
                        }
                    }",
            ),
            DirectionCommitmentStatus(
                commitment = "Wrap it in a LifeOS shell for modes, maintenance, transitions, and action",
                satisfied = lifeOsShellReady,
                evidence =
                    "Modes ${if (signature.operatingModesEnabled) "on" else "off"} • Maintenance ${if (signature.maintenanceEnabled) "on" else "off"} • Protocols ${if (signature.transitionProtocolsEnabled) "on" else "off"}",
            ),
            DirectionCommitmentStatus(
                commitment = "Make TajOS remember life",
                satisfied = rememberLifeReady,
                evidence =
                    "Second Brain coverage ${distinction.secondBrainCoveragePercent}% • Relationship records ${relationships.people.size}",
            ),
            DirectionCommitmentStatus(
                commitment = "Make TajOS help run life",
                satisfied = runLifeReady,
                evidence =
                    "LifeOS coverage ${distinction.lifeOSCoveragePercent}% • Next-action signals ${dashboard.upcomingDeadlines.size + dashboard.suggestedContextTasks.size}",
            ),
            DirectionCommitmentStatus(
                commitment = "Make TajOS help recover from derailment",
                satisfied = recoveryReady,
                evidence =
                    "Recovery modes ${if (signature.recoveryModeEnabled) "available" else "missing"} • Recovery protocol ${
                        if (protocols.templates.any {
                                it.key.contains(
                                    "recovery",
                                )
                            }
                        )
                            {
                                "available"
                            } else {
                            "not detected"
                        }
                    }",
            ),
            DirectionCommitmentStatus(
                commitment = "Make TajOS practical in real-world motion, not only inside neat dashboards",
                satisfied = practicalMotionReady,
                evidence =
                    "Context filtering ${if (signature.contextAwareFilteringEnabled) "on" else "off"} • Place/bring signals ${logistics.placeBasedTasks.size + logistics.whatToBringLists.size + logistics.leaveHomeChecklists.size}",
            ),
        )

    val completion =
        if (commitments.isEmpty()) 0 else ((commitments.count { it.satisfied } * 100.0) / commitments.size).toInt()

    val practicalitySignals =
        buildList {
            if (logistics.placeBasedTasks.isNotEmpty()) add("${logistics.placeBasedTasks.size} place-based tasks")
            if (logistics.leaveHomeChecklists.isNotEmpty()) add("${logistics.leaveHomeChecklists.size} leave-home checklist items")
            if (dashboard.suggestedContextTasks.isNotEmpty()) add("${dashboard.suggestedContextTasks.size} context-suggested tasks")
            if (openLoops.review.isNotEmpty()) add("${openLoops.review.size} loops pending review")
            capacity.capacityAwareSuggestions.firstOrNull()?.let { add(it) }
        }

    val posture =
        when
            {
                completion >= 85 -> "ready_to_ship"
                completion >= 60 -> "mostly_operational"
                else -> "underconfigured"
            }

    return CombinedDirectionSnapshot(
        commitments = commitments,
        completionPercent = completion,
        practicalitySignals = practicalitySignals,
        postureLabel = posture,
    )
}

/**
 * Evaluates major life shifts or transitions by assessing critical path nodes, significant events,
 * and recent large-scale accomplishments.
 *
 * @param distinction The calculated second brain snapshot.
 * @param signature The calculated lifeOS signature.
 * @param direction The calculated combined direction snapshot.
 * @param dashboard The current dashboard UI state.
 * @param time The calculated time architecture snapshot.
 * @param areaHealth The calculated area health snapshot.
 * @param openLoops The calculated open loops snapshot.
 * @param maintenance The calculated maintenance snapshot.
 * @param protocols The calculated transition protocols snapshot.
 * @param capacity The calculated capacity snapshot.
 * @param currentMode The currently active focus mode.
 * @return A compiled [CoreLifeOSShiftSnapshot] highlighting pivotal changes.
 */
fun calculateCoreLifeOSShiftSnapshot(
    distinction: LifeOSSecondBrainSnapshot,
    signature: LifeOSSignatureSnapshot,
    direction: CombinedDirectionSnapshot,
    dashboard: com.tajemniktv.tajsos.ui.DashboardUIState,
    time: com.tajemniktv.tajsos.ui.TimeArchitectureSnapshot,
    areaHealth: com.tajemniktv.tajsos.ui.AreaHealthSnapshot,
    openLoops: com.tajemniktv.tajsos.ui.OpenLoopsSnapshot,
    maintenance: com.tajemniktv.tajsos.ui.MaintenanceSnapshot,
    protocols: TransitionProtocolsSnapshot,
    capacity: CapacitySnapshot,
    currentMode: ModeEntity?,
): CoreLifeOSShiftSnapshot {
    val operatingLayerReady =
        direction.commitments.any {
            it.commitment == "Keep the Second Brain layer for storage, notes, connections, and memory" && it.satisfied
        } &&
            direction.commitments.any {
                it.commitment == "Make TajOS help run life" && it.satisfied
            } &&
            distinction.postureLabel != "memory_heavy"

    val lifeInMotionReady =
        time.todayLayer.isNotEmpty() ||
            time.weekLayer.isNotEmpty() ||
            dashboard.suggestedContextTasks.isNotEmpty()

    val stateContextModeReady =
        signature.operatingModesEnabled &&
            signature.contextAwareFilteringEnabled &&
            (currentMode != null || dashboard.modeSuggestion != null)

    val transitionsReady =
        signature.transitionProtocolsEnabled &&
            (protocols.protocols.isNotEmpty() || protocols.templates.isNotEmpty())

    val decayOverloadTrackingReady =
        areaHealth.areas.any { it.status in setOf("neglected", "overloaded", "on_fire") } ||
            openLoops.review.isNotEmpty() ||
            maintenance.overdue.isNotEmpty() ||
            capacity.openLoopsOverloadWarning != null ||
            capacity.adminDebtWarning != null

    val moveThroughTimeReady =
        time.todayLayer.isNotEmpty() &&
            (time.weekLayer.isNotEmpty() || time.monthLayer.isNotEmpty() || time.countdowns.isNotEmpty())

    val items =
        listOf(
            CoreLifeOSShiftItem(
                criterion = "Treat TajOS as a personal operating layer, not only a storage system",
                satisfied = operatingLayerReady,
                evidence =
                    "Direction ${direction.completionPercent}% • posture ${distinction.postureLabel} • LifeOS coverage ${distinction.lifeOSCoveragePercent}%",
            ),
            CoreLifeOSShiftItem(
                criterion = "Build TajOS to understand life in motion, not just static information",
                satisfied = lifeInMotionReady,
                evidence =
                    "Today ${time.todayLayer.size} • Week ${time.weekLayer.size} • Context suggestions ${dashboard.suggestedContextTasks.size}",
            ),
            CoreLifeOSShiftItem(
                criterion = "Make TajOS state-aware, context-aware, and mode-aware",
                satisfied = stateContextModeReady,
                evidence =
                    "Mode ${currentMode?.key ?: "unset"} • Context filter ${if (signature.contextAwareFilteringEnabled) "on" else "off"} • Mode suggestion ${dashboard.modeSuggestion ?: "none"}",
            ),
            CoreLifeOSShiftItem(
                criterion = "Make TajOS support real-life transitions, not just pages and tasks",
                satisfied = transitionsReady,
                evidence =
                    "Protocols active ${protocols.protocols.size} • templates ${protocols.templates.size}",
            ),
            CoreLifeOSShiftItem(
                criterion = "Make TajOS track what is decaying, neglected, or overloaded",
                satisfied = decayOverloadTrackingReady,
                evidence =
                    "Area alerts ${
                        areaHealth.areas.count {
                            it.status in
                                setOf(
                                    "neglected",
                                    "overloaded",
                                    "on_fire",
                                )
                        }
                    } • Loop review ${openLoops.review.size} • Overdue maintenance ${maintenance.overdue.size}",
            ),
            CoreLifeOSShiftItem(
                criterion = "Make TajOS help the user move through time, not just save information in place",
                satisfied = moveThroughTimeReady,
                evidence =
                    "Today ${time.todayLayer.size} • Week ${time.weekLayer.size} • Month ${time.monthLayer.size} • Countdowns ${time.countdowns.size}",
            ),
        )

    val completion =
        if (items.isEmpty()) 0 else ((items.count { it.satisfied } * 100.0) / items.size).toInt()
    val connectedProperly = completion >= 100 && direction.completionPercent >= 100
    val warning =
        if (connectedProperly) {
            null
        } else {
            "Some Core LifeOS Shift criteria are not fully satisfied or not fully integrated yet."
        }

    return CoreLifeOSShiftSnapshot(
        items = items,
        completionPercent = completion,
        connectedProperly = connectedProperly,
        integrationWarning = warning,
    )
}

/**
 * Evaluates nodes structurally bound to academic templates or metadata to generate
 * a specialized [StudentBoardState] dashboard payload for studying.
 *
 * Performance is optimized by evaluating strings case-insensitively and preventing
 * duplicate string instantiations within filtering logic.
 *
 * @param nodes The complete list of active nodes in the system.
 * @param relations The complete list of formal database links between nodes.
 * @param sessions A list of recorded [FocusSessionEntity] records tracking deep work time.
 * @param templates A list of [TemplateEntity] to identify missing components or configurations.
 * @return A [StudentBoardState] snapshot tracking course progress, mastery levels, and exams.
 */
fun calculateStudentBoardState(
    nodes: List<NodeWithPin>,
    relations: List<RelationEntity>,
    sessions: List<FocusSessionEntity>,
    templates: List<TemplateEntity>,
): StudentBoardState {
    val now = Clock.System.now().toEpochMilliseconds()
    val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
    val activeNodes = nodes.filter { it.node.status == "active" }
    activeNodes.associateBy { it.node.id }

    fun NodeWithPin.hasTag(tag: String): Boolean = tags.any { it.normalizedName.equals(tag, ignoreCase = true) }

    fun NodeWithPin.student(): StudentMetadata? = node.metadataEnvelopeOrNull()?.student

    val assignmentTracker =
        activeNodes
            .filter { item ->
                item.node.isTaskItem() &&
                        (
                                item.student()?.assignmentType != null ||
                                        item.hasTag("assignment")
                                )
            }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    val examNodes =
        activeNodes
            .filter { item ->
                item.node.dueAt != null &&
                    (
                        item.hasTag("exam") ||
                            item.student()?.assignmentType.equals(
                                            "exam",
                                            ignoreCase = true,
                            ) ||
                            item.node.title.contains("exam", ignoreCase = true)
                    )
            }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    val examCountdownNode = examNodes.firstOrNull()
    val examCountdownDays =
        examCountdownNode?.node?.dueAt?.let { due ->
            (((due - now).coerceAtLeast(0L)) / (24 * 60 * 60 * 1000L))
        }

    val examPrepBoard =
        activeNodes
            .filter {
                it.hasTag("exam_prep") ||
                    it.hasTag("revisit_before_exam") ||
                    it.student()?.revisitBeforeExam == true
            }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    val psychologyConceptMaps =
        activeNodes.filter {
            it.node.isNoteItem() &&
                it.node.noteType == "concept" &&
                it.hasTag("psychology")
        }

    val glossaryCards =
        activeNodes.filter {
            it.node.isNoteItem() &&
                (it.hasTag("glossary") || it.hasTag("knowledge_card") || it.node.noteType == "concept")
        }

    val researchIdeaVault =
        activeNodes.filter {
            it.node.isNoteItem() &&
                (it.hasTag("research") || it.hasTag("research_idea") || it.node.noteType == "research")
        }

    val quoteBank =
        activeNodes.filter {
            it.node.isNoteItem() && it.node.noteType == "quote"
        }

    val caseReflectionNotes =
        activeNodes.filter {
            it.node.isKnowledgeItem() &&
                (it.node.noteType == "reflection" || it.hasTag("case_study") || it.hasTag("reflection"))
        }

    val readingBacklog =
        activeNodes
            .filter {
                it.node.isNoteItem() &&
                    (it.node.noteType == "reading" || it.hasTag("reading"))
            }.sortedByDescending { it.node.updatedAt }

    val readingProgress =
        readingBacklog
            .mapNotNull { note ->
                note.student()?.readingProgressPercent?.let { progress ->
                    StudentProgressItem(
                        node = note,
                        progressPercent = progress.coerceIn(0, 100),
                    )
                }
            }.sortedByDescending { it.progressPercent }

    val assignmentDeadlines =
        assignmentTracker
            .filter { it.node.dueAt != null }
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
            .take(8)

    val revisitBeforeExam =
        activeNodes
            .filter {
                it.hasTag("revisit_before_exam") || it.student()?.revisitBeforeExam == true
            }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    val topicMastery =
        activeNodes
            .mapNotNull { item ->
                val student = item.student() ?: return@mapNotNull null
                val mastery = student.masteryPercent ?: return@mapNotNull null
                val topic = student.topic ?: item.node.title
                StudentMasteryItem(
                    node = item,
                    topic = topic,
                    masteryPercent = mastery.coerceIn(0, 100),
                )
            }.sortedByDescending { it.masteryPercent }

    val byCourse =
        activeNodes
            .mapNotNull { item ->
                val student = item.student() ?: return@mapNotNull null
                val courseId = student.courseId ?: return@mapNotNull null
                Triple(courseId, student, item)
            }.groupBy { it.first }

    val courseDashboard =
        byCourse
            .map { (courseId, entries) ->
                val courseName =
                    entries.firstNotNullOfOrNull { it.second.courseName }
                        ?: courseId
                val openAssignments =
                    entries.count {
                        it.third.node.isTaskItem() &&
                            it.third.node.taskStateOrNull() == TaskState.ACTIVE &&
                            it.second.assignmentType != null
                    }
                val upcomingExams =
                    entries.count {
                        (
                            it.third.hasTag("exam") ||
                                it.second.assignmentType.equals(
                                    "exam",
                                    ignoreCase = true,
                                )
                        ) &&
                            (it.third.node.dueAt ?: Long.MAX_VALUE) >= now
                    }
                val masteryValues =
                    entries.mapNotNull { it.second.masteryPercent }.map { it.coerceIn(0, 100) }
                StudentCourseSummary(
                    courseId = courseId,
                    courseName = courseName,
                    semester = entries.firstNotNullOfOrNull { it.second.semester },
                    openAssignments = openAssignments,
                    upcomingExams = upcomingExams,
                    avgMasteryPercent =
                        if (masteryValues.isNotEmpty()) {
                            masteryValues
                                .average()
                                .toInt()
                        } else {
                            null
                        },
                )
            }.sortedWith { a, b -> a.courseName.compareTo(b.courseName, ignoreCase = true) }

    val bySemester =
        activeNodes
            .mapNotNull { item ->
                val student = item.student() ?: return@mapNotNull null
                val semester = student.semester ?: return@mapNotNull null
                semester to item
            }.groupBy { it.first }

    val semesterDashboard =
        bySemester
            .map { (semester, entries) ->
                val semesterNodes = entries.map { it.second }
                val courseCount =
                    semesterNodes.mapNotNull { it.student()?.courseId }.distinct().size
                val openAssignments =
                    semesterNodes.count { it.node.isTaskItem() && it.student()?.assignmentType != null }
                val upcomingExams =
                    semesterNodes.count {
                        it.node.dueAt != null &&
                            (
                                it.hasTag("exam") ||
                                    it.student()?.assignmentType.equals(
                                        "exam",
                                        ignoreCase = true,
                                    )
                            )
                    }
                val dueSoon =
                    semesterNodes.count {
                        val due = it.node.dueAt ?: return@count false
                        due in now..(now + 7 * 24 * 60 * 60 * 1000L)
                    }
                StudentSemesterSummary(
                    semester = semester,
                    courseCount = courseCount,
                    openAssignments = openAssignments,
                    upcomingExams = upcomingExams,
                    dueSoon = dueSoon,
                )
            }.sortedWith { a, b -> a.semester.compareTo(b.semester, ignoreCase = true) }

    val topicToNoteLinks =
        relations.count {
            it.relationType.equals("TOPIC_LINK", ignoreCase = true)
        }

    val paperToNoteLinks =
        relations.count {
            it.relationType.equals("PAPER_REFERENCE", ignoreCase = true)
        }

    val conceptNodeIds =
        activeNodes
            .filter {
                it.node.isNoteItem() && (it.node.noteType == "concept" || it.hasTag("psychology"))
            }.map { it.node.id }
            .toSet()

    val conceptEdges =
        relations.count { relation ->
            conceptNodeIds.contains(relation.fromNodeId) && conceptNodeIds.contains(relation.toNodeId)
        }

    val flashcardCandidates =
        activeNodes
            .filter {
                it.hasTag("flashcard") ||
                    it.hasTag("flashcard_candidate") ||
                    it.student()?.flashcardCandidate == true
            }.sortedByDescending { it.node.updatedAt }

    val studentNodeIds = activeNodes.map { it.node.id }.toSet()
    val studySessionsThisWeek =
        sessions.count {
            it.startedAt >= sevenDaysAgo && studentNodeIds.contains(it.nodeId)
        }
    val studyMinutesThisWeek =
        sessions
            .filter { it.startedAt >= sevenDaysAgo && studentNodeIds.contains(it.nodeId) }
            .sumOf { if (it.durationSec > 0) it.durationSec else ((now - it.startedAt) / 1000).toInt() }
            .div(60)

    val templateNames = templates.map { it.name.trim() }
    return StudentBoardState(
        lectureTemplateReady = templateNames.any { it.equals("lecture note template", ignoreCase = true) },
        readingTemplateReady = templateNames.any { it.equals("reading note template", ignoreCase = true) },
        paperSummaryTemplateReady = templateNames.any { it.equals("paper summary template", ignoreCase = true) },
        assignmentTracker = assignmentTracker,
        examPrepBoard = examPrepBoard,
        psychologyConceptMaps = psychologyConceptMaps,
        glossaryCards = glossaryCards,
        researchIdeaVault = researchIdeaVault,
        quoteBank = quoteBank,
        caseReflectionNotes = caseReflectionNotes,
        readingBacklog = readingBacklog,
        revisitBeforeExam = revisitBeforeExam,
        readingProgress = readingProgress,
        assignmentDeadlines = assignmentDeadlines,
        topicMastery = topicMastery,
        courseDashboard = courseDashboard,
        semesterDashboard = semesterDashboard,
        examCountdownNode = examCountdownNode,
        examCountdownDays = examCountdownDays,
        topicToNoteLinks = topicToNoteLinks,
        paperToNoteLinks = paperToNoteLinks,
        conceptGraphNodes = conceptNodeIds.size,
        conceptGraphEdges = conceptEdges,
        flashcardCandidates = flashcardCandidates,
        studySessionsThisWeek = studySessionsThisWeek,
        studyMinutesThisWeek = studyMinutesThisWeek,
    )
}

/**
 * Calculates a list of stale tasks that are overdue by more than the threshold.
 * Excludes completed, archived, already someday, pinned, and recurring tasks.
 *
 * @param nodes The complete list of nodes in the system.
 * @param now The current time to use for calculations.
 * @param cutoffDays The threshold in days before a task is considered stale.
 */
fun calculateStaleTasks(
    nodes: List<NodeEntity>,
    now: Instant,
    cutoffDays: Int = 3,
): List<NodeEntity> {
    val cutoffThreshold = (now - cutoffDays.days).toEpochMilliseconds()
    return nodes.filter { node ->
        node.isTaskItem() &&
            node.status == "active" &&
            node.taskStateOrNull() == TaskState.ACTIVE &&
            !node.isPinned &&
            (!node.isRecurring && node.recurringInterval == null) &&
            node.dueAt != null &&
            node.dueAt < cutoffThreshold
    }
}

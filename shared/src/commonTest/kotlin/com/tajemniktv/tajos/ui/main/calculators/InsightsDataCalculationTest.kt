package com.tajemniktv.tajos.ui.main.calculators

import com.tajemniktv.tajos.data.FocusSessionEntity
import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.NodeWithPin
import com.tajemniktv.tajos.data.TrackEntryEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

class InsightsDataCalculationTest {
    private val now = Instant.parse("2026-10-13T12:00:00Z").toEpochMilliseconds()
    private val zone = TimeZone.UTC

    private fun defaultNode(id: Long, type: String = "task", status: String = "active", areaId: Long? = null) = NodeEntity(
        id = id,
        title = "Test Node $id",
        content = "",
        type = type,
        status = status,
        areaId = areaId,
        inboxState = false,
        reminderAt = null,
        updatedAt = 0L,
        createdAt = now
    )

    private fun createTestNodeWithPin(node: NodeEntity): NodeWithPin {
        return NodeWithPin(
            node = node,
            pin = null,
            tags = emptyList()
        )
    }

    @Test
    fun calculateInsights_emptyData() {
        val insights = calculateInsights(emptyList(), emptyList(), emptyList(), emptyList(), now, zone)
        assertEquals(0, insights.weeklyCaptures)
        assertEquals(0, insights.weeklyCompletions)
        assertEquals(0.0, insights.weeklyFocusHours)
        assertEquals(-1, insights.bestFocusHour)
        assertEquals(-1, insights.mostProductiveHour)
        assertFalse(insights.passiveBehaviorSummary.contains("around 0:00"))
    }

    @Test
    fun calculateInsights_computesBasicMetrics() {

        val node1 = createTestNodeWithPin(defaultNode(1L).copy(createdAt = now, projectId = 10L))
        val node2 = createTestNodeWithPin(defaultNode(2L).copy(createdAt = now))

        val doneNode = createTestNodeWithPin(defaultNode(3L, status = "done").copy(completedAt = now))

        val session1 = FocusSessionEntity(id = 1L, nodeId = 1L, startedAt = now, endedAt = now + 3600000, durationSec = 3600, sessionType = "deep_work", interrupted = false, completed = true, note = null)
        val session2 = FocusSessionEntity(id = 2L, nodeId = 2L, startedAt = now, endedAt = now + 1800000, durationSec = 1800, sessionType = "deep_work", interrupted = false, completed = true, note = null)

        val project1 = defaultNode(10L, type = "project").copy(status = "active", updatedAt = now - (20L * 24 * 60 * 60 * 1000L))

        val insights = calculateInsights(
            nodes = listOf(node1, node2, doneNode),
            sessions = listOf(session1, session2),
            tracks = emptyList(),
            projects = listOf(project1),
            now = now,
            sysZone = zone,
        )

        assertEquals(3, insights.weeklyCaptures) // node1, node2, doneNode all created now
        assertEquals(1, insights.weeklyCompletions) // doneNode

        // 3600 + 1800 = 5400 seconds = 1.5 hours
        assertEquals(1.5, insights.weeklyFocusHours)

        assertEquals(setOf(10L), insights.neglectedProjects.map { it.id }.toSet())

    }

    private fun at(hour: Int): Long =
        Instant.parse("2026-10-13T${hour.toString().padStart(2, '0')}:00:00Z").toEpochMilliseconds()

    private fun session(id: Long, hour: Int, ended: Boolean = true) = FocusSessionEntity(
        id = id, nodeId = id, startedAt = at(hour),
        endedAt = if (ended) at(hour) + 60000 else null, durationSec = 60,
    )

    private fun completion(id: Long, hour: Int) =
        createTestNodeWithPin(defaultNode(id, status = "done").copy(completedAt = at(hour)))

    @Test
    fun midnightIsActivityNotMissingData() {
        val result = calculateInsights(
            listOf(completion(1, 0)), listOf(session(1, 0)), emptyList(), emptyList(), now, zone,
        )
        assertEquals(0, result.bestFocusHour)
        assertEquals(0, result.mostProductiveHour)
        assertTrue(result.passiveBehaviorSummary.contains("around 0:00"))
    }

    @Test
    fun tiedNonzeroPeaksChooseEarlierHourRegardlessOfInputOrder() {
        val result = calculateInsights(
            listOf(completion(1, 10), completion(2, 7)),
            listOf(session(1, 10), session(2, 7)), emptyList(), emptyList(), now, zone,
        )
        assertEquals(7, result.bestFocusHour)
        assertEquals(7, result.mostProductiveHour)
    }

    @Test
    fun unfinishedSessionsAndOldOrUndatedCompletionsDoNotCreatePeaks() {
        val result = calculateInsights(
            listOf(
                createTestNodeWithPin(defaultNode(1, status = "done")),
                completion(2, 0).let { it.copy(node = it.node.copy(completedAt = now - 8 * 86400000L)) },
            ), listOf(session(1, 0, ended = false)), emptyList(), emptyList(), now, zone,
        )
        assertEquals(-1, result.bestFocusHour)
        assertEquals(-1, result.mostProductiveHour)
    }

    @Test
    fun correlationsReturnZeroWhenEitherComparisonGroupIsMissing() {
        for (tookMeds in listOf(false, true)) {
            val result = calculateInsights(
                emptyList(), emptyList(), listOf(TrackEntryEntity(
                    date = "2026-10-13", moodScore = 4, focusScore = 5,
                    energyScore = if (tookMeds) 5 else 1,
                    sleepScore = if (tookMeds) 8f else 4f, tookMeds = tookMeds,
                )), emptyList(), now, zone,
            )
            assertEquals(0.0, result.moodVsCompletions)
            assertEquals(0.0, result.sleepVsFocus)
            assertEquals(0.0, result.energyVsCaptures)
            assertEquals(0.0, result.medsEffectiveness)
        }
    }

    @Test
    fun correlationsPreserveDifferencesWithBothGroups() {
        val result = calculateInsights(
            (1L..3L).map { completion(it, 7) }, listOf(session(1, 7).copy(durationSec = 3600)),
            listOf(
                TrackEntryEntity(date = "2026-10-13", moodScore = 5, focusScore = 5, energyScore = 5, sleepScore = 8f, tookMeds = true),
                TrackEntryEntity(date = "2026-10-12", moodScore = 2, focusScore = 2, energyScore = 1, sleepScore = 4f),
            ), emptyList(), now, zone,
        )
        assertEquals(3.0, result.moodVsCompletions)
        assertEquals(1.0, result.sleepVsFocus)
        assertEquals(3.0, result.energyVsCaptures)
        assertEquals(3.0, result.medsEffectiveness)
    }

    @Test
    fun nullableDifferenceRejectsNonfiniteInputsAndOverflow() {
        for (invalid in listOf(null, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertEquals(0.0, finiteDifferenceOrZero(invalid, 1.0))
            assertEquals(0.0, finiteDifferenceOrZero(1.0, invalid))
        }
        assertEquals(0.0, finiteDifferenceOrZero(Double.MAX_VALUE, -Double.MAX_VALUE))
        assertEquals(-2.5, finiteDifferenceOrZero(1.0, 3.5))
    }
}

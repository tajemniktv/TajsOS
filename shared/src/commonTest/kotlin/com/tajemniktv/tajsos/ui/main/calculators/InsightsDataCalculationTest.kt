package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TrackEntryEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class InsightsDataCalculationTest {

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
        createdAt = Clock.System.now().toEpochMilliseconds()
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
        val insights = calculateInsights(emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(0, insights.weeklyCaptures)
        assertEquals(0, insights.weeklyCompletions)
        assertEquals(0.0, insights.weeklyFocusHours)
        assertEquals(0, insights.bestFocusHour)
    }

    @Test
    fun calculateInsights_computesBasicMetrics() {
        val now = Clock.System.now().toEpochMilliseconds()

        val node1 = createTestNodeWithPin(defaultNode(1L).copy(createdAt = now))
        val node2 = createTestNodeWithPin(defaultNode(2L).copy(createdAt = now))

        val doneNode = createTestNodeWithPin(defaultNode(3L, status = "done").copy(completedAt = now))

        val session1 = FocusSessionEntity(id = 1L, nodeId = 1L, startedAt = now, endedAt = now + 3600000, durationSec = 3600, sessionType = "deep_work", interrupted = false, completed = true, note = null)
        val session2 = FocusSessionEntity(id = 2L, nodeId = 2L, startedAt = now, endedAt = now + 1800000, durationSec = 1800, sessionType = "deep_work", interrupted = false, completed = true, note = null)

        val project1 = defaultNode(10L, type = "project").copy(status = "active", updatedAt = now - (20L * 24 * 60 * 60 * 1000L))

        val insights = calculateInsights(
            nodes = listOf(node1, node2, doneNode),
            sessions = listOf(session1, session2),
            tracks = emptyList(),
            projects = listOf(project1)
        )

        assertEquals(3, insights.weeklyCaptures) // node1, node2, doneNode all created now
        assertEquals(1, insights.weeklyCompletions) // doneNode

        // 3600 + 1800 = 5400 seconds = 1.5 hours
        assertEquals(1.5, insights.weeklyFocusHours)

        assertEquals(0, insights.neglectedProjects.size)

    }
}
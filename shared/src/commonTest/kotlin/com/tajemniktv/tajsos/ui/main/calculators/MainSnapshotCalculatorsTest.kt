package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class MainSnapshotCalculatorsTest {

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
    fun calculateAreaHealthSnapshot_emptyAreas() {
        val snapshot = calculateAreaHealthSnapshot(emptyList(), emptyList())
        assertTrue(snapshot.areas.isEmpty())
        assertEquals("balanced", snapshot.imbalanceLabel)
    }

    @Test
    fun calculateAreaHealthSnapshot_computesMetricsCorrectly() {
        val area = defaultNode(10L, type = "area", status = "active")

        val activeTask = createTestNodeWithPin(defaultNode(1L, "task", "active", areaId = 10L).copy(updatedAt = now))
        val openLoop = createTestNodeWithPin(defaultNode(2L, "open_loop", "active", areaId = 10L))

        val now = Clock.System.now().toEpochMilliseconds()
        val overdueTask = createTestNodeWithPin(defaultNode(3L, "task", "active", areaId = 10L).copy(dueAt = now - 100000))
        val dueSoonTask = createTestNodeWithPin(defaultNode(4L, "task", "active", areaId = 10L).copy(dueAt = now + 100000))

        val doneTask = createTestNodeWithPin(defaultNode(5L, "task", "done", areaId = 10L).copy(completedAt = now))

        val nodes = listOf(activeTask, openLoop, overdueTask, dueSoonTask, doneTask)

        val snapshot = calculateAreaHealthSnapshot(nodes, listOf(area))

        assertEquals(1, snapshot.areas.size)
        val metrics = snapshot.areas[0]

        assertEquals(10L, metrics.areaId)
        assertEquals(4, metrics.activeItems) // task, openLoop, overdueTask, dueSoonTask
        assertEquals(1, metrics.openLoops)
        assertEquals(2, metrics.deadlines) // overdue, dueSoon
        assertEquals(1, metrics.overdueDeadlines)
        assertEquals(1, metrics.doneThisWeek)
        assertEquals(1, metrics.recentActivity)
    }
}
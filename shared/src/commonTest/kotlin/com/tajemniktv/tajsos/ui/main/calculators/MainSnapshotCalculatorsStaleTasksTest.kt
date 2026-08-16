package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class MainSnapshotCalculatorsStaleTasksTest {

    private val now = Instant.fromEpochMilliseconds(100000000000L)
    private val cutoffMs = (now - 3.days).toEpochMilliseconds()

    private fun defaultNode(id: Long, type: String = "task", status: String = "active", dueAt: Long? = null) = NodeEntity(
        id = id,
        title = "Test Node $id",
        content = "",
        type = type,
        status = status,
        updatedAt = 0L,
        dueAt = dueAt
    )

    private fun assertStaleTasks(vararg nodes: NodeEntity, expectedIds: List<Long> = emptyList()) {
        val staleTasks = calculateStaleTasks(nodes.toList(), now)
        assertEquals(expectedIds.size, staleTasks.size)
        assertEquals(expectedIds, staleTasks.map { it.id })
    }

    @Test
    fun testCalculateStaleTasks_overdueTasks() {
        val staleNode = defaultNode(1L, dueAt = cutoffMs - 1000L)
        val freshNode = defaultNode(2L, dueAt = cutoffMs + 1000L)
        assertStaleTasks(staleNode, freshNode, expectedIds = listOf(1L))
    }

    @Test
    fun testCalculateStaleTasks_exactThreshold() {
        val thresholdNode = defaultNode(1L, dueAt = cutoffMs)
        assertStaleTasks(thresholdNode)
    }

    @Test
    fun testCalculateStaleTasks_nonTaskTypes() {
        val noteNode = defaultNode(1L, type = "note", dueAt = cutoffMs - 1000L)
        val ideaNode = defaultNode(2L, type = "idea", dueAt = cutoffMs - 1000L)
        assertStaleTasks(noteNode, ideaNode)
    }

    @Test
    fun testCalculateStaleTasks_inactiveStatus() {
        val onHoldNode = defaultNode(1L, status = TaskState.ON_HOLD.storageKey, dueAt = cutoffMs - 1000L)
        val completedNode = defaultNode(2L, status = TaskState.DONE.storageKey, dueAt = cutoffMs - 1000L)
        assertStaleTasks(onHoldNode, completedNode)
    }

    @Test
    fun testCalculateStaleTasks_noDueDate() {
        val noDueNode = defaultNode(1L, dueAt = null)
        assertStaleTasks(noDueNode)
    }

    @Test
    fun testCalculateStaleTasks_pinnedOrRecurring() {
        val pinnedNode = defaultNode(1L, dueAt = cutoffMs - 1000L).copy(isPinned = true)
        val recurringNode1 = defaultNode(2L, dueAt = cutoffMs - 1000L).copy(isRecurring = true)
        val recurringNode2 = defaultNode(3L, dueAt = cutoffMs - 1000L).copy(recurringInterval = "DAILY")
        assertStaleTasks(pinnedNode, recurringNode1, recurringNode2)
    }
}
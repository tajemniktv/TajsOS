package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class MainSnapshotCalculatorsStaleTasksTest {

    private fun defaultNode(id: Long, type: String = "task", status: String = "active", dueAt: Long? = null) = NodeEntity(
        id = id,
        title = "Test Node $id",
        content = "",
        type = type,
        status = status,
        updatedAt = 0L,
        dueAt = dueAt
    )

    @Test
    fun testCalculateStaleTasks_overdueTasks() {
        val now = Instant.fromEpochMilliseconds(100000000000L)
        val cutoffMs = (now - 3.days).toEpochMilliseconds()

        val staleNode = defaultNode(1L, dueAt = cutoffMs - 1000L)
        val freshNode = defaultNode(2L, dueAt = cutoffMs + 1000L)

        val nodes = listOf(staleNode, freshNode)
        val staleTasks = calculateStaleTasks(nodes, now)

        assertEquals(1, staleTasks.size)
        assertEquals(1L, staleTasks[0].id)
    }

    @Test
    fun testCalculateStaleTasks_exactThreshold() {
        val now = Instant.fromEpochMilliseconds(100000000000L)
        val cutoffMs = (now - 3.days).toEpochMilliseconds()

        val thresholdNode = defaultNode(1L, dueAt = cutoffMs)

        val nodes = listOf(thresholdNode)
        val staleTasks = calculateStaleTasks(nodes, now)

        assertTrue(staleTasks.isEmpty())
    }

    @Test
    fun testCalculateStaleTasks_nonTaskTypes() {
        val now = Instant.fromEpochMilliseconds(100000000000L)
        val cutoffMs = (now - 3.days).toEpochMilliseconds()

        val noteNode = defaultNode(1L, type = "note", dueAt = cutoffMs - 1000L)
        val ideaNode = defaultNode(2L, type = "idea", dueAt = cutoffMs - 1000L)

        val nodes = listOf(noteNode, ideaNode)
        val staleTasks = calculateStaleTasks(nodes, now)

        assertTrue(staleTasks.isEmpty())
    }

    @Test
    fun testCalculateStaleTasks_inactiveStatus() {
        val now = Instant.fromEpochMilliseconds(100000000000L)
        val cutoffMs = (now - 3.days).toEpochMilliseconds()

        val onHoldNode = defaultNode(1L, status = TaskState.ON_HOLD.storageKey, dueAt = cutoffMs - 1000L)
        val completedNode = defaultNode(2L, status = TaskState.DONE.storageKey, dueAt = cutoffMs - 1000L)

        val nodes = listOf(onHoldNode, completedNode)
        val staleTasks = calculateStaleTasks(nodes, now)

        assertTrue(staleTasks.isEmpty())
    }

    @Test
    fun testCalculateStaleTasks_noDueDate() {
        val now = Instant.fromEpochMilliseconds(100000000000L)

        val noDueNode = defaultNode(1L, dueAt = null)

        val nodes = listOf(noDueNode)
        val staleTasks = calculateStaleTasks(nodes, now)

        assertTrue(staleTasks.isEmpty())
    }

    @Test
    fun testCalculateStaleTasks_pinnedOrRecurring() {
        val now = Instant.fromEpochMilliseconds(100000000000L)
        val cutoffMs = (now - 3.days).toEpochMilliseconds()

        val pinnedNode = defaultNode(1L, dueAt = cutoffMs - 1000L).copy(isPinned = true)
        val recurringNode1 = defaultNode(2L, dueAt = cutoffMs - 1000L).copy(isRecurring = true)
        val recurringNode2 = defaultNode(3L, dueAt = cutoffMs - 1000L).copy(recurringInterval = "DAILY")

        val nodes = listOf(pinnedNode, recurringNode1, recurringNode2)
        val staleTasks = calculateStaleTasks(nodes, now)

        assertTrue(staleTasks.isEmpty())
    }
}
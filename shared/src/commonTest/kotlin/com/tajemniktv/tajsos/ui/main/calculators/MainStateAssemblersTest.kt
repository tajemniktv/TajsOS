package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.ProtocolHistoryEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class MainStateAssemblersTest {


    private fun createTestNodeWithPin(
        node: NodeEntity
    ): NodeWithPin {
        return NodeWithPin(
            node = node,
            pin = null,
            tags = emptyList()
        )
    }

    private fun defaultNode(id: Long) = NodeEntity(
        id = id,
        title = "Test Node",
        content = "",
        type = "task",
        status = "active",
        inboxState = false,
        reminderAt = null,
        updatedAt = 0L
    )

    @Test
    fun `categorizeNodes categorizes archived nodes correctly`() {
        val archivedNode = createTestNodeWithPin(defaultNode(1).copy(status = "archived", inboxState = true))
        val activeNode = createTestNodeWithPin(defaultNode(2).copy(status = "active", inboxState = false))

        val result = categorizeNodes(listOf(archivedNode, activeNode))

        assertEquals(1, result.archived.size)
        assertEquals(1L, result.archived[0].node.id)

        assertEquals(0, result.inbox.size)
    }

    @Test
    fun `categorizeNodes categorizes inbox nodes correctly but ignores projects and areas`() {
        val taskInbox = createTestNodeWithPin(defaultNode(1).copy(type = "task", inboxState = true))
        val noteInbox = createTestNodeWithPin(defaultNode(2).copy(type = "note", inboxState = true))
        val projectInbox = createTestNodeWithPin(defaultNode(3).copy(type = "project", inboxState = true))
        val areaInbox = createTestNodeWithPin(defaultNode(4).copy(type = "area", inboxState = true))
        val nonInbox = createTestNodeWithPin(defaultNode(5).copy(type = "task", inboxState = false))

        val result = categorizeNodes(listOf(taskInbox, noteInbox, projectInbox, areaInbox, nonInbox))

        assertEquals(2, result.inbox.size)
        val inboxIds = result.inbox.map { it.node.id }.toSet()
        assertEquals(setOf(1L, 2L), inboxIds)
    }

    @Test
    fun `categorizeNodes categorizes reminders correctly for active items with past reminderAt`() {
        val now = Clock.System.now().toEpochMilliseconds()

        val pastReminder = createTestNodeWithPin(defaultNode(1).copy(status = "active", reminderAt = now - 10000))
        val futureReminder = createTestNodeWithPin(defaultNode(2).copy(status = "active", reminderAt = now + 10000))
        val pastReminderOnHold = createTestNodeWithPin(defaultNode(3).copy(status = "on_hold", reminderAt = now - 10000))
        val noReminder = createTestNodeWithPin(defaultNode(4).copy(status = "active", reminderAt = null))

        val result = categorizeNodes(listOf(pastReminder, futureReminder, pastReminderOnHold, noReminder))

        assertEquals(1, result.reminders.size)
        assertEquals(1L, result.reminders[0].id)
    }

    @Test
    fun `buildProtocolHistoryItems maps correctly and handles missing nodes`() {
        val history1 = ProtocolHistoryEntity(id = 10, protocolNodeId = 1, executedAt = 1000L, notes = "note1")
        val history2 = ProtocolHistoryEntity(id = 20, protocolNodeId = 2, executedAt = 2000L, notes = "note2")

        val node1 = createTestNodeWithPin(defaultNode(1).copy(title = "Morning Routine"))
        // node 2 is missing from nodes list

        val result = buildProtocolHistoryItems(
            history = listOf(history1, history2),
            nodes = listOf(node1)
        )

        assertEquals(2, result.size)

        assertEquals(2, result.size)
        assertEquals(setOf(10L, 20L), result.map { it.historyId }.toSet())
        val item1 = result.first { it.historyId == 10L }
        assertEquals(1L, item1.protocolNodeId)
        assertEquals("Morning Routine", item1.protocolLabel)
        assertEquals(1000L, item1.executedAt)
        assertEquals("note1", item1.notes)

        assertEquals(20L, result[1].historyId)
        assertEquals(2L, result[1].protocolNodeId)
        assertEquals("Unknown protocol", result[1].protocolLabel)
        assertEquals("note2", result[1].notes)
    }

    @Test
    fun `categorizeNodes handles missing or null timestamps gracefully`() {
        val noReminderNode = createTestNodeWithPin(defaultNode(1).copy(status = "active", reminderAt = null))
        val result = categorizeNodes(listOf(noReminderNode))
        assertEquals(0, result.reminders.size)
    }

    @Test
    fun `categorizeNodes only processes reminders for active items`() {
        val now = Clock.System.now().toEpochMilliseconds()
        val archivedReminder = createTestNodeWithPin(defaultNode(1).copy(status = "archived", reminderAt = now - 1000))
        val onHoldReminder = createTestNodeWithPin(defaultNode(2).copy(status = "on_hold", reminderAt = now - 1000))
        val result = categorizeNodes(listOf(archivedReminder, onHoldReminder))
        assertEquals(0, result.reminders.size)
    }

    @Test
    fun `buildProtocolHistoryItems handles empty lists gracefully`() {
        val emptyResult = buildProtocolHistoryItems(emptyList(), emptyList())
        assertEquals(0, emptyResult.size)
    }
}

package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.ProtocolHistoryEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class MainStateAssemblersTest {

    private fun createTestNodeWithPin(
        id: Long,
        type: String = "task",
        status: String = "active",
        inboxState: Boolean = false,
        reminderAt: Long? = null,
        title: String = "Test Node"
    ): NodeWithPin {
        return NodeWithPin(
            node = NodeEntity(
                id = id,
                title = title,
                content = "",
                type = type,
                status = status,
                inboxState = inboxState,
                reminderAt = reminderAt,
                updatedAt = 0L
            ),
            pin = null,
            tags = emptyList()
        )
    }

    @Test
    fun `categorizeNodes categorizes archived nodes correctly`() {
        val archivedNode = createTestNodeWithPin(id = 1, status = "archived", inboxState = true)
        val activeNode = createTestNodeWithPin(id = 2, status = "active", inboxState = false)

        val result = categorizeNodes(listOf(archivedNode, activeNode))

        assertEquals(1, result.archived.size)
        assertEquals(1L, result.archived[0].node.id)

        assertEquals(0, result.inbox.size)
    }

    @Test
    fun `categorizeNodes categorizes inbox nodes correctly but ignores projects and areas`() {
        val taskInbox = createTestNodeWithPin(id = 1, type = "task", inboxState = true)
        val noteInbox = createTestNodeWithPin(id = 2, type = "note", inboxState = true)
        val projectInbox = createTestNodeWithPin(id = 3, type = "project", inboxState = true)
        val areaInbox = createTestNodeWithPin(id = 4, type = "area", inboxState = true)
        val nonInbox = createTestNodeWithPin(id = 5, type = "task", inboxState = false)

        val result = categorizeNodes(listOf(taskInbox, noteInbox, projectInbox, areaInbox, nonInbox))

        assertEquals(2, result.inbox.size)
        val inboxIds = result.inbox.map { it.node.id }.toSet()
        assertEquals(setOf(1L, 2L), inboxIds)
    }

    @Test
    fun `categorizeNodes categorizes reminders correctly for active items with past reminderAt`() {
        val now = Clock.System.now().toEpochMilliseconds()

        val pastReminder = createTestNodeWithPin(id = 1, status = "active", reminderAt = now - 10000)
        val futureReminder = createTestNodeWithPin(id = 2, status = "active", reminderAt = now + 10000)
        val pastReminderOnHold = createTestNodeWithPin(id = 3, status = "on_hold", reminderAt = now - 10000)
        val noReminder = createTestNodeWithPin(id = 4, status = "active", reminderAt = null)

        val result = categorizeNodes(listOf(pastReminder, futureReminder, pastReminderOnHold, noReminder))

        assertEquals(1, result.reminders.size)
        assertEquals(1L, result.reminders[0].id)
    }

    @Test
    fun `buildProtocolHistoryItems maps correctly and handles missing nodes`() {
        val history1 = ProtocolHistoryEntity(id = 10, protocolNodeId = 1, executedAt = 1000L, notes = "note1")
        val history2 = ProtocolHistoryEntity(id = 20, protocolNodeId = 2, executedAt = 2000L, notes = "note2")

        val node1 = createTestNodeWithPin(id = 1, title = "Morning Routine")
        // node 2 is missing from nodes list

        val result = buildProtocolHistoryItems(
            history = listOf(history1, history2),
            nodes = listOf(node1)
        )

        assertEquals(2, result.size)

        assertEquals(10L, result[0].historyId)
        assertEquals(1L, result[0].protocolNodeId)
        assertEquals("Morning Routine", result[0].protocolLabel)
        assertEquals("note1", result[0].notes)

        assertEquals(20L, result[1].historyId)
        assertEquals(2L, result[1].protocolNodeId)
        assertEquals("Unknown protocol", result[1].protocolLabel)
        assertEquals("note2", result[1].notes)
    }

    @Test
    fun `categorizeNodes handles missing or null timestamps gracefully`() {
        // Node with no reminder should not crash and not be in reminders
        val noReminderNode = createTestNodeWithPin(id = 1, status = "active", reminderAt = null)
        val result = categorizeNodes(listOf(noReminderNode))
        assertEquals(0, result.reminders.size)
    }

    @Test
    fun `categorizeNodes only processes reminders for active items`() {
        val now = Clock.System.now().toEpochMilliseconds()
        // Archived and on_hold nodes should NOT be considered for reminders even if reminderAt is in the past
        val archivedReminder = createTestNodeWithPin(id = 1, status = "archived", reminderAt = now - 1000)
        val onHoldReminder = createTestNodeWithPin(id = 2, status = "on_hold", reminderAt = now - 1000)
        val result = categorizeNodes(listOf(archivedReminder, onHoldReminder))
        assertEquals(0, result.reminders.size)
    }

    @Test
    fun `buildProtocolHistoryItems handles empty lists gracefully`() {
        val emptyResult = buildProtocolHistoryItems(emptyList(), emptyList())
        assertEquals(0, emptyResult.size)
    }
}

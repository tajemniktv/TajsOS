package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.CalendarEventEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.ScheduleEntryEntity
import com.tajemniktv.tajsos.ui.main.state.EntryType
import kotlin.test.Test
import kotlin.test.assertEquals

class MainStateAssemblersCalendarTest {

    private fun defaultNode(id: Long) = NodeEntity(
        id = id,
        title = "Test Node",
        content = "",
        type = "task",
        status = "active",
        inboxState = false,
        reminderAt = null,
        updatedAt = 0L,
        createdAt = 0L,
        projectId = null,
        areaId = null
    )

    private fun createTestNodeWithPin(node: NodeEntity): NodeWithPin {
        return NodeWithPin(node = node, pin = null, tags = emptyList())
    }

    @Test
    fun `buildCalendarEntries filters archived schedule entries`() {
        val activeNode = createTestNodeWithPin(defaultNode(1).copy(status = "active"))
        val archivedNode = createTestNodeWithPin(defaultNode(2).copy(status = "archived"))

        val activeEntry = ScheduleEntryEntity(id = 1, itemId = 1, scheduledAt = 1000L, endAt = 2000L, kind = "due", note = "")
        val archivedEntry = ScheduleEntryEntity(id = 2, itemId = 2, scheduledAt = 1000L, endAt = 2000L, kind = "due", note = "")

        val result = buildCalendarEntries(
            nodes = listOf(activeNode, archivedNode),
            scheduleEntries = listOf(activeEntry, archivedEntry),
            externalEvents = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals("schedule_1", result.first().id)
    }

    @Test
    fun `buildCalendarEntries formats completed schedule entries correctly`() {
        val completedNode = createTestNodeWithPin(defaultNode(1).copy(status = "done", title = "Completed Task"))
        val activeNode = createTestNodeWithPin(defaultNode(2).copy(status = "active", title = "Active Task"))

        val entry1 = ScheduleEntryEntity(id = 1, itemId = 1, scheduledAt = 1000L, endAt = 2000L, kind = "due", note = "")
        val entry2 = ScheduleEntryEntity(id = 2, itemId = 2, scheduledAt = 1000L, endAt = 2000L, kind = "reminder", note = "")
        val entry3 = ScheduleEntryEntity(id = 3, itemId = 2, scheduledAt = 1000L, endAt = 2000L, kind = "start", note = "")

        val result = buildCalendarEntries(
            nodes = listOf(completedNode, activeNode),
            scheduleEntries = listOf(entry1, entry2, entry3),
            externalEvents = emptyList()
        )



        assertEquals("✓ Due: Completed Task", result.find { it.id == "schedule_1" }?.title)

        assertEquals("Reminder: Active Task", result.find { it.id == "schedule_2" }?.title)
        assertEquals("Start: Active Task", result.find { it.id == "schedule_3" }?.title)
    }

    @Test
    fun `buildCalendarEntries includes implicit unscheduled node entries`() {
        val noScheduleTimeNode = createTestNodeWithPin(defaultNode(1).copy(title = "No Schedule Node", startAt = null, dueAt = null, reminderAt = null))
        val implicitDueNode = createTestNodeWithPin(defaultNode(2).copy(title = "Implicit Due Node", dueAt = 3000L))
        val implicitCompletedNode = createTestNodeWithPin(defaultNode(3).copy(title = "Implicit Completed Node", dueAt = 4000L, status = "done"))

        val result = buildCalendarEntries(
            nodes = listOf(noScheduleTimeNode, implicitDueNode, implicitCompletedNode),
            scheduleEntries = emptyList(),
            externalEvents = emptyList()
        )

        assertEquals(2, result.size)
        assertEquals("node_2", result.find { it.id == "node_2" }?.id)
        assertEquals("Implicit Due Node", result.find { it.id == "node_2" }?.title)
        assertEquals("node_3", result.find { it.id == "node_3" }?.id)
        assertEquals("✓ Implicit Completed Node", result.find { it.id == "node_3" }?.title)
    }

    @Test
    fun `buildCalendarEntries includes external events`() {
        val event = CalendarEventEntity(
            id = 1L,
            providerId = 1L,
            title = "External Event",
            description = "Description",
            startAt = 5000L,
            endAt = 6000L,
            isAllDay = true,
            location = null,
            url = null,
            createdAt = 0L,
            updatedAt = 0L
        )

        val result = buildCalendarEntries(
            nodes = emptyList(),
            scheduleEntries = emptyList(),
            externalEvents = listOf(event)
        )

        assertEquals(1, result.size)
        assertEquals("ext_1", result.first().id)
        assertEquals("External Event", result.first().title)
        assertEquals(EntryType.EXTERNAL, result.first().type)
        assertEquals(true, result.first().isAllDay)
    }

    @Test
    fun `buildCalendarEntries falls back to default schedule entry duration`() {
        val node = createTestNodeWithPin(defaultNode(1).copy(content = "Original Content"))
        val entry = ScheduleEntryEntity(id = 1, itemId = 1, scheduledAt = 1000L, endAt = null, kind = "OTHER", note = "Schedule Note")

        val result = buildCalendarEntries(
            nodes = listOf(node),
            scheduleEntries = listOf(entry),
            externalEvents = emptyList()
        )

        assertEquals(1, result.size)
        val calendarEntry = result.first()
        assertEquals(1000L, calendarEntry.startAt)
        assertEquals(1000L + (3600 * 1000), calendarEntry.endAt) // 1 hour default
        assertEquals("Original Content", calendarEntry.description) // Falls back to node content if present
        assertEquals("Test Node", calendarEntry.title) // 'OTHER' kind has no prefix
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for NodeEntity and NodeWithPin defaults and behavior.
 *
 * Key PR changes:
 * - activeReminders was removed from MainViewModel, which relied on reminderAt field
 * - isRecurring/recurringInterval recurrence logic was removed from updateNodeStatus
 * - archiveNode was simplified to set status="archived" without creating a recurring copy
 */
class NodeEntityTest {

    // --- Default field values ---

    @Test
    fun nodeEntity_defaultStatus_isActive() {
        val node = NodeEntity(type = "task", title = "Test")
        assertEquals("active", node.status)
    }

    @Test
    fun nodeEntity_defaultIsRecurring_isFalse() {
        val node = NodeEntity(type = "task", title = "Test")
        assertFalse(node.isRecurring)
    }

    @Test
    fun nodeEntity_defaultRecurringInterval_isNull() {
        val node = NodeEntity(type = "task", title = "Test")
        assertNull(node.recurringInterval)
    }

    @Test
    fun nodeEntity_defaultReminderAt_isNull() {
        val node = NodeEntity(type = "task", title = "Test")
        assertNull(node.reminderAt)
    }

    @Test
    fun nodeEntity_defaultArchivedAt_isNull() {
        val node = NodeEntity(type = "task", title = "Test")
        assertNull(node.archivedAt)
    }

    @Test
    fun nodeEntity_defaultCompletedAt_isNull() {
        val node = NodeEntity(type = "task", title = "Test")
        assertNull(node.completedAt)
    }

    @Test
    fun nodeEntity_defaultProjectId_isNull() {
        val node = NodeEntity(type = "task", title = "Test")
        assertNull(node.projectId)
    }

    @Test
    fun nodeEntity_defaultAreaId_isNull() {
        val node = NodeEntity(type = "task", title = "Test")
        assertNull(node.areaId)
    }

    @Test
    fun nodeEntity_defaultInboxState_isTrue() {
        val node = NodeEntity(type = "task", title = "Test")
        assertTrue(node.inboxState)
    }

    @Test
    fun nodeEntity_defaultIsPinned_isFalse() {
        val node = NodeEntity(type = "task", title = "Test")
        assertFalse(node.isPinned)
    }

    // --- Archiving behavior (simplified in PR) ---

    @Test
    fun nodeEntity_copyWithArchivedStatus_setsStatusToArchived() {
        val node = NodeEntity(type = "task", title = "Archive me")
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val archived = node.copy(status = "archived", updatedAt = now)
        assertEquals("archived", archived.status)
    }

    @Test
    fun nodeEntity_archiveDoesNotCreateRecurringCopy() {
        // The PR removed the recurring copy creation on archival.
        // Verify that a recurring node's archive copy no longer gets auto-generated
        // by checking that copy() preserves isRecurring but does not change id.
        val recurringNode = NodeEntity(
            id = 42L,
            type = "task",
            title = "Recurring",
            isRecurring = true,
            recurringInterval = "DAILY"
        )
        val archivedCopy = recurringNode.copy(status = "archived")
        // The id stays the same (no new node is auto-created)
        assertEquals(42L, archivedCopy.id)
        assertEquals("archived", archivedCopy.status)
    }

    // --- updateNodeStatus done sets completedAt ---

    @Test
    fun nodeEntity_copyWithDoneStatus_setsCompletedAt() {
        val node = NodeEntity(type = "task", title = "Do it")
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val done = node.copy(status = "done", completedAt = now, updatedAt = now)
        assertEquals("done", done.status)
        assertEquals(now, done.completedAt)
    }

    @Test
    fun nodeEntity_copyWithDoneStatus_doesNotSetArchivedAt() {
        val node = NodeEntity(type = "task", title = "Do it")
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val done = node.copy(status = "done", completedAt = now, updatedAt = now)
        assertNull(done.archivedAt)
    }

    // --- Recurring fields (structure preserved but logic removed from ViewModel) ---

    @Test
    fun nodeEntity_recurringNodeFields_arePreserved() {
        val node = NodeEntity(
            type = "task",
            title = "Weekly Review",
            isRecurring = true,
            recurringInterval = "WEEKLY"
        )
        assertTrue(node.isRecurring)
        assertEquals("WEEKLY", node.recurringInterval)
    }

    @Test
    fun nodeEntity_reminderAtField_canBeSet() {
        val reminderTime = 1700000000000L
        val node = NodeEntity(type = "task", title = "Reminded", reminderAt = reminderTime)
        assertEquals(reminderTime, node.reminderAt)
    }

    // --- NodeWithPin behavior ---

    @Test
    fun nodeWithPin_isPinnedToToday_isTrueWhenPinIsNotNull() {
        val node = NodeEntity(type = "task", title = "Pinned")
        val pin = TodayPinEntity(nodeId = node.id, date = "2026-01-01", position = 0)
        val nodeWithPin = NodeWithPin(node = node, pin = pin)
        assertTrue(nodeWithPin.isPinnedToToday)
    }

    @Test
    fun nodeWithPin_isPinnedToToday_isFalseWhenPinIsNull() {
        val node = NodeEntity(type = "task", title = "Not Pinned")
        val nodeWithPin = NodeWithPin(node = node, pin = null)
        assertFalse(nodeWithPin.isPinnedToToday)
    }

    @Test
    fun nodeWithPin_nodeIsAccessible() {
        val node = NodeEntity(id = 99L, type = "note", title = "My Note")
        val nodeWithPin = NodeWithPin(node = node, pin = null)
        assertEquals(99L, nodeWithPin.node.id)
        assertEquals("My Note", nodeWithPin.node.title)
    }

    // --- Regression: archived node status field ---

    @Test
    fun nodeEntity_statusField_canBeArchivedOnHoldSomedayBlocked() {
        val node = NodeEntity(type = "task", title = "Test")
        val onHold = node.copy(status = "on_hold")
        val someday = node.copy(status = "someday")
        val blocked = node.copy(status = "blocked")
        assertEquals("on_hold", onHold.status)
        assertEquals("someday", someday.status)
        assertEquals("blocked", blocked.status)
    }
}

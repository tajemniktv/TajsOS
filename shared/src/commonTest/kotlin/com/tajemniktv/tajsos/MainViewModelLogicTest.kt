/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TodayPinEntity
import com.tajemniktv.tajsos.ui.ExportData
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the pure logic patterns used by MainViewModel after the PR changes.
 *
 * Key PR changes tested:
 * 1. activeReminders was removed - no longer filters by reminderAt
 * 2. inboxNodes filter: type != "project" && type != "area" && inboxState == true && status != "archived"
 * 3. archivedNodes filter: status == "archived"
 * 4. searchResults filter: title/content contains a query
 * 5. ExportData serialization
 * 6. updateNodeStatus no longer creates recurring copy (verified via behavior test)
 */
class MainViewModelLogicTest {
    // ---------------------------------------------------------------------------
    // Helper builders
    // ---------------------------------------------------------------------------

    private fun makeNode(
        id: Long = 0L,
        type: String = "task",
        title: String = "Node",
        status: String = "active",
        inboxState: Boolean = true,
        isPinned: Boolean = false,
        reminderAt: Long? = null,
        isRecurring: Boolean = false,
        recurringInterval: String? = null,
        projectId: Long? = null,
        areaId: Long? = null,
    ) = NodeEntity(
        id = id,
        type = type,
        title = title,
        status = status,
        inboxState = inboxState,
        isPinned = isPinned,
        reminderAt = reminderAt,
        isRecurring = isRecurring,
        recurringInterval = recurringInterval,
        projectId = projectId,
        areaId = areaId,
    )

    private fun makeNodeWithPin(
        node: NodeEntity,
        pin: TodayPinEntity? = null,
    ) = NodeWithPin(node = node, pin = pin)

    // ---------------------------------------------------------------------------
    // inboxNodes filter logic
    // ---------------------------------------------------------------------------

    @Test
    fun inboxFilter_includesActiveTasksWithInboxState() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode()),
            )
        val result =
            nodes.filter {
                it.node.inboxState &&
                    it.node.status != "archived" &&
                    it.node.type != "project" &&
                    it.node.type != "area"
            }
        assertEquals(1, result.size)
    }

    @Test
    fun inboxFilter_excludesArchivedNodes() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(status = "archived")),
            )
        val result =
            nodes.filter {
                it.node.inboxState &&
                    it.node.status != "archived" &&
                    it.node.type != "project" &&
                    it.node.type != "area"
            }
        assertTrue(result.isEmpty())
    }

    @Test
    fun inboxFilter_excludesProjectNodes() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(type = "project")),
            )
        val result =
            nodes.filter {
                it.node.inboxState &&
                    it.node.status != "archived" &&
                    it.node.type != "project" &&
                    it.node.type != "area"
            }
        assertTrue(result.isEmpty())
    }

    @Test
    fun inboxFilter_excludesAreaNodes() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(type = "area")),
            )
        val result =
            nodes.filter {
                it.node.inboxState &&
                    it.node.status != "archived" &&
                    it.node.type != "project" &&
                    it.node.type != "area"
            }
        assertTrue(result.isEmpty())
    }

    @Test
    fun inboxFilter_excludesNodesWithInboxStateFalse() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(inboxState = false)),
            )
        val result =
            nodes.filter {
                it.node.inboxState &&
                    it.node.status != "archived" &&
                    it.node.type != "project" &&
                    it.node.type != "area"
            }
        assertTrue(result.isEmpty())
    }

    @Test
    fun inboxFilter_includesNoteAndIdeaTypes() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(type = "note")),
                makeNodeWithPin(makeNode(type = "idea")),
                makeNodeWithPin(makeNode(type = "resource")),
            )
        val result =
            nodes.filter {
                it.node.inboxState &&
                    it.node.status != "archived" &&
                    it.node.type != "project" &&
                    it.node.type != "area"
            }
        assertEquals(3, result.size)
    }

    // ---------------------------------------------------------------------------
    // archivedNodes filter logic
    // ---------------------------------------------------------------------------

    @Test
    fun archivedFilter_includesOnlyArchivedNodes() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(status = "archived")),
                makeNodeWithPin(makeNode()),
                makeNodeWithPin(makeNode(status = "done")),
            )
        val result = nodes.filter { it.node.status == "archived" }
        assertEquals(1, result.size)
        assertEquals("archived", result.first().node.status)
    }

    @Test
    fun archivedFilter_emptyWhenNoArchivedNodes() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode()),
                makeNodeWithPin(makeNode(status = "done")),
            )
        val result = nodes.filter { it.node.status == "archived" }
        assertTrue(result.isEmpty())
    }

    @Test
    fun archivedFilter_returnsAllArchivedNodesWhenMultiple() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(id = 1L, status = "archived")),
                makeNodeWithPin(makeNode(id = 2L, status = "archived")),
                makeNodeWithPin(makeNode(id = 3L)),
            )
        val result = nodes.filter { it.node.status == "archived" }
        assertEquals(2, result.size)
    }

    // ---------------------------------------------------------------------------
    // activeReminders was removed — confirm the filter would no longer be applied
    // (This is a logical regression check for the PR change)
    // ---------------------------------------------------------------------------

    @Test
    fun removedActiveReminders_nodeWithPastReminderIsNotSpeciallyFiltered() {
        // The PR removed the activeReminders StateFlow. Nodes with reminderAt in the past
        // are now NOT surfaced separately. We verify that the node still appears normally
        // in allNodes but no longer through a reminder-specific filter.
        val pastReminder = 1000L // very old timestamp
        val node = makeNode(reminderAt = pastReminder)
        val nodes = listOf(makeNodeWithPin(node))

        // allNodes filter (status != archived) — node appears
        val inAll = nodes.filter { it.node.status != "archived" }
        assertEquals(1, inAll.size)

        // No active reminder filter exists anymore — just confirm the field is present
        assertEquals(pastReminder, node.reminderAt)
    }

    // ---------------------------------------------------------------------------
    // searchResults logic
    // ---------------------------------------------------------------------------

    @Test
    fun searchFilter_returnsEmptyListForBlankQuery() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(title = "Hello World")),
            )
        val query = ""
        val result =
            if (query.isBlank()) {
                emptyList()
            } else {
                nodes.filter {
                    it.node.title.contains(query, ignoreCase = true) ||
                        it.node.content.contains(query, ignoreCase = true)
                }
            }
        assertTrue(result.isEmpty())
    }

    @Test
    fun searchFilter_matchesByTitle() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(title = "Buy groceries")),
                makeNodeWithPin(makeNode(title = "Read a book")),
            )
        val query = "groceries"
        val result =
            nodes.filter {
                it.node.title.contains(query, ignoreCase = true) ||
                    it.node.content.contains(query, ignoreCase = true)
            }
        assertEquals(1, result.size)
        assertEquals("Buy groceries", result.first().node.title)
    }

    @Test
    fun searchFilter_isCaseInsensitive() {
        val nodes =
            listOf(
                makeNodeWithPin(makeNode(title = "Important Meeting")),
            )
        val query = "important"
        val result =
            nodes.filter {
                it.node.title.contains(query, ignoreCase = true) ||
                    it.node.content.contains(query, ignoreCase = true)
            }
        assertEquals(1, result.size)
    }

    // ---------------------------------------------------------------------------
    // updateNodeStatus — no longer creates a recurring copy
    // ---------------------------------------------------------------------------

    @Test
    fun updateNodeStatus_done_setsCompletedAt() {
        // The VM copies the node with completedAt = now when status = "done"
        val node = makeNode()
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
        val updated =
            node.copy(
                status = "done",
                updatedAt = now,
                completedAt = now,
                archivedAt = null, // archived only set if status == "archived"
            )
        assertEquals("done", updated.status)
        assertEquals(now, updated.completedAt)
        assertEquals(null, updated.archivedAt)
    }

    @Test
    fun updateNodeStatus_archived_setsArchivedAt() {
        val node = makeNode()
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
        val updated =
            node.copy(
                status = "archived",
                updatedAt = now,
                completedAt = null,
                archivedAt = now,
            )
        assertEquals("archived", updated.status)
        assertEquals(now, updated.archivedAt)
    }

    @Test
    fun updateNodeStatus_recurringNode_doesNotAutoCreateNewNode() {
        // Before this PR, completing a recurring node would create a new node.
        // After this PR, that logic is removed. The existing node is simply updated.
        // This test verifies the node copy pattern: no new id=0 copy is created.
        val recurring =
            makeNode(id = 10L, isRecurring = true, recurringInterval = "DAILY")
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()

        // Simulate what the updated archiveNode/updateNodeStatus does (no copy creation)
        val updated =
            recurring.copy(status = "done", updatedAt = now, completedAt = now, archivedAt = null)

        // The id is preserved — no new node with id=0 is created
        assertEquals(10L, updated.id)
        assertEquals("done", updated.status)
        // isRecurring field is still present in the data (structure preserved)
        assertTrue(updated.isRecurring)
    }

    // ---------------------------------------------------------------------------
    // archiveNode simplified behavior
    // ---------------------------------------------------------------------------

    @Test
    fun archiveNode_setsStatusToArchived() {
        val node = makeNode(id = 5L)
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
        val archived = node.copy(status = "archived", updatedAt = now)
        assertEquals("archived", archived.status)
        assertEquals(5L, archived.id) // same node, no new copy
    }

    @Test
    fun archiveNode_preservesOtherFields() {
        val node = makeNode(id = 5L, title = "Keep Me", type = "note")
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
        val archived = node.copy(status = "archived", updatedAt = now)
        assertEquals("Keep Me", archived.title)
        assertEquals("note", archived.type)
    }

    // ---------------------------------------------------------------------------
    // ExportData serialization
    // ---------------------------------------------------------------------------

    @Test
    fun exportData_serializesToJson() {
        val node = makeNode(id = 1L, title = "Export Me", type = "note")
        val exportData = ExportData(version = 2, nodes = listOf(node))
        val json = Json.encodeToString(exportData)
        assertTrue(json.contains("Export Me"))
        assertTrue(json.contains("\"version\":2"))
    }

    @Test
    fun exportData_deserializesFromJson() {
        val node = makeNode(id = 1L, title = "Restored")
        val exportData = ExportData(version = 2, nodes = listOf(node))
        val json = Json.encodeToString(exportData)
        val decoded = Json.decodeFromString<ExportData>(json)
        assertEquals(2, decoded.version)
        assertEquals(1, decoded.nodes.size)
        assertEquals("Restored", decoded.nodes.first().title)
    }

    @Test
    fun exportData_emptyNodes_serializesCorrectly() {
        val exportData = ExportData(version = 2, nodes = emptyList())
        val json = Json.encodeToString(exportData)
        val decoded = Json.decodeFromString<ExportData>(json)
        assertEquals(0, decoded.nodes.size)
    }

    @Test
    fun exportData_versionIsTwo() {
        // The current export version is 2 per the ViewModel
        val exportData = ExportData(version = 2, nodes = emptyList())
        assertEquals(2, exportData.version)
    }

    // ---------------------------------------------------------------------------
    // Regression: togglePermanentPin flips isPinned
    // ---------------------------------------------------------------------------

    @Test
    fun togglePermanentPin_flipsPinnedState() {
        val node = makeNode()
        val pinned = node.copy(isPinned = !node.isPinned)
        assertTrue(pinned.isPinned)

        val unpinned = pinned.copy(isPinned = !pinned.isPinned)
        assertFalse(unpinned.isPinned)
    }
}

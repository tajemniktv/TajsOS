/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AppRepositoryTest {

    private val fakeNodeDao = FakeNodeDao()
    private val fakeFocusSessionDao = FakeFocusSessionDao()
    private val fakeTrackDao = FakeTrackDao()
    private val fakeRelationDao = FakeRelationDao()
    private val fakeTagDao = FakeTagDao()
    private val fakeEventLogDao = FakeEventLogDao()
    private val fakeAttachmentDao = FakeAttachmentDao()
    private val fakeTemplateDao = FakeTemplateDao()
    private val fakeNodeSnapshotDao = FakeNodeSnapshotDao()
    private val fakeReviewDao = FakeReviewDao()
    private val fakeCalendarProviderDao = FakeCalendarProviderDao()
    private val fakeCalendarEventDao = FakeCalendarEventDao()

    private val repository = AppRepository(
        nodeDao = fakeNodeDao,
        focusSessionDao = fakeFocusSessionDao,
        trackDao = fakeTrackDao,
        relationDao = fakeRelationDao,
        tagDao = fakeTagDao,
        eventLogDao = fakeEventLogDao,
        attachmentDao = fakeAttachmentDao,
        templateDao = fakeTemplateDao,
        nodeSnapshotDao = fakeNodeSnapshotDao,
        reviewDao = fakeReviewDao,
        calendarProviderDao = fakeCalendarProviderDao,
        calendarEventDao = fakeCalendarEventDao
    )

    private fun assertLogEventAdded(
        expectedType: String,
        expectedNodeId: Long? = null,
        expectedSize: Int = 1,
        logIndex: Int = 0
    ) {
        val logs = fakeEventLogDao.getLogs()
        assertEquals(expectedSize, logs.size)
        assertEquals(expectedType, logs[logIndex].eventType)
        if (expectedNodeId != null) {
            assertEquals(expectedNodeId, logs[logIndex].nodeId)
        }
    }

    @Test
    fun testInsertNodeLogsEvent() = runTest {
        val node = NodeEntity(type = "task", title = "Test Node")
        val id = repository.insertNode(node)
        assertLogEventAdded("NODE_CREATED", id)
    }

    @Test
    fun testUpdateNodeCompletedLogsEvent() = runTest {
        val node = NodeEntity(type = "task", title = "Test Node", status = "active")
        val id = repository.insertNode(node)
        val doneNode = node.copy(id = id, status = "done")
        repository.updateNode(doneNode)
        assertLogEventAdded("NODE_COMPLETED", id, expectedSize = 2, logIndex = 1)
    }

    @Test
    fun testUpdateNodeArchivedLogsEvent() = runTest {
        val node = NodeEntity(type = "task", title = "Test Node", status = "active")
        val id = repository.insertNode(node)
        val archivedNode = node.copy(id = id, status = "archived")
        repository.updateNode(archivedNode)
        assertLogEventAdded("NODE_ARCHIVED", id, expectedSize = 2, logIndex = 1)
    }

    @Test
    fun testPinToTodayLogsEvent() = runTest {
        repository.pinToToday(1)
        assertLogEventAdded("TODAY_ASSIGNED", 1)
    }

    @Test
    fun testInsertSessionLogsEvent() = runTest {
        val session = FocusSessionEntity(nodeId = 1, startedAt = 1000)
        repository.insertSession(session)
        assertLogEventAdded("SESSION_STARTED", 1)
    }

    @Test
    fun testUpdateSessionLogsEvent() = runTest {
        val session = FocusSessionEntity(id = 1, nodeId = 1, startedAt = 1000, endedAt = 2000)
        repository.updateSession(session)
        assertLogEventAdded("SESSION_ENDED", 1)
    }

    @Test
    fun testInsertTrackEntryLogsEvent() = runTest {
        val entry = TrackEntryEntity(date = "2024-03-21")
        repository.insertTrackEntry(entry)
        assertLogEventAdded("CHECKIN_CREATED")
    }

    @Test
    fun testInsertRelationLogsEvent() = runTest {
        val relation = RelationEntity(fromNodeId = 1, toNodeId = 2, relationType = "RELATED")
        repository.insertRelation(relation)
        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("NODE_LINKED", logs[0].eventType)
        assertEquals(1, logs[0].nodeId)
        assertEquals(2, logs[0].relatedNodeId)
    }

    @Test
    fun testUnpinFromTodayIsDelegated() = runTest {
        repository.pinToToday(1)
        var isPinned = repository.isPinnedToToday(1).first()
        assertTrue(isPinned)

        repository.unpinFromToday(1)
        isPinned = repository.isPinnedToToday(1).first()
        assertFalse(isPinned)
    }

    @Test
    fun testDeleteNodeDelegatesProperly() = runTest {
        val node = NodeEntity(type = "task", title = "Test Node")
        val id = repository.insertNode(node)

        val retrievedNode = repository.getNodeById(id)
checkNotNull(retrievedNode) { "Node with id $id should exist after insertion" }

        repository.deleteNode(retrievedNode)

        val nullNode = repository.getNodeById(id)
        assertTrue(nullNode == null)
    }

    @Test
    fun testGetNodesByTypeExcludesArchived() = runTest {
        val node1 = NodeEntity(type = "project", title = "Project 1")
        val node2 = NodeEntity(type = "project", title = "Project 2", status = "archived")
        repository.insertNode(node1)
        repository.insertNode(node2)

        val projectsFlow = repository.getNodesByType("project")
        val projects = projectsFlow.first()

        assertEquals(1, projects.size)
        assertEquals("Project 1", projects[0].title)
    }
}

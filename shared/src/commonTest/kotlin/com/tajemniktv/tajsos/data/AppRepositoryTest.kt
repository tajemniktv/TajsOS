package com.tajemniktv.tajsos.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppRepositoryTest {

    private val fakeNodeDao = FakeNodeDao()
    private val fakeFocusSessionDao = FakeFocusSessionDao()
    private val fakeTrackDao = FakeTrackDao()
    private val fakeRelationDao = FakeRelationDao()
    private val fakeTagDao = FakeTagDao()
    private val fakeEventLogDao = FakeEventLogDao()
    private val fakeAttachmentDao = FakeAttachmentDao()
    private val fakeTemplateDao = FakeTemplateDao()

    private val repository = AppRepository(
        nodeDao = fakeNodeDao,
        focusSessionDao = fakeFocusSessionDao,
        trackDao = fakeTrackDao,
        relationDao = fakeRelationDao,
        tagDao = fakeTagDao,
        eventLogDao = fakeEventLogDao,
        attachmentDao = fakeAttachmentDao,
        templateDao = fakeTemplateDao
    )

    @Test
    fun testInsertNodeLogsEvent() = runTest {
        val node = NodeEntity(type = "task", title = "Test Node")
        val id = repository.insertNode(node)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("NODE_CREATED", logs[0].eventType)
        assertEquals(id, logs[0].nodeId)
    }

    @Test
    fun testUpdateNodeCompletedLogsEvent() = runTest {
        val node = NodeEntity(id = 1, type = "task", title = "Test Node", status = "done")
        repository.updateNode(node)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("NODE_COMPLETED", logs[0].eventType)
        assertEquals(1, logs[0].nodeId)
    }

    @Test
    fun testUpdateNodeArchivedLogsEvent() = runTest {
        val node = NodeEntity(id = 1, type = "task", title = "Test Node", status = "archived")
        repository.updateNode(node)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("NODE_ARCHIVED", logs[0].eventType)
        assertEquals(1, logs[0].nodeId)
    }

    @Test
    fun testPinToTodayLogsEvent() = runTest {
        repository.pinToToday(1)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("TODAY_ASSIGNED", logs[0].eventType)
        assertEquals(1, logs[0].nodeId)
    }

    @Test
    fun testInsertSessionLogsEvent() = runTest {
        val session = FocusSessionEntity(nodeId = 1, startedAt = 1000)
        val id = repository.insertSession(session)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("SESSION_STARTED", logs[0].eventType)
        assertEquals(1, logs[0].nodeId)
    }

    @Test
    fun testUpdateSessionLogsEvent() = runTest {
        val session = FocusSessionEntity(id = 1, nodeId = 1, startedAt = 1000, endedAt = 2000)
        repository.updateSession(session)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("SESSION_ENDED", logs[0].eventType)
        assertEquals(1, logs[0].nodeId)
    }

    @Test
    fun testInsertTrackEntryLogsEvent() = runTest {
        val entry = TrackEntryEntity(date = "2024-03-21")
        repository.insertTrackEntry(entry)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("CHECKIN_CREATED", logs[0].eventType)
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
}

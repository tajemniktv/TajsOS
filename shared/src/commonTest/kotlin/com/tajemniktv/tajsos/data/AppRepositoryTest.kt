package com.tajemniktv.tajsos.data

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.first

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
        calendarEventDao = fakeCalendarEventDao,
        modeDao = FakeModeDao(),
        protocolDao = FakeProtocolDao(),
        decisionDao = FakeDecisionDao(),
        userDao = FakeUserDao(),
        medicationDao = FakeMedicationDao()
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
        val id = fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Test Node", status = "active"))
        val node = NodeEntity(id = id, type = "task", title = "Test Node", status = "done")
        repository.updateNode(node)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("NODE_COMPLETED", logs[0].eventType)
        assertEquals(id, logs[0].nodeId)
    }

    @Test
    fun testUpdateNodeArchivedLogsEvent() = runTest {
        val id = fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Test Node", status = "active"))
        val node = NodeEntity(id = id, type = "task", title = "Test Node", status = "archived")
        repository.updateNode(node)

        val logs = fakeEventLogDao.getLogs()
        assertEquals(1, logs.size)
        assertEquals("NODE_ARCHIVED", logs[0].eventType)
        assertEquals(id, logs[0].nodeId)
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
    fun testGetActiveSession_returnsSession() = runTest {
        val session = FocusSessionEntity(nodeId = 1, startedAt = 1000)
        fakeFocusSessionDao.insertSession(session)

        val activeSession = repository.getActiveSession().first()
        assertNotNull(activeSession)
        assertEquals(1, activeSession.nodeId)
    }

    @Test
    fun testGetTodayNodes_filtersActiveNodes() = runTest {
        val date = kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()
        val activeId = fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Active", status = "active"))
        val doneId = fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Done", status = "done"))

        fakeNodeDao.pinToToday(TodayPinEntity(nodeId = activeId, date = date, position = 0))
        fakeNodeDao.pinToToday(TodayPinEntity(nodeId = doneId, date = date, position = 0))

        val todayNodes = repository.getTodayNodes().first()
        assertEquals(1, todayNodes.size)
        assertEquals(activeId, todayNodes[0].id)
    }

    @Test
    fun testGetNodesByType_filtersOutArchived() = runTest {
        val activeId = fakeNodeDao.insertNode(NodeEntity(type = "note", title = "Active Note", status = "active"))
        val archivedId = fakeNodeDao.insertNode(NodeEntity(type = "note", title = "Archived Note", status = "archived"))

        val noteNodes = repository.getNodesByType("note").first()
        assertEquals(1, noteNodes.size)
        assertEquals(activeId, noteNodes[0].id)
    }

    @Test
    fun testPinToToday_addsPin() = runTest {
        val nodeId = fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Task", status = "active"))
        repository.pinToToday(nodeId)

        val isPinned = repository.isPinnedToToday(nodeId).first()
        assertTrue(isPinned)
    }

    @Test
    fun testUnpinFromToday_removesPin() = runTest {
        val nodeId = fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Task", status = "active"))
        val date = "2024-03-21"

        fakeNodeDao.pinToToday(TodayPinEntity(nodeId = nodeId, date = date, position = 0))
        repository.unpinFromToday(nodeId)

        val isPinned = repository.isPinnedToToday(nodeId).first()
        assertFalse(isPinned)
    }
}

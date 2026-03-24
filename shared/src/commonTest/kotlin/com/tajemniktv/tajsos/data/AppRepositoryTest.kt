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
        val date = kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()

        fakeNodeDao.pinToToday(TodayPinEntity(nodeId = nodeId, date = date, position = 0))
        repository.unpinFromToday(nodeId)

        val isPinned = repository.isPinnedToToday(nodeId).first()
        assertFalse(isPinned)
    }

    @Test
    fun testDecideOn_updatesNodeAndOptions() = runTest {
        val nodeId = fakeNodeDao.insertNode(
            NodeEntity(
                type = "decision",
                title = "What framework?",
                decisionStatus = "pending",
                inboxState = true,
                status = "active"
            )
        )

        val option1Id = repository.insertDecisionOption(
            DecisionOptionEntity(decisionNodeId = nodeId, title = "Option A")
        )
        val option2Id = repository.insertDecisionOption(
            DecisionOptionEntity(decisionNodeId = nodeId, title = "Option B")
        )

        repository.decideOn(nodeId, "Went with Option B", option2Id)

        val updatedNode = repository.getNodeById(nodeId)
        assertNotNull(updatedNode)
        assertEquals("decided", updatedNode.decisionStatus)
        assertEquals("Went with Option B", updatedNode.decisionOutcome)
        assertEquals("done", updatedNode.status)
        assertFalse(updatedNode.inboxState)

        val options = repository.getOptionsForDecision(nodeId).first()
        assertEquals(2, options.size)

        val updatedOption1 = options.find { it.id == option1Id }
        val updatedOption2 = options.find { it.id == option2Id }

        assertNotNull(updatedOption1)
        assertNotNull(updatedOption2)
        assertFalse(updatedOption1.isSelected)
        assertTrue(updatedOption2.isSelected)
    }

    private suspend fun createBaseDecisionNode(title: String, content: String, outcome: String, areaId: Long, projectId: Long? = null): Long {
        return fakeNodeDao.insertNode(
            NodeEntity(
                type = "decision",
                title = title,
                content = content,
                decisionOutcome = outcome,
                areaId = areaId,
                projectId = projectId
            )
        )
    }

    private suspend fun assertDerivedRelation(originalId: Long, derivedId: Long) {
        val relations = repository.getAllRelations().first()
        val derivedRelation = relations.find { it.fromNodeId == originalId && it.toNodeId == derivedId }
        assertNotNull(derivedRelation)
        assertEquals("DERIVED_FROM", derivedRelation.relationType)
    }

    @Test
    fun testConvertDecisionToProject_createsDerivedProject() = runTest {
        val originalNodeId = createBaseDecisionNode(
            title = "Move to New York",
            content = "Need to figure out if it's worth it",
            outcome = "Decided to move",
            areaId = 42L
        )

        val projectId = repository.convertDecisionToProject(originalNodeId)

        val newProject = repository.getNodeById(projectId)
        assertNotNull(newProject)
        assertEquals("project", newProject.type)
        assertEquals("Action Plan: Move to New York", newProject.title)
        assertEquals("Derived from decision: Decided to move", newProject.content)
        assertEquals(42L, newProject.areaId)
        assertTrue(newProject.inboxState)
        assertEquals("active", newProject.status)

        assertDerivedRelation(originalNodeId, projectId)
    }

    @Test
    fun testConvertDecisionToTask_createsDerivedTask() = runTest {
        val originalNodeId = createBaseDecisionNode(
            title = "Buy new laptop",
            content = "Old one is breaking",
            outcome = "Buying the M3 Pro",
            areaId = 10L,
            projectId = 20L
        )

        val taskId = repository.convertDecisionToTask(originalNodeId)

        val newTask = repository.getNodeById(taskId)
        assertNotNull(newTask)
        assertEquals("task", newTask.type)
        assertEquals("Follow-up: Buy new laptop", newTask.title)
        assertEquals("Outcome: Buying the M3 Pro\n\nOld one is breaking", newTask.content)
        assertEquals(10L, newTask.areaId)
        assertEquals(20L, newTask.projectId)
        assertTrue(newTask.inboxState)
        assertEquals("active", newTask.status)

        assertDerivedRelation(originalNodeId, taskId)
    }
}

/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
    private val fakeNodeSnapshotDao = FakeNodeSnapshotDao()
    private val fakeReviewDao = FakeReviewDao()
    private val fakeCalendarProviderDao = FakeCalendarProviderDao()
    private val fakeCalendarEventDao = FakeCalendarEventDao()

    private val repository =
        AppRepository(
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
            medicationDao = FakeMedicationDao(),
        )

    @Test
    fun testInsertNodeLogsEvent(): TestResult =
        runTest {
            val node = NodeEntity(type = "task", title = "Test Node")
            val id = repository.insertNode(node)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(1, logs.size)
            assertEquals("NODE_CREATED", logs[0].eventType)
            assertEquals(id, logs[0].nodeId)
        }

    @Test
    fun testUpdateNodeCompletedLogsEvent(): TestResult =
        runTest {
            val id =
                fakeNodeDao.insertNode(
                    NodeEntity(
                        type = "task",
                        title = "Test Node",
                    ),
                )
            val node = NodeEntity(id = id, type = "task", title = "Test Node", status = "done")
            repository.updateNode(node)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(1, logs.size)
            assertEquals("NODE_COMPLETED", logs[0].eventType)
            assertEquals(id, logs[0].nodeId)
        }

    @Test
    fun testUpdateNodeArchivedLogsEvent(): TestResult =
        runTest {
            val id =
                fakeNodeDao.insertNode(
                    NodeEntity(
                        type = "task",
                        title = "Test Node",
                    ),
                )
            val node = NodeEntity(id = id, type = "task", title = "Test Node", status = "archived")
            repository.updateNode(node)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(1, logs.size)
            assertEquals("NODE_ARCHIVED", logs[0].eventType)
            assertEquals(id, logs[0].nodeId)
        }

    @Test
    fun testPinToTodayLogsEvent(): TestResult =
        runTest {
            repository.pinToToday(1)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(1, logs.size)
            assertEquals("TODAY_ASSIGNED", logs[0].eventType)
            assertEquals(1, logs[0].nodeId)
        }

    @Test
    fun testInsertSessionLogsEvent(): TestResult =
        runTest {
            val session = FocusSessionEntity(nodeId = 1, startedAt = 1000)
            repository.insertSession(session)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(1, logs.size)
            assertEquals("SESSION_STARTED", logs[0].eventType)
            assertEquals(1, logs[0].nodeId)
        }

    @Test
    fun testUpdateSessionLogsEvent(): TestResult =
        runTest {
            val session = FocusSessionEntity(id = 1, nodeId = 1, startedAt = 1000, endedAt = 2000)
            repository.updateSession(session)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(1, logs.size)
            assertEquals("SESSION_ENDED", logs[0].eventType)
            assertEquals(1, logs[0].nodeId)
        }

    @Test
    fun testInsertTrackEntryLogsEvent(): TestResult =
        runTest {
            val entry = TrackEntryEntity(date = "2024-03-21")
            repository.insertTrackEntry(entry)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(1, logs.size)
            assertEquals("CHECKIN_CREATED", logs[0].eventType)
        }

    @Test
    fun testGetActiveSession_returnsSession(): TestResult =
        runTest {
            val session = FocusSessionEntity(nodeId = 1, startedAt = 1000)
            fakeFocusSessionDao.insertSession(session)

            val activeSession = repository.getActiveSession().first()
            assertNotNull(activeSession)
            assertEquals(1, activeSession.nodeId)
        }

    @Test
    fun testGetTodayNodes_filtersActiveNodes(): TestResult =
        runTest {
            val date =
                kotlin.time.Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toString()
            val activeId =
                fakeNodeDao.insertNode(
                    NodeEntity(
                        type = "task",
                        title = "Active",
                    ),
                )
            val doneId =
                fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Done", status = "done"))

            fakeNodeDao.pinToToday(TodayPinEntity(nodeId = activeId, date = date, position = 0))
            fakeNodeDao.pinToToday(TodayPinEntity(nodeId = doneId, date = date, position = 0))

            val todayNodes = repository.getTodayNodes().first()
            assertEquals(1, todayNodes.size)
            assertEquals(activeId, todayNodes[0].id)
        }

    @Test
    fun testGetNodesByType_filtersOutArchived(): TestResult =
        runTest {
            val activeId =
                fakeNodeDao.insertNode(
                    NodeEntity(
                        type = "note",
                        title = "Active Note",
                    ),
                )
            fakeNodeDao.insertNode(
                NodeEntity(
                    type = "note",
                    title = "Archived Note",
                    status = "archived",
                ),
            )

            val noteNodes = repository.getNodesByType("note").first()
            assertEquals(1, noteNodes.size)
            assertEquals(activeId, noteNodes[0].id)
        }

    @Test
    fun testPinToToday_addsPin(): TestResult =
        runTest {
            val nodeId =
                fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Task"))
            repository.pinToToday(nodeId)

            val isPinned = repository.isPinnedToToday(nodeId).first()
            assertTrue(isPinned)
        }

    @Test
    fun testUnpinFromToday_removesPin(): TestResult =
        runTest {
            val nodeId =
                fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Task"))
            val date =
                kotlin.time.Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toString()

            fakeNodeDao.pinToToday(TodayPinEntity(nodeId = nodeId, date = date, position = 0))
            repository.unpinFromToday(nodeId)

            val isPinned = repository.isPinnedToToday(nodeId).first()
            assertFalse(isPinned)
        }

    @Test
    fun testDecideOn_updatesNodeAndOptions(): TestResult =
        runTest {
            val nodeId =
                fakeNodeDao.insertNode(
                    NodeEntity(
                        type = "decision",
                        title = "What framework?",
                        decisionStatus = "pending",
                    ),
                )

            val option1Id =
                repository.insertDecisionOption(
                    DecisionOptionEntity(decisionNodeId = nodeId, title = "Option A"),
                )
            val option2Id =
                repository.insertDecisionOption(
                    DecisionOptionEntity(decisionNodeId = nodeId, title = "Option B"),
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

    private suspend fun assertDerivedRelation(
        originalId: Long,
        derivedId: Long,
    ) {
        val relations = repository.getAllRelations().first()
        val derivedRelation =
            relations.find { it.fromNodeId == originalId && it.toNodeId == derivedId }
        assertNotNull(derivedRelation)
        assertEquals("DERIVED_FROM", derivedRelation.relationType)
    }

    @Test
    fun testConvertDecisionToProject_createsDerivedProject(): TestResult =
        runTest {
            val originalNodeId =
                fakeNodeDao.insertNode(
                    NodeEntity(
                        type = "decision",
                        title = "Move to New York",
                        content = "Need to figure out if it's worth it",
                        decisionOutcome = "Decided to move",
                        areaId = 42L,
                    ),
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
    fun testConvertDecisionToTask_createsDerivedTask(): TestResult =
        runTest {
            val originalNodeId =
                fakeNodeDao.insertNode(
                    NodeEntity(
                        type = "decision",
                        title = "Buy new laptop",
                        content = "Old one is breaking",
                        decisionOutcome = "Buying the M3 Pro",
                        areaId = 10L,
                        projectId = 20L,
                    ),
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

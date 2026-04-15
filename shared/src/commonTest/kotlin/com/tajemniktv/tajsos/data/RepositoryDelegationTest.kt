/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

/**
 * Tests for AppRepository delegation to new DAO methods introduced in this PR:
 * - getNodesByProjectWithPins(projectId)
 * - getNodesByAreaWithPins(areaId)
 * - getProjectsByArea(areaId)
 *
 * Each test creates a fake NodeDao that records which method was called and
 * returns a predictable Flow, then verifies AppRepository passes through correctly.
 */
@Suppress("TestMethodWithoutAssertion")
class RepositoryDelegationTest {
    // ---------------------------------------------------------------------------
    // Fake implementations
    // ---------------------------------------------------------------------------

    /** Minimal fake NodeDao — only the three new methods return real Flows; rest throw. */
    private class FakeNodeDao(
        private val projectWithPinsFlow: Flow<List<NodeWithPin>> = flowOf(emptyList()),
        private val areaWithPinsFlow: Flow<List<NodeWithPin>> = flowOf(emptyList()),
        private val projectsByAreaFlow: Flow<List<NodeEntity>> = flowOf(emptyList())
    ) : NodeDao {
        var lastProjectWithPinsId: Long? = null
        var lastAreaWithPinsId: Long? = null
        var lastProjectsByAreaId: Long? = null

        override fun getNodesByProjectWithPins(projectId: Long): Flow<List<NodeWithPin>> {
            lastProjectWithPinsId = projectId
            return projectWithPinsFlow
        }

        override fun getNodesByAreaWithPins(areaId: Long): Flow<List<NodeWithPin>> {
            lastAreaWithPinsId = areaId
            return areaWithPinsFlow
        }

        override fun getProjectsByArea(areaId: Long): Flow<List<NodeEntity>> {
            lastProjectsByAreaId = areaId
            return projectsByAreaFlow
        }

        // Stubs — not called in these tests
        override fun getAllNodesWithPins(): Flow<List<NodeWithPin>> = flowOf(emptyList())

        override fun getTodayNodes(date: String): Flow<List<NodeEntity>> = flowOf(emptyList())

        override fun getNodesByType(type: String): Flow<List<NodeEntity>> = flowOf(emptyList())

        override fun getNodesByProject(projectId: Long): Flow<List<NodeEntity>> =
            flowOf(emptyList())

        override fun getNodesByArea(areaId: Long): Flow<List<NodeEntity>> = flowOf(emptyList())

        override suspend fun getNodeById(id: Long): NodeEntity? = null

        override suspend fun insertNode(node: NodeEntity): Long = 0L

        override suspend fun insertNodes(nodes: List<NodeEntity>): List<Long> = nodes.map { 0L }

        override suspend fun updateNode(node: NodeEntity) {
        }

        override suspend fun deleteNode(node: NodeEntity) {
        }

        override suspend fun pinToToday(pin: TodayPinEntity) {
        }

        override suspend fun unpinFromToday(nodeId: Long) {
        }

        override fun isPinnedToToday(nodeId: Long): Flow<Boolean> = flowOf(false)
    }

    private class FakeFocusSessionDao : FocusSessionDao {
        override fun getAllSessions(): Flow<List<FocusSessionEntity>> = flowOf(emptyList())

        override suspend fun insertSession(session: FocusSessionEntity): Long = 0L

        override suspend fun updateSession(session: FocusSessionEntity) {
        }

        override fun getActiveSession(): Flow<FocusSessionEntity?> = flowOf(null)
    }

    private class FakeTrackDao : TrackDao {
        override fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> = flowOf(emptyList())

        override suspend fun insertTrackEntry(entry: TrackEntryEntity): Long = 0

        override suspend fun getTrackEntryByDate(date: String): TrackEntryEntity? = null

        override suspend fun insertTrackMedication(join: TrackMedicationJoinEntity) {
        }

        override fun getTrackMedications(
            trackEntryId: Long
        ): Flow<List<TrackMedicationJoinEntity>> = flowOf(emptyList())
    }

    private class FakeRelationDao : RelationDao {
        override fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>> =
            flowOf(emptyList())

        override suspend fun insertRelation(relation: RelationEntity) {
        }

        override suspend fun insertRelations(relations: List<RelationEntity>) {
        }

        override suspend fun deleteRelation(relation: RelationEntity) {
        }

        override suspend fun deleteRelationsForNode(nodeId: Long) {
        }

        override suspend fun deleteBelongsToRelations(nodeId: Long) {
        }

        override suspend fun deleteBelongsToRelations(nodeIds: List<Long>) {
        }

        override suspend fun getBelongsToRelations(nodeId: Long): List<RelationEntity> = emptyList()

        override suspend fun anyRelationExists(
            from: Long,
            to: Long,
            relationType: String,
        ): Boolean = false

        override fun getAllRelations(): Flow<List<RelationEntity>> = flowOf(emptyList())
    }

    private class FakeTagDao : TagDao {
        override fun getAllTags(): Flow<List<TagEntity>> = flowOf(emptyList())

        override suspend fun insertTag(tag: TagEntity): Long = 0L

        override fun getTagsForNode(nodeId: Long): Flow<List<TagEntity>> = flowOf(emptyList())

        override suspend fun attachTagToNode(nodeTag: NodeTagEntity) {
        }

        override suspend fun detachTagFromNode(nodeId: Long, tagId: Long) {
        }

        override suspend fun detachAllTagsFromNode(nodeId: Long) {
        }
    }

    private class FakeEventLogDao : EventLogDao {
        override fun getRecentLogs(limit: Int): Flow<List<EventLogEntity>> = flowOf(emptyList())

        override fun getLogsForNode(nodeId: Long): Flow<List<EventLogEntity>> = flowOf(emptyList())

        override suspend fun insertLog(log: EventLogEntity) {
        }

        override suspend fun insertLogs(logs: List<EventLogEntity>) {
        }
    }

    private class FakeAttachmentDao : AttachmentDao {
        override fun getAllAttachments(): Flow<List<AttachmentEntity>> = flowOf(emptyList())

        override fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>> =
            flowOf(emptyList())

        override suspend fun insertAttachment(attachment: AttachmentEntity) {
        }

        override suspend fun deleteAttachment(attachment: AttachmentEntity) {
        }

        override suspend fun deleteAttachmentsForNode(nodeId: Long) {
        }
    }

    private class FakeTemplateDao : TemplateDao {
        override fun getAllTemplates(): Flow<List<TemplateEntity>> = flowOf(emptyList())

        override suspend fun insertTemplate(template: TemplateEntity) {
        }

        override suspend fun updateTemplate(template: TemplateEntity) {
        }

        override suspend fun deleteTemplate(template: TemplateEntity) {
        }
    }

    private fun buildRepository(nodeDao: FakeNodeDao = FakeNodeDao()): AppRepository =
        AppRepository(
            nodeDao = nodeDao,
            focusSessionDao = FakeFocusSessionDao(),
            trackDao = FakeTrackDao(),
            relationDao = FakeRelationDao(),
            tagDao = FakeTagDao(),
            eventLogDao = FakeEventLogDao(),
            attachmentDao = FakeAttachmentDao(),
            templateDao = FakeTemplateDao(),
            nodeSnapshotDao = FakeNodeSnapshotDao(),
            reviewDao = FakeReviewDao(),
            calendarProviderDao = FakeCalendarProviderDao(),
            calendarEventDao = FakeCalendarEventDao(),
            modeDao = FakeModeDao(),
            protocolDao = FakeProtocolDao(),
            decisionDao = FakeDecisionDao(),
            userDao = FakeUserDao(),
            medicationDao = FakeMedicationDao()
        )

    // ---------------------------------------------------------------------------
    // getNodesByProjectWithPins delegation
    // ---------------------------------------------------------------------------

    @Test
    fun getNodesByProjectWithPins_delegatesToNodeDao(): TestResult = runTest {
        val projectId = 42L
        val expectedNode = NodeEntity(id = 1L, type = "task", title = "Task in Project")
        val expectedFlow = flowOf(listOf(NodeWithPin(node = expectedNode, pin = null)))
        val fakeDao = FakeNodeDao(projectWithPinsFlow = expectedFlow)

        val repo = buildRepository(fakeDao)
        val result = repo.getNodesByProjectWithPins(projectId).first()

        assertEquals(projectId, fakeDao.lastProjectWithPinsId)
        assertEquals(1, result.size)
        assertEquals("Task in Project", result.first().node.title)
    }

    @Test
    fun getNodesByProjectWithPins_returnsEmptyListWhenNoNodes(): TestResult = runTest {
        val fakeDao = FakeNodeDao(projectWithPinsFlow = flowOf(emptyList()))
        val repo = buildRepository(fakeDao)
        val result = repo.getNodesByProjectWithPins(99L).first()
        assertEquals(emptyList(), result)
    }

    @Test
    fun getNodesByProjectWithPins_passesCorrectProjectIdToDao(): TestResult = runTest {
        val fakeDao = FakeNodeDao()
        val repo = buildRepository(fakeDao)
        repo.getNodesByProjectWithPins(77L).first()
        assertEquals(77L, fakeDao.lastProjectWithPinsId)
    }

    // ---------------------------------------------------------------------------
    // getNodesByAreaWithPins delegation
    // ---------------------------------------------------------------------------

    @Test
    fun getNodesByAreaWithPins_delegatesToNodeDao(): TestResult = runTest {
        val areaId = 10L
        val expectedNode = NodeEntity(id = 2L, type = "note", title = "Note in Area")
        val expectedFlow = flowOf(listOf(NodeWithPin(node = expectedNode, pin = null)))
        val fakeDao = FakeNodeDao(areaWithPinsFlow = expectedFlow)

        val repo = buildRepository(fakeDao)
        val result = repo.getNodesByAreaWithPins(areaId).first()

        assertEquals(areaId, fakeDao.lastAreaWithPinsId)
        assertEquals(1, result.size)
        assertEquals("Note in Area", result.first().node.title)
    }

    @Test
    fun getNodesByAreaWithPins_returnsEmptyListWhenNoNodes(): TestResult = runTest {
        val fakeDao = FakeNodeDao(areaWithPinsFlow = flowOf(emptyList()))
        val repo = buildRepository(fakeDao)
        val result = repo.getNodesByAreaWithPins(5L).first()
        assertEquals(emptyList(), result)
    }

    @Test
    fun getNodesByAreaWithPins_passesCorrectAreaIdToDao(): TestResult = runTest {
        val fakeDao = FakeNodeDao()
        val repo = buildRepository(fakeDao)
        repo.getNodesByAreaWithPins(55L).first()
        assertEquals(55L, fakeDao.lastAreaWithPinsId)
    }

    @Test
    fun getNodesByAreaWithPins_multiplePinnedNodes_areAllReturned(): TestResult = runTest {
        val node1 = NodeEntity(id = 1L, type = "task", title = "First")
        val node2 = NodeEntity(id = 2L, type = "note", title = "Second")
        val expectedFlow =
            flowOf(
                listOf(
                    NodeWithPin(node = node1, pin = null),
                    NodeWithPin(node = node2, pin = null)
                )
            )
        val fakeDao = FakeNodeDao(areaWithPinsFlow = expectedFlow)
        val repo = buildRepository(fakeDao)
        val result = repo.getNodesByAreaWithPins(1L).first()
        assertEquals(2, result.size)
    }

    // ---------------------------------------------------------------------------
    // getProjectsByArea delegation
    // ---------------------------------------------------------------------------

    @Test
    fun getProjectsByArea_delegatesToNodeDao(): TestResult = runTest {
        val areaId = 7L
        val expectedProject = NodeEntity(id = 3L, type = "project", title = "My Project")
        val expectedFlow = flowOf(listOf(expectedProject))
        val fakeDao = FakeNodeDao(projectsByAreaFlow = expectedFlow)

        val repo = buildRepository(fakeDao)
        val result = repo.getProjectsByArea(areaId).first()

        assertEquals(areaId, fakeDao.lastProjectsByAreaId)
        assertEquals(1, result.size)
        assertEquals("My Project", result.first().title)
        assertEquals("project", result.first().type)
    }

    @Test
    fun getProjectsByArea_returnsEmptyListWhenNoProjects(): TestResult = runTest {
        val fakeDao = FakeNodeDao(projectsByAreaFlow = flowOf(emptyList()))
        val repo = buildRepository(fakeDao)
        val result = repo.getProjectsByArea(3L).first()
        assertEquals(emptyList(), result)
    }

    @Test
    fun getProjectsByArea_passesCorrectAreaIdToDao(): TestResult = runTest {
        val fakeDao = FakeNodeDao()
        val repo = buildRepository(fakeDao)
        repo.getProjectsByArea(33L).first()
        assertEquals(33L, fakeDao.lastProjectsByAreaId)
    }

    @Test
    fun getProjectsByArea_multipleProjects_areAllReturned(): TestResult = runTest {
        val proj1 = NodeEntity(id = 1L, type = "project", title = "Alpha")
        val proj2 = NodeEntity(id = 2L, type = "project", title = "Beta")
        val proj3 = NodeEntity(id = 3L, type = "project", title = "Gamma")
        val expectedFlow = flowOf(listOf(proj1, proj2, proj3))
        val fakeDao = FakeNodeDao(projectsByAreaFlow = expectedFlow)
        val repo = buildRepository(fakeDao)
        val result = repo.getProjectsByArea(1L).first()
        assertEquals(3, result.size)
    }

    // ---------------------------------------------------------------------------
    // Regression: pre-existing delegation methods still work
    // ---------------------------------------------------------------------------

    @Test
    fun getNodesByProject_delegatesToNodeDao(): TestResult = runTest {
        // getNodesByProject (non-WithPins) was present before this PR; still works
        val fakeDao = FakeNodeDao()
        val repo = buildRepository(fakeDao)
        val result = repo.getNodesByProject(1L).first()
        assertEquals(emptyList(), result)
    }

    @Test
    fun getNodesByArea_delegatesToNodeDao(): TestResult = runTest {
        // getNodesByArea (non-WithPins) was present before this PR; still works
        val fakeDao = FakeNodeDao()
        val repo = buildRepository(fakeDao)
        val result = repo.getNodesByArea(1L).first()
        assertEquals(emptyList(), result)
    }
}

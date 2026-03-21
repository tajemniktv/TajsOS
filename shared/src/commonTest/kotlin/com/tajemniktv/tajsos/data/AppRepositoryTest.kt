package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AppRepositoryTest {

    // Simple manual mock of NodeDao for testing
    class MockNodeDao : NodeDao {
        var pinnedNodeId: Long? = null
        var pinnedDate: String? = null
        var pinnedPosition: Int? = null

        override fun getAllNodesWithPins(): Flow<List<NodeWithPin>> = flowOf(emptyList())
        override fun getTodayNodes(date: String): Flow<List<NodeEntity>> = flowOf(emptyList())
        override fun getNodesByType(type: String): Flow<List<NodeEntity>> = flowOf(emptyList())
        override fun getNodesByProject(projectId: Long): Flow<List<NodeEntity>> = flowOf(emptyList())
        override fun getNodesByArea(areaId: Long): Flow<List<NodeEntity>> = flowOf(emptyList())
        override suspend fun getNodeById(id: Long): NodeEntity? = null
        override suspend fun insertNode(node: NodeEntity): Long = 0
        override suspend fun updateNode(node: NodeEntity) {}
        override suspend fun deleteNode(node: NodeEntity) {}

        override suspend fun pinToToday(pin: TodayPinEntity) {
            pinnedNodeId = pin.nodeId
            pinnedDate = pin.date
            pinnedPosition = pin.position
        }

        override suspend fun unpinFromToday(nodeId: Long) {}
        override fun isPinnedToToday(nodeId: Long): Flow<Boolean> = flowOf(false)
    }

    class MockFocusSessionDao : FocusSessionDao {
        override fun getAllSessions(): Flow<List<FocusSessionEntity>> = flowOf(emptyList())
        override suspend fun insertSession(session: FocusSessionEntity): Long = 0
        override suspend fun updateSession(session: FocusSessionEntity) {}
        override fun getActiveSession(): Flow<FocusSessionEntity?> = flowOf(null)
    }

    class MockTrackDao : TrackDao {
        override fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> = flowOf(emptyList())
        override suspend fun insertTrackEntry(entry: TrackEntryEntity) {}
    }

    class MockRelationDao : RelationDao {
        override fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>> = flowOf(emptyList())
        override suspend fun insertRelation(relation: RelationEntity) {}
        override suspend fun deleteRelation(relation: RelationEntity) {}
    }

    class MockTagDao : TagDao {
        override fun getAllTags(): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun insertTag(tag: TagEntity): Long = 0
        override fun getTagsForNode(nodeId: Long): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun attachTagToNode(nodeTag: NodeTagEntity) {}
        override suspend fun detachTagFromNode(nodeId: Long, tagId: Long) {}
    }

    class MockEventLogDao : EventLogDao {
        var loggedEventType: String? = null
        var loggedNodeId: Long? = null

        override fun getRecentLogs(limit: Int): Flow<List<EventLogEntity>> = flowOf(emptyList())
        override suspend fun insertLog(log: EventLogEntity) {
            loggedEventType = log.eventType
            loggedNodeId = log.nodeId
        }
    }

    class MockAttachmentDao : AttachmentDao {
        override fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>> = flowOf(emptyList())
        override suspend fun insertAttachment(attachment: AttachmentEntity) {}
        override suspend fun deleteAttachment(attachment: AttachmentEntity) {}
    }

    class MockTemplateDao : TemplateDao {
        override fun getAllTemplates(): Flow<List<TemplateEntity>> = flowOf(emptyList())
        override suspend fun insertTemplate(template: TemplateEntity) {}
        override suspend fun updateTemplate(template: TemplateEntity) {}
        override suspend fun deleteTemplate(template: TemplateEntity) {}
    }

    @Test
    fun testPinToToday() = runBlocking {
        val mockNodeDao = MockNodeDao()
        val mockEventLogDao = MockEventLogDao()

        val repository = AppRepository(
            nodeDao = mockNodeDao,
            focusSessionDao = MockFocusSessionDao(),
            trackDao = MockTrackDao(),
            relationDao = MockRelationDao(),
            tagDao = MockTagDao(),
            eventLogDao = mockEventLogDao,
            attachmentDao = MockAttachmentDao(),
            templateDao = MockTemplateDao()
        )

        val nodeId = 42L
        repository.pinToToday(nodeId)

        assertEquals(nodeId, mockNodeDao.pinnedNodeId)
        assertEquals(0, mockNodeDao.pinnedPosition)

        val expectedDate = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        assertEquals(expectedDate, mockNodeDao.pinnedDate)

        assertEquals("TODAY_ASSIGNED", mockEventLogDao.loggedEventType)
        assertEquals(nodeId, mockEventLogDao.loggedNodeId)
    }
}

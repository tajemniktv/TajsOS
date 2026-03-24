/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.tajemniktv.tajsos.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.serialization.json.Json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.AfterTest

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    class TestNodeDao(val testNodesFlow: Flow<List<NodeWithPin>>) : NodeDao {
        override fun getAllNodesWithPins(): Flow<List<NodeWithPin>> = testNodesFlow
        override fun getTodayNodes(date: String): Flow<List<NodeEntity>> = flowOf(emptyList())
        override fun getNodesByType(type: String): Flow<List<NodeEntity>> = flowOf(emptyList())
        override fun getNodesByProject(projectId: Long): Flow<List<NodeEntity>> =
            flowOf(emptyList())

        override suspend fun getNodeById(id: Long): NodeEntity? = null
        override suspend fun insertNode(node: NodeEntity): Long = 0
        override suspend fun updateNode(node: NodeEntity) {}
        override suspend fun deleteNode(node: NodeEntity) {}
        override suspend fun pinToToday(pin: TodayPinEntity) {}
        override suspend fun unpinFromToday(nodeId: Long) {}
        override fun getNodesByArea(areaId: Long): Flow<List<NodeEntity>> = flowOf(emptyList())
        override fun isPinnedToToday(nodeId: Long): Flow<Boolean> = flowOf(false)
        override fun getNodesByProjectWithPins(projectId: Long): Flow<List<NodeWithPin>> = flowOf(emptyList())
        override fun getNodesByAreaWithPins(areaId: Long): Flow<List<NodeWithPin>> = flowOf(emptyList())
        override fun getProjectsByArea(areaId: Long): Flow<List<NodeEntity>> = flowOf(emptyList())
    }

    class StubFocusSessionDao : FocusSessionDao {
        override suspend fun insertSession(session: FocusSessionEntity): Long = 0
        override suspend fun updateSession(session: FocusSessionEntity) {}
        override fun getAllSessions(): Flow<List<FocusSessionEntity>> = flowOf(emptyList())
        override fun getActiveSession(): Flow<FocusSessionEntity?> = flowOf(null)
    }

    class StubTrackDao : TrackDao {
        override fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> = flowOf(emptyList())
        override suspend fun insertTrackEntry(entry: TrackEntryEntity): Long { return 0 }
        override suspend fun getTrackEntryByDate(date: String): TrackEntryEntity? = null
        override suspend fun insertTrackMedication(join: TrackMedicationJoinEntity) {}
        override fun getTrackMedications(trackEntryId: Long): Flow<List<TrackMedicationJoinEntity>> = flowOf(emptyList())
    }

    class StubRelationDao : RelationDao {
        override fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>> =
            flowOf(emptyList())

        override suspend fun insertRelation(relation: RelationEntity) {}
        override suspend fun deleteRelation(relation: RelationEntity) {}
        override suspend fun deleteBelongsToRelations(nodeId: Long) {}
        override suspend fun getBelongsToRelations(nodeId: Long): List<RelationEntity> = emptyList()
        override suspend fun anyRelationExists(from: Long, to: Long): Boolean = false
        override fun getAllRelations(): Flow<List<RelationEntity>> = flowOf(emptyList())
    }

    class StubTagDao : TagDao {
        override fun getAllTags(): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun insertTag(tag: TagEntity): Long = 0
        override fun getTagsForNode(nodeId: Long): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun attachTagToNode(nodeTag: NodeTagEntity) {}
        override suspend fun detachTagFromNode(nodeId: Long, tagId: Long) {}
    }

    class StubEventLogDao : EventLogDao {
        override suspend fun insertLog(log: EventLogEntity) {}
        override fun getRecentLogs(limit: Int): Flow<List<EventLogEntity>> = flowOf(emptyList())
        override fun getLogsForNode(nodeId: Long): Flow<List<EventLogEntity>> = flowOf(emptyList())
    }

    class StubAttachmentDao : AttachmentDao {
        override fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>> =
            flowOf(emptyList())

        override suspend fun insertAttachment(attachment: AttachmentEntity) {}
        override suspend fun deleteAttachment(attachment: AttachmentEntity) {}
    }

    class StubTemplateDao : TemplateDao {
        override fun getAllTemplates(): Flow<List<TemplateEntity>> = flowOf(emptyList())
        override suspend fun insertTemplate(template: TemplateEntity) {}
        override suspend fun updateTemplate(template: TemplateEntity) {}
        override suspend fun deleteTemplate(template: TemplateEntity) {}
    }

    class FakeDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flowOf(emptyPreferences())
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return emptyPreferences()
        }
    }

    @Test
    fun exportDataJson_serializes_current_nodes_state_to_JSON() = runTest {
        val testNodes = listOf(
            NodeWithPin(
                node = NodeEntity(
                    id = 1,
                    type = "task",
                    title = "Task 1",
                    content = "Content 1",
                    status = "active",
                    source = "manual",
                    inboxState = false
                ),
                pin = null
            ),
            NodeWithPin(
                node = NodeEntity(
                    id = 2,
                    type = "note",
                    title = "Note 1",
                    content = "Content 2",
                    status = "archived",
                    source = "manual",
                    inboxState = true
                ),
                pin = null
            )
        )

        val nodesFlow = MutableStateFlow(testNodes)

        val testRepo = AppRepository(
            nodeDao = TestNodeDao(nodesFlow),
            focusSessionDao = StubFocusSessionDao(),
            trackDao = StubTrackDao(),
            relationDao = StubRelationDao(),
            tagDao = StubTagDao(),
            eventLogDao = StubEventLogDao(),
            attachmentDao = StubAttachmentDao(),
            templateDao = StubTemplateDao(),
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

        val fakeDataStore = FakeDataStore()
        val testPrefs = PreferencesRepository(fakeDataStore)
        val calendarManager = com.tajemniktv.tajsos.calendar.CalendarManager(testRepo, io.ktor.client.HttpClient())

        val viewModel = MainViewModel(testRepo, testPrefs, calendarManager)

        advanceTimeBy(100)
        runCurrent()

        val nodes = viewModel.allNodes.value

        val exportJson = viewModel.exportDataJson()

        assertTrue(exportJson.isNotEmpty(), "Export JSON should not be empty")

        val parsedData = Json.decodeFromString<ExportData>(exportJson)
        assertEquals(2, parsedData.version, "ExportData version should be 2")
        assertEquals(2, parsedData.nodes.size, "ExportData should contain 2 nodes")

        assertEquals(1, parsedData.nodes[0].id)
        assertEquals("Task 1", parsedData.nodes[0].title)

        assertEquals(2, parsedData.nodes[1].id)
        assertEquals("Note 1", parsedData.nodes[1].title)
    }
}

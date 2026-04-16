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
    private val fakeInboxEntryDao = FakeInboxEntryDao()
    private val fakeTaskFacetDao = FakeTaskFacetDao()
    private val fakeNoteFacetDao = FakeNoteFacetDao()
    private val fakeProjectFacetDao = FakeProjectFacetDao()
    private val fakeAreaFacetDao = FakeAreaFacetDao()
    private val fakeRecordFacetDao = FakeRecordFacetDao()
    private val fakeItemDomainDao = FakeItemDomainDao()
    private val fakeRichContentDocumentDao = FakeRichContentDocumentDao()
    private val fakeScheduleEntryDao = FakeScheduleEntryDao()
    private val fakeSavedViewDao = FakeSavedViewDao()

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
            inboxEntryDao = fakeInboxEntryDao,
            taskFacetDao = fakeTaskFacetDao,
            noteFacetDao = fakeNoteFacetDao,
            projectFacetDao = fakeProjectFacetDao,
            areaFacetDao = fakeAreaFacetDao,
            recordFacetDao = fakeRecordFacetDao,
            itemDomainDao = fakeItemDomainDao,
            richContentDocumentDao = fakeRichContentDocumentDao,
            scheduleEntryDao = fakeScheduleEntryDao,
            savedViewDao = fakeSavedViewDao,
        )

    // ---- insertNodes tests ----

    @Test
    fun testInsertNodes_returnsIdsForAllNodes(): TestResult =
        runTest {
            val nodes = listOf(
                NodeEntity(type = "note", title = "Note A"),
                NodeEntity(type = "note", title = "Note B"),
                NodeEntity(type = "note", title = "Note C"),
            )

            val ids = repository.insertNodes(nodes)

            assertEquals(3, ids.size)
            assertTrue(ids.all { it > 0 }, "All returned IDs should be positive")
            assertEquals(ids.distinct().size, ids.size, "All IDs should be unique")
        }

    @Test
    fun testInsertNodes_logsNodeCreatedForEachNode(): TestResult =
        runTest {
            val nodes = listOf(
                NodeEntity(type = "note", title = "Note A"),
                NodeEntity(type = "note", title = "Note B"),
            )

            val ids = repository.insertNodes(nodes)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(2, logs.size)
            assertTrue(logs.all { it.eventType == "NODE_CREATED" })
            assertEquals(ids.toSet(), logs.map { it.nodeId }.toSet())
        }

    @Test
    fun testInsertNodes_createsRelationsForProjectId(): TestResult =
        runTest {
            val projectId = 42L
            val nodes = listOf(
                NodeEntity(type = "note", title = "Note A", projectId = projectId),
                NodeEntity(type = "note", title = "Note B", projectId = projectId),
            )

            val ids = repository.insertNodes(nodes)

            val relations = fakeRelationDao.getAllRelations().first()
            val belongsToRelations = relations.filter { it.relationType == "BELONGS_TO" && it.toNodeId == projectId }
            assertEquals(2, belongsToRelations.size)
            assertEquals(ids.toSet(), belongsToRelations.map { it.fromNodeId }.toSet())
        }

    @Test
    fun testInsertNodes_createsRelationsForAreaId(): TestResult =
        runTest {
            val areaId = 99L
            val nodes = listOf(
                NodeEntity(type = "note", title = "Note A", areaId = areaId),
            )

            val ids = repository.insertNodes(nodes)

            val relations = fakeRelationDao.getAllRelations().first()
            val belongsToRelations = relations.filter { it.relationType == "BELONGS_TO" && it.toNodeId == areaId }
            assertEquals(1, belongsToRelations.size)
            assertEquals(ids[0], belongsToRelations[0].fromNodeId)
        }

    @Test
    fun testInsertNodes_createsRelationsForBothProjectAndArea(): TestResult =
        runTest {
            val projectId = 10L
            val areaId = 20L
            val nodes = listOf(
                NodeEntity(type = "note", title = "Note A", projectId = projectId, areaId = areaId),
            )

            val ids = repository.insertNodes(nodes)

            val relations = fakeRelationDao.getAllRelations().first()
            val belongsToRelations = relations.filter { it.relationType == "BELONGS_TO" && it.fromNodeId == ids[0] }
            assertEquals(2, belongsToRelations.size)
            assertTrue(belongsToRelations.any { it.toNodeId == projectId })
            assertTrue(belongsToRelations.any { it.toNodeId == areaId })
        }

    @Test
    fun testInsertNodes_nodesWithoutProjectOrArea_createsNoRelations(): TestResult =
        runTest {
            val nodes = listOf(
                NodeEntity(type = "note", title = "Note A"),
                NodeEntity(type = "note", title = "Note B"),
            )

            repository.insertNodes(nodes)

            val relations = fakeRelationDao.getAllRelations().first()
            assertTrue(relations.isEmpty(), "No relations should be created for nodes without projectId or areaId")
        }

    @Test
    fun testInsertNodes_emptyList_returnsEmptyAndNoSideEffects(): TestResult =
        runTest {
            val ids = repository.insertNodes(emptyList())

            assertEquals(0, ids.size)
            assertEquals(0, fakeEventLogDao.getLogs().size)
            assertTrue(fakeRelationDao.getAllRelations().first().isEmpty())
        }

    @Test
    fun testInsertNodes_mixedNodesWithAndWithoutProject(): TestResult =
        runTest {
            val projectId = 5L
            val nodes = listOf(
                NodeEntity(type = "note", title = "Has Project", projectId = projectId),
                NodeEntity(type = "note", title = "No Project"),
            )

            val ids = repository.insertNodes(nodes)

            val logs = fakeEventLogDao.getLogs()
            assertEquals(2, logs.size, "Should log NODE_CREATED for every node")

            val relations = fakeRelationDao.getAllRelations().first()
            assertEquals(1, relations.size, "Only the node with projectId should have a relation")
            assertEquals(ids[0], relations[0].fromNodeId)
            assertEquals(projectId, relations[0].toNodeId)
        }

    @Test
    fun testInsertTemplates_batchInsertsCorrectly(): TestResult =
        runTest {
            val templates = listOf(
                TemplateEntity(name = "T1", nodeType = "note", defaultTitle = "Title 1", defaultContent = "C1"),
                TemplateEntity(name = "T2", nodeType = "note", defaultTitle = "Title 2", defaultContent = "C2"),
            )

            repository.insertTemplates(templates)

            val savedTemplates = repository.getAllTemplates().first()
            assertEquals(2, savedTemplates.size)
            assertTrue(savedTemplates.any { it.name == "T1" })
            assertTrue(savedTemplates.any { it.name == "T2" })
        }

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
    fun insertLifeItem_createsTypedAggregateWithDomainsAndDocument(): TestResult =
        runTest {
            val id =
                repository.insertLifeItem(
                    kind = ItemKind.NOTE,
                    title = "Therapy notes",
                    content = "Patterns from this week",
                    noteKind = NoteKind.REFLECTION,
                    domains = setOf(com.tajemniktv.tajsos.domain.DomainKind.HEALTH),
                )

            val aggregate = repository.getLifeObject(id)

            assertNotNull(aggregate)
            assertEquals(ItemKind.NOTE, aggregate.kind)
            assertEquals(NoteKind.REFLECTION, aggregate.note?.kind)
            assertEquals("Patterns from this week", aggregate.document?.body)
            assertEquals(1, aggregate.domains.size)
            assertEquals(com.tajemniktv.tajsos.domain.DomainKind.HEALTH, aggregate.domains.first().domain)
        }

    @Test
    fun insertNode_mirrorsTaskSchedulingIntoTypedScheduleEntries(): TestResult =
        runTest {
            val node =
                NodeEntity(
                    type = "task",
                    title = "Submit taxes",
                    startAt = 1_700_000_000_000,
                    dueAt = 1_700_086_400_000,
                    reminderAt = 1_699_950_000_000,
                    recurringInterval = "weekly",
                )

            val id = repository.insertNode(node)
            val entries = repository.getScheduleEntriesForItem(id).first()

            assertEquals(3, entries.size)
            assertTrue(entries.all { it.localDateEpochDay != null })
            assertTrue(entries.all { !it.timezoneId.isNullOrBlank() })
            assertTrue(entries.any { it.kind == ScheduleEntryKind.DUE.storageKey })
        }

    @Test
    fun saveSavedView_roundTripsProjectionDefinition(): TestResult =
        runTest {
            val viewId =
                repository.saveSavedView(
                    SavedViewDefinition(
                        name = "Tasks by Area",
                        lens = SavedViewLens.OPERATE,
                        layout = SavedViewLayout.MATRIX,
                        sourceKinds = setOf(ItemKind.TASK),
                        filters =
                            listOf(
                                SavedViewFilter(
                                    fieldKey = SavedViewFieldKey.STATUS,
                                    operator = SavedViewFilterOperator.NOT_EQUALS,
                                    value = "archived",
                                ),
                            ),
                        sorts =
                            listOf(
                                SavedViewSort(
                                    fieldKey = SavedViewFieldKey.DUE_DATE,
                                    direction = SavedViewSortDirection.ASCENDING,
                                ),
                            ),
                        visibleFields = listOf(SavedViewFieldKey.TITLE, SavedViewFieldKey.AREA),
                        rowDimension = SavedViewFieldKey.AREA,
                        columnDimension = SavedViewFieldKey.STATUS,
                        measure = SavedViewMeasure.COUNT,
                    ),
                )

            val saved = repository.getSavedView(viewId)

            assertNotNull(saved)
            assertEquals(SavedViewLayout.MATRIX, saved.layout)
            assertEquals(setOf(ItemKind.TASK), saved.sourceKinds)
            assertEquals(SavedViewFieldKey.AREA, saved.rowDimension)
            assertEquals(SavedViewFieldKey.STATUS, saved.columnDimension)
            assertEquals(SavedViewMeasure.COUNT, saved.measure)
            assertEquals(1, saved.filters.size)
        }

    @Test
    fun deleteNode_cleansCompanionPersistenceRows(): TestResult =
        runTest {
            val id =
                repository.insertLifeItem(
                    kind = ItemKind.TASK,
                    title = "Call clinic",
                    content = "Ask for bloodwork referral",
                    domains = setOf(com.tajemniktv.tajsos.domain.DomainKind.HEALTH),
                )

            repository.deleteNode(NodeEntity(id = id, type = "task", title = "Call clinic"))

            assertEquals(null, fakeTaskFacetDao.getTaskFacetByItemId(id))
            assertEquals(null, fakeRichContentDocumentDao.getDocumentForItem(id))
            assertTrue(fakeItemDomainDao.getDomainsForItem(id).first().isEmpty())
            assertTrue(fakeScheduleEntryDao.getScheduleEntriesForItem(id).first().isEmpty())
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

    @Test
    fun testDecideOn_withNullOption_updatesNodeButNoOptionsSelected(): TestResult =
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
                    DecisionOptionEntity(decisionNodeId = nodeId, title = "Option A", isSelected = true),
                )
            val option2Id =
                repository.insertDecisionOption(
                    DecisionOptionEntity(decisionNodeId = nodeId, title = "Option B"),
                )

            repository.decideOn(nodeId, "Decided not to choose")

            val updatedNode = repository.getNodeById(nodeId)
            assertNotNull(updatedNode)
            assertEquals("decided", updatedNode.decisionStatus)
            assertEquals("Decided not to choose", updatedNode.decisionOutcome)
            assertEquals("done", updatedNode.status)

            val options = repository.getOptionsForDecision(nodeId).first()
            assertEquals(2, options.size)

            val updatedOption1 = options.find { it.id == option1Id }
            val updatedOption2 = options.find { it.id == option2Id }

            assertNotNull(updatedOption1)
            assertNotNull(updatedOption2)
            assertFalse(updatedOption1.isSelected)
            assertFalse(updatedOption2.isSelected)
        }

    @Test
    fun testDecideOn_deselectsPreviousOption(): TestResult =
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
                    DecisionOptionEntity(decisionNodeId = nodeId, title = "Option A", isSelected = true),
                )
            val option2Id =
                repository.insertDecisionOption(
                    DecisionOptionEntity(decisionNodeId = nodeId, title = "Option B"),
                )

            repository.decideOn(nodeId, "Changed to B", option2Id)

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

    @Test
    fun testBuildExportBundle_includesSchemaAndPacks(): TestResult =
        runTest {
            fakeNodeDao.insertNode(NodeEntity(type = "task", title = "Export me"))

            val bundle =
                repository.buildExportBundle(
                    enabledPacks = setOf(AppPack.STUDENT.key, AppPack.CREATOR.key),
                )

            assertEquals(EXPORT_SCHEMA_VERSION, bundle.schemaVersion)
            assertTrue(bundle.nodes.isNotEmpty())
            assertTrue(bundle.enabledPacks.contains(AppPack.STUDENT.key))
            assertTrue(bundle.enabledPacks.contains(AppPack.CREATOR.key))
        }
}

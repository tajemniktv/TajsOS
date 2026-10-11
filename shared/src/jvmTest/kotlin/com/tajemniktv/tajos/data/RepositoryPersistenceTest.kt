/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.Room
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tajemniktv.tajos.domain.DomainKind
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Exercises production Room transactions and backup contracts, not independent DAO fakes. */
class RepositoryPersistenceTest {
    private fun database(): AppDatabase =
        Room.inMemoryDatabaseBuilder<AppDatabase>(factory = AppDatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver())
            .build()

    @Test
    fun everyPersistedTableSurvivesJsonBackupRoundTrip() = runBlocking {
        val source = database()
        val destination = database()
        try {
            source.backupDao().restore(fixture())
            val exported = AppRepository(source).buildExportBundle(setOf("STUDENT"))
            val decoded = Json.decodeFromString<ExportBundle>(Json.encodeToString(exported))
            AppRepository(destination).importBundle(decoded)
            val restored = AppRepository(destination).buildExportBundle(decoded.enabledPacks)
            assertEquals(exported.copy(exportedAt = 0), restored.copy(exportedAt = 0))
            assertEquals(fixture().copy(exportedAt = 0), restored.copy(exportedAt = 0))

            // Fail when the Room schema grows without extending the portable backup contract.
            val tables = source.useReaderConnection { connection ->
                connection.usePrepared("SELECT name FROM sqlite_master WHERE type = 'table'") { statement ->
                    buildSet {
                        while (statement.step()) add(statement.getText(0))
                    }
                }
            }.filterNot { it.startsWith("sqlite_") || it.startsWith("room_") }.toSet()
            assertEquals(setOf(
                "nodes",
                "relations",
                "tags",
                "templates",
                "reviews",
                "track_entries",
                "focus_sessions",
                "calendar_events",
                "calendar_providers",
                "event_log",
                "node_tags",
                "today_pins",
                "attachments",
                "node_snapshots",
                "modes",
                "mode_preferences",
                "mode_area_filters",
                "mode_type_filters",
                "mode_usage_logs",
                "protocol_history",
                "decision_options",
                "users",
                "medications",
                "track_medications",
                "inbox_entries",
                "task_facets",
                "note_facets",
                "record_facets",
                "project_facets",
                "area_facets",
                "item_domains",
                "rich_content_documents",
                "schedule_entries",
                "saved_views",
                "saved_view_source_kinds",
                "saved_view_filters",
                "saved_view_sorts",
                "saved_view_visible_fields",
            ), tables)
        } finally {
            source.close()
            destination.close()
        }
    }

    @Test
    fun restoringOverExistingObjectsReplacesTheirOwnedStateButKeepsUnrelatedObjects(): Unit = runBlocking {
        val db = database()
        try {
            val repository = AppRepository(db)
            db.backupDao().restore(fixture())
            repository.insertNode(NodeEntity(id = 99, type = ItemKind.NOTE.storageKey, title = "Unrelated"))
            val cleared = fixture().copy(
                nodeTags = emptyList(), pins = emptyList(), attachments = emptyList(),
                taskFacets = emptyList(), savedViewFilters = emptyList(), relations = emptyList(),
            )
            repository.importBundle(cleared)
            val aggregate = assertNotNull(repository.getLifeObject(1))
            assertTrue(aggregate.tags.isEmpty())
            assertTrue(aggregate.attachments.isEmpty())
            assertTrue(aggregate.relations.isEmpty())
            assertNull(aggregate.task)
            assertTrue(db.backupDao().readPins().isEmpty())
            assertTrue(db.backupDao().readSavedViewFilters().isEmpty())
            assertNotNull(repository.getNodeById(99))
        } finally {
            db.close()
        }
    }

    @Test
    fun legacyImportsHydrateTypedStateAndDoNotInventCreationLogs() = runBlocking {
        val db = database()
        try {
            val repository = AppRepository(db)
            val task = NodeEntity(
                id = 1, type = ItemKind.TASK.storageKey, title = "Legacy", content = "Body",
                estimatedMinutes = 30, startAt = 100, dueAt = 200, reminderAt = 50,
            )
            repository.importBundle(fixture().copy(
                schemaVersion = 3, nodes = listOf(task), relations = emptyList(), recentEvents = emptyList(),
            ))
            val aggregate = assertNotNull(repository.getLifeObject(1))
            assertEquals(30, aggregate.task?.estimatedMinutes)
            assertEquals("Body", aggregate.document?.body)
            assertEquals(3, aggregate.schedule.size)
            assertTrue(db.backupDao().readRecentEvents().isEmpty())
            repository.importLegacyNodes(listOf(task.copy(estimatedMinutes = null, dueAt = null)))
            val changed = assertNotNull(repository.getLifeObject(1))
            assertNull(changed.task?.estimatedMinutes)
            assertEquals(2, changed.schedule.size)
        } finally {
            db.close()
        }
    }

    @Test
    fun unsupportedSchemaFailsBeforeAnyWrites() = runBlocking {
        val db = database()
        try {
            val repository = AppRepository(db)
            for (version in listOf(0, EXPORT_SCHEMA_VERSION + 1)) {
                assertFailsWith<IllegalArgumentException> { repository.importBundle(fixture().copy(schemaVersion = version)) }
                assertTrue(db.backupDao().readNodes().isEmpty())
            }
        } finally {
            db.close()
        }
    }

    @Test
    fun legacyBundlePreservesOwnershipRelationIdentityWithoutDuplicateEdges() = runBlocking {
        val db = database()
        try {
            val repository = AppRepository(db)
            val task = fixture().nodes.first().copy(projectId = 3, areaId = 4)
            repository.insertNode(task)
            val legacy = fixture().copy(
                schemaVersion = 3,
                nodes = fixture().nodes.map { if (it.id == task.id) task else it },
                relations = listOf(
                    RelationEntity(id = 42, fromNodeId = 1, toNodeId = 3, relationType = "BELONGS_TO", createdAt = 1, metadataJson = "{}"),
                    RelationEntity(id = 43, fromNodeId = 1, toNodeId = 4, relationType = "BELONGS_TO", createdAt = 1),
                ),
            )
            repository.importBundle(legacy)
            assertEquals(legacy.relations, db.backupDao().readRelations())
        } finally {
            db.close()
        }
    }

    @Test
    fun failedFullRestoreRollsBackClearsAndEarlierTableWrites() = runBlocking {
        val db = database()
        try {
            val repository = AppRepository(db)
            db.backupDao().restore(fixture())
            val before = repository.buildExportBundle()
            db.useWriterConnection { connection ->
                connection.usePrepared(
                    "CREATE TRIGGER reject_document BEFORE INSERT ON rich_content_documents " +
                        "BEGIN SELECT RAISE(ABORT, 'injected restore failure'); END",
                ) { it.step() }
            }
            assertFailsWith<androidx.sqlite.SQLiteException> {
                repository.importBundle(fixture().copy(
                    nodes = fixture().nodes.map { it.copy(title = "Changed") },
                    nodeTags = emptyList(), pins = emptyList(),
                ))
            }
            assertEquals(before.copy(exportedAt = 0), repository.buildExportBundle().copy(exportedAt = 0))
        } finally {
            db.close()
        }
    }

    @Test
    fun clearingNullableTaskFieldsClearsBothRepresentations() = runBlocking {
        val db = database()
        try {
            val repository = AppRepository(db)
            val original = NodeEntity(
                id = 1, type = ItemKind.TASK.storageKey, title = "Task", estimatedMinutes = 30,
                energyLevel = 2, friction = "high", nextSmallestStep = "Begin",
                completionNote = "Finished", completedAt = 100, isRecurring = true, recurringInterval = "daily",
            )
            repository.insertNode(original)
            repository.updateNode(original.copy(
                estimatedMinutes = null, energyLevel = null, friction = null, nextSmallestStep = null,
                completionNote = null, completedAt = null, isRecurring = false, recurringInterval = null,
            ))
            assertEquals(TaskFacetEntity(itemId = 1), db.taskFacetDao().getTaskFacetByItemId(1))
            assertNull(repository.getNodeById(1)?.estimatedMinutes)
        } finally {
            db.close()
        }
    }

    @Test
    fun failedBatchInsertRollsBackEarlierNodesFacetsAndLogs() = runBlocking {
        val db = database()
        try {
            val real = db.nodeDao()
            val failing = object : NodeDao by real {
                override suspend fun insertNode(node: NodeEntity): Long {
                    val id = real.insertNode(node)
                    if (node.title == "Fail") error("Injected failure after write")
                    return id
                }
            }
            val repository = AppRepository(db, nodeDao = failing)
            assertFailsWith<IllegalStateException> {
                repository.insertNodes(listOf(
                    NodeEntity(type = ItemKind.TASK.storageKey, title = "First"),
                    NodeEntity(type = ItemKind.TASK.storageKey, title = "Fail"),
                ))
            }
            assertTrue(db.backupDao().readNodes().isEmpty())
            assertTrue(db.backupDao().readTaskFacets().isEmpty())
            assertTrue(db.backupDao().readRecentEvents().isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun failedCalendarReplacementRestoresCacheAndSuccessTimestamp() = runBlocking {
        val db = database()
        try {
            val original = fixture()
            db.backupDao().restore(original)
            val real = db.calendarEventDao()
            val failing = object : CalendarEventDao by real {
                override suspend fun insertEvents(events: List<CalendarEventEntity>) {
                    real.insertEvents(events)
                    error("Injected failure after replacement insert")
                }
            }
            assertFailsWith<IllegalStateException> {
                AppRepository(db, calendarEventDao = failing).replaceCalendarEvents(
                    original.providers.single(), original.calendars.map { it.copy(title = "New") }, 1234,
                )
            }
            assertEquals(original.calendars, db.backupDao().readCalendars())
            assertEquals(original.providers, db.backupDao().readProviders())
        } finally {
            db.close()
        }
    }

    @Test
    fun failedSavedViewReplacementRollsBackParentAndChildChanges() = runBlocking {
        val db = database()
        try {
            db.backupDao().restore(fixture())
            val before = db.backupDao().snapshot(emptySet())
            val real = db.savedViewDao()
            val failing = object : SavedViewDao by real {
                override suspend fun insertSavedViewFilters(filters: List<SavedViewFilterEntity>) {
                    real.insertSavedViewFilters(filters)
                    error("Injected failure after filter insert")
                }
            }
            val repository = AppRepository(db, savedViewDao = failing)
            val view = assertNotNull(repository.getSavedView(1))
            assertFailsWith<IllegalStateException> { repository.saveSavedView(view.copy(name = "Changed")) }
            assertEquals(before.savedViews, db.backupDao().readSavedViews())
            assertEquals(before.savedViewFilters, db.backupDao().readSavedViewFilters())
            assertEquals(before.savedViewSourceKinds, db.backupDao().readSavedViewSourceKinds())
        } finally {
            db.close()
        }
    }

    private fun fixture(): ExportBundle = ExportBundle(
        exportedAt = 0, enabledPacks = setOf("STUDENT"),
        nodes = listOf(
            NodeEntity(id = 1, type = ItemKind.TASK.storageKey, title = "Task", estimatedMinutes = 30, createdAt = 1, updatedAt = 1),
            NodeEntity(id = 2, type = ItemKind.NOTE.storageKey, title = "Note", createdAt = 1, updatedAt = 1),
            NodeEntity(id = 3, type = ItemKind.PROJECT.storageKey, title = "Project", createdAt = 1, updatedAt = 1),
            NodeEntity(id = 4, type = ItemKind.AREA.storageKey, title = "Area", createdAt = 1, updatedAt = 1),
            NodeEntity(id = 5, type = ItemKind.RECORD.storageKey, title = "Record", createdAt = 1, updatedAt = 1),
        ),
        relations = listOf(RelationEntity(id = 1, fromNodeId = 1, toNodeId = 3, relationType = "BELONGS_TO", createdAt = 1)),
        tags = listOf(TagEntity(id = 1, name = "Tag", normalizedName = "tag")),
        templates = listOf(TemplateEntity(id = 1, name = "Template", nodeType = ItemKind.TASK.storageKey)),
        reviews = listOf(ReviewEntity(id = 1, type = "daily", date = "2026-10-11", completedAt = 1)),
        tracks = listOf(TrackEntryEntity(id = 1, date = "2026-10-11", createdAt = 1)),
        sessions = listOf(FocusSessionEntity(id = 1, nodeId = 1, startedAt = 1)),
        calendars = listOf(CalendarEventEntity(id = 1, providerId = 1, title = "Event", startAt = 1, endAt = 2, createdAt = 1, updatedAt = 1)),
        providers = listOf(CalendarProviderEntity(id = 1, name = "ICS", type = "ICS", url = "https://example.com/feed.ics", lastSyncedAt = 1)),
        recentEvents = listOf(EventLogEntity(id = 1, eventType = "NODE_CREATED", nodeId = 1, timestamp = 1)),
        nodeTags = listOf(NodeTagEntity(nodeId = 1, tagId = 1)),
        pins = listOf(TodayPinEntity(id = 1, nodeId = 1, date = "2026-10-11", position = 0, selectedAt = 1)),
        attachments = listOf(AttachmentEntity(id = 1, nodeId = 2, assetType = "URL", uriOrPath = "https://example.com/reference")),
        snapshots = listOf(NodeSnapshotEntity(id = 1, nodeId = 2, title = "Snapshot", content = "Body", timestamp = 1)),
        modes = listOf(ModeEntity(id = 1, key = "FOCUS", name = "Focus", createdAt = 1, updatedAt = 1)),
        modePreferences = listOf(ModePreferenceEntity(id = 1, modeId = 1)),
        modeAreaFilters = listOf(ModeAreaFilterEntity(modeId = 1, areaId = 4)),
        modeTypeFilters = listOf(ModeTypeFilterEntity(modeId = 1, nodeType = ItemKind.TASK.storageKey)),
        modeUsageLogs = listOf(ModeUsageLogEntity(id = 1, modeId = 1, activatedAt = 1)),
        protocolHistory = listOf(ProtocolHistoryEntity(id = 1, protocolNodeId = 1, executedAt = 1)),
        decisionOptions = listOf(DecisionOptionEntity(id = 1, decisionNodeId = 1, title = "Option")),
        users = listOf(UserEntity(firstName = "Test", createdAt = 1, updatedAt = 1)),
        medications = listOf(MedicationEntity(id = 1, substance = "Test", createdAt = 1, updatedAt = 1)),
        trackMedications = listOf(TrackMedicationJoinEntity(trackEntryId = 1, medicationId = 1, wasTaken = true)),
        inboxEntries = listOf(InboxEntryEntity(id = 1, rawText = "Unprocessed", capturedAt = 1)),
        taskFacets = listOf(TaskFacetEntity(itemId = 1, estimatedMinutes = 30)),
        noteFacets = listOf(NoteFacetEntity(itemId = 2, sourceAuthor = "Author")),
        projectFacets = listOf(ProjectFacetEntity(itemId = 3, purpose = "Outcome")),
        areaFacets = listOf(AreaFacetEntity(itemId = 4, standardOfCare = "Maintain")),
        recordFacets = listOf(RecordFacetEntity(itemId = 5, occurredAt = 1)),
        itemDomains = listOf(ItemDomainEntity(itemId = 1, domainKey = DomainKind.HEALTH.name, assignedAt = 1)),
        documents = listOf(RichContentDocumentEntity(itemId = 2, body = "Markdown", structuredContentJson = "{}", updatedAt = 1)),
        scheduleEntries = listOf(ScheduleEntryEntity(id = 1, itemId = 1, kind = ScheduleEntryKind.DUE.storageKey, scheduledAt = 2)),
        savedViews = listOf(SavedViewEntity(id = 1, name = "Tasks", createdAt = 1, updatedAt = 1)),
        savedViewSourceKinds = listOf(SavedViewSourceKindEntity(viewId = 1, itemKind = ItemKind.TASK.storageKey)),
        savedViewFilters = listOf(SavedViewFilterEntity(viewId = 1, position = 0, fieldKey = "title", operatorKey = "contains", value = "Task")),
        savedViewSorts = listOf(SavedViewSortEntity(viewId = 1, position = 0, fieldKey = "title")),
        savedViewVisibleFields = listOf(SavedViewVisibleFieldEntity(viewId = 1, position = 0, fieldKey = "title")),
    )
}

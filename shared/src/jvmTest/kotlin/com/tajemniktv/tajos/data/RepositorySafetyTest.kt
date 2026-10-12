/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tajemniktv.tajos.ui.main.actions.NodeCommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/** Replays stale and concurrent commands against synthetic data in real Room databases. */
class RepositorySafetyTest {
    private suspend fun withDatabase(block: suspend (AppDatabase) -> Unit) {
        val db = Room.inMemoryDatabaseBuilder<AppDatabase>(factory = AppDatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver())
            .build()
        try {
            block(db)
        } finally {
            db.close()
        }
    }

    private suspend fun changeStatus(repository: AppRepository, stale: NodeEntity, status: String) = coroutineScope {
        NodeCommands(repository, this, { emptyList() }, { emptyList() }, {}, { _, _, _ -> })
            .updateNodeStatus(stale, status)
    }

    @Test
    fun backupRedactsCredentialsWithoutChangingLiveProvider() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val provider = CalendarProviderEntity(
                id = 1, name = "Synthetic calendar", type = "GOOGLE", accountEmail = "test@example.invalid",
                accessToken = "ACCESS_SENTINEL", refreshToken = "REFRESH_SENTINEL", lastSyncedAt = 123,
            )
            repository.insertCalendarProvider(provider)
            val bundle = repository.buildExportBundle()
            val exported = bundle.providers.single()
            assertNull(exported.accessToken)
            assertNull(exported.refreshToken)
            assertFalse(exported.isEnabled)
            for (format in listOf(Json, Json { encodeDefaults = true })) {
                val json = format.encodeToString(bundle)
                assertFalse(json.contains("ACCESS_SENTINEL"))
                assertFalse(json.contains("REFRESH_SENTINEL"))
                withDatabase { restored ->
                    AppRepository(restored).importBundle(format.decodeFromString<ExportBundle>(json))
                    assertEquals(exported, restored.backupDao().readProviders().single())
                }
            }
            assertEquals(provider, db.backupDao().readProviders().single())
        }
    }

    @Test
    fun restoreDoesNotReactivateCredentialsFromHistoricalBackups() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val bundle = repository.buildExportBundle().copy(providers = listOf(
                CalendarProviderEntity(id = 1, name = "Legacy", type = "GOOGLE", accessToken = "OLD_ACCESS", refreshToken = "OLD_REFRESH"),
            ))
            for (version in listOf(3, EXPORT_SCHEMA_VERSION)) {
                repository.importBundle(bundle.copy(schemaVersion = version))
                val restored = db.backupDao().readProviders().single()
                assertNull(restored.accessToken)
                assertNull(restored.refreshToken)
                assertFalse(restored.isEnabled)
            }
        }
    }

    @Test
    fun triageRetriesAcrossRepositoriesReturnOneItemAndOneEvent() = runBlocking {
        withDatabase { db ->
            val first = AppRepository(db)
            val second = AppRepository(db)
            val capture = first.captureInboxEntry("Synthetic task\nBody")
            val ids = coroutineScope {
                (0 until 16).map { index -> async(Dispatchers.Default) {
                    (if (index % 2 == 0) first else second).triageInboxEntry(capture, ItemKind.TASK)
                } }.awaitAll()
            }
            val target = ids.first()
            assertTrue(target > 0)
            assertEquals(setOf(target), ids.toSet())
            assertEquals(target, first.triageInboxEntry(capture, ItemKind.NOTE))
            assertEquals(listOf(target), db.backupDao().readNodes().map { it.id })
            assertEquals(target, db.inboxEntryDao().getInboxEntryById(capture)?.triagedItemId)
            assertEquals(1, db.backupDao().readTaskFacets().size)
            assertEquals(1, db.backupDao().readRecentEvents().count { it.eventType == "INBOX_TRIAGED" })
        }
    }

    @Test
    fun dismissalAndStaleDismissalCannotCreateOrUnlinkTriagedItems() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val dismissedId = repository.captureInboxEntry("Dismiss")
            repository.dismissInboxEntry(assertNotNull(db.inboxEntryDao().getInboxEntryById(dismissedId)))
            assertEquals(0L, repository.triageInboxEntry(dismissedId, ItemKind.TASK))
            val capture = repository.captureInboxEntry("Keep")
            val stale = assertNotNull(db.inboxEntryDao().getInboxEntryById(capture))
            val target = repository.triageInboxEntry(capture, ItemKind.NOTE)
            val processed = db.inboxEntryDao().getInboxEntryById(capture)
            repository.dismissInboxEntry(stale)
            assertEquals(processed, db.inboxEntryDao().getInboxEntryById(capture))
            assertEquals(target, repository.triageInboxEntry(capture, ItemKind.TASK))
            assertEquals(1, db.backupDao().readNodes().size)
            repository.deleteNode(assertNotNull(repository.getNodeById(target)))
            assertEquals(target, repository.triageInboxEntry(capture, ItemKind.NOTE))
            assertTrue(db.backupDao().readNodes().isEmpty())
        }
    }

    @Test
    fun failedTriageRollsBackItsItemCompanionsAndLink() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val capture = repository.captureInboxEntry("Rollback task\nBody")
            val before = repository.buildExportBundle().copy(exportedAt = 0)
            db.useWriterConnection { connection ->
                connection.usePrepared("CREATE TRIGGER reject_triage BEFORE UPDATE ON inbox_entries BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END") { it.step() }
            }
            assertFailsWith<androidx.sqlite.SQLiteException> { repository.triageInboxEntry(capture, ItemKind.TASK) }
            assertEquals(before, repository.buildExportBundle().copy(exportedAt = 0))
        }
    }

    @Test
    fun repeatedPinsProduceOneTodayRowAndOneAssignmentEvent() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val id = repository.insertLifeItem(ItemKind.TASK, "Pin")
            coroutineScope {
                (0 until 16).map { async(Dispatchers.Default) { AppRepository(db).pinToToday(id) } }.awaitAll()
            }
            val firstPin = db.backupDao().readPins().single()
            repository.pinToToday(id)
            assertEquals(listOf(firstPin), db.backupDao().readPins())
            assertEquals(listOf(id), repository.getTodayNodes().first().map { it.id })
            assertEquals(1, db.backupDao().readRecentEvents().count { it.eventType == "TODAY_ASSIGNED" })
        }
    }

    @Test
    fun pinRepairsHistoricalDuplicatesWithoutRemovingOtherDays() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val id = repository.insertLifeItem(ItemKind.TASK, "Repair")
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val keeper = TodayPinEntity(id = 10, nodeId = id, date = date, position = 4, selectedAt = 1)
            val otherDay = keeper.copy(id = 12, date = "2000-01-01")
            db.backupDao().writePins(listOf(keeper, keeper.copy(id = 11), otherDay))
            repository.pinToToday(id)
            assertEquals(listOf(keeper.copy(position = 0), otherDay), db.backupDao().readPins().sortedBy { it.id })
        }
    }

    @Test
    fun failedPinRepairRollsBackItsDeletedRows() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val id = repository.insertLifeItem(ItemKind.TASK, "Pin rollback")
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            db.backupDao().writePins(listOf(
                TodayPinEntity(id = 10, nodeId = id, date = date, position = 4),
                TodayPinEntity(id = 11, nodeId = id, date = date, position = 5),
            ))
            val before = repository.buildExportBundle().copy(exportedAt = 0)
            db.useWriterConnection { connection ->
                connection.usePrepared("CREATE TRIGGER reject_pin BEFORE INSERT ON today_pins BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END") { it.step() }
            }
            assertFailsWith<androidx.sqlite.SQLiteException> { repository.pinToToday(id) }
            assertEquals(before, repository.buildExportBundle().copy(exportedAt = 0))
        }
    }

    @Test
    fun staleCompletionCannotRestoreRecurrenceOrRecreateDeletedNodes() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val id = repository.insertLifeItem(ItemKind.TASK, "Disable recurrence", isRecurring = true, recurringInterval = "daily")
            val stale = assertNotNull(repository.getNodeById(id))
            repository.updateNode(stale.copy(isRecurring = false, recurringInterval = null))
            changeStatus(repository, stale, TaskState.DONE.storageKey)
            assertEquals(1, db.backupDao().readNodes().size)
            assertFalse(assertNotNull(repository.getNodeById(id)).isRecurring)
            repository.deleteNode(assertNotNull(repository.getNodeById(id)))
            changeStatus(repository, stale, TaskState.DONE.storageKey)
            assertTrue(db.backupDao().readNodes().isEmpty())
        }
    }

    @Test
    fun staleCompletionsUseCurrentStateAndSpawnOneOccurrencePerTransition() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val id = repository.insertLifeItem(ItemKind.TASK, "Stale", isRecurring = true, recurringInterval = "weekly", dueAt = 100)
            val stale = assertNotNull(repository.getNodeById(id))
            repository.updateNode(stale.copy(title = "Current", content = "Keep body", dueAt = 200))
            coroutineScope {
                (0 until 8).map { async(Dispatchers.Default) { changeStatus(AppRepository(db), stale, TaskState.DONE.storageKey) } }.awaitAll()
            }
            val completed = assertNotNull(repository.getNodeById(id))
            assertEquals("Current", completed.title)
            assertEquals("Keep body", completed.content)
            assertEquals(200L, completed.dueAt)
            assertNotNull(completed.completedAt)
            assertEquals(completed.completedAt, db.taskFacetDao().getTaskFacetByItemId(id)?.completedAt)
            val next = db.backupDao().readNodes().single { it.id != id }
            assertEquals("Current", next.title)
            assertEquals(200 + 7 * 86_400_000L, next.dueAt)
            changeStatus(repository, stale, TaskState.DONE.storageKey)
            assertEquals(completed, repository.getNodeById(id))
            assertEquals(2, db.backupDao().readNodes().size)
            changeStatus(repository, stale, TaskState.ACTIVE.storageKey)
            assertNull(repository.getNodeById(id)?.completedAt)
            assertNull(db.taskFacetDao().getTaskFacetByItemId(id)?.completedAt)
            changeStatus(repository, stale, TaskState.DONE.storageKey)
            assertEquals(3, db.backupDao().readNodes().size)
            assertEquals(2, db.backupDao().readRecentEvents().count { it.eventType == "NODE_COMPLETED" })
        }
    }

    @Test
    fun failedRecurringCopyRollsBackCompletionAndItsEvent() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val id = repository.insertLifeItem(ItemKind.TASK, "Rollback recurring", isRecurring = true, recurringInterval = "daily")
            val stale = assertNotNull(repository.getNodeById(id))
            val before = repository.buildExportBundle().copy(exportedAt = 0)
            db.useWriterConnection { connection ->
                connection.usePrepared("CREATE TRIGGER reject_occurrence BEFORE INSERT ON nodes BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END") { it.step() }
            }
            assertFailsWith<androidx.sqlite.SQLiteException> { changeStatus(repository, stale, TaskState.DONE.storageKey) }
            assertEquals(before, repository.buildExportBundle().copy(exportedAt = 0))
        }
    }

    @Test
    fun unrelatedSingleAndBatchEditsPreserveEveryScheduleField() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val id = repository.insertLifeItem(ItemKind.TASK, "Schedule", dueAt = 200)
            val original = assertNotNull(repository.getNodeById(id))
            val rows = listOf(
                ScheduleEntryEntity(id = 10, itemId = id, kind = ScheduleEntryKind.DUE.storageKey, scheduledAt = 200, localDateEpochDay = 20000, timezoneId = "Europe/Warsaw", isAllDay = true, endAt = 300, recurrenceRule = "custom", note = "Preserve", completedAt = 400),
                ScheduleEntryEntity(id = 11, itemId = id, kind = ScheduleEntryKind.REMINDER.storageKey, scheduledAt = 100, timezoneId = "Etc/UTC", note = "First"),
                ScheduleEntryEntity(id = 12, itemId = id, kind = ScheduleEntryKind.REMINDER.storageKey, scheduledAt = 150, timezoneId = "Etc/UTC", note = "Second"),
            )
            db.scheduleEntryDao().deleteScheduleEntriesForItem(id)
            db.scheduleEntryDao().insertScheduleEntries(rows)
            repository.updateNode(original.copy(title = "Changed"))
            assertEquals(rows.sortedBy { it.id }, db.backupDao().readScheduleEntries().sortedBy { it.id })
            repository.updateNodes(listOf(assertNotNull(repository.getNodeById(id)).copy(content = "Body", status = TaskState.ON_HOLD.storageKey)))
            assertEquals(rows.sortedBy { it.id }, db.backupDao().readScheduleEntries().sortedBy { it.id })
            repository.updateNode(assertNotNull(repository.getNodeById(id)).copy(dueAt = 500))
            val changed = db.backupDao().readScheduleEntries().sortedBy { it.id }
            assertEquals(rows.drop(1), changed.drop(1))
            assertEquals(rows.first().copy(scheduledAt = 500, endAt = 600, localDateEpochDay = 0), changed.first())
            repository.updateNode(assertNotNull(repository.getNodeById(id)).copy(dueAt = null))
            assertEquals(rows.drop(1), db.backupDao().readScheduleEntries().sortedBy { it.id })
        }
    }

    @Test
    fun recurrenceEditPreservesIndependentRulesAndUnspecifiedTimezone() = runBlocking {
        withDatabase { db ->
            val repository = AppRepository(db)
            val id = repository.insertLifeItem(ItemKind.TASK, "Recurrence", dueAt = 200)
            val rows = listOf(
                ScheduleEntryEntity(id = 10, itemId = id, kind = ScheduleEntryKind.DUE.storageKey, scheduledAt = 200, endAt = 300, note = "Mirror"),
                ScheduleEntryEntity(id = 11, itemId = id, kind = ScheduleEntryKind.DUE.storageKey, scheduledAt = 400, recurrenceRule = "independent", note = "Extra"),
                ScheduleEntryEntity(id = 12, itemId = id, kind = ScheduleEntryKind.REMINDER.storageKey, scheduledAt = 100, recurrenceRule = "custom"),
            )
            db.scheduleEntryDao().deleteScheduleEntriesForItem(id)
            db.scheduleEntryDao().insertScheduleEntries(rows)
            repository.updateNode(assertNotNull(repository.getNodeById(id)).copy(isRecurring = true, recurringInterval = "weekly"))
            assertEquals(rows.map { if (it.id == 10L) it.copy(recurrenceRule = "weekly") else it }, db.backupDao().readScheduleEntries().sortedBy { it.id })
        }
    }
}

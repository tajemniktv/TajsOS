/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import java.nio.file.Files

/** Adversarial second-pass fixtures; every database and credential is synthetic. */
class RepositoryAdversarialReviewTest {
    private suspend fun withDatabase(block: suspend (AppDatabase, AppRepository) -> Unit) {
        val db = Room.inMemoryDatabaseBuilder<AppDatabase>(factory = AppDatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver()).build()
        try {
            block(db, AppRepository(db))
        } finally {
            db.close()
        }
    }

    private fun localProvider(enabled: Boolean = true) = CalendarProviderEntity(
        id = 17, name = "Synthetic local account", type = "GOOGLE", accountEmail = "review@example.invalid",
        accessToken = "LOCAL_ACCESS_SENTINEL", refreshToken = "LOCAL_REFRESH_SENTINEL", isEnabled = enabled,
    )

    @Test
    fun everySupportedSchemaPreservesLocalAuthorizationAndIgnoresImportedTokens() = runBlocking {
        for (version in 1..EXPORT_SCHEMA_VERSION) {
            for (enabled in listOf(true, false)) {
                withDatabase { db, repository ->
                    val local = localProvider(enabled)
                    repository.insertCalendarProvider(local)
                    val incoming = local.copy(
                        name = "Restored display name", color = 123,
                        accessToken = "UNTRUSTED_ACCESS", refreshToken = "UNTRUSTED_REFRESH", isEnabled = !enabled,
                    )
                    val bundle = repository.buildExportBundle().copy(schemaVersion = version, providers = listOf(incoming))
                    repository.importBundle(Json.decodeFromString<ExportBundle>(Json.encodeToString(bundle)))
                    assertEquals(incoming.copy(
                        accessToken = local.accessToken, refreshToken = local.refreshToken, isEnabled = local.isEnabled,
                    ), db.calendarProviderDao().getProviderById(local.id))
                    for (format in listOf(Json, Json { encodeDefaults = true })) {
                        val json = format.encodeToString(repository.buildExportBundle())
                        for (secret in listOf("LOCAL_ACCESS_SENTINEL", "LOCAL_REFRESH_SENTINEL", "UNTRUSTED_ACCESS", "UNTRUSTED_REFRESH")) {
                            assertFalse(secret in json)
                        }
                    }
                    withDatabase { empty, destination ->
                        destination.importBundle(bundle)
                        val restored = assertNotNull(empty.calendarProviderDao().getProviderById(local.id))
                        assertNull(restored.accessToken)
                        assertNull(restored.refreshToken)
                        assertFalse(restored.isEnabled)
                    }
                }
            }
        }
    }

    @Test
    fun redactedSameDatasetBackupDoesNotDeleteLiveCredentialsViaDaoOrRepository() = runBlocking {
        withDatabase { db, repository ->
            val local = localProvider()
            repository.insertCalendarProvider(local)
            val unrelated = local.copy(id = 18, accountEmail = "other@example.invalid")
            repository.insertCalendarProvider(unrelated)
            val bundle = repository.buildExportBundle().copy(providers = listOf(local.copy(accessToken = null, refreshToken = null, isEnabled = false)))
            db.backupDao().restore(bundle)
            assertEquals(local, db.calendarProviderDao().getProviderById(local.id))
            assertEquals(unrelated, db.calendarProviderDao().getProviderById(unrelated.id))
            repository.importBundle(bundle)
            assertEquals(local, db.calendarProviderDao().getProviderById(local.id))
        }
    }

    @Test
    fun concurrentCredentialRefreshCannotBeErasedByAnySupportedRestore() = runBlocking {
        withDatabase { db, repository ->
            val local = localProvider()
            repository.insertCalendarProvider(local)
            val bundle = repository.buildExportBundle()
            val refreshed = local.copy(accessToken = "FRESH_ACCESS", refreshToken = "FRESH_REFRESH", isEnabled = false)
            coroutineScope {
                val refresh = async(Dispatchers.Default) { AppRepository(db).updateCalendarProvider(refreshed) }
                val restores = (0 until 16).map { index -> async(Dispatchers.Default) {
                    AppRepository(db).importBundle(bundle.copy(schemaVersion = 1 + index % EXPORT_SCHEMA_VERSION))
                } }
                refresh.await()
                restores.awaitAll()
            }
            assertEquals(refreshed, db.calendarProviderDao().getProviderById(local.id))
        }
    }

    @Test
    fun independentRoomInstancesShareSqliteCommandIdempotency() = runBlocking {
        val directory = Files.createTempDirectory("tajsos-review-synthetic-")
        val path = directory.resolve("review.sqlite").toString()
        val first = Room.databaseBuilder<AppDatabase>(path, factory = AppDatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver()).build()
        var second: AppDatabase? = null
        try {
            val firstRepository = AppRepository(first)
            val capture = firstRepository.captureInboxEntry("Synthetic independent-connection task")
            val other = Room.databaseBuilder<AppDatabase>(path, factory = AppDatabaseConstructor::initialize)
                .setDriver(BundledSQLiteDriver()).build()
            second = other
            val repositories = listOf(firstRepository, AppRepository(other))
            val ids = coroutineScope {
                (0 until 16).map { index -> async(Dispatchers.Default) {
                    repositories[index % 2].triageInboxEntry(capture, ItemKind.TASK)
                } }.awaitAll()
            }
            assertEquals(1, ids.toSet().size)
            val id = ids.first()
            assertTrue(id > 0)
            firstRepository.updateNode(assertNotNull(firstRepository.getNodeById(id)).copy(isRecurring = true, recurringInterval = "weekly", dueAt = 100))
            coroutineScope {
                (0 until 16).map { index -> async(Dispatchers.Default) { repositories[index % 2].pinToToday(id) } }.awaitAll()
                (0 until 16).map { index -> async(Dispatchers.Default) { repositories[index % 2].updateNodeStatus(id, TaskState.DONE.storageKey) } }.awaitAll()
            }
            val bundle = firstRepository.buildExportBundle()
            assertEquals(2, bundle.nodes.size)
            assertEquals(1, bundle.pins.size)
            assertEquals(1, bundle.recentEvents.count { it.eventType == "INBOX_TRIAGED" })
            assertEquals(1, bundle.recentEvents.count { it.eventType == "TODAY_ASSIGNED" })
            assertEquals(1, bundle.recentEvents.count { it.eventType == "NODE_COMPLETED" })
        } finally {
            second?.close()
            first.close()
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun credentialIdentityCollisionsRejectEverySchemaAndRollBackTheWholeImport() = runBlocking {
        for (version in 1..EXPORT_SCHEMA_VERSION) {
            for (identity in listOf("account", "type", "url")) {
                withDatabase { db, repository ->
                    val id = repository.insertLifeItem(ItemKind.TASK, "Keep original task", dueAt = 100)
                    val local = localProvider()
                    repository.insertCalendarProvider(local)
                    val before = repository.buildExportBundle().copy(exportedAt = 0)
                    val conflict = when (identity) {
                        "account" -> local.copy(accountEmail = "different@example.invalid")
                        "type" -> local.copy(type = "OUTLOOK")
                        else -> local.copy(url = "https://example.invalid/different.ics")
                    }
                    val incoming = before.copy(
                        schemaVersion = version,
                        nodes = listOf(assertNotNull(repository.getNodeById(id)).copy(title = "Must not commit")),
                        providers = listOf(conflict.copy(accessToken = "UNTRUSTED_ACCESS", refreshToken = "UNTRUSTED_REFRESH")),
                    )
                    assertFailsWith<IllegalArgumentException> { repository.importBundle(incoming) }
                    assertEquals(before, repository.buildExportBundle().copy(exportedAt = 0))
                    assertEquals(local, db.calendarProviderDao().getProviderById(local.id))
                }
            }
        }
    }

    @Test
    fun currentExportsAlwaysContainTheirExplicitSchemaVersion() = runBlocking {
        withDatabase { _, repository ->
            val json = Json.parseToJsonElement(Json.encodeToString(repository.buildExportBundle())).jsonObject
            assertEquals(EXPORT_SCHEMA_VERSION.toString(), json["schemaVersion"]?.jsonPrimitive?.content)
        }
    }

    @Test
    fun staleAndConcurrentPinCallbacksCannotLeaveDeletedOrMissingTargetsPinned() = runBlocking {
        withDatabase { db, repository ->
            val id = repository.insertLifeItem(ItemKind.TASK, "Delete before stale pin")
            repository.deleteNode(assertNotNull(repository.getNodeById(id)))
            coroutineScope {
                (0 until 16).map { async(Dispatchers.Default) { AppRepository(db).pinToToday(id) } }.awaitAll()
            }
            repository.pinToToday(987654321)
            assertTrue(db.backupDao().readPins().isEmpty())
            assertTrue(db.backupDao().readRecentEvents().none { it.eventType == "TODAY_ASSIGNED" })
            val racingId = repository.insertLifeItem(ItemKind.TASK, "Race delete and pin")
            val stale = assertNotNull(repository.getNodeById(racingId))
            coroutineScope {
                val delete = async(Dispatchers.Default) { AppRepository(db).deleteNode(stale) }
                val pins = (0 until 16).map { async(Dispatchers.Default) { AppRepository(db).pinToToday(racingId) } }
                delete.await()
                pins.awaitAll()
            }
            assertNull(repository.getNodeById(racingId))
            assertTrue(db.backupDao().readPins().isEmpty())
        }
    }

    @Test
    fun duplicateBatchIdsUseFinalRowOnceWithoutDuplicateSchedulesOrEvents() = runBlocking {
        withDatabase { db, repository ->
            val id = repository.insertLifeItem(ItemKind.TASK, "Original", dueAt = 100)
            val original = assertNotNull(repository.getNodeById(id))
            val schedule = db.backupDao().readScheduleEntries().single().copy(
                timezoneId = "Etc/UTC", endAt = 160, note = "Keep detail", recurrenceRule = "custom", isAllDay = true,
            )
            db.scheduleEntryDao().insertScheduleEntry(schedule)
            val first = original.copy(title = "Intermediate", dueAt = 200, status = TaskState.DONE.storageKey, completedAt = 5)
            val final = original.copy(title = "Final", dueAt = 300, status = TaskState.DONE.storageKey, completedAt = 6)
            repository.updateNodes(listOf(first, final, final))
            assertEquals(final, repository.getNodeById(id))
            assertEquals(listOf(schedule.copy(scheduledAt = 300, endAt = 360, localDateEpochDay = 0)), db.backupDao().readScheduleEntries())
            assertEquals(1, db.backupDao().readRecentEvents().count { it.eventType == "NODE_COMPLETED" })
            assertEquals(6L, db.taskFacetDao().getTaskFacetByItemId(id)?.completedAt)
        }
    }

    @Test
    fun failedPinCleanupRollsBackTheDeletedNodeAndItsCompanions() = runBlocking {
        withDatabase { db, repository ->
            val id = repository.insertLifeItem(ItemKind.TASK, "Delete rollback", dueAt = 100)
            repository.pinToToday(id)
            val before = repository.buildExportBundle().copy(exportedAt = 0)
            db.useWriterConnection { connection ->
                connection.usePrepared("CREATE TRIGGER reject_pin_delete BEFORE DELETE ON today_pins BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END") { it.step() }
            }
            assertFailsWith<androidx.sqlite.SQLiteException> {
                repository.deleteNode(assertNotNull(repository.getNodeById(id)))
            }
            assertEquals(before, repository.buildExportBundle().copy(exportedAt = 0))
        }
    }

    @Test
    fun recurrenceOnlyEditPreservesAnUnknownStoredTimezoneWithoutResolvingIt() = runBlocking {
        withDatabase { db, repository ->
            val id = repository.insertLifeItem(ItemKind.TASK, "Unknown timezone", dueAt = 100)
            val schedule = db.backupDao().readScheduleEntries().single().copy(
                timezoneId = "Legacy/Unresolved", localDateEpochDay = 12345, endAt = 200, note = "Keep detail",
            )
            db.scheduleEntryDao().insertScheduleEntry(schedule)
            repository.updateNode(assertNotNull(repository.getNodeById(id)).copy(isRecurring = true, recurringInterval = "weekly"))
            assertEquals(listOf(schedule.copy(recurrenceRule = "weekly")), db.backupDao().readScheduleEntries())
        }
    }
}

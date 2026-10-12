/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.Room
import androidx.room.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tajemniktv.tajos.calendar.CalendarManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Validates review findings against disposable Room databases and a network-free engine. */
class RepositoryReviewGuardrailsTest {
    private suspend fun withDatabase(block: suspend (AppDatabase, AppRepository) -> Unit) {
        val database = Room.inMemoryDatabaseBuilder<AppDatabase>(factory = AppDatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver()).build()
        try {
            block(database, AppRepository(database))
        } finally {
            database.close()
        }
    }

    @Test
    fun invalidLifecycleKeyCannotChangeCompletionMetadata() = runBlocking {
        withDatabase { _, repository ->
            val id = repository.insertLifeItem(ItemKind.TASK, "Keep completed task")
            repository.updateNodeStatus(id, TaskState.DONE)
            val before = repository.buildExportBundle().copy(exportedAt = 0)
            assertFailsWith<IllegalArgumentException> { repository.updateNodeStatus(id, "donne") }
            assertEquals(before, repository.buildExportBundle().copy(exportedAt = 0))
        }
    }

    @Test
    fun pinLookupUsesCompositeIndexWithoutForbiddingHistoricalDuplicates() = runBlocking {
        withDatabase { database, repository ->
            val id = repository.insertLifeItem(ItemKind.TASK, "Indexed pin target")
            database.backupDao().writePins(listOf(
                TodayPinEntity(id = 10, nodeId = id, date = "2026-10-12", position = 0),
                TodayPinEntity(id = 11, nodeId = id, date = "2026-10-12", position = 1),
            ))
            assertEquals(2, database.nodeDao().getPinsForNodeOnDate(id, "2026-10-12").size)
            val plan = database.useReaderConnection { connection ->
                connection.usePrepared("EXPLAIN QUERY PLAN SELECT * FROM today_pins WHERE nodeId = 1 AND date = '2026-10-12' ORDER BY id ASC") { query ->
                    buildList { while (query.step()) add(query.getText(3)) }
                }
            }
            assertTrue(plan.any { "index_today_pins_nodeId_date" in it }, plan.joinToString())
        }
    }

    @Test
    fun everySupportedImportLeavesUnconfiguredSourcesDisabled() = runBlocking {
        for (version in 1..EXPORT_SCHEMA_VERSION) verifyNewSourceDisabled(version)
    }

    /** A backup may retain an opaque private URL, but cannot approve a new network source locally. */
    private suspend fun verifyNewSourceDisabled(version: Int) = withDatabase { database, repository ->
        val source = syntheticFeed(enabled = true)
        val bundle = repository.buildExportBundle().copy(schemaVersion = version, providers = listOf(source))
        repository.importBundle(bundle)
        assertEquals(source.copy(isEnabled = false), database.calendarProviderDao().getProviderById(source.id))
        assertNoCalendarRequest(repository)
    }

    /** Uses a mock engine to observe attempted requests without contacting any host. */
    private suspend fun assertNoCalendarRequest(repository: AppRepository) {
        var requested = false
        val client = HttpClient(MockEngine { requested = true; error("Unapproved synthetic source") })
        try {
            CalendarManager(repository, client).syncAll()
            assertFalse(requested)
        } finally {
            client.close()
        }
    }

    @Test
    fun matchingLocalFeedRetainsEnablementAndChangedIdentityNeedsApproval() = runBlocking {
        for (version in 1..EXPORT_SCHEMA_VERSION) {
            verifyLocalFeedEnablement(version, enabled = true)
            verifyLocalFeedEnablement(version, enabled = false)
        }
    }

    /** Reused credential-free IDs may change configuration, but cannot carry approval to another URL. */
    private suspend fun verifyLocalFeedEnablement(version: Int, enabled: Boolean) = withDatabase { database, repository ->
        val local = syntheticFeed(enabled)
        repository.insertCalendarProvider(local)
        val incoming = local.copy(name = "Restored display", isEnabled = !enabled)
        val bundle = repository.buildExportBundle().copy(schemaVersion = version, providers = listOf(incoming))
        repository.importBundle(bundle)
        assertEquals(incoming.copy(isEnabled = enabled), database.calendarProviderDao().getProviderById(local.id))
        val changed = incoming.copy(url = "https://calendar.example.invalid/changed-feed", isEnabled = true)
        repository.importBundle(bundle.copy(providers = listOf(changed)))
        assertEquals(changed.copy(isEnabled = false), database.calendarProviderDao().getProviderById(local.id))
    }

    private fun syntheticFeed(enabled: Boolean) = CalendarProviderEntity(
        id = 17, name = "Synthetic private feed", type = "ICS",
        url = "https://calendar.example.invalid/opaque-private-feed", isEnabled = enabled,
    )

    @Test
    fun invalidExplicitTimezoneRejectsTimestampEditWithoutPartialWrites() = runBlocking {
        withDatabase { database, repository ->
            val id = repository.insertLifeItem(ItemKind.TASK, "Keep invalid-zone metadata", dueAt = 100)
            val schedule = database.backupDao().readScheduleEntries().single().copy(timezoneId = "Legacy/Unresolved")
            database.scheduleEntryDao().insertScheduleEntry(schedule)
            val before = repository.buildExportBundle().copy(exportedAt = 0)
            val current = assertNotNull(repository.getNodeById(id))
            assertFailsWith<IllegalArgumentException> { repository.updateNode(current.copy(title = "Must roll back", dueAt = 200)) }
            assertEquals(before, repository.buildExportBundle().copy(exportedAt = 0))
        }
    }
}

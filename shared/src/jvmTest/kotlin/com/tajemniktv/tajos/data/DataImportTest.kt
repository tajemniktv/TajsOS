/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Verifies typed import feedback with the actual transactional Room restore implementation. */
class DataImportTest {
    private suspend fun withRepository(block: suspend (AppRepository) -> Unit) {
        val db = Room.inMemoryDatabaseBuilder<AppDatabase>(factory = AppDatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver()).build()
        try {
            block(AppRepository(db))
        } finally {
            db.close()
        }
    }

    @Test
    fun typedFeedbackMatchesCurrentAndLegacyImports() = runBlocking {
        withRepository { source ->
            source.insertLifeItem(ItemKind.TASK, "Synthetic import task")
            val bundle = source.buildExportBundle()
            withRepository { destination ->
                val result = assertIs<DataImportResult.Bundle>(destination.importJson(Json.encodeToString(bundle)))
                assertEquals(bundle.nodes.size, result.report.nodes)
                assertEquals(bundle.copy(exportedAt = 0), destination.buildExportBundle().copy(exportedAt = 0))
            }
            withRepository { destination ->
                assertEquals(DataImportResult.Legacy(1), destination.importJson(Json.encodeToString(ExportData(version = 2, nodes = bundle.nodes))))
                assertEquals(bundle.nodes, destination.buildExportBundle().nodes)
            }
        }
    }

    @Test
    fun invalidAndUnsupportedImportsLeaveTheDatabaseUnchanged() = runBlocking {
        withRepository { repository ->
            repository.insertLifeItem(ItemKind.NOTE, "Keep synthetic note")
            val before = repository.buildExportBundle().copy(exportedAt = 0)
            assertEquals(DataImportResult.EmptyPayload, repository.importJson(" \n "))
            assertEquals(DataImportResult.InvalidFormat, repository.importJson("{invalid}"))
            assertEquals(DataImportResult.InvalidFormat, repository.importJson("[]"))
            for (version in listOf(0, EXPORT_SCHEMA_VERSION + 1)) {
                val unsupported = before.copy(schemaVersion = version, nodes = emptyList())
                assertEquals(DataImportResult.UnsupportedSchema(version), repository.importJson(Json.encodeToString(unsupported)))
            }
            val futureVersion = EXPORT_SCHEMA_VERSION + 1
            val futurePayload = """{"schemaVersion":$futureVersion,"futureField":{"synthetic":true}}"""
            assertEquals(DataImportResult.UnsupportedSchema(futureVersion), repository.importJson(futurePayload))
            assertEquals(before, repository.buildExportBundle().copy(exportedAt = 0))
        }
    }
}

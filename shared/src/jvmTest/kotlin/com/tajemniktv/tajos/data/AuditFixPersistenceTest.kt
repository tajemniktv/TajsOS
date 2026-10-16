/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuditFixPersistenceTest {
    private suspend fun database(block: suspend (AppDatabase, AppRepository) -> Unit) {
        val db = Room.inMemoryDatabaseBuilder<AppDatabase>(factory = AppDatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver()).build()
        try { block(db, AppRepository(db)) } finally { db.close() }
    }

    @Test fun disjointTextPatchesAndFavoritePreserveCurrentFields() = runBlocking {
        database { _, repo ->
            val id = repo.insertLifeItem(ItemKind.NOTE, "Original", content = "Body")
            repo.updateNodeText(id, title = "Edited title")
            repo.togglePermanentPin(id)
            repo.patchNode(id) { it.copy(isSticky = true) }
            repo.updateNodeText(id, content = "Edited body")
            val result = assertNotNull(repo.getNodeById(id))
            assertEquals("Edited title", result.title)
            assertEquals("Edited body", result.content)
            assertTrue(result.isPinned)
            assertTrue(result.isSticky)
            repo.deleteNode(result)
            assertFailsWith<IllegalStateException> { repo.updateNodeText(id, title = "Do not recreate") }
        }
    }

    private suspend fun rejectInsert(db: AppDatabase, table: String) {
        db.useWriterConnection { connection ->
            connection.usePrepared("CREATE TRIGGER reject_$table BEFORE INSERT ON $table BEGIN SELECT RAISE(ABORT, 'injected failure'); END") { it.step() }
        }
    }

    @Test fun failedMedicationJoinRollsBackCheckinAndItsEvent() = runBlocking {
        database { db, repo ->
            val medId = repo.insertMedication(MedicationEntity(substance = "Synthetic"))
            val before = repo.buildExportBundle().copy(exportedAt = 0)
            rejectInsert(db, "track_medications")
            assertFailsWith<Exception> {
                repo.saveTrackCheckIn(TrackEntryEntity(date = "2026-10-16", tookMeds = true), setOf(medId))
            }
            assertEquals(before, repo.buildExportBundle().copy(exportedAt = 0))
        }
    }

    @Test fun successfulCheckinKeepsAllMedicationSelections() = runBlocking {
        database { _, repo ->
            val ids = setOf(repo.insertMedication(MedicationEntity(substance = "A")), repo.insertMedication(MedicationEntity(substance = "B")))
            val entryId = repo.saveTrackCheckIn(TrackEntryEntity(date = "2026-10-16", tookMeds = true), ids)
            assertEquals(ids, repo.getTrackMedications(entryId).first().filter { it.wasTaken }.map { it.medicationId }.toSet())
        }
    }

    @Test fun failedReviewRowRollsBackReflectionAndFacets() = runBlocking {
        database { db, repo ->
            val before = repo.buildExportBundle().copy(exportedAt = 0)
            rejectInsert(db, "reviews")
            assertFailsWith<Exception> { repo.completeReview("daily", "Synthetic reflection", mood = 3) }
            assertEquals(before, repo.buildExportBundle().copy(exportedAt = 0))
        }
    }

    @Test fun failedReviewCheckinRollsBackReviewAndReflection() = runBlocking {
        database { db, repo ->
            val before = repo.buildExportBundle().copy(exportedAt = 0)
            rejectInsert(db, "track_entries")
            assertFailsWith<Exception> { repo.completeReview("weekly", "Synthetic reflection", energy = 4) }
            assertEquals(before, repo.buildExportBundle().copy(exportedAt = 0))
        }
    }

    @Test fun reviewAndCheckinShareAnExplicitLocalDate() = runBlocking {
        database { _, repo ->
            repo.completeReview("daily", "Content", mood = 3, timestamp = 0, timezone = TimeZone.of("America/Los_Angeles"))
            val bundle = repo.buildExportBundle()
            assertEquals("1969-12-31", bundle.reviews.single().date)
            assertEquals(bundle.reviews.single().date, bundle.tracks.single().date)
            assertEquals(bundle.nodes.single().id, bundle.reviews.single().resultNodeId)
            assertEquals("Content", bundle.nodes.single().content)
        }
    }

    @Test fun existingTodaySubscribersSwitchDatesWithoutDatabaseWrites() = runBlocking {
        database { db, _ ->
            val dayOne = LocalDate(2026, 10, 16)
            val dayTwo = LocalDate(2026, 10, 17)
            val dates = MutableStateFlow(dayOne)
            val repo = AppRepository(db, localDates = dates)
            val project = repo.insertLifeItem(ItemKind.PROJECT, "Project")
            val area = repo.insertLifeItem(ItemKind.AREA, "Area")
            val first = repo.insertLifeItem(ItemKind.TASK, "First day", activeProjectId = project, homeAreaId = area)
            val second = repo.insertLifeItem(ItemKind.TASK, "Second day", activeProjectId = project, homeAreaId = area)
            repo.pinToDate(first, dayOne)
            repo.pinToDate(second, dayTwo)
            val observations = Channel<Pair<List<Set<Long>>, Boolean>>(Channel.UNLIMITED)
            val collector = launch {
                combine(repo.getAllNodes(), repo.getTodayNodes(), repo.isPinnedToToday(first),
                    repo.getNodesByProjectWithPins(project), repo.getNodesByAreaWithPins(area)) { all, today, pinned, projects, areas ->
                    listOf(all, projects, areas).map { nodes -> nodes.filter { it.isPinnedToToday }.map { it.node.id }.toSet() }
                        .plus(listOf(today.map { it.id }.toSet())) to pinned
                }.collect { observations.send(it) }
            }
            suspend fun await(expected: Pair<List<Set<Long>>, Boolean>) = withTimeout(5_000) {
                while (observations.receive() != expected) { }
            }
            try {
                await(List(4) { setOf(first) } to true)
                dates.value = dayTwo
                await(List(4) { setOf(second) } to false)
                dates.value = dayOne
                await(List(4) { setOf(first) } to true)
            } finally { collector.cancel(); collector.join() }
        }
    }

    @Test fun invalidMedicationCannotBePersistedByNonUiCallers() = runBlocking {
        database { _, repo ->
            assertFailsWith<IllegalArgumentException> { repo.insertMedication(MedicationEntity(substance = " ")) }
            assertFailsWith<IllegalArgumentException> { repo.insertMedication(MedicationEntity(substance = "A", takeAtHour = 24)) }
            val id = repo.insertMedication(MedicationEntity(substance = "A", takeAtHour = 23))
            assertFailsWith<IllegalArgumentException> { repo.updateMedication(MedicationEntity(id = id, substance = "A", takeAtHour = -1)) }
            assertEquals(23, repo.getAllMedications().first().single().takeAtHour)
        }
    }
}

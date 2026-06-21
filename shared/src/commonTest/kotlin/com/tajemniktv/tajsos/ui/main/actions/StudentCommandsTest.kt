/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.FakeAttachmentDao
import com.tajemniktv.tajsos.data.FakeCalendarEventDao
import com.tajemniktv.tajsos.data.FakeCalendarProviderDao
import com.tajemniktv.tajsos.data.FakeDecisionDao
import com.tajemniktv.tajsos.data.FakeEventLogDao
import com.tajemniktv.tajsos.data.FakeFocusSessionDao
import com.tajemniktv.tajsos.data.FakeMedicationDao
import com.tajemniktv.tajsos.data.FakeModeDao
import com.tajemniktv.tajsos.data.FakeNodeDao
import com.tajemniktv.tajsos.data.FakeNodeSnapshotDao
import com.tajemniktv.tajsos.data.FakeProtocolDao
import com.tajemniktv.tajsos.data.FakeRelationDao
import com.tajemniktv.tajsos.data.FakeReviewDao
import com.tajemniktv.tajsos.data.FakeTagDao
import com.tajemniktv.tajsos.data.FakeTemplateDao
import com.tajemniktv.tajsos.data.FakeTrackDao
import com.tajemniktv.tajsos.data.FakeUserDao
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.data.metadataEnvelopeOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StudentCommandsTest {



    @Test
    fun testSetReadingProgress_clampsValues() = runTest {
        val repo = buildTestRepository()
        val scope = TestScope(testScheduler)

        val node = NodeEntity(id = 1, type = "reading", title = "Book")
        repo.insertNode(node)

        var tags = emptyList<TagEntity>()
        scope.launch {
            repo.getAllTags().collect { tags = it }
        }
        testScheduler.advanceUntilIdle()

        val commands = StudentCommands(
            repository = repo,
            scope = scope,
            currentTags = { tags },
            addNodeForResult = { _, _, _, _, _, _ -> 0L },
            startFocusSession = {},
            addRelation = { _, _, _ -> }
        )

        // Test normal value
        commands.setReadingProgress(node, 50)
        testScheduler.advanceUntilIdle()
        var updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals(50, updatedNode.metadataEnvelopeOrNull()?.student?.readingProgressPercent)

        // Test below 0 clamping
        commands.setReadingProgress(updatedNode, -10)
        testScheduler.advanceUntilIdle()
        updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals(0, updatedNode.metadataEnvelopeOrNull()?.student?.readingProgressPercent)

        // Test above 100 clamping
        commands.setReadingProgress(updatedNode, 150)
        testScheduler.advanceUntilIdle()
        updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals(100, updatedNode.metadataEnvelopeOrNull()?.student?.readingProgressPercent)
    }

    @Test
    fun testSetTopicMastery_clampsValuesAndSetsTopic() = runTest {
        val repo = buildTestRepository()
        val scope = TestScope(testScheduler)

        val node = NodeEntity(id = 1, type = "topic", title = "Math")
        repo.insertNode(node)

        var tags = emptyList<TagEntity>()
        scope.launch {
            repo.getAllTags().collect { tags = it }
        }
        testScheduler.advanceUntilIdle()

        val commands = StudentCommands(
            repository = repo,
            scope = scope,
            currentTags = { tags },
            addNodeForResult = { _, _, _, _, _, _ -> 0L },
            startFocusSession = {},
            addRelation = { _, _, _ -> }
        )

        commands.setTopicMastery(node, "Calculus", 75)
        testScheduler.advanceUntilIdle()
        var updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals("Calculus", updatedNode.metadataEnvelopeOrNull()?.student?.topic)
        assertEquals(75, updatedNode.metadataEnvelopeOrNull()?.student?.masteryPercent)

        // Clamping and blank topic to null
        commands.setTopicMastery(updatedNode, "   ", 120)
        testScheduler.advanceUntilIdle()
        updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals(null, updatedNode.metadataEnvelopeOrNull()?.student?.topic)
        assertEquals(100, updatedNode.metadataEnvelopeOrNull()?.student?.masteryPercent)
    }

    @Test
    fun testSetStudentCourse() = runTest {
        val repo = buildTestRepository()
        val scope = TestScope(testScheduler)

        val node = NodeEntity(id = 1, type = "assignment", title = "Homework")
        repo.insertNode(node)

        var tags = emptyList<TagEntity>()
        scope.launch {
            repo.getAllTags().collect { tags = it }
        }
        testScheduler.advanceUntilIdle()

        val commands = StudentCommands(
            repository = repo,
            scope = scope,
            currentTags = { tags },
            addNodeForResult = { _, _, _, _, _, _ -> 0L },
            startFocusSession = {},
            addRelation = { _, _, _ -> }
        )

        commands.setStudentCourse(node, "CS101", "Intro to CS", "Fall2026", "Essay")
        testScheduler.advanceUntilIdle()

        val updatedNode = requireNotNull(repo.getNodeById(1))
        val studentMeta = updatedNode.metadataEnvelopeOrNull()?.student
        assertEquals("CS101", studentMeta?.courseId)
        assertEquals("Intro to CS", studentMeta?.courseName)
        assertEquals("Fall2026", studentMeta?.semester)
        assertEquals("Essay", studentMeta?.assignmentType)
    }

    @Test
    fun testToggleFlashcardCandidate() = runTest {
        val repo = buildTestRepository()
        val scope = TestScope(testScheduler)

        val node = NodeEntity(id = 1, type = "note", title = "Biology Note")
        repo.insertNode(node)

        var tags = emptyList<TagEntity>()
        scope.launch {
            repo.getAllTags().collect { tags = it }
        }
        testScheduler.advanceUntilIdle()

        val commands = StudentCommands(
            repository = repo,
            scope = scope,
            currentTags = { tags },
            addNodeForResult = { _, _, _, _, _, _ -> 0L },
            startFocusSession = {},
            addRelation = { _, _, _ -> }
        )

        commands.toggleFlashcardCandidate(node, true)
        testScheduler.advanceUntilIdle()

        var updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals(true, updatedNode.metadataEnvelopeOrNull()?.student?.flashcardCandidate)

        val tagsOnNode = repo.getTagsForNode(1).first()
        assertTrue(tagsOnNode.any { it.normalizedName == "flashcard_candidate" })

        commands.toggleFlashcardCandidate(updatedNode, false)
        testScheduler.advanceUntilIdle()

        updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals(false, updatedNode.metadataEnvelopeOrNull()?.student?.flashcardCandidate)

        val tagsOnNodeAfter = repo.getTagsForNode(1).first()
        assertFalse(tagsOnNodeAfter.any { it.normalizedName == "flashcard_candidate" })
    }

    @Test
    fun testToggleRevisitBeforeExam() = runTest {
        val repo = buildTestRepository()
        val scope = TestScope(testScheduler)

        val node = NodeEntity(id = 1, type = "note", title = "History Note")
        repo.insertNode(node)

        var tags = emptyList<TagEntity>()
        scope.launch {
            repo.getAllTags().collect { tags = it }
        }
        testScheduler.advanceUntilIdle()

        val commands = StudentCommands(
            repository = repo,
            scope = scope,
            currentTags = { tags },
            addNodeForResult = { _, _, _, _, _, _ -> 0L },
            startFocusSession = {},
            addRelation = { _, _, _ -> }
        )

        commands.toggleRevisitBeforeExam(node, true)
        testScheduler.advanceUntilIdle()

        var updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals(true, updatedNode.metadataEnvelopeOrNull()?.student?.revisitBeforeExam)

        val tagsOnNode = repo.getTagsForNode(1).first()
        assertTrue(tagsOnNode.any { it.normalizedName == "revisit_before_exam" })

        commands.toggleRevisitBeforeExam(updatedNode, false)
        testScheduler.advanceUntilIdle()

        updatedNode = requireNotNull(repo.getNodeById(1))
        assertEquals(false, updatedNode.metadataEnvelopeOrNull()?.student?.revisitBeforeExam)

        val tagsOnNodeAfter = repo.getTagsForNode(1).first()
        assertFalse(tagsOnNodeAfter.any { it.normalizedName == "revisit_before_exam" })
    }
}

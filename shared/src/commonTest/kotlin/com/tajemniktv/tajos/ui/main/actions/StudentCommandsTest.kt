/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.main.actions

import com.tajemniktv.tajos.data.AppRepository
import com.tajemniktv.tajos.data.FakeAttachmentDao
import com.tajemniktv.tajos.data.FakeCalendarEventDao
import com.tajemniktv.tajos.data.FakeCalendarProviderDao
import com.tajemniktv.tajos.data.FakeDecisionDao
import com.tajemniktv.tajos.data.FakeEventLogDao
import com.tajemniktv.tajos.data.FakeFocusSessionDao
import com.tajemniktv.tajos.data.FakeMedicationDao
import com.tajemniktv.tajos.data.FakeModeDao
import com.tajemniktv.tajos.data.FakeNodeDao
import com.tajemniktv.tajos.data.FakeNodeSnapshotDao
import com.tajemniktv.tajos.data.FakeProtocolDao
import com.tajemniktv.tajos.data.FakeRelationDao
import com.tajemniktv.tajos.data.FakeReviewDao
import com.tajemniktv.tajos.data.FakeTagDao
import com.tajemniktv.tajos.data.FakeTemplateDao
import com.tajemniktv.tajos.data.FakeTrackDao
import com.tajemniktv.tajos.data.FakeUserDao
import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.TagEntity
import com.tajemniktv.tajos.data.metadataEnvelopeOrNull
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
        var updatedNode = repo.getNodeById(1)!!
        assertEquals(50, updatedNode.metadataEnvelopeOrNull()?.student?.readingProgressPercent)

        // Test below 0 clamping
        commands.setReadingProgress(updatedNode, -10)
        testScheduler.advanceUntilIdle()
        updatedNode = repo.getNodeById(1)!!
        assertEquals(0, updatedNode.metadataEnvelopeOrNull()?.student?.readingProgressPercent)

        // Test above 100 clamping
        commands.setReadingProgress(updatedNode, 150)
        testScheduler.advanceUntilIdle()
        updatedNode = repo.getNodeById(1)!!
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
        var updatedNode = repo.getNodeById(1)!!
        assertEquals("Calculus", updatedNode.metadataEnvelopeOrNull()?.student?.topic)
        assertEquals(75, updatedNode.metadataEnvelopeOrNull()?.student?.masteryPercent)

        // Clamping and blank topic to null
        commands.setTopicMastery(updatedNode, "   ", 120)
        testScheduler.advanceUntilIdle()
        updatedNode = repo.getNodeById(1)!!
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

        val updatedNode = repo.getNodeById(1)!!
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

        var updatedNode = repo.getNodeById(1)!!
        assertEquals(true, updatedNode.metadataEnvelopeOrNull()?.student?.flashcardCandidate)

        val tagsOnNode = repo.getTagsForNode(1).first()
        assertTrue(tagsOnNode.any { it.normalizedName == "flashcard_candidate" })

        commands.toggleFlashcardCandidate(updatedNode, false)
        testScheduler.advanceUntilIdle()

        updatedNode = repo.getNodeById(1)!!
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

        var updatedNode = repo.getNodeById(1)!!
        assertEquals(true, updatedNode.metadataEnvelopeOrNull()?.student?.revisitBeforeExam)

        val tagsOnNode = repo.getTagsForNode(1).first()
        assertTrue(tagsOnNode.any { it.normalizedName == "revisit_before_exam" })

        commands.toggleRevisitBeforeExam(updatedNode, false)
        testScheduler.advanceUntilIdle()

        updatedNode = repo.getNodeById(1)!!
        assertEquals(false, updatedNode.metadataEnvelopeOrNull()?.student?.revisitBeforeExam)

        val tagsOnNodeAfter = repo.getTagsForNode(1).first()
        assertFalse(tagsOnNodeAfter.any { it.normalizedName == "revisit_before_exam" })
    }
}

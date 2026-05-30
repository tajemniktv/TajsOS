package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.StudentMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class MainSnapshotCalculatorsStudentBoardTest {

    private fun createTestNodeWithPin(node: NodeEntity, tags: List<TagEntity> = emptyList()): NodeWithPin {
        return NodeWithPin(
            node = node,
            pin = null,
            tags = tags
        )
    }

    private fun createTag(name: String): TagEntity {
        return TagEntity(id = 1L, name = name, normalizedName = name.lowercase(), color = null)
    }

    private fun defaultNode(id: Long, type: String = "task", status: String = "active", title: String = "Test Node $id") = NodeEntity(
        id = id,
        title = title,
        content = "",
        type = type,
        status = status,
        updatedAt = 0L
    )

    private fun attachStudentMetadata(node: NodeEntity, studentMetadata: StudentMetadata): NodeEntity {
        val metadataString = """{"student": ${Json.encodeToString(studentMetadata)}}"""
        return node.copy(metadataJson = metadataString)
    }

    @Test
    fun calculateStudentBoardState_emptyInputs() {
        val state = calculateStudentBoardState(emptyList(), emptyList(), emptyList(), emptyList())
        assertTrue(state.assignmentTracker.isEmpty())
        assertTrue(state.examPrepBoard.isEmpty())
        assertEquals(0, state.courseDashboard.size)
        assertEquals(0, state.semesterDashboard.size)
    }

    @Test
    fun calculateStudentBoardState_assignmentTracker_includesTasksWithAssignmentTag() {
        val activeTask = defaultNode(1L, "task", "active")
        val assignmentTag = createTag("assignment")
        val nodeWithPin = createTestNodeWithPin(activeTask, listOf(assignmentTag))

        val state = calculateStudentBoardState(listOf(nodeWithPin), emptyList(), emptyList(), emptyList())
        assertEquals(1, state.assignmentTracker.size)
        assertEquals(1L, state.assignmentTracker[0].node.id)
    }

    @Test
    fun calculateStudentBoardState_examPrepBoard_includesTasksWithExamPrepTag() {
        val activeTask = defaultNode(1L, "task", "active")
        val examPrepTag = createTag("exam_prep")
        val nodeWithPin = createTestNodeWithPin(activeTask, listOf(examPrepTag))

        val state = calculateStudentBoardState(listOf(nodeWithPin), emptyList(), emptyList(), emptyList())
        assertEquals(1, state.examPrepBoard.size)
        assertEquals(1L, state.examPrepBoard[0].node.id)
    }

    @Test
    fun calculateStudentBoardState_psychologyConceptMaps_includesNotesWithConceptTypeAndPsychologyTag() {
        val activeNote = defaultNode(1L, "note", "active").copy(noteType = "concept")
        val psychologyTag = createTag("psychology")
        val nodeWithPin = createTestNodeWithPin(activeNote, listOf(psychologyTag))

        val state = calculateStudentBoardState(listOf(nodeWithPin), emptyList(), emptyList(), emptyList())
        assertEquals(1, state.psychologyConceptMaps.size)
        assertEquals(1L, state.psychologyConceptMaps[0].node.id)
    }

    @Test
    fun calculateStudentBoardState_readingBacklog_includesNotesWithReadingTag() {
        val activeNote = defaultNode(1L, "note", "active")
        val readingTag = createTag("reading")
        val nodeWithPin = createTestNodeWithPin(activeNote, listOf(readingTag))

        val state = calculateStudentBoardState(listOf(nodeWithPin), emptyList(), emptyList(), emptyList())
        assertEquals(1, state.readingBacklog.size)
        assertEquals(1L, state.readingBacklog[0].node.id)
    }

    @Test
    fun calculateStudentBoardState_courseDashboard_aggregatesByCourse() {
        val studentMetadata = StudentMetadata(courseId = "CS101", courseName = "Computer Science 101", semester = "Fall 2026")
        val node1 = attachStudentMetadata(defaultNode(1L, "task", "active"), studentMetadata)
        val node2 = attachStudentMetadata(defaultNode(2L, "note", "active"), studentMetadata)

        val state = calculateStudentBoardState(
            listOf(createTestNodeWithPin(node1), createTestNodeWithPin(node2)),
            emptyList(),
            emptyList(),
            emptyList()
        )

        assertEquals(1, state.courseDashboard.size)
        assertEquals("CS101", state.courseDashboard[0].courseId)
        assertEquals("Computer Science 101", state.courseDashboard[0].courseName)
    }

    @Test
    fun calculateStudentBoardState_studySessionsThisWeek_countsCorrectly() {
        val now = Clock.System.now().toEpochMilliseconds()
        val node = defaultNode(1L, "task", "active")

        val session1 = FocusSessionEntity(id = 100L, nodeId = 1L, startedAt = now - 1000, durationSec = 3600) // Within 7 days
        val session2 = FocusSessionEntity(id = 101L, nodeId = 1L, startedAt = now - (8 * 24 * 3600 * 1000L), durationSec = 3600) // Older than 7 days
        val session3 = FocusSessionEntity(id = 102L, nodeId = 2L, startedAt = now - 1000, durationSec = 3600) // Node ID not in active nodes

        val state = calculateStudentBoardState(
            listOf(createTestNodeWithPin(node)),
            emptyList(),
            listOf(session1, session2, session3),
            emptyList()
        )

        assertEquals(1, state.studySessionsThisWeek)
        assertEquals(60, state.studyMinutesThisWeek)
    }
}

package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MainSnapshotCalculatorsTimeArchitectureTest {

    private fun createTestNodeWithPin(
        id: Long,
        dueAt: Long? = null,
        type: String = "task",
        status: String = "active",
        noteType: String? = null,
        tags: List<String> = emptyList(),
        startAt: Long? = null,
        title: String = "Test $id",
        updatedAt: Long = 0
    ): NodeWithPin {
        val node = NodeEntity(id = id, title = title, type = type, status = status, dueAt = dueAt, noteType = noteType, startAt = startAt, updatedAt = updatedAt)
        return NodeWithPin(node = node, pin = null, tags = tags.map { TagEntity(id = 0, name = it, normalizedName = it.lowercase()) })
    }

    @Test
    fun `calculateTimeArchitectureSnapshot correctly stratifies tasks by timeline`() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        val pastDue = createTestNodeWithPin(1, dueAt = now - dayMs)
        val todayNode = createTestNodeWithPin(2, dueAt = now + (dayMs / 2))
        val weekNode = createTestNodeWithPin(3, dueAt = now + 3 * dayMs)
        val monthNode = createTestNodeWithPin(4, dueAt = now + 15 * dayMs)
        val semesterNode = createTestNodeWithPin(5, dueAt = now + 60 * dayMs)
        val farFutureNode = createTestNodeWithPin(6, dueAt = now + 150 * dayMs)
        val noDueNode = createTestNodeWithPin(7, dueAt = null)

        val nodes = listOf(pastDue, todayNode, weekNode, monthNode, semesterNode, farFutureNode, noDueNode)

        val snapshot = calculateTimeArchitectureSnapshot(nodes, listOf(todayNode.node), emptyList())

        assertEquals(1, snapshot.todayLayer.size)
        assertEquals(2L, snapshot.todayLayer[0].node.id)

        assertEquals(2, snapshot.weekLayer.size) // todayNode, weekNode
        assertEquals(2L, snapshot.weekLayer[0].node.id)
        assertEquals(3L, snapshot.weekLayer[1].node.id)

        assertEquals(3, snapshot.monthLayer.size) // todayNode, weekNode, monthNode
        assertEquals(4, snapshot.semesterLayer.size) // todayNode, weekNode, monthNode, semesterNode

        assertEquals(2, snapshot.shortHorizonTasks.size)
        assertEquals(2, snapshot.longHorizonTasks.size) // semesterNode, farFutureNode
    }

    @Test
    fun `calculateTimeArchitectureSnapshot computes secondary lists correctly`() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        val seasonalGoalNote = createTestNodeWithPin(1, noteType = "goal_seasonal")
        val seasonalGoalTag = createTestNodeWithPin(2, tags = listOf("seasonal_goal"))
        val notSeasonalGoal = createTestNodeWithPin(3)
        val inactiveSeasonalGoal = createTestNodeWithPin(4, noteType = "goal_seasonal", status = "done")

        val temporaryFocus = createTestNodeWithPin(5, startAt = now, dueAt = now + 10 * dayMs)
        val notTemporaryFocus = createTestNodeWithPin(6, startAt = now, dueAt = now + 20 * dayMs)
        val inactiveTemporaryFocus = createTestNodeWithPin(7, startAt = now, dueAt = now + 10 * dayMs, status = "done")

        val lifePeriodMarkerNote = createTestNodeWithPin(8, noteType = "period_marker", updatedAt = now + 100)
        val lifePeriodMarkerTag = createTestNodeWithPin(9, tags = listOf("life_period_marker"), updatedAt = now)
        val inactiveLifePeriodMarkerNote = createTestNodeWithPin(10, noteType = "period_marker", status = "done", updatedAt = now + 50)

        val nodes = listOf(
            seasonalGoalNote, seasonalGoalTag, notSeasonalGoal, inactiveSeasonalGoal,
            temporaryFocus, notTemporaryFocus, inactiveTemporaryFocus,
            lifePeriodMarkerNote, lifePeriodMarkerTag, inactiveLifePeriodMarkerNote
        )

        val snapshot = calculateTimeArchitectureSnapshot(nodes, emptyList(), emptyList())

        assertEquals(2, snapshot.seasonalGoals.size)
        assertEquals(1L, snapshot.seasonalGoals[0].node.id)
        assertEquals(2L, snapshot.seasonalGoals[1].node.id)

        assertEquals(1, snapshot.temporaryFocusPeriods.size)
        assertEquals(5L, snapshot.temporaryFocusPeriods[0].node.id)

        // lifePeriodMarkers includes ALL nodes (even inactive ones) according to the current implementation
        assertEquals(3, snapshot.lifePeriodMarkers.size)
        // Check sorting by updatedAt descending
        assertEquals(8L, snapshot.lifePeriodMarkers[0].node.id)
        assertEquals(10L, snapshot.lifePeriodMarkers[1].node.id)
        assertEquals(9L, snapshot.lifePeriodMarkers[2].node.id)
    }

    @Test
    fun `calculateTimeArchitectureSnapshot computes examPeriodMode correctly`() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        // Due in 15 days, title contains "exam" -> should trigger
        val examSoonTitle = createTestNodeWithPin(1, dueAt = now + 15 * dayMs, title = "Final Exam")
        val snapshot1 = calculateTimeArchitectureSnapshot(listOf(examSoonTitle), emptyList(), emptyList())
        assertTrue(snapshot1.examPeriodMode)

        // Due in 15 days, tag contains "exam" -> should trigger
        val examSoonTag = createTestNodeWithPin(2, dueAt = now + 15 * dayMs, tags = listOf("midterm_exam"))
        val snapshot2 = calculateTimeArchitectureSnapshot(listOf(examSoonTag), emptyList(), emptyList())
        assertTrue(snapshot2.examPeriodMode)

        // Due in 45 days, title contains "exam" -> should NOT trigger (daysLeft > 30)
        val examFar = createTestNodeWithPin(3, dueAt = now + 45 * dayMs, title = "Final Exam")
        val snapshot3 = calculateTimeArchitectureSnapshot(listOf(examFar), emptyList(), emptyList())
        assertFalse(snapshot3.examPeriodMode)

        // Due in 15 days, title DOES NOT contain "exam" -> should NOT trigger
        val notExamSoon = createTestNodeWithPin(4, dueAt = now + 15 * dayMs, title = "Regular Task")
        val snapshot4 = calculateTimeArchitectureSnapshot(listOf(notExamSoon), emptyList(), emptyList())
        assertFalse(snapshot4.examPeriodMode)
    }

    @Test
    fun `calculateTimeArchitectureSnapshot computes weeklyMap correctly`() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        // Tasks due within the week horizon
        val task1 = createTestNodeWithPin(1, dueAt = now + dayMs)
        val task2 = createTestNodeWithPin(2, dueAt = now + 2 * dayMs)
        val task3 = createTestNodeWithPin(3, dueAt = now + 2 * dayMs)
        val task4 = createTestNodeWithPin(4, dueAt = now + 8 * dayMs) // Outside week

        val nodes = listOf(task1, task2, task3, task4)
        val snapshot = calculateTimeArchitectureSnapshot(nodes, emptyList(), emptyList())

        val dayOfWeek1 = kotlinx.datetime.Instant.fromEpochMilliseconds(task1.node.dueAt!!).toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.name
        val dayOfWeek2 = kotlinx.datetime.Instant.fromEpochMilliseconds(task2.node.dueAt!!).toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.name

        if (dayOfWeek1 == dayOfWeek2) {
            assertEquals(3, snapshot.weeklyMap[dayOfWeek1])
            assertEquals(1, snapshot.weeklyMap.size)
        } else {
            assertEquals(1, snapshot.weeklyMap[dayOfWeek1])
            assertEquals(2, snapshot.weeklyMap[dayOfWeek2])
            assertEquals(2, snapshot.weeklyMap.size)
        }
    }

    @Test
    fun `calculateTimeArchitectureSnapshot computes projectPhases correctly`() {
        val project1 = NodeEntity(id = 1, title = "B Project", type = "project", projectStatus = "active")
        val project2 = NodeEntity(id = 2, title = "C Project", type = "project", projectStatus = "exploratory")
        val project3 = NodeEntity(id = 3, title = "A Project", type = "project", projectStatus = "on_hold")
        val project4 = NodeEntity(id = 4, title = "D Project", type = "project", projectStatus = null) // Defaults to active

        val snapshot = calculateTimeArchitectureSnapshot(emptyList(), emptyList(), listOf(project1, project2, project3, project4))

        assertEquals(4, snapshot.projectPhases.size)

        // Should be sorted alphabetically by title
        assertEquals("A Project", snapshot.projectPhases[0].project.title)
        assertFalse(snapshot.projectPhases[0].isActivePhase)
        assertEquals("inactive_phase", snapshot.projectPhases[0].phaseLabel)

        assertEquals("B Project", snapshot.projectPhases[1].project.title)
        assertTrue(snapshot.projectPhases[1].isActivePhase)
        assertEquals("active_phase", snapshot.projectPhases[1].phaseLabel)

        assertEquals("C Project", snapshot.projectPhases[2].project.title)
        assertTrue(snapshot.projectPhases[2].isActivePhase)
        assertEquals("active_phase", snapshot.projectPhases[2].phaseLabel)

        assertEquals("D Project", snapshot.projectPhases[3].project.title)
        assertTrue(snapshot.projectPhases[3].isActivePhase) // null projectStatus falls back to active
        assertEquals("active_phase", snapshot.projectPhases[3].phaseLabel)
    }
}
